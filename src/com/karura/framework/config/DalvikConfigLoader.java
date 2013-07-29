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

package com.karura.framework.config;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

public class DalvikConfigLoader extends ConfigLoader {

	static final String TAG = "DalvikConfigLoader";

	Context context;

	public DalvikConfigLoader(Context context) {
		this.context = context;
	}

	private static class DalvikXmlNode implements XMLNode {

		XmlResourceParser xml;

		DalvikXmlNode(XmlResourceParser xml) {
			this.xml = xml;
		}

		@Override
		public String getName() {
			return xml.getName();
		}

		@Override
		public String getAttributeValue(String attr) {
			return xml.getAttributeValue(null, attr);
		}

		@Override
		public boolean getAttributeBooleanValue(String attr, boolean defaultVal) {
			return xml.getAttributeBooleanValue(null, attr, defaultVal);
		}
	}

	public ConfigStore loadConfig() {

		int id = context.getResources().getIdentifier("config", "xml",
				context.getPackageName());

		if (id == 0) {
			Log.i(TAG, "config.xml missing. Ignoring...");
			return configStore;
		}

		XmlResourceParser xml = context.getResources().getXml(id);
		int eventType = -1;

		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				elementStart(new DalvikXmlNode(xml));
			} else if (eventType == XmlResourceParser.END_TAG) {
				elementEnd(new DalvikXmlNode(xml));
			}
			try {
				eventType = xml.next();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return configStore;
	}

}
