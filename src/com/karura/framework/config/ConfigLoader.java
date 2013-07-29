package com.karura.framework.config;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

public abstract class ConfigLoader {
	
	static final String TAG = "ConfigLoader";
	
	public abstract ConfigStore loadConfig() ;
	
	protected ConfigStore configStore = new ConfigStore();
	private boolean frameworkBlock = false, whitelistBlock = false;
	
	
	
	protected void elementStart(XMLNode xml){
		String strNode = xml.getName();
		if (strNode.equals("framework")) {
			frameworkBlock = true;
		} else if (frameworkBlock && strNode.equals("name")) {
			configStore.setFrameworkName(xml.getAttributeValue("value"));
		} else if (frameworkBlock && strNode.equals("version")) {
			configStore.setFrameworkVersion(xml.getAttributeValue("value"));
		} else if (frameworkBlock && strNode.equals("no_activity_title")) {
			configStore.showActivityTitle(xml.getAttributeValue("value"));
		} else if (frameworkBlock && strNode.equals("full_screen")) {
				configStore.showActivityFullScreen(xml.getAttributeValue("value"));
		} else if (frameworkBlock && strNode.equals("access")) {
			String origin = xml.getAttributeValue("origin");
			String subdomains = xml.getAttributeValue("subdomains");
			if (origin != null) {
				configStore.addWhitelistEntry(origin, (subdomains != null) && (subdomains.compareToIgnoreCase("true") == 0));
			}
		} else if (frameworkBlock && strNode.equals("log")) {
			String level = xml.getAttributeValue("level");
			Log.i(TAG, String.format("Found Log level %s", level));
			if (level != null) {
				configStore.setLogLevel(level);
			}
		} else if (frameworkBlock && strNode.equals("plugin-path")){
			String pkg = xml.getAttributeValue("package");
			if (!TextUtils.isEmpty(pkg)){
				configStore.addPackagePath(pkg);
			}
		}
		else if (frameworkBlock && strNode.equals("homeurl")) {
			String src = xml.getAttributeValue("src");

			Log.i(TAG, String.format("Found start page location: %s", src));

			if (src != null) {
				Pattern schemeRegex = Pattern.compile("^[a-z]+://");
				Matcher matcher = schemeRegex.matcher(src);
				if (matcher.find()) {
					configStore.setStartUrl(src);
				} else {
					if (src.charAt(0) == '/') {
						src = src.substring(1);
					}
					configStore.setStartUrl("file:///android_asset/" + src);
				}
			}
		} else if (frameworkBlock && strNode.equals("whitelist")) {
			whitelistBlock = true;
		} else if (frameworkBlock && whitelistBlock && strNode.equals("item")) {
			String src = xml.getAttributeValue("src");
			Boolean subdomains = xml.getAttributeBooleanValue("subdomains", false);
			if (src != null) {
				configStore.addWhitelistEntry(src, subdomains);
			}
		}
	}
	
	protected void elementEnd(XMLNode xml){
		String strNode = xml.getName();
		if (strNode.equals("framework")) {
			frameworkBlock = false;
		} else if (frameworkBlock && strNode.equals("whitelist")) {
			whitelistBlock = false;
		}
	}
}
