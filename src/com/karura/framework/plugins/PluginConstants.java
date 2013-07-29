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

import com.karura.framework.annotations.ExportToJs;

public interface PluginConstants {

	static final String ON_RESOLVE_ACTION = "resolve";
	static final String ON_PROGRESS_ACTION = "progress";
	static final String ON_REJECT_ACTION = "reject";

	static final String RESPONSE_PAYLOAD = "data";
	static final String EVENT_PAYLOAD = "data";

	public static final int INVALID_TIMEOUT = -1;

	@ExportToJs
	public static final int SUCCESS = 0;
	@ExportToJs
	public static final int ERR_UNKNOWN = 1;
	@ExportToJs
	public static final int ERR_INVALID_ARGUMENT = 2;
	@ExportToJs
	public static final int ERR_TIMEOUT = 3;
	@ExportToJs
	public static final int ERR_IO = 4;
	@ExportToJs
	public static final int ERR_NOT_SUPPORTED = 5;
	@ExportToJs
	public static final int ERR_INVALID_ARG = 6;
	@ExportToJs
	public static final int ERR_EMPTY_RESULT = 7;
	@ExportToJs
	public static final int ERR_INVALID_JSON = 8;
	@ExportToJs
	public static final int ERR_RECORD_NOT_FOUND = 9;
	@ExportToJs
	public static final int ERR_PERMISSION_DENIED = 20;
}
