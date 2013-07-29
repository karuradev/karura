package com.karura.autogen;

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

/*
 * Used to keep track of function types which will be exported from the 
 * native layer
 */
public enum FunctionType {
	SYNC_FN(1), ASYNC_FN(2), EVENT(3);
	int type;

	FunctionType(int type) {
		this.type = type;
	}
}