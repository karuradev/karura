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

import android.content.Context;

public class LoaderFactory {
	public static ConfigLoader getLoader(boolean isAndroid, Object context){
		if (isAndroid){
			return new DalvikConfigLoader((Context)context);
		}
		return new JavaConfigLoader((String)context);
	}
}
