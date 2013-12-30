package org.freeflow.layouts.animations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.freeflow.core.Container;
import org.freeflow.core.Frame;
import org.freeflow.core.ItemProxy;
import org.freeflow.core.LayoutChangeSet;
import org.freeflow.core.StateListener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.View.MeasureSpec;
import android.view.animation.DecelerateInterpolator;

public class DefaultLayoutAnimator extends LayoutAnimator {

	public static final String TAG = "DefaultLayoutAnimator";
	private int duration = 250;

	protected Container callback;
	protected AnimatorSet disappearingSet;
	protected AnimatorSet appearingSet;
	protected ArrayList<ValueAnimator> movingSet;

	public DefaultLayoutAnimator() {
		movingSet = new ArrayList<ValueAnimator>();
	}

	@Override
	public void cancel() {
		for (ValueAnimator anim : movingSet) {
			anim.cancel();
		}

		movingSet.clear();

		if (disappearingSet != null)
			disappearingSet.cancel();

		if (appearingSet != null)
			appearingSet.cancel();

	}

	@Override
	public void animateChanges(LayoutChangeSet changeSet, final Container callback) {
		this.changeSet = changeSet;
		this.callback = callback;

		Comparator<ItemProxy> cmp = new Comparator<ItemProxy>() {

			@Override
			public int compare(ItemProxy lhs, ItemProxy rhs) {
				return (lhs.itemSection * 1000 + lhs.itemIndex) - (rhs.itemSection * 1000 + rhs.itemIndex);
			}
		};

		AnimatorSet lastAnim = null;
		AnimatorSet firstAnim = null;
		ArrayList<ItemProxy> removed = changeSet.getRemoved();
		if (removed.size() > 0) {
			Collections.sort(removed, cmp);
			disappearingSet = new AnimatorSet();
			ArrayList<Animator> fades = new ArrayList<Animator>();
			for (ItemProxy proxy : removed) {
				fades.add(ObjectAnimator.ofFloat(proxy.view, "alpha", 0));
			}
			disappearingSet.setDuration(300 / removed.size());
			disappearingSet.playSequentially(fades);
			lastAnim = disappearingSet;
			firstAnim = disappearingSet;
		}

		ArrayList<ItemProxy> added = changeSet.getAdded();
		if (added.size() > 0) {
			Collections.sort(added, cmp);
			appearingSet = new AnimatorSet();
			ArrayList<Animator> fadeIns = new ArrayList<Animator>();
			for (ItemProxy proxy : added) {
				proxy.view.setAlpha(0);
				fadeIns.add(ObjectAnimator.ofFloat(proxy.view, "alpha", 1));
			}
			appearingSet.playSequentially(fadeIns);
			appearingSet.setDuration(300 / added.size());
			if (firstAnim == null) {
				firstAnim = appearingSet;
			} else {
				appearingSet.setStartDelay(300);
				lastAnim = appearingSet;
			}
		}

		if (firstAnim != null) {
			firstAnim.start();
		} else {
			callback.onLayoutChangeAnimationsCompleted(this);
		}

		if (lastAnim != null) {

			lastAnim.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					animateMovedViews();
					callback.onLayoutChangeAnimationsCompleted(DefaultLayoutAnimator.this);
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}

			});
			lastAnim.start();
		} else {
			animateMovedViews();
			callback.onLayoutChangeAnimationsCompleted(DefaultLayoutAnimator.this);
		}

	}

	protected void animateMovedViews() {
		ArrayList<Pair<ItemProxy, Frame>> moved = changeSet.getMoved();

		for (Pair<ItemProxy, Frame> item : moved) {
			ItemProxy proxy = ItemProxy.clone(item.first);
			View v = proxy.view;

			if (v instanceof StateListener)
				((StateListener) v).ReportCurrentState(proxy.state);

			proxy.frame.left -= callback.viewPortX;
			proxy.frame.top -= callback.viewPortY;

			// Log.d(TAG, "vpx = " + callback.viewPortX + ", vpy = " +
			// callback.viewPortY);

			// if (v instanceof StateListener)
			// ((StateListener) v).ReportCurrentState(proxy.state);

			transitionToFrame(item.second, proxy, v);

		}
	}

	// @Override
	public void transitionToFrame(final Frame of, final ItemProxy nf, final View v) {
		ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
		anim.setDuration(duration);
		final float alpha = v.getAlpha();
		anim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {

				try {

					int itemWidth = of.width + (int) ((nf.frame.width - of.width) * animation.getAnimatedFraction());
					int itemHeight = of.height
							+ (int) ((nf.frame.height - of.height) * animation.getAnimatedFraction());
					int widthSpec = MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY);
					int heightSpec = MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY);

					v.measure(widthSpec, heightSpec);

					Frame frame = new Frame();
					Frame nff = nf.frame;

					frame.left = (int) (of.left + (nff.left - of.left) * animation.getAnimatedFraction());
					frame.top = (int) (of.top + (nff.top - of.top) * animation.getAnimatedFraction());
					frame.width = (int) (of.width + (nff.width - of.width) * animation.getAnimatedFraction());
					frame.height = (int) (of.height + (nff.height - of.height) * animation.getAnimatedFraction());

					v.layout(frame.left, frame.top, frame.left + frame.width, frame.top + frame.height);

					v.setAlpha((1 - alpha) * animation.getAnimatedFraction() + alpha);
				} catch (NullPointerException e) {
					e.printStackTrace();
					animation.cancel();
				}
			}

		});

		movingSet.add(anim);

		anim.setInterpolator(new DecelerateInterpolator(2.0f));

		anim.start();

	}

	@Override
	public void start() {
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
