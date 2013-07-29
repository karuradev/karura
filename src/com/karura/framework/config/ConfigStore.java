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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.karura.framework.FrameworkConfig;
import com.karura.framework.utils.StringUtils;

public class ConfigStore extends HashMap<String, Object> {
	private static final String TAG = "Config";
	private static final long serialVersionUID = 1L;

	ConfigStore() {
		super();
		put("whiteList", new ArrayList<Pattern>());
		put("startUrl", "file:///android_asset/karura/index.html");
		put("whiteListCache", new HashMap<String, Boolean>());
		put("pluginPaths", new ArrayList<String>());
	}

	@SuppressWarnings("unchecked")
	public List<Pattern> whiteList() {
		return (List<Pattern>) get("whiteList");
	}

	@SuppressWarnings("unchecked")
	public List<String> pluginPackagePaths() {
		return (List<String>) get("pluginPaths");
	}

	public String startUrl() {
		return (String) get("startUrl");
	}

	public void setStartUrl(String url) {
		if (url != null && url.trim().length() > 0) {
			put("startUrl", url);
		}
	}

	public boolean noActivityTitle() {
		return (Boolean) get("no_activity_title");
	}
	
	public boolean appRequiresFullScreen(){
		return (Boolean)get("full_screen");
	}

	@SuppressWarnings("unchecked")
	HashMap<String, Boolean> whiteListCache() {
		return (HashMap<String, Boolean>) get("whiteListCache");
	}

	/**
	 * Determine if URL is in approved list of URLs to load.
	 * 
	 * @param url
	 * @return
	 */
	public boolean isUrlWhiteListed(String url) {

		// Check to see if we have matched url previously
		if (whiteListCache().get(url) != null) {
			return true;
		}

		// Look for match in white list
		Iterator<Pattern> pit = whiteList().iterator();
		while (pit.hasNext()) {
			Pattern p = pit.next();
			Matcher m = p.matcher(url);

			// If match found, then cache it to speed up subsequent comparisons
			if (m.find()) {
				whiteListCache().put(url, true);
				return true;
			}
		}
		return false;
	}

	/**
	 * Add entry to approved list of URLs (whitelist)
	 * 
	 * @param origin
	 *            URL regular expression to allow
	 * @param subdomains
	 *            T=include all subdomains under origin
	 */

	public void addWhitelistEntry(String origin, boolean subdomains) {
		try {
			// Unlimited access to network resources
			if (origin.compareTo("*") == 0) {
				Log.d(TAG, "Warning : Unlimited access to network resources");
				whiteList().add(Pattern.compile(".*"));
			} else { // specific access
				// check if subdomains should be included
				// if the user has not specified any preference for URLs we will add https by default.
				if (subdomains) {
					if (origin.startsWith("http")) {
						whiteList().add(Pattern.compile(origin.replaceFirst("https?://", "^https?://(.*\\.)?")));
					} else {
						whiteList().add(Pattern.compile("^https?://(.*\\.)?" + origin));
					}
					Log.d(TAG, String.format("Origin to allow with subdomains: %s", origin));
				} else {
					if (origin.startsWith("http")) {
						whiteList().add(Pattern.compile(origin.replaceFirst("https?://", "^https?://")));
					} else {
						whiteList().add(Pattern.compile("^https?://" + origin));
					}
					Log.d(TAG, String.format("Origin to allow: %s", origin));
				}
			}
		} catch (Exception e) {
			Log.d(TAG, String.format("Failed to add origin %s", origin));
		}
	}

	public void setLogLevel(String level) {
		put("loglevel", level);
		
	}

	public void setFrameworkVersion(String version) {
		FrameworkConfig.FRAMEWORK_VERSION = version;
		
	}

	public void setFrameworkName(String frameworkName) {
		FrameworkConfig.FRAMEWORK_NAME = frameworkName;
		FrameworkConfig.FRAMEWORK_JS_FILENAME = FrameworkConfig.FRAMEWORK_NAME + ".js";
		FrameworkConfig.FRAMEWORK_JS_HANDLE = "_" + FrameworkConfig.FRAMEWORK_NAME + "_plugins";
		FrameworkConfig.FRAMEWORK_JS_CLASS_NAME = StringUtils.capitalize(FrameworkConfig.FRAMEWORK_NAME);
	}

	public void showActivityTitle(String attributeValue) {
		put("no_activity_title", Boolean.valueOf(attributeValue));
		
	}
	
	public void showActivityFullScreen(String attributeValue){
		put("full_screen", Boolean.valueOf(attributeValue));
	}

	@SuppressWarnings("unchecked")
	public void addPackagePath(String pkg) {
		((ArrayList<String>)get("pluginPaths")).add(pkg);
	}
}
