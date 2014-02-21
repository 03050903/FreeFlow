package org.freeflow.layouts;

import java.util.HashMap;

import org.freeflow.core.SectionedAdapter;
import org.freeflow.core.ItemProxy;
import org.freeflow.core.Section;
import org.freeflow.layouts.AbstractLayout.FreeFlowLayoutParams;
import org.freeflow.layouts.VGridLayout.LayoutParams;
import org.freeflow.utils.ViewUtils;

import android.graphics.Rect;

public class HGridLayout extends AbstractLayout {

	private boolean dataChanged = false;
	private static final String TAG = "HGridLayout";
	private int itemHeight = -1;
	private int itemWidth = -1;
	private int width = -1;
	private int height = -1;
	private SectionedAdapter itemsAdapter;
	private HashMap<Object, ItemProxy> frameDescriptors = new HashMap<Object, ItemProxy>();
	private int headerWidth = -1;
	private int headerHeight = -1;
	private int cellBufferSize = 0;
	private int bufferCount = 1;


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
	public void setLayoutParams(FreeFlowLayoutParams params){
		if(params.equals(this.layoutParams)){
			return;
		}
		LayoutParams lp = (LayoutParams)params;
		this.itemWidth = lp.itemWidth;
		this.itemHeight = lp.itemHeight;
		this.headerWidth = lp.headerWidth;
		this.headerHeight = lp.headerHeight;
		cellBufferSize = bufferCount * cellBufferSize;
		dataChanged = true;
	}

	@Override
	public void setAdapter(SectionedAdapter adapter) {
		this.itemsAdapter = adapter;
		dataChanged = true;
	}

	public void generateItemProxies() {
		dataChanged = false;

		frameDescriptors.clear();

		int rows = height / itemHeight;
		int leftStart = 0;

		for (int i = 0; i < itemsAdapter.getNumberOfSections(); i++) {

			Section s = itemsAdapter.getSection(i);

			if (itemsAdapter.shouldDisplaySectionHeaders()) {
				
				if (headerWidth < 0) {
					throw new IllegalStateException("headerWidth not set");
				}

				if (headerHeight < 0) {
					throw new IllegalStateException("headerHeight not set");
				}
				
				ItemProxy header = new ItemProxy();
				Rect hframe = new Rect();
				header.itemSection = i;
				header.itemIndex = -1;
				header.isHeader = true;
				hframe.left = leftStart;
				hframe.top = 0;
				hframe.right = leftStart + headerWidth;
				hframe.bottom = headerHeight;
				header.frame = hframe;
				header.data = s.getSectionTitle();
				frameDescriptors.put(header.data, header);

				leftStart += headerWidth;
			}

			for (int j = 0; j < s.getDataCount(); j++) {
				ItemProxy descriptor = new ItemProxy();
				Rect frame = new Rect();
				descriptor.itemSection = i;
				descriptor.itemIndex = j;
				frame.left = (j / rows) * itemWidth + leftStart;
				frame.top = (j % rows) * itemHeight;
				frame.right = frame.left + itemWidth;
				frame.bottom = frame.top + itemHeight;
				descriptor.frame = frame;
				descriptor.data = s.getDataAtIndex(j);
				frameDescriptors.put(descriptor.data, descriptor);
			}
			int mod = 0;
			if (s.getDataCount() % rows != 0)
				mod = 1;
			leftStart += ((s.getDataCount() / rows) + mod) * itemWidth;
		}
	}

	/**
	 * NOTE: In this instance, we subtract/add the cellBufferSize (computed when
	 * item width is set, defaulted to 1 cell) to add a buffer of cellBufferSize
	 * to each end of the viewport. <br>
	 * 
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public HashMap<? extends Object, ItemProxy> getItemProxies(int viewPortLeft, int viewPortTop) {
		HashMap<Object, ItemProxy> desc = new HashMap<Object, ItemProxy>();

		if (frameDescriptors.size() == 0 || dataChanged) {
			generateItemProxies();
		}

		for (ItemProxy fd : frameDescriptors.values()) {

			if (fd.frame.left + itemWidth > viewPortLeft - cellBufferSize
					&& fd.frame.left < viewPortLeft + width + cellBufferSize) {
				ItemProxy newDesc = ItemProxy.clone(fd);
				desc.put(newDesc.data, newDesc);
			}
		}

		return desc;
	}
	
	@Override
	public ItemProxy getItemAt(float x, float y){
		return ViewUtils.getItemAt(frameDescriptors, (int)x, (int)y);
	}

	@Override
	public boolean horizontalDragEnabled() {
		return true;
	}

	@Override
	public boolean verticalDragEnabled() {
		return false;
	}

	@Override
	public int getContentWidth() {
		if (itemsAdapter == null)
			return 0;

		int sectionIndex = itemsAdapter.getNumberOfSections() - 1;
		Section s = itemsAdapter.getSection(sectionIndex);

		if (s.getDataCount() == 0)
			return 0;

		Object lastFrameData = s.getDataAtIndex(s.getDataCount() - 1);
		ItemProxy fd = frameDescriptors.get(lastFrameData);

		return (fd.frame.left + fd.frame.width());
	}

	@Override
	public int getContentHeight() {
		if (itemsAdapter == null)
			return 0;

		return height;
	}

	@Override
	public ItemProxy getItemProxyForItem(Object data) {
		if (frameDescriptors.size() == 0 || dataChanged) {
			generateItemProxies();
		}

		ItemProxy fd = ItemProxy.clone(frameDescriptors.get(data));

		return fd;
	}

	public void setBufferCount(int bufferCount) {
		this.bufferCount = bufferCount;
	}
	
	public static class LayoutParams extends FreeFlowLayoutParams{
		public int itemWidth = 0;
		public int itemHeight = 0;
		public int headerWidth = 0;
		public int headerHeight = 0;
		
		public LayoutParams(int itemWidth, int itemHeight){
			this.itemWidth = itemWidth;
			this.itemHeight = itemHeight;
		}
		
		public LayoutParams(int itemWidth, int itemHeight, int headerWidth, int headerHeight){
			this.itemWidth = itemWidth;
			this.itemHeight = itemHeight;
			this.headerWidth = headerWidth;
			this.headerHeight = headerHeight;
		}
		
	}

}
