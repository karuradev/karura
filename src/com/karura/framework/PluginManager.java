package com.karura.framework;

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

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.karura.framework.annotations.Description;
import com.karura.framework.annotations.Param;
import com.karura.framework.annotations.Params;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.annotations.Synchronous;
import com.karura.framework.plugins.WebViewPlugin;
import com.karura.framework.ui.webview.KaruraWebView;
import com.karura.framework.utils.JsHelper;
import com.karura.framework.utils.MsgQueue;
import com.karura.framework.utils.PackageInspector;

/**
 * This component manages and provides access to various web view plugins available to the user.
 * 
 * @author nitin
 * 
 */
@SuppressLint("UseSparseArrays")
public class PluginManager extends WebViewPlugin implements Constants {
	static final String TAG = "PluginManager";

	/**
	 * Interface for pluginManager observer.
	 */
	public static interface Callback {
		/**
		 * Delivered to the observer when the pluginManager has successfully initialzied
		 */
		void onReady();

		/**
		 * Delivered to the observer when the plugin manager was not properly initialized.
		 */
		void onInternalError();
	}

	/**
	 * Constructor for the plugin manager clas
	 * 
	 * @param callbackListener
	 *            The observer for the plugin manager lifecycle
	 * @param pluginPackagePath
	 *            The user specified paths, from where the plugins are to be loaded. We are loading plugins from restricted paths for
	 *            security reasons.
	 */
	public PluginManager(final Context appContext, Callback callbackListener, final List<String> pluginPackagePaths) {
		super(FRAMEWORK_RECEIVER_ID);
		// allocate the javascript queue. This is used for temporary holding scripts pending execution
		jsMessageQueue = new MsgQueue();
		init(callbackListener);
		runInBackground(new Runnable() {
			public void run() {
				pluginMap = new PackageInspector(appContext, pluginPackagePaths).searchForPlugins();
				managerReady();
			}
		});
	}

