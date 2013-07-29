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

package com.karura.framework.plugins;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.karura.framework.BuildConfig;
import com.karura.framework.PluginManager;
import com.karura.framework.annotations.Asynchronous;
import com.karura.framework.annotations.Description;
import com.karura.framework.annotations.ExportToJs;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.annotations.MinimumPlatformVersion;
import com.karura.framework.annotations.Param;
import com.karura.framework.annotations.Params;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.plugins.utils.ContactAccessor;
import com.karura.framework.plugins.utils.ContactAccessorSdk5;
import com.karura.framework.ui.webview.KaruraWebView;

@JavascriptModule(exportedClasses = { ContactAccessor.class })
@MinimumPlatformVersion(5)
@Description("A plugin to access, search and modify native contacts")
@ExportToJs(classes = { com.karura.framework.plugins.PluginConstants.class })
public class Contacts extends WebViewPlugin {
	private static final String TAG = Contacts.class.getSimpleName();

	public Contacts(int pluginId, PluginManager pluginManager, KaruraWebView webView, Bundle savedInstance) {
		super(pluginId, pluginManager, webView, savedInstance);
		contactAccessor = new ContactAccessorSdk5();
		contactAccessor.setContext(getContext());
	}

	private ContactAccessor contactAccessor;

	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Find the number of contacts in the native database which match the given criteria")
	@Asynchronous(retVal = "Returns the number of contacts which match the given criteria")
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "projection", description = "Comma separated list of fields to be retrieved from the contacts db. For selecting all fields, use *."),
			@Param(name = "selection", description = "Criteria for selection. This value will be used in where clause.") })
	public void getCount(final String callId, final String projection, final String selection) {
		runInBackground(new Runnable() {
			public void run() {
				final JSONArray fields = new JSONArray();
				String fldsArr[] = projection.split(",");
				for (String field : fldsArr) {
					fields.put(field);
				}
				int res = contactAccessor.getCount(fields, selection);
				resolveWithResult(callId, res);
			}
		});
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Search contacts in the native address book based upon mentioned criteria")
	@Asynchronous(retVal = "JSON array of Contacts. The fields of each contact are as specified in the projection criteria.")
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "projection", description = "Comma separated list of fields to be retrieved from the contacts db. For selecting all fields, use *."),
			@Param(name = "selection", description = "Criteria for selection. This value will be used in where clause."),
			@Param(name = "startIndex", description = "Specifies the start index for the page which needs to be fetched"),
			@Param(name = "limit", description = "Specifies the number of entries in the current page to be fetched.") })
	public void getContact(final String callId, final String projection, final String selection, final int startIndex, final int limit) {
		runInBackground(new Runnable() {
			public void run() {
				final JSONArray fields = new JSONArray();
				String fldsArr[] = projection.split(",");
				for (String field : fldsArr) {
					fields.put(field);
				}
				JSONArray res = contactAccessor.search(fields, selection, startIndex, limit);
				resolveWithResult(callId, res);
			}
		});
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Asynchronous(retVal = "Returns the new contact record (a json object) on success, or the error code on failure.")
	@Description("Update the specified contact in the native address book.")
	@Params({
			@Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "jsonEncodedContact", description = "Stringified json object representing the contact to be updated. Look at the field constants to see what parameters can be updated.") })
	public void saveContact(final String callId, final String jsonEncodedContact) {
		runInBackground(new Runnable() {
			public void run() {

				try {
					JSONObject contact = new JSONObject(jsonEncodedContact);
					String id = contactAccessor.save(contact);

					if (id != null) {
						JSONObject res = contactAccessor.getContactById(id);
						if (res != null) {
							resolveWithResult(callId, res);
						} else {
							rejectWithCode(callId, ERR_RECORD_NOT_FOUND);
						}
					}
				} catch (JSONException e) {
					Log.e(TAG, "JSON erorr..", e);
					rejectWithCode(callId, ERR_INVALID_JSON);
					return;
				}
			}
		});
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Delete a contact from the native address book")
	@Asynchronous(retVal = "integer specifying success or failure, depending upon whether the api was executed successfully or not.")
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "contactId", description = "Id of the contact to be deleted.") })
	public void removeContact(final String callId, final String contactId) {
		runInBackground(new Runnable() {
			public void run() {
				if (contactAccessor.remove(contactId)) {
					resolveWithResult(callId, SUCCESS);
				} else {
					rejectWithCode(callId, ERR_UNKNOWN);
				}
			}
		});
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Resolve the content URI to a base64 image.")
	@Asynchronous(retVal = "Returns the base64 encoded photograph of the specified contact")
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "contactContentUri", description = "Content URI of the photograph which was returned by the plugin in the call to getContact API") })
	public void getPhoto(final String callId, final String contactContentUri) {
		runInBackground(new Runnable() {
			public void run() {
				try {
					String decodedUri = URLDecoder.decode(contactContentUri, "UTF-8");

					ByteArrayOutputStream buffer = new ByteArrayOutputStream();

					int bytesRead = 0;
					byte[] data = new byte[8192];

					Uri uri = Uri.parse(decodedUri);

					InputStream in = getActivity().getContentResolver().openInputStream(uri);

					while ((bytesRead = in.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, bytesRead);
					}

					in.close();
					buffer.flush();

					String base64EncodedImage = Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT);
					resolveWithResult(callId, base64EncodedImage);

				} catch (Exception e) {
					if (BuildConfig.DEBUG) {
						e.printStackTrace();
					}
					rejectWithCode(callId, ERR_INVALID_ARG);
				}
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle saveInInstance) {

	}

}
