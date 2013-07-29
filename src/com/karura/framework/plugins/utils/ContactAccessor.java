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
package com.karura.framework.plugins.utils;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.karura.framework.annotations.ExportToJs;

import android.content.Context;
import android.util.Log;

public abstract class ContactAccessor {

	protected final String LOG_TAG = ContactAccessor.class.getSimpleName();

	@ExportToJs
	public static final String CONTACT_ID = "id";
	@ExportToJs
	public static final String DISPLAY_NAME = "displayName";
	@ExportToJs
	public static final String NAME = "name";

	public static final String FAMILY_NAME = "name.familyName";
	public static final String GIVEN_NAME = "name.givenName";
	public static final String MIDDLE_NAME = "name.middleName";
	public static final String PREFIX = "name.honorificPrefix";
	public static final String SUFFIX = "name.honorificSuffix";

	@ExportToJs
	public static final String PHONE_NUMBER = "phoneNumbers";
	@ExportToJs
	public static final String EMAIL = "emails";
	@ExportToJs
	public static final String FORMATTED_ADDRESS = "addresses";
	public static final String STREET = "addresses.streetAddress";
	public static final String CITY = "addresses.locality";
	public static final String REGION = "addresses.region";
	public static final String POSTCODE = "addresses.postalCode";
	public static final String COUNTRY = "addresses.country";
	@ExportToJs
	public static final String IMS = "ims";
	@ExportToJs
	public static final String ORGANIZATION = "organizations";
	public static final String DEPARTMENT = "organizations.department";
	public static final String TITLE = "organizations.title";
	@ExportToJs
	public static final String BIRTHDAY = "birthday";
	@ExportToJs
	public static final String NOTE = "note";
	@ExportToJs
	public static final String PHOTO = "photos";
	@ExportToJs
	public static final String URLS = "urls";

	private Context context;

	/**
	 * A static map that converts the JavaScript property name to Android database column name.
	 */
	static final Map<String, String> dbMap = new HashMap<String, String>();

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	/**
	 * Check to see if the data associated with the key is required to be populated in the Contact object.
	 * 
	 * @param key
	 * @param map
	 *            created by running buildPopulationSet.
	 * @return true if the key data is required
	 */
	protected boolean isRequired(String key, HashMap<String, Boolean> map) {
		Boolean retVal = map.get(key);
		return (retVal == null) ? false : retVal.booleanValue();
	}

	/**
	 * Create a hash map of what data needs to be populated in the Contact object
	 * 
	 * @param fields
	 *            the list of fields to populate
	 * @return the hash map of required data
	 */
	protected HashMap<String, Boolean> buildPopulationSet(JSONArray fields) {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();

		String key;
		try {
			if (fields.length() == 1 && fields.getString(0).equals("*")) {
				map.put(DISPLAY_NAME, true);
				map.put(NAME, true);
				map.put(PHONE_NUMBER, true);
				map.put(EMAIL, true);
				map.put(FORMATTED_ADDRESS, true);
				map.put(IMS, true);
				map.put(ORGANIZATION, true);
				map.put(BIRTHDAY, true);
				map.put(NOTE, true);
				map.put(URLS, true);
				map.put(PHOTO, true);
			} else {
				for (int i = 0; i < fields.length(); i++) {
					key = fields.getString(i);
					map.put(key, true);
				}
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return map;
	}

	/**
	 * Convenience method to get a string from a JSON object. Saves a lot of try/catch writing. If the property is not found in the object
	 * null will be returned.
	 * 
	 * @param obj
	 *            contact object to search
	 * @param property
	 *            to be looked up
	 * @return The value of the property
	 */
	protected String getJsonString(JSONObject obj, String property) {
		String value = null;
		try {
			if (obj != null) {
				value = obj.getString(property);
				if (value.equals("null")) {
					Log.d(LOG_TAG, property + " is string called 'null'");
					value = null;
				}
			}
		} catch (JSONException e) {
			Log.d(LOG_TAG, "Could not get = " + e.getMessage());
		}
		return value;
	}

	/**
	 * If the user passes in the '*' wildcard character for search then they want all fields for each contact
	 * 
	 * @param fields
	 * @return true if wildcard search requested, false otherwise
	 */
	protected boolean isGetAllFieldsSearch(JSONArray fields) {
		// Only do a wildcard search if we are passed ["*"]
		if (fields.length() == 1) {
			try {
				if ("*".equals(fields.getString(0))) {
					return true;
				}
			} catch (JSONException e) {
				return false;
			}
		}
		return false;
	}

	public abstract int getCount(JSONArray fields, String selection);

	/**
	 * Handles adding a JSON Contact object into the database.
	 * 
	 * @return TODO
	 */
	public abstract String save(JSONObject contact);

	/**
	 * Handles searching through SDK-specific contacts API.
	 */
	public abstract JSONArray search(JSONArray filter, String where, int startIndex, int limit);

	/**
	 * Handles searching through SDK-specific contacts API.
	 * 
	 * @throws JSONException
	 */
	public abstract JSONObject getContactById(String id) throws JSONException;

	/**
	 * Handles removing a contact from the database.
	 */
	public abstract boolean remove(String id);

	/**
	 * A class that represents the where clause to be used in the database query
	 */
	class WhereOptions {
		private String where;
		private String[] whereArgs;
		private String limit = "";

		public void setWhere(String where) {
			this.where = where;
		}

		public String getWhere() {
			return where;
		}

		public void setWhereArgs(String[] whereArgs) {
			this.whereArgs = whereArgs;
		}

		public String[] getWhereArgs() {
			return whereArgs;
		}

		public String getLimit() {
			return limit;
		}

		public void setLimit(int startIndex, int count) {
			if (startIndex != -1 && count != -1) {
				limit = String.format("LIMIT %d,%d", startIndex, count);
			} else if (count != -1) {
				limit = String.format("LIMIT %d", count);
			}
		}

	}

}
