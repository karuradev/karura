/**
This file derived from Cordova Android Project available under the Apache 2.0 License
Please refer to APACHE-LICENCE2.0.txt for attribution.
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

import android.os.Bundle;
import android.util.Log;

import com.karura.framework.ui.webview.KaruraWebView;
//#if TARGET_SDK==17
import android.webkit.JavascriptInterface;
//#endif

import com.karura.framework.PluginManager;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.annotations.Param;
import com.karura.framework.annotations.Params;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.annotations.Synchronous;
import com.karura.framework.annotations.Description;

@JavascriptModule
@Description("Use android logging facility")
public class Logger extends WebViewPlugin {

	public Logger(int pluginId, PluginManager pluginManager, KaruraWebView webView, Bundle savedInstance) {
		super(pluginId, pluginManager, webView, savedInstance);
	}

//#if TARGET_SDK==17
	@JavascriptInterface
//#endif
	@SupportJavascriptInterface
	@Synchronous(retVal="none")
	@Description("Log an error log in the android syslogger")
	@Params({
		@Param(name="Tag", description ="Tag to be used for logging in the android log system"),
		@Param(name="msg", description="The message to be logged.")
	})
	public void e(String Tag, String msg) {
		Log.e(Tag, msg);
	}

//#if TARGET_SDK==17
	@JavascriptInterface
//#endif
	@SupportJavascriptInterface
	@Synchronous(retVal="none")
	@Description("Log a debug log in the android syslogger")
	@Params({
		@Param(name="Tag", description ="Tag to be used for logging in the android log system"),
		@Param(name="msg", description="The message to be logged.")
	})
	public void d(String Tag, String msg) {
		Log.d(Tag, msg);
	}
	
//#if TARGET_SDK==17
	@JavascriptInterface
//#endif
	@SupportJavascriptInterface
	@Synchronous(retVal="none")
	@Description("Log a verbose log in the android syslogger")
	@Params({
		@Param(name="Tag", description ="Tag to be used for logging in the android log system"),
		@Param(name="msg", description="The message to be logged.")
	})
	public void v(String Tag, String msg) {
		Log.v(Tag, msg);
	}

//#if TARGET_SDK==17
	@JavascriptInterface
//#endif
	@SupportJavascriptInterface
	@Synchronous(retVal="none")
	@Description("Log an info log in the android syslogger")
	@Params({
		@Param(name="Tag", description ="Tag to be used for logging in the android log system"),
		@Param(name="msg", description="The message to be logged.")
	})
	public void i(String Tag, String msg) {
		Log.i(Tag, msg);
	}	
	
	@Override
	public void onSaveInstanceState(Bundle saveInInstance) {
		// TODO Auto-generated method stub

	}
}
