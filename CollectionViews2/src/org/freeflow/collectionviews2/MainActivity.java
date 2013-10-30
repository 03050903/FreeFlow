package org.freeflow.collectionviews2;

import org.freeflow.core.Container;
import org.freeflow.layouts.HLayout;
import org.freeflow.layouts.VGridLayout;
import org.freeflow.layouts.VLayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements OnKeyListener, OnTouchListener {

	private static final String TAG = "MainActivity";
	Container container = null;
	HLayout hLayout = null;
	VLayout vLayout = null;
	VGridLayout vGridLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

		ImageAdapter adapter = new ImageAdapter(null);

		container = new Container(this);
		container.setOnKeyListener(this);
		container.setOnTouchListener(this);
		container.setFocusable(true);
		container.requestFocus();
		hLayout = new HLayout();
		hLayout.setItemWidth(100);

		vLayout = new VLayout();
		vLayout.setItemHeight(100);

		vGridLayout = new VGridLayout();
		vGridLayout.setItemHeight(200);
		vGridLayout.setItemWidth(200);

		container.setAdapter(adapter);
		container.setLayout(vGridLayout);

		frameLayout.addView(container);

	}

	class ImageAdapter extends BaseAdapter {

		private static final String TAG = "ImageAdapter";
		private String[] images;

		public ImageAdapter(String[] images) {
			this.images = images;
		}

		@Override
		public int getCount() {
			return 100;
		}

		@Override
		public Object getItem(int position) {
			return String.valueOf(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Button button = null;
			if (convertView != null) {
				// Log.d(TAG, "Convert view not null");
				button = (Button) convertView;
			} else {
				button = new Button(MainActivity.this);
			}

			button.setFocusable(false);
			button.setOnKeyListener(MainActivity.this);
			button.setOnTouchListener(MainActivity.this);
			button.setText("" + position);

			return button;
		}

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (keyCode == KeyEvent.KEYCODE_SPACE) {
				// Log.d(TAG, "Space pressed");
				if (container.getLayoutController() == hLayout)
					container.setLayout(vLayout);
				else if (container.getLayoutController() == vLayout)
					container.setLayout(vGridLayout);
				else
					container.setLayout(hLayout);

				return true;
			} else if (keyCode == KeyEvent.KEYCODE_D) {
				// Log.d(TAG, "D pressed");
				if (container.getLayoutController() == hLayout) {
					container.viewPortX += 5;
					container.viewPortX = container.viewPortX > 1000 ? 1000 : container.viewPortX;
					container.viewPortY = 0;
				} else {
					container.viewPortY += 5;
					container.viewPortY = container.viewPortY > 1000 ? 1000 : container.viewPortY;
					container.viewPortX = 0;
				}
				container.requestLayout();
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_A) {
				// Log.d(TAG, "A pressed");
				if (container.getLayoutController() == hLayout) {
					container.viewPortX -= 5;
					container.viewPortX = container.viewPortX < 0 ? 0 : container.viewPortX;
					container.viewPortY = 0;
				} else {
					container.viewPortY -= 5;
					container.viewPortY = container.viewPortY < 0 ? 0 : container.viewPortY;
					container.viewPortX = 0;
				}
				container.requestLayout();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (container.getLayoutController() == hLayout)
				container.setLayout(vLayout);
			else if (container.getLayoutController() == vLayout)
				container.setLayout(vGridLayout);
			else
				container.setLayout(hLayout);
		}

		return false;
	}

}
