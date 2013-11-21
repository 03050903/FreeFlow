package org.freeflow.layouts;

import java.util.HashMap;

import org.freeflow.core.BaseSectionedAdapter;
import org.freeflow.core.Frame;
import org.freeflow.core.FrameDescriptor;
import org.freeflow.core.Section;

public class VGridLayout extends AbstractLayout {

	private boolean dataChanged = false;
	private static final String TAG = "VGridLayout";
	private int itemHeight = -1;
	private int itemWidth = -1;
	private int headerWidth = -1;
	private int headerHeight = -1;
	private int width = -1;
	private int height = -1;
	private BaseSectionedAdapter itemsAdapter;
	private HashMap<Object, FrameDescriptor> frameDescriptors = new HashMap<Object, FrameDescriptor>();

	public void setItemHeight(int itemHeight) {
		if (itemHeight == this.itemHeight)
			return;

		this.itemHeight = itemHeight;
		dataChanged = true;
	}

	public void setItemWidth(int itemWidth) {
		if (itemWidth == this.itemWidth)
			return;

		this.itemWidth = itemWidth;
		dataChanged = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDimensions(int measuredWidth, int measuredHeight) {
		if (measuredHeight == height && measuredWidth == width) {
			return;
		}
		this.width = measuredWidth;
		this.height = measuredHeight;

		dataChanged = true;
	}

	@Override
	public void setItems(BaseSectionedAdapter adapter) {
		this.itemsAdapter = adapter;
		dataChanged = true;
	}

	/**
	 * TODO: Future optimization: can we avoid object allocation here?
	 */
	@Override
	public void generateFrameDescriptors() {
		if (itemHeight < 0) {
			throw new IllegalStateException("itemHeight not set");
		}

		if (itemWidth < 0) {
			throw new IllegalStateException("itemWidth not set");
		}

		if (headerWidth < 0) {
			throw new IllegalStateException("headerWidth not set");
		}

		if (headerHeight < 0) {
			throw new IllegalStateException("headerHeight not set");
		}

		if (height < 0 || width < 0)
			throw new IllegalStateException("dimensions not set");

		dataChanged = false;

		frameDescriptors.clear();

		int cols = width / itemWidth;

		int topStart = 0;

		for (int i = 0; i < itemsAdapter.getNumberOfSections(); i++) {

			Section s = itemsAdapter.getSection(i);

			if (s.shouldDisplayHeader()) {
				FrameDescriptor header = new FrameDescriptor();
				Frame hframe = new Frame();
				header.itemSection = i;
				header.itemIndex = -1;
				header.isHeader = true;
				hframe.left = 0;
				hframe.top = topStart;
				hframe.width = headerWidth;
				hframe.height = headerHeight;
				header.frame = hframe;
				header.data = s.getSectionTitle();
				frameDescriptors.put(header.data, header);
				topStart += headerHeight;
			}

			for (int j = 0; j < s.getDataCount(); j++) {
				FrameDescriptor descriptor = new FrameDescriptor();
				Frame frame = new Frame();
				descriptor.itemSection = i;
				descriptor.itemIndex = j;
				frame.left = (j % cols) * itemWidth;
				frame.top = (j / cols) * itemHeight + topStart;
				frame.width = itemWidth;
				frame.height = itemHeight;
				descriptor.frame = frame;
				descriptor.data = s.getData().get(j);
				frameDescriptors.put(descriptor.data, descriptor);
			}
			int mod = 0;
			if (s.getDataCount() % cols != 0)
				mod = 1;

			topStart += ((s.getDataCount() / cols) + mod) * itemHeight;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HashMap<? extends Object, FrameDescriptor> getFrameDescriptors(int viewPortLeft, int viewPortTop) {
		HashMap<Object, FrameDescriptor> desc = new HashMap<Object, FrameDescriptor>();

		if(frameDescriptors.size() == 0 || dataChanged) {
			generateFrameDescriptors();
		}
		
		for (FrameDescriptor fd : frameDescriptors.values()) {
			if (fd.frame.top + itemHeight > viewPortTop && fd.frame.top < viewPortTop + height) {
				FrameDescriptor newDesc = FrameDescriptor.clone(fd);
				desc.put(newDesc.data, newDesc);
			}
		}

		return desc;
	}

	@Override
	public Frame getOffScreenStartFrame() {
		Frame frame = new Frame();
		frame.height = itemHeight;
		frame.width = itemWidth;
		frame.left = 0;
		frame.top = height;

		return frame;
	}

	@Override
	public boolean horizontalDragEnabled() {
		return false;
	}

	@Override
	public boolean verticalDragEnabled() {
		return true;
	}

	@Override
	public int getMinimumViewPortX() {
		return 0;
	}

	@Override
	public int getMinimumViewPortY() {
		return 0;
	}

	@Override
	public int getMaximumViewPortX() {
		return 0;
	}

	@Override
	public int getMaximumViewPortY() {
		if (itemsAdapter == null)
			return 0;

		int sectionIndex = itemsAdapter.getNumberOfSections() - 1;
		Section s = itemsAdapter.getSection(sectionIndex);

		if (s.getDataCount() == 0)
			return 0;

		Object lastFrameData = s.getData().get(s.getDataCount() - 1);
		FrameDescriptor fd = frameDescriptors.get(lastFrameData);

		return (fd.frame.top + fd.frame.height) - height;
	}

	@Override
	public FrameDescriptor getFrameDescriptorForItem(Object data) {
		if(frameDescriptors.size() == 0 || dataChanged) {
			generateFrameDescriptors();
		}
		
		if (frameDescriptors.get(data) == null)
			return null;

		FrameDescriptor fd = FrameDescriptor.clone(frameDescriptors.get(data));
		return fd;
	}

	@Override
	public void setHeaderItemDimensions(int hWidth, int hHeight) {
		if (hWidth == headerWidth && headerHeight == hHeight)
			return;

		dataChanged = true;
		headerWidth = hWidth;
		headerHeight = hHeight;
	}

}
