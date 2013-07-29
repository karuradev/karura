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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import android.content.res.Resources;

public class Reader {
	public static String readRawString(Resources res, int resourceId) {
		StringBuilder sb = new StringBuilder();
		Scanner s = new Scanner(res.openRawResource(resourceId));

		while (s.hasNextLine()) {
			sb.append(s.nextLine() + "\n");
		}

		return sb.toString();
	}

	public static byte[] readRawByteArray(Resources res, int resourceId) {
		InputStream is = null;
		byte[] raw = new byte[] {};
		try {
			is = res.openRawResource(resourceId);
			raw = new byte[is.available()];
			is.read(raw);
		} catch (IOException e) {
			e.printStackTrace();
			raw = null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return raw;
	}

	public static String readRemoteString(String fileUrl) {
		StringBuilder sb = new StringBuilder();

		try {
			URLConnection connection = (new URL(fileUrl)).openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			connection.connect();

			Scanner s = new Scanner(connection.getInputStream());

			while (s.hasNextLine()) {
				sb.append(s.nextLine() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static byte[] readRemoteByteArray(String fileUrl) {
		InputStream is = null;
		byte[] raw = new byte[] {};
		try {
			URLConnection connection = (new URL(fileUrl)).openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			connection.connect();
			is = connection.getInputStream();
			raw = new byte[is.available()];
			is.read(raw);
		} catch (Exception e) {
			e.printStackTrace();
			raw = null;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return raw;
	}
}
