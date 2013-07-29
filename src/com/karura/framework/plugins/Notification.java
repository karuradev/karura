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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.karura.framework.PluginManager;
import com.karura.framework.annotations.Asynchronous;
import com.karura.framework.annotations.Description;
import com.karura.framework.annotations.ExportToJs;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.annotations.Param;
import com.karura.framework.annotations.Params;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.annotations.Synchronous;
import com.karura.framework.ui.webview.KaruraWebView;

/**
 * This class provides access to notifications on the device.
 */
@JavascriptModule
@Description("This plugin allows you to display native dialogs from javascript.")
public class Notification extends WebViewPlugin {

	public Notification(int pluginId, PluginManager pluginManager, KaruraWebView webView, Bundle savedInstance) {
		super(pluginId, pluginManager, webView, savedInstance);
	}

	@ExportToJs
	@Description("Used to identify the positive button for the dialogs.")
	public static final int POSITIVE_BTN = 1;

	@ExportToJs
	@Description("Used to identify the negative button for the dialogs.")
	public static final int NEGATIVE_BTN = 2;

	@ExportToJs
	@Description("Used to signal that a dialog was cancelled.")
	public static final int CANCELED = 3;

	@ExportToJs
	@Description("Used to identify the optional third button on dialogs.")
	public static final int NEUTRAL_BTN = 4;

	public int confirmResult = -1;
	public ProgressDialog spinnerDialog = null;
	public ProgressDialog progressDialog = null;

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------

