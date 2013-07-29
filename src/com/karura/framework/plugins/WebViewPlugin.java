package com.karura.framework.plugins;

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

import static com.karura.framework.Constants.INVALID_RECEIVER_ID;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.webkit.JavascriptInterface;

import com.karura.framework.PluginManager;
import com.karura.framework.annotations.Event;
import com.karura.framework.annotations.Events;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.annotations.Synchronous;
import com.karura.framework.ui.webview.KaruraWebView;
import com.karura.framework.utils.JsHelper;
//#if TARGET_SDK==17
//#endif

public abstract class WebViewPlugin implements PluginConstants {
	final String TAG = "WebViewPlugin";

	protected int jsObjId = INVALID_RECEIVER_ID;
	protected Context context;
	protected PluginManager pluginManager;
	private KaruraWebView webView;

	protected WebViewPlugin(int pluginId, PluginManager pluginManager, KaruraWebView webView, Bundle savedInstance) {
		jsObjId = pluginId;
		this.context = webView.getContext();
		this.pluginManager = pluginManager;
		this.setWebView(webView);
	}

	protected WebViewPlugin(int pluginId) {
		jsObjId = pluginId;
	}

	public Context getContext() {
		return this.context;
	}

	public Activity getActivity() {
		return (Activity) webView.getActivity();
	}

	public abstract void onSaveInstanceState(Bundle saveInInstance);

	public void release() {
		pluginManager.cancelPending(jsObjId);
	}

	// #if TARGET_SDK==17
	@JavascriptInterface
	// #endif
	@SupportJavascriptInterface
	@Synchronous(retVal = "Object identifier of the this javascript object")
	public String getId() {
		return String.valueOf(jsObjId);
	}

	protected void sendToWebView(String javascript) throws JSONException {
		pluginManager.scheduleJsForExecution(javascript, getId());
	}

	public void onReset() {

	}

	public void onDestory() {

	}

	/**
	 * 
	 * @return
	 */

	public void resolveWithResult(String callId, Object response) throws IllegalArgumentException {
		resolve(callId, Pair.create(RESPONSE_PAYLOAD, response));
	}

	public void resolve(String callId, Pair... values) throws IllegalArgumentException {
		call(callId, ON_RESOLVE_ACTION, values);
	}

	public void progress(String callId, Pair... values) throws IllegalArgumentException {
		call(callId, ON_PROGRESS_ACTION, values);
	}

	public void reject(String callId, Pair... values) throws IllegalArgumentException {
		call(callId, ON_REJECT_ACTION, values);
	}

	public void reject(String callId, int exCode, Exception error) {
		reject(callId, exCode, error == null ? "no description" : error.getMessage());
	}

	protected void rejectWithCode(String callId, int errorCode) throws IllegalArgumentException {
		reject(callId, errorCode, null);
	}

	public void rejectWithError(String callId, int errorCode, String errorMsg) throws IllegalArgumentException {
		reject(callId, errorCode, errorMsg, (Pair[]) null);
	}

	protected void reject(String callId, int errorCode, String errorMsg, Pair... params) throws IllegalArgumentException {
		Pair[] newparams;
		if (params == null) {
			newparams = new Pair[2];
		} else {
			newparams = new Pair[params.length + 2];
		}
		newparams[0] = Pair.create("code", errorCode);
		newparams[1] = Pair.create("msg", errorMsg);
		if (params != null) {
			System.arraycopy(params, 0, newparams, 2, params.length);
		}
		reject(callId, newparams);
	}

	/**
	 * This method can be used by the java components
	 * 
	 * @param eventName
	 * @param values
	 * @throws IllegalArgumentException
	 */

	public void eventWithData(String eventName, Object response) {
		event(eventName, INVALID_TIMEOUT, response);
	}

	public void event(String eventName, int timeOut, Object response) {
		event(eventName, timeOut, Pair.create(EVENT_PAYLOAD, response));
	}

	public void event(String eventName, Pair... values) throws IllegalArgumentException {
		event(eventName, INVALID_TIMEOUT, values);
	}

	public void event(String eventName, int timeout, Pair... values) throws IllegalArgumentException {
		sanitizeEvent(eventName, values);
		call(eventName, null, timeout, values);
	}

	public void sanitizeEvent(String eventName, Pair... values) {
		boolean found = false;
		if (!getClass().isAnnotationPresent(Events.class)) {
			throw new IllegalArgumentException();
		}

		Events events = getClass().getAnnotation(Events.class);
		Event matchedEvent = null;

		for (Event event : events.value()) {
			if (event.name().compareTo(eventName) == 0) {
				found = true;
				matchedEvent = event;
				break;
			}
		}

		if (!found) {
			throw new IllegalArgumentException();
		}

		if (values.length != matchedEvent.args().length) {
			throw new IllegalArgumentException();
		}

		int i = 0;
		for (Class<?> cls : matchedEvent.args()) {
			if (values[i++].getClass() != cls) {
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * @param callId
	 * @param params
	 * @throws IllegalArgumentException
	 */
	protected void call(String callId, String action, Pair... params) throws IllegalArgumentException {
		call(callId, action, INVALID_TIMEOUT, params);
	}

	protected void call(final String callId, final String action, final int timeout, final Pair... params) throws IllegalArgumentException {

		Runnable r = new Runnable() {
			public void run() {
				String receiver = "";
				try {
					receiver = getId();
					String script = JsHelper.jsFragmentForAsyncResponse(receiver, callId, action, timeout, params);
					sendToWebView(script);
				} catch (JSONException e) {
					throw new IllegalArgumentException();
				}
			}
		};

		runOnUiThread(r);
	}

	public KaruraWebView getWebView() {
		return webView;
	}

	public void setWebView(KaruraWebView webView) {
		this.webView = webView;
	}

	protected void runOnUiThread(Runnable r) {
		pluginManager.runOnUiThread(r);
	}

	protected void runInBackground(Runnable r) {
		pluginManager.runInBackground(r);

	}

}
