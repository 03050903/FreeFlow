/*******************************************************************************
 * Copyright 2013 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.comcast.freeflow.layouts;

import java.util.HashMap;

import com.comcast.freeflow.core.ItemProxy;
import com.comcast.freeflow.core.Section;
import com.comcast.freeflow.core.SectionedAdapter;
import com.comcast.freeflow.layouts.FreeFlowLayout.FreeFlowLayoutParams;
import com.comcast.freeflow.layouts.HLayout.LayoutParams;
import com.comcast.freeflow.utils.ViewUtils;

import android.graphics.Rect;

public class VLayout implements FreeFlowLayout {

	private boolean layoutChanged = false;
	private static final String TAG = "VLayout";
	private int itemHeight = -1;
	private int width = -1;
	private int height = -1;
	private SectionedAdapter itemsAdapter;
	private HashMap<Object, ItemProxy> proxies = new HashMap<Object, ItemProxy>();
	private int headerHeight = -1;
	private int headerWidth = -1;

	private int cellBufferSize = 0;
	private int bufferCount = 1;
	
	protected FreeFlowLayoutParams layoutParams;
	
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
		layoutChanged = true;
	}

	@Override
	public void setLayoutParams(FreeFlowLayoutParams params) {
		if (params.equals(this.layoutParams)) {
			return;
		}
		LayoutParams lp = (LayoutParams) params;
		this.itemHeight = lp.itemHeight;
		this.headerWidth = lp.headerWidth;
		this.headerHeight = lp.headerHeight;
		cellBufferSize = bufferCount * cellBufferSize;
		layoutChanged = true;
	}

	@Override
	public void setAdapter(SectionedAdapter adapter) {
		this.itemsAdapter = adapter;

		layoutChanged = true;
	}

	public void generateItemProxies() {
		if (itemHeight < 0) {
			throw new IllegalStateException("itemHeight not set");
		}

		layoutChanged = false;

		proxies.clear();
		int topStart = 0;

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
				hframe.left = 0;
				hframe.top = topStart;
				hframe.right = headerWidth;
				hframe.bottom = topStart + headerHeight;
				header.frame = hframe;
				header.data = s.getSectionTitle();
				proxies.put(header.data, header);

				topStart += headerHeight;
			}

			for (int j = 0; j < s.getDataCount(); j++) {
				ItemProxy descriptor = new ItemProxy();
				Rect frame = new Rect();
				descriptor.itemSection = i;
				descriptor.itemIndex = j;
				frame.left = 0;
				frame.top = j * itemHeight + topStart;
				frame.right = width;
				frame.bottom = frame.top + itemHeight;
				descriptor.frame = frame;
				descriptor.data = s.getDataAtIndex(j);
				proxies.put(descriptor.data, descriptor);
			}

			topStart += (s.getDataCount()) * itemHeight;
		}

	}

	/**
	 * NOTE: In this instance, we subtract/add the cellBufferSize (computed when
	 * item height is set, defaulted to 1 cell) to add a buffer of
	 * cellBufferSize to each end of the viewport. <br>
	 * 
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public HashMap<? extends Object, ItemProxy> getItemProxies(
			int viewPortLeft, int viewPortTop) {
		HashMap<Object, ItemProxy> desc = new HashMap<Object, ItemProxy>();

		if (proxies.size() == 0 || layoutChanged) {
			generateItemProxies();
		}

		for (ItemProxy fd : proxies.values()) {
			if (fd.frame.top + itemHeight > viewPortTop - cellBufferSize
					&& fd.frame.top < viewPortTop + height + cellBufferSize) {
				ItemProxy newDesc = ItemProxy.clone(fd);
				desc.put(newDesc.data, newDesc);
			}
		}

		return desc;
	}

	@Override
	public boolean horizontalScrollEnabled() {
		return false;
	}

	@Override
	public boolean verticalScrollEnabled() {
		return true;
	}

	@Override
	public int getContentWidth() {
		return width;
	}

	@Override
	public int getContentHeight() {
		if (itemsAdapter == null)
			return 0;

		int sectionIndex = itemsAdapter.getNumberOfSections() - 1;
		Section s = itemsAdapter.getSection(sectionIndex);

		if (s.getDataCount() == 0)
			return 0;

		Object lastFrameData = s.getDataAtIndex(s.getDataCount() - 1);
		ItemProxy fd = proxies.get(lastFrameData);

		return (fd.frame.top + fd.frame.height());
	}

	@Override
	public ItemProxy getItemProxyForItem(Object data) {
		if (proxies.size() == 0 || layoutChanged) {
			generateItemProxies();
		}

		ItemProxy fd = ItemProxy.clone(proxies.get(data));

		return fd;
	}

	@Override
	public ItemProxy getItemAt(float x, float y) {
		return ViewUtils.getItemAt(proxies, (int) x, (int) y);
	}

	public void setHeaderItemDimensions(int hWidth, int hHeight) {
		headerWidth = hWidth;
		headerHeight = hHeight;
	}

	public void setBufferCount(int bufferCount) {
		this.bufferCount = bufferCount;
	}

	public static class LayoutParams extends FreeFlowLayoutParams {
		public int itemHeight = 0;
		public int headerWidth = 0;
		public int headerHeight = 0;

		public LayoutParams(int itemHeight) {
			this.itemHeight = itemHeight;
		}

		public LayoutParams(int itemHeight, int headerWidth, int headerHeight) {
			this.itemHeight = itemHeight;
			this.headerWidth = headerWidth;
			this.headerHeight = headerHeight;
		}
	}

}
