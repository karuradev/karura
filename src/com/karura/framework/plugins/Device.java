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

import static android.provider.Settings.Secure.ANDROID_ID;

import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;

import com.karura.framework.PluginManager;
import com.karura.framework.annotations.Description;
import com.karura.framework.annotations.ExportToJs;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.annotations.Synchronous;
import com.karura.framework.ui.webview.KaruraWebView;

@JavascriptModule
@Description("Plugin can be used to query device specific parameters")
public class Device extends WebViewPlugin {

	private static String platform = "Android"; // Device OS
	private static String uuid; // Device UUID

	@ExportToJs
	@Description("Field which will contain device UUID")
	private static final String UUID_FIELD = "uuid";

	@ExportToJs
	@Description("Field for returning the platform version")
	private static final String PLATFORM_VERSION_FIELD = "version";

	@ExportToJs
	@Description("Field for returning platform name")
	private static final String PLATFORM_FIELD = "platform";

	@ExportToJs
	@Description("Field for return device name")
	private static final String DEVICE_NAME_FIELD = "name";

	@ExportToJs
	@Description("Field for returning device model")
	private static final String DEVICE_MODEL_FIELD = "model";

	protected Device(int pluginId, PluginManager pluginManager, KaruraWebView webView, Bundle savedInstance) {
		super(pluginId, pluginManager, webView, savedInstance);
		uuid = getUuid();
	}

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 * @param callbackContext
	 *            The callback id used when calling back into JavaScript.
	 * @return True if the action was valid, false if not.
	 */
	@Synchronous(retVal = "Returns the stringified json object containing device params ")
	@Description("Fetch device specific details")
	@JavascriptInterface
	@SupportJavascriptInterface
	public String getDeviceInfo() {

		try {
			JSONObject r = new JSONObject();
			r.put(UUID_FIELD, Device.uuid);
			r.put(PLATFORM_VERSION_FIELD, this.getOSVersion());
			r.put(PLATFORM_FIELD, Device.platform);
			r.put(DEVICE_NAME_FIELD, this.getProductName());
			r.put(DEVICE_MODEL_FIELD, this.getModel());

			return r.toString();
		} catch (JSONException e) {
			// should never hapen
		}
		return null;
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------

	/**
	 * Get the OS name.
	 * 
	 * @return
	 */
	public String getPlatform() {
		return Device.platform;
	}

	/**
	 * Get the device's Universally Unique Identifier (UUID).
	 * 
	 * @return
	 */
	public String getUuid() {
		String uuid = Settings.Secure.getString(getContext().getContentResolver(), ANDROID_ID);
		return uuid;
	}

	public String getModel() {
		String model = android.os.Build.MODEL;
		return model;
	}

	public String getProductName() {
		String productname = android.os.Build.PRODUCT;
		return productname;
	}

	/**
	 * Get the OS version.
	 * 
	 * @return
	 */
	public String getOSVersion() {
		String osversion = android.os.Build.VERSION.RELEASE;
		return osversion;
	}

	public String getSDKVersion() {
		String sdkversion = String.valueOf(android.os.Build.VERSION.SDK_INT);
		return sdkversion;
	}

	public String getTimeZoneID() {
		TimeZone tz = TimeZone.getDefault();
		return (tz.getID());
	}

	@Override
	public void onSaveInstanceState(Bundle saveInInstance) {
		// TODO Auto-generated method stub

	}

}