	/**
	 * Beep plays the default notification ringtone.
	 * 
	 * @param count
	 *            Number of times to play notification
	 */
	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Play a small alert tone")
	@Synchronous(retVal = "none")
	@Params({ @Param(name = "count", description = "duration in miliseconds for which the beep sound needs to be played"), })
	public void beep(long count) {
		Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone notification = RingtoneManager.getRingtone(getContext(), ringtone);

		// If phone is not set to silent mode
		if (notification != null) {
			for (long i = 0; i < count; ++i) {
				notification.play();
				long timeout = 5000;
				while (notification.isPlaying() && (timeout > 0)) {
					timeout = timeout - 100;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	/**
	 * Vibrates the device for the specified amount of time.
	 * 
	 * @param time
	 *            Time to vibrate in ms.
	 */

	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Play a small vibrator alert")
	@Synchronous(retVal = "none")
	@Params({ @Param(name = "time", description = "duration in miliseconds for which the vibrator needs to be played"), })
	public void vibrate(long time) {
		// Start the vibration, 0 defaults to half a second.
		if (time == 0) {
			time = 500;
		}
		Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(time);
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Builds and shows a native Android alert with given Strings")
	@Asynchronous(retVal = "Will send a the button code to javascript based on user action. Currently POSITIVE_BTN and CANCELLED are sent.")
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "title", description = "The title of the alert"), @Param(name = "message", description = "The message the alert should display"),
			@Param(name = "buttonLabel", description = "The label of the button") })
	public synchronized void alert(final String callId, final String title, final String message, final String buttonLabel) {

		Runnable runnable = new Runnable() {
			public void run() {

				AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
				dlg.setMessage(message);
				dlg.setTitle(title);
				dlg.setCancelable(true);
				dlg.setPositiveButton(buttonLabel, new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						resolveWithResult(callId, POSITIVE_BTN);
					}
				});
				dlg.setOnCancelListener(new AlertDialog.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						resolveWithResult(callId, CANCELED);
					}
				});

				dlg.create();
				dlg.show();
			};
		};
		runOnUiThread(runnable);
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Builds and shows a native Android confirm dialog with given title, message, buttons. This dialog only shows up to 3 buttons. Any"
			+ "labels after that will be ignored. The index of the button pressed will be returned to the JavaScript callback identified by " + "callId.")
	@Asynchronous(retVal = "Index of the button selected by the user or cancelled in case the dialog was cancelled")
	@Params({
			@Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "title", description = "The title of the alert"),
			@Param(name = "message", description = "The message the alert should display"),
			@Param(name = "buttonLabels", description = " A comma separated list of button labels (Up to 3 buttons). The first button is identified by NEGATIVE_BTN, second by NEUTRAL_BTN and third by POSITIVE_BTN. You dont have to give all three buttons "), })
	public synchronized void confirm(final String callId, final String title, final String message, String buttonLabels) {

		final String[] fButtons = buttonLabels.split(",");

		Runnable runnable = new Runnable() {
			public void run() {
				AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
				dlg.setMessage(message);
				dlg.setTitle(title);
				dlg.setCancelable(true);

				// First button
				if (fButtons.length > 0) {
					dlg.setNegativeButton(fButtons[0], new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							resolveWithResult(callId, NEGATIVE_BTN);
						}
					});
				}

				// Second button
				if (fButtons.length > 1) {
					dlg.setNeutralButton(fButtons[1], new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							resolveWithResult(callId, NEUTRAL_BTN);
						}
					});
				}

				// Third button
				if (fButtons.length > 2) {
					dlg.setPositiveButton(fButtons[2], new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							resolveWithResult(callId, POSITIVE_BTN);
						}
					});
				}
				dlg.setOnCancelListener(new AlertDialog.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						resolveWithResult(callId, CANCELED);
					}
				});

				dlg.create();
				dlg.show();
			};
		};
		runOnUiThread(runnable);
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Show the spinner.")
	@Params({ @Param(name = "title", description = "The title of the spinner dialog"),
			@Param(name = "message", description = "The message to be displayed on the spinner"), })
	public synchronized void showSpinner(final String title, final String message) {
		if (this.spinnerDialog != null) {
			this.spinnerDialog.dismiss();
			this.spinnerDialog = null;
		}

		Runnable runnable = new Runnable() {
			public void run() {
				Notification.this.spinnerDialog = ProgressDialog.show(getActivity(), title, message, true, true, new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						Notification.this.spinnerDialog = null;
					}
				});
			}
		};
		runOnUiThread(runnable);
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Stop spinner.")
	public synchronized void hideSpinner() {
		if (this.spinnerDialog != null) {
			this.spinnerDialog.dismiss();
			this.spinnerDialog = null;
		}
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Show the progress dialog.")
	@Params({ @Param(name = "title", description = "Title of the dialog"), @Param(name = "message", description = "The message of the dialog"), })
	public synchronized void progressStart(final String title, final String message) {
		if (this.progressDialog != null) {
			this.progressDialog.dismiss();
			this.progressDialog = null;
		}
		final Notification notification = this;

		Runnable runnable = new Runnable() {
			public void run() {
				notification.progressDialog = new ProgressDialog(getActivity());
				notification.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				notification.progressDialog.setTitle(title);
				notification.progressDialog.setMessage(message);
				notification.progressDialog.setCancelable(true);
				notification.progressDialog.setMax(100);
				notification.progressDialog.setProgress(0);
				notification.progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						notification.progressDialog = null;
					}
				});
				notification.progressDialog.show();
			}
		};
		runOnUiThread(runnable);
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Set value of progress bar.")
	@Params({ @Param(name = "value", description = "value of the progress bar between 0 and 100.") })
	public synchronized void progressValue(int value) {
		if (this.progressDialog != null) {
			this.progressDialog.setProgress(value);
		}
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Stop progress dialog.")
	public synchronized void progressStop() {
		if (this.progressDialog != null) {
			this.progressDialog.dismiss();
			this.progressDialog = null;
		}
	}

	@JavascriptInterface
	@SupportJavascriptInterface
	@Synchronous(retVal = "none")
	@Description("Display a toast to the user")
	@Params({ @Param(name = "message", description = "Message to be displayed in the toast") })
	public void showToast(String message) {
		Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSaveInstanceState(Bundle saveInInstance) {
		// TODO Auto-generated method stub

	}

}
