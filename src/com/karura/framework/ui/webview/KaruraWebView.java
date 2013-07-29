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


package com.karura.framework.ui.webview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.karura.framework.KaruraApp;
import com.karura.framework.Constants;
import com.karura.framework.FrameworkConfig;
import com.karura.framework.PluginManager;
import com.karura.framework.config.ConfigStore;

public class KaruraWebView extends WebView implements Constants {
	static final String TAG = "KaruraWebView";

	public String baseUrl;
	public boolean disallowFileAccess = false;

	public KaruraWebView(Context context) {
		super(context);
		setup(context);
	}

	public KaruraWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}

	public KaruraWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void setup(final Context context, String... pluginPackagePath) {
		Log.e(TAG, "setup()");

		setWebViewClient(new KaruraWebViewClient(this));
		setWebChromeClient(new KaruraWebChromeClient(this));

		WebSettings settings = getSettings();

		settings.setJavaScriptEnabled(true);

		settings.setLoadsImagesAutomatically(true);
		settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setAppCacheEnabled(false);

		settings.setUseWideViewPort(true);

		// settings.setDefaultZoom(ZoomDensity.FAR);
		settings.setSupportZoom(false);
		settings.setBuiltInZoomControls(false);

		settings.setAllowFileAccess(true);
		settings.setLightTouchEnabled(true);

		settings.setDatabaseEnabled(true);
		String databasePath = context.getDir("database", Context.MODE_PRIVATE).getPath();
		settings.setDatabasePath(databasePath);
		settings.setGeolocationDatabasePath(databasePath);

		settings.setDomStorageEnabled(true);

		settings.setGeolocationEnabled(true);
		
		injectPluginManager();

		disallowFileAccess = false;
	}
	
	
	
	@Override
	public WebBackForwardList restoreState(Bundle inState) {
		WebBackForwardList response  = super.restoreState(inState);
		injectPluginManager();
		return response;
	}

	public KaruraApp getApp(){
		return KaruraApp.getApp();
	}
	
	public ConfigStore appConfig(){
		return getApp().getAppConfig();
	}
	
	public PluginManager pluginManager(){
		return getApp().getPluginManager();
	}

	public Context getActivity() {
		return getContext();
	}

	public void onBackPressed() {
		//
	}

	public void onOptionsMenuKeyDown() {
		//PluginManager.getInstance().onOptionsMenuKeyDown();
	}
	
	

	@Override
	public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
		this.baseUrl = baseUrl;
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
	}

	@Override
	public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
		if (urls.empty()){
			baseUrl = url;
		}
		super.loadUrl(url, additionalHttpHeaders);
	}

	@Override
	public void loadUrl(String url) {
		if (urls.empty()){
			baseUrl = url;
		}
		super.loadUrl(url);
	}
	
	protected void injectPluginManager(){
		pluginManager().attachToWebView(this);
		addJavascriptInterface(pluginManager(), FrameworkConfig.FRAMEWORK_JS_HANDLE);
	}

	public boolean isUrlWhiteListed(String url) {
		for (Pattern urlPattern : appConfig().whiteList()) {
			Matcher matcher = urlPattern.matcher(url);
			if (matcher.find() && matcher.start() == 0) {
				return true;
			}
		}
		return false;
	}

	public void showWebPage(String url, boolean openExternal, boolean clearHistory, HashMap<String, Object> params) {
		Log.d(TAG, String.format("showWebPage(%s, %b, %b, HashMap", url, openExternal, clearHistory));

		// If clearing history
		if (clearHistory) {
			this.clearHistory();
		}

		// If loading into our webview
		if (!openExternal) {

			// Make sure url is in whitelist
			if (url.startsWith("file://") || url.indexOf(this.baseUrl) == 0 || isUrlWhiteListed(url)) {
				// TODO: What about params?

				// Clear out current url from history, since it will be replacing it
				if (clearHistory) {
					// this.urls.clear();
				}

				// Load new URL
				this.loadUrl(url);
			}
			// Load in default viewer if not
			else {
				Log.w(TAG, "showWebPage: Cannot load URL into webview since it is not in white list.  Loading into browser instead. (URL="
						+ url + ")");
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					if (params != null) {
						for (String k : params.keySet()) {
							intent.putExtra(k, (Serializable) params.get(k));
						}
					}
					getActivity().startActivity(intent);
				} catch (android.content.ActivityNotFoundException e) {
					Log.e(TAG, "Error loading url " + url, e);
				}
			}
		}

		// Load in default view intent
		else {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				getActivity().startActivity(intent);
			} catch (android.content.ActivityNotFoundException e) {
				Log.e(TAG, "Error loading url " + url, e);
			}
		}
	}

	

	// Wrapping these functions in their own class prevents warnings in adb like:
	// VFY: unable to resolve virtual method 285: Landroid/webkit/WebSettings;.setAllowUniversalAccessFromFileURLs

	private static class Level16Apis {
		@SuppressLint("NewApi")
		static void enableUniversalAccess(WebSettings settings) {
//#if TARGET_SDK>15
			settings.setAllowUniversalAccessFromFileURLs(true);
//#endif
		}
	}

	public void printBackForwardList() {
		WebBackForwardList currentList = this.copyBackForwardList();
		int currentSize = currentList.getSize();
		for (int i = 0; i < currentSize; ++i) {
			WebHistoryItem item = currentList.getItemAtIndex(i);
			String url = item.getUrl();
			Log.d(TAG, "The URL at index: " + Integer.toString(i) + "is " + url);
		}
	}

	// Can Go Back is BROKEN!
	public boolean startOfHistory() {
		WebBackForwardList currentList = this.copyBackForwardList();
		WebHistoryItem item = currentList.getItemAtIndex(0);
		if (item != null) { // Null-fence in case they haven't called loadUrl yet (CB-2458)
			String url = item.getUrl();
			String currentUrl = this.getUrl();
			Log.d(TAG, "The current URL is: " + currentUrl);
			Log.d(TAG, "The URL at item 0 is:" + url);
			return currentUrl.equals(url);
		}
		return false;
	}

	private ArrayList<Integer> keyDownCodes = new ArrayList<Integer>();
	private ArrayList<Integer> keyUpCodes = new ArrayList<Integer>();
	boolean useBrowserHistory = true;
	private Stack<String> urls = new Stack<String>();
	private boolean bound;
	private long lastMenuEventTime = 0;

	/*
	 * onKeyDown
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyDownCodes.contains(keyCode)) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				// only override default behavior is event bound
				Log.d(TAG, "Down Key Hit");
				this.loadUrl("javascript:fireDocumentEvent('volumedownbutton');");
				return true;
			}
			// If volumeup key
			else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				Log.d(TAG, "Up Key Hit");
				this.loadUrl("javascript:fireDocumentEvent('volumeupbutton');");
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Because exit is fired on the keyDown and not the key up on Android 4.x
			// we need to check for this.
			// Also, I really wished "canGoBack" worked!
			if (this.useBrowserHistory)
				return !(this.startOfHistory()) || this.bound;
			else
				return this.urls.size() > 1 || this.bound;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// If back key
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// A custom view is currently displayed (e.g. playing a video)
			if (mCustomView != null) {
				this.hideCustomView();
			} else {
				// The webview is currently displayed
				// If back key is bound, then send event to JavaScript
				if (this.bound) {
					this.loadUrl("javascript:fireDocumentEvent('backbutton');");
					return true;
				} else {
					// If not bound
					// Go to previous page in webview if it is possible to go back
					if (this.backHistory()) {
						return true;
					}
					// If not, then invoke default behaviour
					else {
						// this.activityState = ACTIVITY_EXITING;
						// return false;
						// If they hit back button when app is initializing, app should exit instead of hang until initilazation (CB2-458)
						((Activity) getActivity()).finish();
					}
				}
			}
		}
		// Legacy
		else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (this.lastMenuEventTime < event.getEventTime()) {
				this.loadUrl("javascript:fireDocumentEvent('menubutton');");
			}
			this.lastMenuEventTime = event.getEventTime();
			return super.onKeyUp(keyCode, event);
		}
		// If search key
		else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			this.loadUrl("javascript:fireDocumentEvent('searchbutton');");
			return true;
		} else if (keyUpCodes.contains(keyCode)) {
			// What the hell should this do?
			return super.onKeyUp(keyCode, event);
		}

		// Does webkit change this behavior?
		return super.onKeyUp(keyCode, event);
	}

	public void bindButton(boolean override) {
		this.bound = override;
	}

	public void bindButton(String button, boolean override) {
		// TODO Auto-generated method stub
		if (button.compareTo("volumeup") == 0) {
			keyDownCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
		} else if (button.compareTo("volumedown") == 0) {
			keyDownCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
		}
	}

	public void bindButton(int keyCode, boolean keyDown, boolean override) {
		if (keyDown) {
			keyDownCodes.add(keyCode);
		} else {
			keyUpCodes.add(keyCode);
		}
	}

	public boolean isBackButtonBound() {
		return this.bound;
	}

	/**
	 * Add a url to the stack
	 * 
	 * @param url
	 */
	public void pushUrl(String url) {
		this.urls.push(url);
	}

	/**
	 * Go to previous page in history. (We manage our own history)
	 * 
	 * @return true if we went back, false if we are already at top
	 */
	public boolean backHistory() {

		// Check webview first to see if there is a history
		// This is needed to support curPage#diffLink, since they are added to appView's history, but not our history url array (JQMobile
		// behavior)
		if (super.canGoBack() && this.useBrowserHistory) {
			printBackForwardList();
			super.goBack();

			return true;
		}
		// If our managed history has prev url
		else if (this.urls.size() > 1 && !this.useBrowserHistory) {
			this.urls.pop(); // Pop current url
			String url = this.urls.pop(); // Pop prev url that we want to load, since it will be added back by loadUrl()
			this.loadUrl(url);
			return true;
		}

		return false;
	}

	/** custom view created by the browser (a video player for example) */
	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;

	public void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
		// This code is adapted from the original Android Browser code, licensed under the Apache License, Version 2.0
		Log.d(TAG, "showing Custom View");
		// if a view already exists then immediately terminate the new one
		if (mCustomView != null) {
			callback.onCustomViewHidden();
			return;
		}

		// Store the view and its callback for later (to kill it properly)
		mCustomView = view;
		mCustomViewCallback = callback;

		// Add the custom view to its container.
		ViewGroup parent = (ViewGroup) this.getParent();
		parent.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
				Gravity.CENTER));

		// Hide the content view.
		this.setVisibility(View.GONE);

		// Finally show the custom view container.
		parent.setVisibility(View.VISIBLE);
		parent.bringToFront();
	}

	public void hideCustomView() {
		// This code is adapted from the original Android Browser code, licensed under the Apache License, Version 2.0
		Log.d(TAG, "Hidding Custom View");
		if (mCustomView == null)
			return;

		// Hide the custom view.
		mCustomView.setVisibility(View.GONE);

		// Remove the custom view from its container.
		ViewGroup parent = (ViewGroup) this.getParent();
		parent.removeView(mCustomView);
		mCustomView = null;
		mCustomViewCallback.onCustomViewHidden();

		// Show the content view.
		this.setVisibility(View.VISIBLE);
	}
}
