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

import com.karura.framework.utils.StringUtils;

public class FrameworkConfig {
	
	/**
	 * Name of the framework. Some of these values are read from config.xml located in the res folder.
	 * You can override these values if you like for your applications, by specifying a custom in config.xml
	 */
	public static String FRAMEWORK_NAME = "karura";
	
	/**
	 * Name of the framework file which will be generated from plugins. Will be overriden by the value
	 * defined in the config.xml file
	 */
	public static String FRAMEWORK_JS_FILENAME = FRAMEWORK_NAME + ".js";
	
	/**
	 * Framework version, can be overridden using config.xml
	 */
	public static String FRAMEWORK_VERSION = "1.0";
	
	/**
	 * The binding name for framework plugin manager in the javascript environment
	 */
	public static String FRAMEWORK_JS_HANDLE = "_" + FRAMEWORK_NAME + "_plugins";
	
	/**
	 * 
	 */
	public static String FRAMEWORK_JS_CLASS_NAME = StringUtils.capitalize(FRAMEWORK_NAME);
}