	/**
	 * Resume the plugin manager initialization. This method can be used to resume the plugin manager which was created earlier
	 * 
	 * @param callbackListener
	 *            The lifecycle observer for the plugin Manager
	 */
	public void resume(Callback callbackListener) {
		if (_managerReady) {
			// since the plugin manager instance already exists, just generate callbacks
			// as needed.
			init(callbackListener);
			managerReady();
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * Keep track of whether the plugin manager instance has been initialized
	 */
	boolean _managerReady = false;
	/**
	 * This message queue is used to temporarily store javascripts as they are scheduled for execution in the webview
	 */
	MsgQueue jsMessageQueue;
	/**
	 * Handler for the mainThread, used to post messages from the non-ui threads to ui threads
	 */
	Handler mainThread = new Handler(Looper.getMainLooper());

	/**
	 * Reference to the webView control with which the plugin manager instance is associated with
	 */
	KaruraWebView webViewEx;

	/**
	 * The background thread for plugins schedule tasks in background
	 */
	ExecutorService DEFAULT_EXECUTOR = Executors.newSingleThreadExecutor();

	/**
	 * Application context to get access to system services
	 */
	Context context;

	/**
	 * Map of plugin names and associated classes.
	 */
	HashMap<String, Class<? extends WebViewPlugin>> pluginMap = new HashMap<String, Class<? extends WebViewPlugin>>();

	/**
	 * Each id which will be allocated to the next plugin instance being allocated from javascript.
	 */
	int _nextPluginId = 1;

	/**
	 * Map of native plugins and ids. The javascript talks to native layer plugins using an unique id corresponding to each instance of the
	 * plugin.
	 */
	HashMap<String, WebViewPlugin> instanceMap = new HashMap<String, WebViewPlugin>();

	/**
	 * Reference to the plugin manager lifecycle observer
	 */
	Callback callbackListener = null;

	private static final String PLUGIN_MGR_INSTANCE_DATA_KEY = "__plugin_manager_data";
	private static final String PLUGIN_CLASS_NAME_KEY = "__plugin_class";
	private static final String PLUGIN_ID_KEY = "__plugin_id";
	private static final String PLUGIN_INSTANCE_DATA_KEY_PREFIX = "__plugin_";

	/**
	 * Initialize the callbackListener
	 * 
	 * @param callbackListener
	 *            plugin manager lifecycle observer
	 */
	void init(Callback callbackListener) {
		this.callbackListener = callbackListener;
	}

	/**
	 * Call this method to associate the plugin manager with
	 * 
	 * @param webViewEx
	 */
	public void attachToWebView(KaruraWebView webViewEx) {
		this.webViewEx = webViewEx;
		this.context = webViewEx.getContext();
	}

	KaruraApp getApp() {
		return KaruraApp.getApp();
	}

	/**
	 * Utility function for getting a file object for the framework javascript
	 * 
	 * @return
	 */
	public File getFrameworkJavascriptFile() {
		return new File(getApp().getApplicationContext().getFilesDir(), FrameworkConfig.FRAMEWORK_JS_FILENAME);
	}

	/**
	 * Utility function to notify the plugin manager lifecycle observer that the plugin manager has successfully initialized
	 */
	private void managerReady() {
		_managerReady = true;
		// as the plugin manager does its initialization in the background thread, we need to make
		// sure that the callback are delivered in the UI thread
		mainThread.postDelayed(notifyManagerReady, 100);
	}

	/**
	 * Wrapper runnable for invoking observer callback in the main thread
	 */
	private Runnable notifyManagerReady = new Runnable() {

		@Override
		public void run() {
			if (callbackListener != null) {
				callbackListener.onReady();
			}
		}
	};

	/**
	 * When called shutsdown the plugin manager, and releases any plugin instances created by the javascript layer
	 */
	public void shutdown() {
		releasePlugins();

		mainThread = null;
	}

	/**
	 * The main activity has been asked to store instance data as the system is about to kill the activity.
	 * 
	 * We ask all plugins if they have any instance data that they would like to persist across the lifetime
	 * 
	 * @param saveBundle
	 *            The bundle instance in which plugin data needs to be persisted
	 */
	public void onSaveInstanceState(Bundle saveBundle) {
		Bundle pluginManagerBundle = new Bundle();

		for (WebViewPlugin plugin : instanceMap.values()) {
			// We are storing each plugin data in an individual bundle
			Bundle pluginInstanceData = new Bundle();
			plugin.onSaveInstanceState(pluginInstanceData);
			// add our meta data information to the bundle
			// this will help us recreate these bundles later
			pluginInstanceData.putString(PLUGIN_CLASS_NAME_KEY, plugin.getClass().getName());
			pluginInstanceData.putString(PLUGIN_ID_KEY, plugin.getId());

			// persist the bundles
			pluginManagerBundle.putBundle(PLUGIN_INSTANCE_DATA_KEY_PREFIX + plugin.getId(), pluginInstanceData);
		}

		saveBundle.putBundle(PLUGIN_MGR_INSTANCE_DATA_KEY, pluginManagerBundle);

		// release any memory associated with the plugins

		// TODO figure out what we should do with the messages which the plugins have scheduled
		// on the javascript library, for now we are releasing everything, assuming webview
		// associated with the plugin manager has been stopped as well, and it might not be
		// able execute any of the scheduled requests
		releasePlugins();
	}

	/**
	 * Called when the webview attached with the plugin manager is being restored with instance data
	 * 
	 * Lets try to recreate all plugins with instance data
	 * 
	 * @param savedInstance
	 *            the memory block which contains information about each of the plugins which were persisted in call to onSaveInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstance) {
		// See if there is a valid instance to restore state from
		if (savedInstance == null || !savedInstance.containsKey(PLUGIN_MGR_INSTANCE_DATA_KEY))
			return;

		Bundle pluginMgrInstanceData = savedInstance.getBundle(PLUGIN_MGR_INSTANCE_DATA_KEY);
		_nextPluginId = 0;
		for (String pluginDataKey : pluginMgrInstanceData.keySet()) {

			// only process those data keys which were created by us
			if (!pluginDataKey.startsWith(PLUGIN_INSTANCE_DATA_KEY_PREFIX)) {
				continue;
			}

			// try and retrieve plugin specific instance data
			Bundle pluginInstanceData = pluginMgrInstanceData.getBundle(pluginDataKey);

			if (pluginInstanceData == null)
				continue;

			String clazz = pluginInstanceData.getString(PLUGIN_CLASS_NAME_KEY);
			pluginInstanceData.remove(PLUGIN_CLASS_NAME_KEY);
			Integer pluginId = Integer.valueOf(pluginInstanceData.getString(PLUGIN_ID_KEY));

			// manage the plugin id counter for subsequent allocation of plugins
			if (_nextPluginId < pluginId) {
				_nextPluginId = pluginId;
			}
			pluginInstanceData.remove(PLUGIN_ID_KEY);

			// Since we are passing the instance data for the plugin in this call, we are hoping it
			// will be able to restore its state
			allocateAndCachePlugin(clazz, pluginId, pluginInstanceData);

		}
		// safe increment
		_nextPluginId++;

		// TODO create a list of pluginIds and pass them back to the javascript layer just in case

	}

	/**
	 * Helper function to release all plugins which have been cached in memory. Currently we are also release all pending javascripts and
	 * associated messages from the main thread queue
	 */
	private void releasePlugins() {
		// Send onDestroy to all plugins
		for (WebViewPlugin plugin : instanceMap.values()) {
			plugin.onDestory();
		}
		// clear all plugin instances and request system gc
		instanceMap.clear();
		System.gc();

		// clear any pending message queues
		jsMessageQueue.clear();

		// remove any messages which have been posted on the main thread
		mainThread.removeCallbacksAndMessages(null);
	}

	/**
	 * Helper method retrieving context
	 * 
	 * @return the reference to the webview context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Helper method to schedule a runnable in the background
	 * 
	 * @param r
	 *            The runnable to be executed in the background
	 */
	public void runInBackground(Runnable r) {
		DEFAULT_EXECUTOR.execute(r);
	}

	/**
	 * Helper method to schedule a Callable on the background
	 * 
	 * @param callable
	 *            The task to be scheduled on the background thread
	 * @param callback
	 *            Reference to the callback object which needs to be notified when the callback has been executed successfully or otherwise.
	 * @return Future Task for the Callable which was submitted to background executor
	 */
	@SuppressWarnings("unchecked")
	public <T> Future<T> runInBackground(Callable<T> callable, final TaskCallback<T> callback) {
		FutureTask<T> ft = new FutureTask<T>(callable) {

			@Override
			protected void done() {
				if (callback == null)
					return;

				try {
					if (!isCancelled()) {
						callback.done(get());
					}
				} catch (Exception e) {
					callback.error(e);
				}
			}

		};
		return (Future<T>) DEFAULT_EXECUTOR.submit(ft);
	}

	/**
	 * Utility method for submitting a task on the UI thread
	 * 
	 * @param r
	 *            runnable to be scheduled on the UI thread
	 */
	public void runOnUiThread(Runnable r) {
		mainThread.post(r);
	}

	/**
	 * Count of the plugins which have been found in the application. This api is accessible from Javascript
	 * 
	 * @return count of plugins in the application
	 */
	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "Integer representing the total number of webview plugins available to the runtime")
	@Description("Get the number of webview plugins")
	public int count() {
		return pluginMap.size();
	}

	/**
	 * Returns the names of all the plugins identified in the application. The names are returned as a json array. This API is available to
	 * javascript
	 * 
	 * @return Stringified JSON Array
	 */
	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "A JSON array containing names of all webview plugins available to plugin manager")
	@Description("Retrieve the list of all webview plugins which are available in the current runtime")
	public String names() {
		JSONArray result = new JSONArray();
		for (String name : pluginMap.keySet()) {
			result.put(name);
		}
		return result.toString();
	}

