package org.freeflow.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.freeflow.layouts.AbstractLayout;
import org.freeflow.layouts.animations.DefaultLayoutAnimator;
import org.freeflow.layouts.animations.LayoutAnimator;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

public class Container extends ViewGroup {

	private static final String TAG = "Container";
	protected HashMap<Object, View> usedViews;
	protected HashMap<Object, View> usedHeaderViews;
	protected ArrayList<View> viewpool;
	protected ArrayList<View> headerViewpool;
	protected HashMap<? extends Object, ItemProxy> frames = null;
	private boolean preventLayout = false;
	protected BaseSectionedAdapter itemAdapter;
	protected AbstractLayout layout;	
	public int viewPortX = 0;
	public int viewPortY = 0;

	private VelocityTracker mVelocityTracker = null;
	private float deltaX = -1f;
	private float deltaY = -1f;
	
	private LayoutAnimator layoutAnimator = new DefaultLayoutAnimator();

	public Container(Context context) {
		super(context);
		init();
	}

	public Container(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Container(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		usedViews = new HashMap<Object, View>();
		viewpool = new ArrayList<View>();
		usedHeaderViews = new HashMap<Object, View>();
		headerViewpool = new ArrayList<View>();
		frames = new HashMap<Object, ItemProxy>();

		((HashMap<Object, ItemProxy>) frames).put(new Object(), new ItemProxy());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		if (layout != null) {
			layout.setDimensions(getMeasuredWidth(), getMeasuredHeight());
			frames = layout.getItemProxies(viewPortX, viewPortY);

			for (ItemProxy frameDesc : frames.values()) {
				addAndMeasureViewIfNeeded(frameDesc);
			}

			cleanupViews();
		}
	}

	private void addAndMeasureViewIfNeeded(ItemProxy frameDesc) {
		View view;
		if (frameDesc.isHeader){
			view = usedHeaderViews.get(frameDesc.data);
			if(view == null){
				view = itemAdapter.getHeaderViewForSection(frameDesc.itemSection,
						headerViewpool.size() > 0 ? headerViewpool.remove(0) : null, this);
				usedHeaderViews.put(frameDesc.data, view);
				addView(view);
			}
			
		}
		else{
			view = usedViews.get(frameDesc.data);
			if(view == null){
				view = itemAdapter.getViewForSection(frameDesc.itemSection, frameDesc.itemIndex,
						viewpool.size() > 0 ? viewpool.remove(0) : null, this);
				usedViews.put(frameDesc.data, view);
				addView(view);
			}
		}
				
		doMeasure(view, frameDesc);
	}

	private void doMeasure(View v, ItemProxy frameDesc) {

		int widthSpec = MeasureSpec.makeMeasureSpec(frameDesc.frame.width, MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(frameDesc.frame.height, MeasureSpec.EXACTLY);
		v.measure(widthSpec, heightSpec);
		if (v instanceof StateListener)
			((StateListener) v).ReportCurrentState(frameDesc.state);

	}

	private void cleanupViews() {

		if (usedViews == null) {
			return;
		}

		Iterator it = usedViews.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();

			if (frames.get(m.getKey()) != null)
				continue;

			final View view = (View) m.getValue();
			it.remove();
			viewpool.add(view);
			removeView(view);

		}

		it = usedHeaderViews.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();

			if (frames.get(m.getKey()) != null)
				continue;

			final View view = (View) m.getValue();
			it.remove();

			headerViewpool.add(view);
			removeView(view);

		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		if (layout == null || frames == null) {
			// if (DEBUG)
			// Log.d(TAG, "onLayout End " + (System.currentTimeMillis() -
			// start));
			return;
		}

		Iterator it = usedViews.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();
			View v = (View) m.getValue();

			ItemProxy desc = frames.get(m.getKey());

			if (desc == null)
				continue;

			Frame frame = desc.frame;

			if (v == null || frame == null)
				continue;

			doLayout(v, frame);

		}

		it = usedHeaderViews.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();

			View v = (View) m.getValue();

			ItemProxy desc = frames.get(m.getKey());

			if (desc == null)
				continue;

			Frame frame = desc.frame;

			if (v == null || frame == null)
				continue;

			doLayout(v, frame);
		}

	}

	private void doLayout(View view, Frame frame) {

		view.layout(frame.left - viewPortX, frame.top - viewPortY, frame.left + frame.width - viewPortX, frame.top
				+ frame.height - viewPortY);

	}

	public void setLayout(AbstractLayout lc) {

		if (lc == layout) {
			return;
		}

		boolean shouldReturn = layout == null;

		layout = lc;

		HashMap<? extends Object, ItemProxy> oldFrames = frames;

		if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0)
			layout.setDimensions(getMeasuredWidth(), getMeasuredHeight());

		if (this.itemAdapter != null) {
			layout.setItems(itemAdapter);
		}

		if (shouldReturn)
			return;

		if (frames != null && frames.size() > 0) {

			Object data = null;
			int lowestSection = 99999;
			int lowestPosition = 99999;
			for (ItemProxy fd : frames.values()) {
				if (fd.itemSection < lowestSection
						|| (fd.itemSection == lowestSection && fd.itemIndex < lowestPosition)) {
					data = fd.data;
					lowestSection = fd.itemSection;
					lowestPosition = fd.itemIndex;
				}
			}

			Frame vpFrame = layout.getItemProxyForItem(data).frame;

			viewPortX = vpFrame.left;
			viewPortY = vpFrame.top;

			if (viewPortX > layout.getMaximumViewPortX())
				viewPortX = layout.getMaximumViewPortX();

			if (viewPortY > layout.getMaximumViewPortY())
				viewPortY = layout.getMaximumViewPortY();

			Log.d(TAG, viewPortX + ", " + viewPortY);

			if (oldFrames != null) {
				// Create a copy of the incoming values because the source Layout
				// may change the map inside its own class 
				HashMap<Object, ItemProxy> newFrames = new HashMap<Object, ItemProxy>(layout.getItemProxies(viewPortX, viewPortY));
				layoutChanged(oldFrames, newFrames);
				
			}

		} else {
			requestLayout();
		}

	}

	protected void transitionToFrame(final ItemProxy nf) {

		boolean newFrame = false;
		if (nf.isHeader) {
			if (usedHeaderViews.get(nf.data) == null) {
				addAndMeasureViewIfNeeded(nf);
				newFrame = true;
			}
		} else {
			if (usedViews.get(nf.data) == null) {
				addAndMeasureViewIfNeeded(nf);
				newFrame = true;
			}
		}

		View v = nf.isHeader ? usedHeaderViews.get(nf.data) : usedViews.get(nf.data);

		Frame of = new Frame();
		if (newFrame) {
			of = layout.getOffScreenStartFrame();
		} else {
			of.left = (int) (v.getLeft() + v.getTranslationX());
			of.top = (int) (v.getTop() + v.getTranslationY());
			of.width = v.getWidth();
			of.height = v.getHeight();
		}

		if (v instanceof StateListener)
			((StateListener) v).ReportCurrentState(nf.state);
		if (nf.frame.equals(of)) {
			return;
		}

		layoutAnimator.transitionToFrame(of, nf, v);

	}

	public void layoutChanged() {
		HashMap<Object, ItemProxy> newFrames = new HashMap<Object, ItemProxy>(layout.getItemProxies(viewPortX, viewPortY));
		layoutChanged(frames, newFrames);
	}

	private void layoutChanged(HashMap<? extends Object, ItemProxy> oldFrames, HashMap<? extends Object, ItemProxy> newFrames) {

		layoutAnimator.clear();
		preventLayout = true;
		// cleanupViews();

		Iterator<?> it = newFrames.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();
			ItemProxy nf = ItemProxy.clone((ItemProxy) m.getValue());
			
			nf.frame.left -= viewPortX;
			nf.frame.top -= viewPortY;
			
			if (oldFrames.get(m.getKey()) != null)
				oldFrames.remove(m.getKey());

			transitionToFrame(nf);

		}

		it = oldFrames.keySet().iterator();
		while (it.hasNext()) {
			ItemProxy nf = layout.getItemProxyForItem(it.next());
			nf.frame.left -= viewPortX;
			nf.frame.top -= viewPortY;
			transitionToFrame(nf);
		}

		layoutAnimator.start();

		preventLayout = false;
		frames = newFrames;
	}

	@Override
	public void requestLayout() {

		if (preventLayout)
			return;

		super.requestLayout();
	}

	public void setAdapter(BaseSectionedAdapter adapter) {
		this.itemAdapter = adapter;
		if (layout != null) {
			layout.setItems(adapter);
		}
	}

	public AbstractLayout getLayoutController() {
		return layout;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (layout == null)
			return false;
		if (!layout.horizontalDragEnabled() && !layout.verticalDragEnabled())
			return false;

		if (mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();

		mVelocityTracker.addMovement(event);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			deltaX = event.getX();
			deltaY = event.getY();

			return true;

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

			moveScreen(event.getX() - deltaX, event.getY() - deltaY);

			deltaX = event.getX();
			deltaY = event.getY();

			return true;

		} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			return true;

		} else if (event.getAction() == MotionEvent.ACTION_UP) {

			mVelocityTracker.computeCurrentVelocity(1000);

			// frames = layoutController.getFrameDescriptors(viewPortX,
			// viewPortY);

			if (Math.abs(mVelocityTracker.getXVelocity()) > 100) {
				final float velocityX = mVelocityTracker.getXVelocity();
				final float velocityY = mVelocityTracker.getYVelocity();
				ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
				animator.addUpdateListener(new AnimatorUpdateListener() {

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						int translateX = (int) ((1 - animation.getAnimatedFraction()) * velocityX / 350);
						int translateY = (int) ((1 - animation.getAnimatedFraction()) * velocityY / 350);

						moveScreen(translateX, translateY);

					}
				});

				animator.setDuration(500);
				animator.start();

			}

			return true;
		}

		return false;

	}

	private void moveScreen(float movementX, float movementY) {

		if (layout.horizontalDragEnabled())
			viewPortX = (int) (viewPortX - movementX);

		if (layout.verticalDragEnabled())
			viewPortY = (int) (viewPortY - movementY);

		if (viewPortX < layout.getMinimumViewPortX())
			viewPortX = layout.getMinimumViewPortX();
		else if (viewPortX > layout.getMaximumViewPortX())
			viewPortX = layout.getMaximumViewPortX();

		if (viewPortY < layout.getMinimumViewPortY())
			viewPortY = layout.getMinimumViewPortY();
		else if (viewPortY > layout.getMaximumViewPortY())
			viewPortY = layout.getMaximumViewPortY();

		frames = layout.getItemProxies(viewPortX, viewPortY);

		Iterator it = frames.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry m = (Map.Entry) it.next();

			ItemProxy desc = (ItemProxy) m.getValue();

			preventLayout = true;
			if (usedViews.get(desc.data) == null && usedHeaderViews.get(desc.data) == null)
				addAndMeasureViewIfNeeded(desc);
			preventLayout = false;

			View view;
			if (desc.isHeader)
				view = usedHeaderViews.get(desc.data);
			else
				view = usedViews.get(desc.data);

			doLayout(view, desc.frame);

		}

		cleanupViews();

	}

	public BaseSectionedAdapter getAdapter() {
		return itemAdapter;
	}
	
	public void setLayoutAnimator(LayoutAnimator anim){
		layoutAnimator = anim;
	}
	
	public LayoutAnimator getLayoutAnimator(){
		return layoutAnimator;
	}

}
