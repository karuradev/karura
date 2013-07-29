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

package com.karura.framework.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.karura.framework.Constants;
import com.karura.framework.KaruraApp;
import com.karura.framework.PluginManager;
import com.karura.framework.R;
import com.karura.framework.config.LoaderFactory;

/**
 * This is the splash screen for your application. Subclass this screen and the splash layout for your application. Make sure the layout
 * file is called splash.xml in your project
 * 
 * @author nitin
 * 
 */
public abstract class SplashActivity extends Activity implements Constants, PluginManager.Callback {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);

		/*
		 * Run the initialization task in background
		 */
		new Thread(new Runnable() {
			public void run() {
				doStuffInBackground();
			}
		}).start();
	}

	/**
	 * Returns the reference to the next intent to be launched when the splash screen is to be closed.
	 * 
	 * Override this method to return an appropriate intent
	 * 
	 * TODO if the app is running then we have to simply move to the next intent
	 * 
	 * @return intent for the activity to be started
	 */
	protected Intent getNextIntent() {
		Intent intent = new Intent(getApplicationContext(), KaruraActivity.class);
		return intent;
	}

	/**
	 * Perform the application initialization tasks in background. Currently we are loading the framework configuration and initializing the
	 * web plugin manager.
	 */
	protected void doStuffInBackground() {
		KaruraApp app = KaruraApp.getApp();
		app.setAppConfig(LoaderFactory.getLoader(true, getApplicationContext()).loadConfig());
		app.setPluginManager(new PluginManager(getApplicationContext(), this, app.getAppConfig().pluginPackagePaths()));
	}

	/**
	 * Override this method to perform any additional initialization in the background. If you override this method make sure you call
	 * super.nextStep in your code. If your initialization is asynchronous then call finalStep() after your initialization is complete to
	 * move the application main activity
	 */
	protected void nextStep() {
		finalStep();
	}

	/**
	 * Close the splash screen and start the following activity
	 */
	protected void finalStep() {
		/*
		 * Continue the UI thread to the next activity
		 */
		runOnUiThread(new Runnable() {
			public void run() {
				startNextActivity();
				finish();
			}
		});
	}

	/**
	 * Start the next activity in the initialization chain.
	 */
	private void startNextActivity() {
		startActivity(getNextIntent());
	}

	/**
	 * Callback method from the PluginManager, which is called when the framework has been successfully initialized
	 */
	@Override
	public void onReady() {
		nextStep();
	}

	/**
	 * This method is called when the pluginManager could not be successfully initialzied. Show an appropriate error dialog message
	 */
	@Override
	public void onInternalError() {

		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), R.string.msg_error_initializing, Toast.LENGTH_LONG).show();
				finish();
			}
		});
	}
}
