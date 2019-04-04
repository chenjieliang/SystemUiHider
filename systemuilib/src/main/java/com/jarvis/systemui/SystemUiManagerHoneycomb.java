package com.jarvis.systemui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * An API 11+ implementation of {@link SystemUiManager}. Uses APIs available in
 * Honeycomb and later (specifically {@link View#setSystemUiVisibility(int)}) to
 * show and hide the system UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SystemUiManagerHoneycomb extends SystemUiManager {
	/**
	 * Flags for {@link View#setSystemUiVisibility(int)} to use when showing the
	 * system UI.
	 */
	private int mShowFlags;

	/**
	 * Flags for {@link View#setSystemUiVisibility(int)} to use when hiding the
	 * system UI.
	 */
	private int mHideFlags;

	/**
	 * Flags to test against the first parameter in
	 * {@link View.OnSystemUiVisibilityChangeListener#onSystemUiVisibilityChange(int)}
	 * to determine the system UI visibility state.
	 */
	private int mTestFlags;

	/**
	 * Whether or not the system UI is currently visible. This is cached from
	 * {@link View.OnSystemUiVisibilityChangeListener}.
	 */
	private boolean mVisible = true;

	/**
	 * Constructor not intended to be called by clients. Use
	 * {@link SystemUiManager#getInstance} to obtain an instance.
	 */
	protected SystemUiManagerHoneycomb(Activity activity) {
		super(activity);
	}

	/** {@inheritDoc} */
	@Override
	public void setup() {

		mShowFlags = View.SYSTEM_UI_FLAG_VISIBLE;
		mHideFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		mTestFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				|View.SYSTEM_UI_FLAG_FULLSCREEN;

		if ( (mFlags == FLAG_FULLSCREEN) ) {
			// If the client requested fullscreen, add flags relevant to hiding
			// the status bar. Note that some of these constants are new as of
			// API 16 (Jelly Bean). It is safe to use them, as they are inlined
			// at compile-time and do nothing on pre-Jelly Bean devices.
			mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LOW_PROFILE
					|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		if ( (mFlags== FLAG_HIDE_NAVIGATION) ) {
			// If the client requested hiding navigation, add relevant flags.
			mHideFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		if ( (mFlags == FLAG_HIDE_STATUS_BAR) ) {
			mHideFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					|View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LOW_PROFILE
					|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		mAnchorView
				.setOnSystemUiVisibilityChangeListener(mSystemUiVisibilityChangeListener);
	}

	/** {@inheritDoc} */
	@Override
	public void hide() {
		if(mAnchorView.getSystemUiVisibility()!=mHideFlags){
			mAnchorView.setSystemUiVisibility(mHideFlags);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void show() {
		mAnchorView.setSystemUiVisibility(mShowFlags);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isVisible() {
		return mVisible;
	}

	private View.OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
		@Override
		public void onSystemUiVisibilityChange(int vis) {
			// Test against mTestFlags to see if the system UI is visible.
			if ((vis & mTestFlags) != 0) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					// Pre-Jelly Bean, we must manually hide the action bar
					// and use the old window flags API.
					mActivity.getActionBar().hide();
					mActivity.getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}

				// Trigger the registered listener and cache the visibility
				// state.
				mOnVisibilityChangeListener.onVisibilityChange(false);
				mVisible = false;
		
			} else {
			
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					// Pre-Jelly Bean, we must manually show the action bar
					// and use the old window flags API.
					mActivity.getActionBar().show();
					mActivity.getWindow().setFlags(0,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}

				// Trigger the registered listener and cache the visibility
				// state.
				mOnVisibilityChangeListener.onVisibilityChange(true);
				mVisible = true;
				
			}
		}
	};
}
