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

/**
This file derived from Cordova Android Project available under the Apache 2.0 License
Please refer to APACHE-LICENCE2.0.txt for attributions
 */

package com.karura.framework.ui.webview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ConsoleMessage.MessageLevel;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.karura.framework.R;

public class KaruraWebChromeClient extends WebChromeClient {

	public KaruraWebChromeClient(KaruraWebView webViewEx) {
	}

	@Override
	public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
		new AlertDialog.Builder(view.getContext()).setTitle(R.string.title_dialog_alert).setMessage(message)
				.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				}).setCancelable(false).create().show();

		return true;
	}

	@Override
	public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
		new AlertDialog.Builder(view.getContext()).setTitle(R.string.title_dialog_confirm).setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						result.cancel();
					}
				}).create().show();

		return true;
	}

	@Override
	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
		return true;
	}

	@Override
	public void onProgressChanged(WebView view, int progress) {
		super.onProgressChanged(view, progress);
		// activity.setProgress(progress * 1000);
	}

	@Override
	public void onReceivedTitle(WebView view, String title) {
		KaruraWebView karuraWebView = (KaruraWebView) view;

		super.onReceivedTitle(view, title);
	}

	@Override
	public boolean onConsoleMessage(ConsoleMessage cm) {
		if (cm.message() == null){
			Log.e(KaruraWebView.TAG, "You passed null as the log message. This request has been ignored.");
			return false;
		}
		if (cm.messageLevel() == MessageLevel.ERROR) {
			Log.e(KaruraWebView.TAG, cm.message() + " at line " + cm.lineNumber() + " in file " + cm.sourceId());
		} else {
			Log.i(KaruraWebView.TAG, cm.message());
		}
		return true;
	}

	@Override
	public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota,
			WebStorage.QuotaUpdater quotaUpdater) {
		quotaUpdater.updateQuota(estimatedSize * 2); // double it
	}
}