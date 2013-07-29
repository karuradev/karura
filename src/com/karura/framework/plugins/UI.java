/**
This file derived from Cordova Android Project available under the Apache 2.0 License
Please refer to APACHE-LICENCE2.0.txt for attributions
 */

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

package com.karura.framework.plugins;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
//#if TARGET_SDK==17
import android.webkit.JavascriptInterface;
//#endif

import com.karura.framework.PluginManager;
import com.karura.framework.annotations.Asynchronous;
import com.karura.framework.annotations.Description;
import com.karura.framework.annotations.ExportToJs;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.annotations.Param;
import com.karura.framework.annotations.Params;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.annotations.Synchronous;
import com.karura.framework.ui.webview.KaruraWebView;

@JavascriptModule
@Description("Application lifecycle components")
public class UI extends WebViewPlugin {
	private static final String TAG = "NativeUiPlugin";

	public UI(int pluginId, PluginManager pluginManager, KaruraWebView webView, Bundle savedInstance) {
		super(pluginId, pluginManager, webView, savedInstance);
	}

	@ExportToJs
	@Description("One of the param keys used in the loadUrl API, specifies the delay after which the webpage should be loaded.")
	private static final String WAIT_KEY = "wait";

	@ExportToJs
	@Description("One of the param keys used in loadUrl API, specifies whether the url should be openned in external viewer.")
	private static final String OPEN_EXTR_KEY = "openexternal";

	@ExportToJs
	@Description("One of the param keys used in loadUrl API, specifies whether the browser history should be cleaned before loading the url")
	private static final String CLEAR_HISTORY_KEY = "clearhistory";

	@ExportToJs
	@Description("One of the params used in overrideButton API, used to specify the volume up button")
	static final String VOLUME_UP_KEY = "volumeup";

	@ExportToJs
	@Description("One of the params used in overrideButton API, used to specify the volume down button")
	static final String VOLUME_DOWN_KEY = "volumedown";

	@Override
	public void onSaveInstanceState(Bundle saveInInstance) {
		// TODO Auto-generated method stub
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------

	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Clear the resource cache.")
	public void clearCache() {
		getWebView().clearCache(true);
	}

	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Description("Load the url into the webview.")
	@Asynchronous(retVal = "none, will load the specified URL in the webview")
	@Params({
			@Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "url", description = "URL to be loaded in the web browser"),
			@Param(name = "props", description = "Specifies the parameters for customizing the loadUrl experience. Look at "
					+ "WAIT_KEY, OPEN_EXTR_KEY and CLEAR_HISTORY_KEY. If the OPEN_EXTR_KEY is specified then this object can also contain additional "
					+ "parameters which need to be passed to the external viewer in the intent. The other keys can only be integer, boolean or string") })
	public void loadUrl(final String callId, String url, JSONObject props) throws JSONException {
		Log.d(TAG, "loadUrl(" + url + "," + props + ")");
		int wait = 0;
		boolean openExternal = false;
		boolean clearHistory = false;

		// If there are properties, then set them on the Activity
		HashMap<String, Object> params = new HashMap<String, Object>();
		if (props != null) {
			JSONArray keys = props.names();
			for (int i = 0; i < keys.length(); i++) {
				String key = keys.getString(i);
				if (key.equals(WAIT_KEY)) {
					wait = props.getInt(key);
				} else if (key.equalsIgnoreCase(OPEN_EXTR_KEY)) {
					openExternal = props.getBoolean(key);
				} else if (key.equalsIgnoreCase(CLEAR_HISTORY_KEY)) {
					clearHistory = props.getBoolean(key);
				} else {
					Object value = props.get(key);
					if (value == null) {

					} else if (value.getClass().equals(String.class)) {
						params.put(key, (String) value);
					} else if (value.getClass().equals(Boolean.class)) {
						params.put(key, (Boolean) value);
					} else if (value.getClass().equals(Integer.class)) {
						params.put(key, (Integer) value);
					}
				}
			}
		}

		// If wait property, then delay loading

		if (wait > 0) {
			try {
				synchronized (this) {
					this.wait(wait);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		getWebView().showWebPage(url, openExternal, clearHistory, params);
	}

	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Clear page history for the app.")
	public void clearHistory() {
		getWebView().clearHistory();
	}

	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Go to previous page displayed. This is the same as pressing the back button on Android device.")
	public void backHistory() {
		runOnUiThread(new Runnable() {
			public void run() {
				getWebView().backHistory();
			}
		});
	}

	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Override the default behavior of the Android back button. If overridden, when the back button is pressed, the \"backKeyDown\" JavaScript event will be fired.")
	@Params({ @Param(name = "override", description = " T=override, F=cancel override") })
	public void overrideBackbutton(boolean override) {
		Log.i(TAG, "WARNING: Back Button Default Behaviour will be overridden.  The backbutton event will be fired!");
		getWebView().bindButton(override);
	}

	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Override the default behavior of the Android volume buttons. If overridden, when the volume button is pressed, the \"volume[up|down]button\" JavaScript event will be fired.")
	@Params({ @Param(name = "button", description = "Specifies the button to be bound, available options VOLUME_UP_KEY, VOLUME_DOWN_KEY"),
			@Param(name = "override", description = " T=override, F=cancel override") })
	public void overrideButton(String button, boolean override) {
		Log.i(TAG, "WARNING: Volume Button Default Behaviour will be overridden.  The volume event will be fired!");
		getWebView().bindButton(button, override);
	}

	/**
	 * 
	 * 
	 * @return boolean
	 */
	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Synchronous(retVal = "boolean")
	@Description("Return whether the Android back button is overridden by the user.")
	public boolean isBackbuttonOverridden() {
		return getWebView().isBackButtonBound();
	}

}
