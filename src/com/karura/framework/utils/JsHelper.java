
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

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Pair;

import com.karura.framework.plugins.PluginConstants;

public class JsHelper implements PluginConstants {
	public static final String BACK_PRESSED = "javascript: karura.events.onBackPressed();";
	public static final String OPTIONS_MENU_KEY_DOWN = "javascript: karura.events.onOptionsMenuKeyDown();";
	
	public static final String KARURA_DISPATCH = "javascript: %s.dispatcher.dispatch('%s');";

	private static final String SEPARATOR_COMMA = ", ";
	private static final String ARRAY_LITERAL_RIGHT = "]";
	private static final String ARRAY_LITERAL_LEFT = "[";
	private static final String STRING_LITERAL_ESCAPE = "\\\"";
	private static final String STRING_LITERAL_BACKSLASH = "\"";

	public static String literal(Object value) {
		return (value instanceof Number) ? value.toString() : stringLiteral(value);
	}

	private static String[] literals(Object... values) {
		String[] literals = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			literals[i] = literal(values[i]);
		}
		return literals;
	}

	private static String stringLiteral(Object value) {
		return STRING_LITERAL_BACKSLASH + value.toString().replaceAll(STRING_LITERAL_BACKSLASH, STRING_LITERAL_ESCAPE) + STRING_LITERAL_BACKSLASH;
	}

	private static String join(String separator, String... strings) {
		StringBuilder buffer = new StringBuilder();
		String s = "";
		for (String string : strings) {
			buffer.append(s);
			s = separator;
			buffer.append(string);
		}
		return buffer.toString();
	}

	public static String jsFragmentForAsyncResponse(String receiver, String method, String action, int timeout, Pair... params) throws IllegalArgumentException {
		JSONObject payload = new JSONObject();
		try {
			payload.put("receiver", receiver);
			String method2 = Character.toUpperCase(method.charAt(0)) + method.substring(1);
			payload.put("method", method2);
			if (timeout != INVALID_TIMEOUT) {
				payload.put("timeout", timeout);
			}
			if (action != null) {
				payload.put("action", action);
			}
			JSONObject apiPayload = new JSONObject();
			for (Pair p : params) {
				apiPayload.put((String) p.first, p.second);
			}
			payload.put("data", apiPayload);
		} catch (JSONException e) {
			throw new IllegalArgumentException();
		}
		String stringify = payload.toString();

		return stringify;
	}

	public static String notify(String dispatchHandle, int id) {
		return String.format(KARURA_DISPATCH, dispatchHandle, id);
	}

	public static String arrayLiterals(Object... values) {
		return ARRAY_LITERAL_LEFT + JsHelper.join(SEPARATOR_COMMA, literals(values)) + ARRAY_LITERAL_RIGHT;
	}

	public static String escapeJs(String javascript) {
		return javascript.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"", "\\\\\"");
	}
}