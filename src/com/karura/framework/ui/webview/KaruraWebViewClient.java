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


package com.karura.framework.ui.webview;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.karura.framework.KaruraApp;
import com.karura.framework.Constants;
import com.karura.framework.config.ConfigStore;

public class KaruraWebViewClient extends WebViewClient implements Constants {
	static final String TAG = "KaruraWebViewClient";
	private KaruraWebView webView;

	public KaruraWebViewClient(KaruraWebView webViewEx) {
		webView = webViewEx;
	}

	public KaruraApp getApp() {
		return KaruraApp.getApp();
	}

	public ConfigStore appConfig() {
		return getApp().getAppConfig();
	}


	/**
	 * Notify the host application of a resource request and allow the application to return the data. If the return
	 * value is null, the WebView will continue to load the resource as usual. Otherwise, the return response and data
	 * will be used.
	 */

	@SuppressLint("NewApi")
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView webView, String url) {
		KaruraWebView webViewEx = (KaruraWebView) webView;
		if (webViewEx.disallowFileAccess && url.startsWith("file://")) {
			Log.e(TAG, "url: " + url);
			return new WebResourceResponse(null, null, null);
		}

		if (url.startsWith("data:image/png;base64")) {
			return super.shouldInterceptRequest(webView, url);
		}

		String fileExt = MimeTypeMap.getFileExtensionFromUrl(url);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt);

		String path = null;
		if (url.startsWith("file:///android_asset/")){
			path = url.substring("file:///android_asset/".length(), url.length());
		}else if (url.startsWith("file:///")){
			path = url.substring("file:///".length(), url.length());
		}
		if (path != null) {
			WebResourceResponse result = getWebResourceResponseFromAsset(webView.getContext(), path, mimeType);
			if (result != null) {
				return result;
			}
		}

		return super.shouldInterceptRequest(webView, url);
	}

	@SuppressLint("NewApi")
	public WebResourceResponse getWebResourceResponseFromAsset(Context context, String path, String mimeType) {
		InputStream is = null;
		try {
			is = context.getAssets().open(path);
			return new WebResourceResponse(mimeType, "UTF-8", is);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	} 

	/**
	 * Notify the host application of a resource request and allow the application to return the data. If the return
	 * value is null, the WebView will continue to load the resource as usual. Otherwise, the return response and data
	 * will be used.
	 */

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.e(TAG, "shouldOverrideUrlLoading(): " + Uri.parse(url).getHost());

		if (url.startsWith(WebView.SCHEME_TEL)) {
			try {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(url));
				webView.getActivity().startActivity(intent);
			} catch (android.content.ActivityNotFoundException e) {
				Log.e(TAG, "Error handling Tel URL -> " + url + ": " + e.toString());
			}
		} else if (url.startsWith("geo:")) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				webView.getActivity().startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "Error loading maps -> " + url + ": " + e.toString());
			}
		} else if (url.startsWith(WebView.SCHEME_MAILTO)) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				webView.getActivity().startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "Error opening email composer -> " + url + ": " + e.toString());
			}
		} else if (url.startsWith("sms:")) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);

				String address = null;
				int parmIndex = url.indexOf('?');
				if (parmIndex == -1) {
					address = url.substring(4);
				} else {
					address = url.substring(4, parmIndex);

					Uri uri = Uri.parse(url);
					String query = uri.getQuery();
					if (query != null) {
						if (query.startsWith("body=")) {
							intent.putExtra("sms_body", query.substring(5));
						}
					}
				}
				intent.setData(Uri.parse("sms:" + address));
				intent.putExtra("address", address);
				intent.setType("vnd.android-dir/mms-sms");
				webView.getActivity().startActivity(intent);
			} catch (android.content.ActivityNotFoundException e) {
				Log.e(TAG, "Error sending sms " + url + ":" + e.toString());
			}
		}

		else {
			if (url.startsWith("file://") || url.indexOf(webView.baseUrl) == 0 || webView.isUrlWhiteListed(url)) {
				webView.loadUrl(url);
			}

			else {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					webView.getActivity().startActivity(intent);
				} catch (android.content.ActivityNotFoundException e) {
					Log.e(TAG, "Error loading url " + url, e);
				}
			}
		}

		return true;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		Log.e(TAG, "onPageStarted()");
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		Log.e(TAG, "error: " + description + " \n errorCode: " + errorCode + "\n  failingUrl: " + failingUrl);
	}
}