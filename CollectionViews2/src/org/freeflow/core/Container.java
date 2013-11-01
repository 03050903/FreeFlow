package org.freeflow.core;

import java.util.ArrayList;

import org.freeflow.layouts.LayoutController;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class Container extends ViewGroup {

	private static final String TAG = "Container";
	protected SparseArray<View> usedViews;
	protected ArrayList<View> viewpool;
	protected SparseArray<FrameDescriptor> frames = null;
	private boolean preventLayout = false;
	protected BaseAdapter itemAdapter;
	protected LayoutController layoutController;
	public int viewPortX = 0;
	public int viewPortY = 0;

	private VelocityTracker mVelocityTracker = null;
	private float deltaX = -1f;
	private float deltaY = -1f;

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
		usedViews = new SparseArray<View>();
		viewpool = new ArrayList<View>();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec) - 100);
		if (layoutController != null) {
			preventLayout = true;
			layoutController.setDimensions(getMeasuredWidth(), getMeasuredHeight());
			frames = layoutController.getFrameDescriptors(viewPortX, viewPortY);

			for (int i = 0; i < frames.size(); i++) {
				FrameDescriptor frameDesc = frames.get(frames.keyAt(i));
				addAndMeasureViewIfNeeded(frameDesc);
			}
			cleanupViews();
		}
		preventLayout = false;

	}

	private void addAndMeasureViewIfNeeded(FrameDescriptor frameDesc) {
		if (usedViews.get(frameDesc.itemIndex) == null) {
			View view = itemAdapter.getView(frameDesc.itemIndex, viewpool.size() > 0 ? viewpool.remove(0) : null, this);
			view.setAlpha(1);
			usedViews.append(frameDesc.itemIndex, view);
			preventLayout = true;
			addView(view);
			preventLayout = false;
			doMeasure(frameDesc);
		} else {
			doMeasure(frameDesc);
		}
	}

	private void doMeasure(FrameDescriptor frameDesc) {

		int widthSpec = MeasureSpec.makeMeasureSpec(frameDesc.frame.width, MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(frameDesc.frame.height, MeasureSpec.EXACTLY);

		usedViews.get(frameDesc.itemIndex).measure(widthSpec, heightSpec);

	}

	private void cleanupViews() {
		if (usedViews == null)
			return;

		for (int i = usedViews.size() - 1; i >= 0; i--) {
			if (frames.get(usedViews.keyAt(i)) != null)
				continue;

			final View view = usedViews.get(usedViews.keyAt(i));
			usedViews.remove(usedViews.keyAt(i));

			view.animate().alpha(0).setDuration(250).withEndAction(new Runnable() {

				@Override
				public void run() {
					viewpool.add(view);
					preventLayout = true;
					removeView(view);
					preventLayout = false;
				}
			}).start();

		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (layoutController == null || frames == null)
			return;
		for (int i = 0; i < usedViews.size(); i++) {

			View v = usedViews.get(usedViews.keyAt(i));

			FrameDescriptor desc = frames.get(usedViews.keyAt(i));

			if (desc == null)
				continue;

			Frame frame = desc.frame;

			if (v == null || frame == null)
				continue;

			doLayout(v, frame);

		}

	}

	private void doLayout(View view, Frame frame) {
		view.layout(frame.left, frame.top, frame.left + frame.width, frame.top + frame.height);
	}

	public void setLayout(LayoutController lc) {

		layoutController = lc;

		SparseArray<FrameDescriptor> oldFrames = frames;

		if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0)
			layoutController.setDimensions(getMeasuredWidth(), getMeasuredHeight());

		if (this.itemAdapter != null) {
			layoutController.setItems(itemAdapter);
		}

		if (frames != null) {
			int index = frames.keyAt(0);

			Frame vpFrame = layoutController.getViewportFrameForItemIndex(index);

			viewPortX = vpFrame.left;
			viewPortY = vpFrame.top;

			if (oldFrames != null) {

				frames = layoutController.getFrameDescriptors(viewPortX, viewPortY);
				preventLayout = true;
				// cleanupViews();

				for (int i = 0; i < frames.size(); i++) {
					int itemIndex = frames.keyAt(i);
					final FrameDescriptor nf = frames.get(itemIndex);

					if (oldFrames.get(itemIndex) != null)
						oldFrames.remove(itemIndex);

					getAnimationForLayoutTransition(itemIndex, nf).start();
				}

				for (int i = 0; i < oldFrames.size(); i++) {
					int itemIndex = oldFrames.keyAt(i);
					final FrameDescriptor nf = new FrameDescriptor();
					nf.frame = layoutController.getFrameForItemIndexAndViewport(itemIndex, viewPortX, viewPortY);
					nf.itemIndex = itemIndex;

					getAnimationForLayoutTransition(itemIndex, nf).start();
				}

				preventLayout = false;

			}

		} else {
			requestLayout();
		}

	}

	protected ValueAnimator getAnimationForLayoutTransition(final int itemIndex, final FrameDescriptor nf) {

		boolean newFrame = false;
		if (usedViews.get(nf.itemIndex) == null) {
			addAndMeasureViewIfNeeded(nf);
			newFrame = true;
		}

		View v = usedViews.get(itemIndex);

		Frame of = new Frame();
		if (newFrame) {
			of = layoutController.getOffScreenStartFrame();
		} else {
			of.left = v.getLeft();
			of.top = v.getTop();
			of.width = v.getMeasuredWidth();
			of.height = v.getMeasuredHeight();
		}

		return layoutController.getAnimationForLayoutTransition(itemIndex, of, nf, v);

	}

	@Override
	public void requestLayout() {

		if (preventLayout)
			return;

		super.requestLayout();
	}

	public void setAdapter(BaseAdapter adapter) {
		this.itemAdapter = adapter;
		if (layoutController != null) {
			layoutController.setItems(adapter);
		}
	}

	public LayoutController getLayoutController() {
		return layoutController;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

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

						if (animation.getAnimatedFraction() == 1f)
							requestLayout();
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

		if (layoutController.horizontalDragEnabled())
			viewPortX = (int) (viewPortX - movementX);

		if (layoutController.verticalDragEnabled())
			viewPortY = (int) (viewPortY - movementY);

		if (viewPortX < layoutController.getMinimumViewPortX())
			viewPortX = layoutController.getMinimumViewPortX();
		else if (viewPortX > layoutController.getMaximumViewPortX())
			viewPortX = layoutController.getMaximumViewPortX();

		if (viewPortY < layoutController.getMinimumViewPortY())
			viewPortY = layoutController.getMinimumViewPortY();
		else if (viewPortY > layoutController.getMaximumViewPortY())
			viewPortY = layoutController.getMaximumViewPortY();

		frames = layoutController.getFrameDescriptors(viewPortX, viewPortY);

		for (int i = 0; i < frames.size(); i++) {
			FrameDescriptor desc = frames.get(frames.keyAt(i));

			preventLayout = true;
			if (usedViews.get(desc.itemIndex) == null)
				addAndMeasureViewIfNeeded(desc);
			preventLayout = false;

			View view = usedViews.get(desc.itemIndex);
			doLayout(view, desc.frame);

		}

		cleanupViews();

	}

	public BaseAdapter getAdapter() {
		return itemAdapter;
	}

}
