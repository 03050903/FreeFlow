package org.freeflow.collectionviews2;

import org.freeflow.core.Container;
import org.freeflow.layouts.HLayout;
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
	boolean hLayoutUsed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

		String[] images = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
				"14", "15", "16", "17", "18", "19" };
		ImageAdapter adapter = new ImageAdapter(images);

		container = new Container(this);
		container.setOnKeyListener(this);
		container.setOnTouchListener(this);
		container.setFocusable(true);
		container.requestFocus();
		hLayout = new HLayout();
		hLayout.setItemWidth(100);

		vLayout = new VLayout();
		vLayout.setItemHeight(100);

		container.setLayout(vLayout);
		container.setAdapter(adapter);

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
			return images.length;
		}

		@Override
		public Object getItem(int position) {
			return images[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Button button = null;
			if (convertView != null) {
				Log.d(TAG, "Convert view not null");
				button = (Button) convertView;
			} else {
				button = new Button(MainActivity.this);
			}

			button.setFocusable(false);
			button.setOnKeyListener(MainActivity.this);
			button.setOnTouchListener(MainActivity.this);
			button.setText("" + images[position]);

			return button;
		}

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (keyCode == KeyEvent.KEYCODE_SPACE) {
				// Log.d(TAG, "Space pressed");
				if (hLayoutUsed)
					container.setLayout(vLayout);
				else
					container.setLayout(hLayout);

				hLayoutUsed = !hLayoutUsed;
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_D) {
				// Log.d(TAG, "D pressed");
				if (hLayoutUsed) {
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
				if (hLayoutUsed) {
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
			if (hLayoutUsed)
				container.setLayout(vLayout);
			else
				container.setLayout(hLayout);

			hLayoutUsed = !hLayoutUsed;
			
		}

		return false;
	}

}
