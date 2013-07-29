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

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.util.Pair;

@SuppressLint("UseSparseArrays")
public class MsgQueue {

	private static final HashMap<Integer, Pair<String, String>> queue = new HashMap<Integer, Pair<String, String>>();
	private static final int MAX_HANDLE_ID = 0x7fffffff;
	private static Integer nextId = 1;

	public String get(int id) {
		Pair<String, String> p = queue.remove(id);
		return p.second;
	}

	public int add(String javascript, String receiverId) {
		synchronized (nextId) {
			int curId = nextId;
			nextId = ++nextId % MAX_HANDLE_ID;
			queue.put(curId, Pair.create(receiverId, javascript));
			return curId;
		}
	}

	public void releaseReceiver(String receiver) {
		for (Integer key : queue.keySet()) {
			Pair<String, String> p = queue.get(key);
			if (p.first.compareTo(receiver) == 0) {
				queue.remove(key);
			}
		}
	}
	
	public void clear(){
		queue.clear();
	}
}