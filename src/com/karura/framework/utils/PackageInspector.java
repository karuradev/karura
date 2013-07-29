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


package com.karura.framework.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.karura.framework.BuildConfig;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.plugins.WebViewPlugin;

import dalvik.system.DexFile;

public class PackageInspector {

	Context context;
	List<String> pathsToInspect;

	public PackageInspector(Context context, List<String> pathsToInspect) {
		this.context = context;
		this.pathsToInspect = pathsToInspect;
	}

	public HashMap<String, Class<? extends WebViewPlugin>> searchForPlugins(){
		HashMap<String, Class<? extends WebViewPlugin>> result = new HashMap<String, Class<? extends WebViewPlugin>>();
		try {
			DexFile df = new DexFile(context.getPackageCodePath());
			for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
				String entry = iter.nextElement();
				for (String pluginPackagePath : pathsToInspect) {
					if (entry.startsWith(pluginPackagePath)) {
						Class<?> clazz = null;
						try {

							clazz = (Class<?>) Class.forName(entry);
							if (!WebViewPlugin.class.isAssignableFrom(clazz)) {
								continue;
							}
							if (clazz.getAnnotation(JavascriptModule.class) == null) {
								continue;
							}
							@SuppressWarnings("unchecked")
							Class<? extends WebViewPlugin> webViewPlugin = (Class<? extends WebViewPlugin>) clazz;
							result.put(clazz.getSimpleName(), webViewPlugin);
						} catch (Exception e) {
							if (BuildConfig.DEBUG){
								e.printStackTrace();
							}
							if (clazz != null){
								Log.e(PackageInspector.class.getSimpleName(), clazz.getSimpleName()  + " was ignored due to an error");
							}else{
								Log.e(PackageInspector.class.getSimpleName(), "Internal but recverable error");
							}
						}
					}
				}
			}
		}catch(Exception e){
			Log.e(PackageInspector.class.getSimpleName(), "Error inspecting the application, could not find any plugins");
		}
		return result;
	}
}
