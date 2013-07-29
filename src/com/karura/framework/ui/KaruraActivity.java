/**

 ============== GPL License ==============
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.


 ============== Commercial License==============
 https://github.com/karuradev/licenses/blob/master/toc.txt
 */

package com.karura.framework.ui;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.karura.framework.KaruraApp;
import com.karura.framework.R;
import com.karura.framework.config.ConfigStore;
import com.karura.framework.ui.webview.KaruraWebView;

/**
 * This is the base activity from which all your hybrid activities should derive
 * 
 * @author nitin
 * 
 */
public abstract class KaruraActivity extends Activity implements OnTouchListener {
	private static final String TAG = "MainActivity";
	
	boolean startUrlWasRequested = false;

	/*
	 * Android Lifecycle Callbacks
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (appConfig().appRequiresFullScreen()){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		if (appConfig().noActivityTitle()){
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);      
		}
		
		super.onCreate(savedInstanceState);

		/*
		 * Handle the wierd case when the application context is invalid, as the app is resumed.
		 */
		if (KaruraApp.getApp().getPackageManager() == null) {
			reinitFramework();
			return;
		}
		
		setContentView(R.layout.webview);

		getWebView().setOnTouchListener(this);

		// incase the activity is being recreated, try to restore the webview
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			getWebView().restoreState(savedInstanceState);
		} else if (shouldLoadContentInWebView()) {
			loadStartUrl();
			startUrlWasRequested = true;
		} 
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!shouldLoadContentInWebView()) {
			startUrlWasRequested = false;
			return;
		}
		if (!startUrlWasRequested) {
			reinitFramework();
			startUrlWasRequested= true;
		} else {
			getWebView().resumeTimers();
		}
	}

	@Override
	public void onPause() {
		// request garbage collection in the system and webview
		System.gc();
		if (getWebView() != null) {
			getWebView().pauseTimers();
			getWebView().freeMemory();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		// stop any ongoing webview activity
		if (getWebView() != null)
			getWebView().stopLoading();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (getWebView() == null) {
			return;
		}
		View contentView = findViewById(android.R.id.content);
		if (contentView instanceof ViewGroup) {
			((ViewGroup) contentView).removeView(getWebView());
		}
		getWebView().removeAllViews();
		// request to the webview to destory itself, and release memory
		getWebView().destroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// request webview to save its instance data
		if (getWebView() != null) {
			getWebView().saveState(outState);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// restore webview's instance data
		if (savedInstanceState != null && !savedInstanceState.isEmpty() && getWebView() != null) {
			getWebView().restoreState(savedInstanceState);
		}
	}
	
	/**
	 * Get the reference to the WebView control
	 * 
	 * @return WebView
	 */
	protected KaruraWebView getWebView() {
		return (KaruraWebView) findViewById(R.id.webview);
	}

	/**
	 * Utility method to access the application wide configStore
	 * 
	 * @return ConfigStore
	 */
	protected ConfigStore appConfig() {
		return KaruraApp.getApp().getAppConfig();
	}

	/**
	 * Get the start URL for the application. 
	 * @return URL for the initial page to be loaded for the applciation
	 */
	protected String startUrl() {
		return appConfig().startUrl();
	}

	private boolean isScreenOn() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	
	private boolean shouldLoadContentInWebView(){
		return isScreenOn();
	}

	private void reinitFramework() {
		Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
		startActivity(intent);
		this.finish();
	}

	private void loadStartUrl() {
		// load the starting url for the application
		getWebView().loadUrl(startUrl());
	}

	@Override
	public void onBackPressed() {

		getWebView().onBackPressed();
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && getWebView().canGoBack()) {
			getWebView().goBack();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
			boolean isShowingKeyboard = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).isAcceptingText();
			if (!isShowingKeyboard) {
				getWebView().onOptionsMenuKeyDown();
			} else {
				Log.w(TAG, "skipping menu key event");
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// http://www.kpbird.com/2011/04/android-webview-detect-html-element-on.html
		// WebView.HitTestResult hr = ((WebView) v).getHitTestResult();
		// Log.i(TAG, "getExtra = " + hr.getExtra() + "\t\t Type=" +
		// hr.getType());
		return false; // disabling=true?
	}

	// move this somewhere else
	public void postUrl(String url, String postData) {
		// postData = "u=username&p=password"
		// String postData = "fileContents=" + URLEncoder.encode(fileCon,
		// "UTF-8");
		byte[] post = EncodingUtils.getBytes(postData, "BASE64");
		getWebView().postUrl(url, post);
	}

	public void appTitle(String title) {
		setTitle(title);
	}

	public void exitApp() {
		finish();
	}

}
