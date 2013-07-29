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

import android.app.Application;
import com.karura.framework.config.ConfigStore;

/**
 * Base application class for your application. The application class is a
 * holder for all global data that you might want to persist in your application
 * 
 * @author nitin
 * 
 */
public class KaruraApp extends Application {

	private static KaruraApp appInstance;
	private ConfigStore appConfig;
	private PluginManager pluginManager;

	/**
	 * Store the singleton instance of the application for future access and
	 * reference
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		appInstance = this;
	}

	public static KaruraApp getApp() {
		return appInstance;
	}

	public void setAppConfig(ConfigStore appConfig) {
		this.appConfig = appConfig;
	}

	public ConfigStore getAppConfig() {
		return appConfig;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

}