	/**
	 * Create a new instance of WebView plugin. This API can be used from Javascript to create a new instance of a WebView plugin
	 * 
	 * @param clazz
	 *            - The class identifying the plugin which has to be instantiated.
	 * @see names()
	 * @return Instance of the WebView plugin which was created as a result of this call.
	 */
	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "A native webview plugin components")
	@Description("Creates an instance of the plugin and caches it for future invocations. This component is also automatically bound to the webview instance."
			+ "Under normal circumstances this method will not be used by developers and is meant for the framework wrapper class which is used for accessing "
			+ "native components and plugins")
	@Params({ @Param(name = "clazz", description = "Class of the plugin which needs to be allocated.") })
	public WebViewPlugin instanceFor(String clazz) {
		return allocateAndCachePlugin(clazz, _nextPluginId++, null);
	}

	/**
	 * Internal method for allocating the plugin and caching it for future javascript calls
	 * 
	 * @param clazz
	 *            Name of the plugin class which needs to be instantiated
	 * @param pluginId
	 *            It which will be used to cache the plugin. This is then used by the javascript layer to dispatch responses and events
	 *            received from java
	 * @param savedInstance
	 *            A bundle containing instance data for the plugin which might have been persisted by the plugin in the previous lifetime
	 * @return
	 */
	protected WebViewPlugin allocateAndCachePlugin(String clazz, int pluginId, Bundle savedInstance) {
		try {
			Class<? extends WebViewPlugin> cls = pluginMap.get(clazz);
			Constructor<?> init = cls.getConstructor(int.class, PluginManager.class, KaruraWebView.class, Bundle.class);

			if (init != null) {
				WebViewPlugin plugin = (WebViewPlugin) init.newInstance(pluginId, this, webViewEx, savedInstance);
				instanceMap.put(plugin.getId(), plugin);
				return plugin;
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

	private String dispatchHandle = null;

	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Sets up the dispatcher handle, for java to reach the framework on the javascript side. This cannot be done automatically because"
			+ "the developers may choose to setup the scripts as per their own liking.")
	@Params({ @Param(name = "dispatchHandle", description = "Fully qualified reference to the karura framework in the javascript address space.") })
	public void setDispatchHandle(String dispatchHandle) {
		this.dispatchHandle = dispatchHandle;
	}

	public void scheduleJsForExecution(String javaScript, String Id) {

		if (dispatchHandle == null) {
			throw new NullPointerException("Dispatcher has not been setup properly for communication from java to webview");
		}
		final int msgQId = jsMessageQueue.add(javaScript, Id);
		mainThread.post(new Runnable() {
			public void run() {
				if (webViewEx != null) {
					webViewEx.loadUrl(JsHelper.notify(dispatchHandle, msgQId));
				}
			}
		});
	}

	/**
	 * This interface is used by the javascript layer to fetch javascript fragments for execution.
	 * 
	 * @param scriptId
	 *            Id of the script fragment to be fetched
	 * @return javascript fragment
	 */
	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "String representing the javascript")
	@Description("Returns the javascript from the cache for the specified script id")
	@Params({ @Param(name = "scriptId", description = "Id of the script to be fetched.") })
	public String getJsWithId(int scriptId) {
		return jsMessageQueue.get(scriptId);
	}

	/**
	 * A utility method to release all pending javascript fragments corresponding to a webview plugin instance. This utility function is
	 * called during the shutdown and cleanup phase for a plugin.
	 * 
	 * @param pluginId
	 */
	public void cancelPending(int pluginId) {
		jsMessageQueue.releaseReceiver(String.valueOf(pluginId));
	}

}