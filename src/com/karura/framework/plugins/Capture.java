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

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.ACTION_VIDEO_CAPTURE;
import static android.provider.MediaStore.EXTRA_DURATION_LIMIT;
import static android.provider.MediaStore.EXTRA_OUTPUT;
import static android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;

import android.webkit.JavascriptInterface;

import android.webkit.MimeTypeMap;

import com.karura.framework.BuildConfig;
import com.karura.framework.PluginManager;
import com.karura.framework.annotations.Asynchronous;
import com.karura.framework.annotations.Description;
import com.karura.framework.annotations.ExportToJs;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.plugins.utils.DirectoryManager;
import com.karura.framework.ui.webview.KaruraWebView;
import com.karura.framework.annotations.Param;
import com.karura.framework.annotations.Params;

@JavascriptModule(configElementName = "capture_plugin")
@Description("This plugin allows users to read media file meta data, and capture audio, video, and photographs using native components ")
public class Capture extends WebViewPlugin {

	private static final String LOG_TAG = "Capture";

	/*
	 * You can add more constants in this plugin which will be automatically made available in the javascript
	 * environment, if you follow the annotation structure illustrated here
	 */

	@ExportToJs
	@Description("MIME type for video 3gpp files")
	private static final String VIDEO_3GPP = "video/3gpp";

	@ExportToJs
	@Description("MIME type for mp4 files")
	private static final String VIDEO_MP4 = "video/mp4";

	@ExportToJs
	@Description("MIME type for audio 3gpp files")
	private static final String AUDIO_3GPP = "audio/3gpp";

	@ExportToJs
	@Description("MIME type for jpeg image files")
	private static final String IMAGE_JPEG = "image/jpeg";

	@ExportToJs
	@Description("Height of the media element whose metadata is being retrieved.")
	private static final String HEIGHT_FIELD = "height";

	@ExportToJs
	@Description("Width of the media element whose metadata is being retrieved.")
	private static final String WIDTH_FIELD = "width";

	@ExportToJs
	@Description("Bitrate of the media element whose metadata is being retrieved.")
	private static final String BITRATE_FIELD = "bitrate";

	@ExportToJs
	@Description("Duration (in miliseconds) of the media element whose metadata is being retrieved.")
	private static final String DURATION_FIELD = "duration";

	@ExportToJs
	@Description("Codecs information for the media element whose metadata is being retrieved.")
	private static final String CODECS_FIELD = "codecs";

	@ExportToJs
	@Description("Name of the media file (image/audio/video) which was just recorded")
	private static final String MEDIA_FILE_NAME_FIELD = "name";

	@ExportToJs
	@Description("File system path for the media component just recorded.")
	private static final String FILE_PATH_FIELD = "fullPath";

	@ExportToJs
	@Description("Mime type for the file just recorded.")
	private static final String FILE_TYPE_FIELD = "type";

	@ExportToJs
	@Description("Date when the file was last modified")
	private static final String FILE_MODIFIED_FIELD = "lastModifiedDate";

	@ExportToJs
	@Description("Size of the file in bytes")
	private static final String FILE_SIZE_FIELD = "fileSize";

	/*
	 * Internal Constants
	 */
	private static final int CAPTURE_AUDIO = 0; // Constant for capture audio
	private static final int CAPTURE_IMAGE = 1; // Constant for capture image
	private static final int CAPTURE_VIDEO = 2; // Constant for capture video

	private static final int CAPTURE_INTERNAL_ERR = 0;
	private static final int CAPTURE_NO_MEDIA_FILES = 3;

	private JSONArray results; // The array of results to be returned to the user
	private int numPics; // Number of pictures before capture activity

	private SparseArray<String> requestCallIdMap; // keep track of callId to actual api call

	/*
	 * Constructor
	 */
	protected Capture(int pluginId, PluginManager pluginManager, KaruraWebView webView, Bundle savedInstance) {
		super(pluginId, pluginManager, webView, savedInstance);
		requestCallIdMap = new SparseArray<String>();
	}

	@Asynchronous(retVal = "A JSON Object which contains the height, width, bitrate, duration and codec information, if available in media object."
			+ " Any field which is not available is returned as 0")
	@JavascriptInterface
	@SupportJavascriptInterface
	@Description("Try and read the metadata associated with the specified media file")
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java."),
			@Param(name = "filePath", description = "The file from which the metadata is to be loaded"),
			@Param(name = "userSuggestedMime", description = "User hint for the mimetype of the file which needs to be processed in this API") })
	public void getMetadataForMedia(final String callId, final String filePath, final String userSuggestedMime) {

		runInBackground(new Runnable() {
			public void run() {
				JSONObject obj = null;
				try {
					obj = getDefaultMetadataResp();
					// setup defaults
					obj.put(HEIGHT_FIELD, 0);
					obj.put(WIDTH_FIELD, 0);
					obj.put(BITRATE_FIELD, 0);
					obj.put(DURATION_FIELD, 0);
					obj.put(CODECS_FIELD, "");

					String mimeType = userSuggestedMime;
					// If the mimeType isn't set the rest will fail
					// so let's see if we can determine it.
					if (mimeType == null || mimeType.equals("") || "null".equals(mimeType)) {
						mimeType = getMimeType(filePath);
					}

					Log.d(LOG_TAG, "Mime type = " + mimeType);

					if (mimeType.equals(IMAGE_JPEG) || filePath.endsWith(".jpg")) {
						obj = getImageMetadata(filePath, obj);
					} else if (mimeType.endsWith(AUDIO_3GPP)) {
						obj = getAudioVideoMetadata(filePath, obj, false);
					} else if (mimeType.equals(VIDEO_3GPP) || mimeType.equals(VIDEO_MP4)) {
						obj = getAudioVideoMetadata(filePath, obj, true);
					}
				} catch (JSONException e) {
					rejectWithCode(callId, ERR_INVALID_JSON);
					return;
				}
				resolveWithResult(callId, obj);
			}
		});
	}

	/*
	 * Returns a JSONObject with default values for the GetMetaData
	 */
	private JSONObject getDefaultMetadataResp() throws JSONException {
		JSONObject obj = new JSONObject();

		// setup defaults
		obj.put(HEIGHT_FIELD, 0);
		obj.put(WIDTH_FIELD, 0);
		obj.put(BITRATE_FIELD, 0);
		obj.put(DURATION_FIELD, 0);
		obj.put(CODECS_FIELD, "");
		return obj;
	}

	/**
	 * Get the Image specific attributes
	 * 
	 * @param filePath
	 *            path to the file
	 * @param obj
	 *            represents the Media File Data
	 * @return a JSONObject that represents the Media File Data
	 * @throws JSONException
	 */
	private JSONObject getImageMetadata(String filePath, JSONObject obj) throws JSONException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(getRealPathFromURI(Uri.parse(filePath), getActivity()), options);
		obj.put(HEIGHT_FIELD, options.outHeight);
		obj.put(WIDTH_FIELD, options.outWidth);
		return obj;
	}

	/**
	 * Get the Image specific attributes
	 * 
	 * @param filePath
	 *            path to the file
	 * @param obj
	 *            represents the Media File Data
	 * @param video
	 *            if true get video attributes as well
	 * @return a JSONObject that represents the Media File Data
	 * @throws JSONException
	 */
	private JSONObject getAudioVideoMetadata(String filePath, JSONObject obj, boolean video) throws JSONException {
		MediaPlayer player = new MediaPlayer();
		try {
			player.setDataSource(filePath);
			player.prepare();
			obj.put(DURATION_FIELD, player.getDuration());
			if (video) {
				obj.put(HEIGHT_FIELD, player.getVideoHeight());
				obj.put(WIDTH_FIELD, player.getVideoWidth());
			}
		} catch (IOException e) {
			Log.d(LOG_TAG, "Error: loading video file");
		}
		return obj;
	}

	/**
	 * Sets up an intent to capture audio. Result handled by onActivityResult()
	 */
	@Asynchronous(retVal = "Returns a json object which contains the file name, type, modified date, size and path if successful.")
	@Description("Sets up an intent to capture audio and then starts the native system component to record audio. "
			+ "If there are more than one activity which can do the needful "
			+ "then a chooser dialog is displayed to the user to select the component which they will like to use to record the audio clip.")
	@JavascriptInterface
	@SupportJavascriptInterface
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java.") })
	public void captureAudio(final String callId) {
		Intent captureAudioIntent = new Intent(RECORD_SOUND_ACTION);
		requestCallIdMap.append(CAPTURE_AUDIO, callId);
		getActivity().startActivityForResult(captureAudioIntent, CAPTURE_AUDIO);
	}

	/**
	 * Sets up an intent to capture images. Result handled by onActivityResult()
	 */
	@Asynchronous(retVal = "Returns a json object which contains the file name, type, modified date, size and path if successful.")
	@Description("Sets up an intent to capture images, and then starts the component.")
	@JavascriptInterface
	@SupportJavascriptInterface
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java.") })
	public void captureImage(final String callId) {
		// Save the number of images currently on disk for later
		this.numPics = queryImgDB(whichContentStore()).getCount();

		Intent captureImageIntent = new Intent(ACTION_IMAGE_CAPTURE);

		// Specify file so that large image is captured and returned
		File photoFile = new File(DirectoryManager.getTempDirectoryPath(getActivity()), "Capture.jpg");
		captureImageIntent.putExtra(EXTRA_OUTPUT, Uri.fromFile(photoFile));

		requestCallIdMap.append(CAPTURE_IMAGE, callId);
		getActivity().startActivityForResult(captureImageIntent, CAPTURE_IMAGE);
	}

	/**
	 * Sets up an intent to capture video. Result handled by onActivityResult()
	 */
	@Asynchronous(retVal = "Returns a json object which contains the file name, type, modified date, size and path if successful.")
	@Description("Sets up an intent to capture video and starts the system component to handle recording of video")
	@JavascriptInterface
	@SupportJavascriptInterface
	@Params({ @Param(name = "callId", description = "The method correlator between javascript and java.") })
	public void captureVideo(final String callId, double duration) {
		Intent captureVideointent = new Intent(ACTION_VIDEO_CAPTURE);
		// Introduced in API 8
		if (Build.VERSION.SDK_INT >= 8) {
			captureVideointent.putExtra(EXTRA_DURATION_LIMIT, duration);
		}
		requestCallIdMap.append(CAPTURE_IMAGE, callId);
		getActivity().startActivityForResult(captureVideointent, CAPTURE_VIDEO);
	}

	/**
	 * Called when the video view exits.
	 * 
	 * @param requestCode
	 *            The request code originally supplied to startActivityForResult(), allowing you to identify who this
	 *            result came from.
	 * @param resultCode
	 *            The integer result code returned by the child activity through its setResult().
	 * @param intent
	 *            An Intent, which can return result data to the caller (various data can be attached to Intent
	 *            "extras").
	 * @throws JSONException
	 */
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		final String callId = requestCallIdMap.get(requestCode);
		final ContentResolver cr = getContext().getContentResolver();

		if (callId == null) {
			return;
		}
		requestCallIdMap.remove(requestCode);

		runInBackground(new Runnable() {
			public void run() {
				// Result received okay
				if (resultCode == Activity.RESULT_OK) {

					// An audio clip was requested
					if (requestCode == CAPTURE_AUDIO) {
						// Get the uri of the audio clip
						Uri data = intent.getData();
						// create a file object from the uri
						results.put(createMediaFile(data));

						// Send Uri back to JavaScript for listening to audio
						resolveWithResult(callId, results);

					} else if (requestCode == CAPTURE_IMAGE) {
						// For some reason if I try to do:
						// Uri data = intent.getData();
						// It crashes in the emulator and on my phone with a null pointer exception
						// To work around it I had to grab the code from CameraLauncher.java
						try {
							// Create entry in media store for image
							// (Don't use insertImage() because it uses default compression setting of 50 - no way to
							// change it)
							ContentValues values = new ContentValues();
							values.put(MIME_TYPE, IMAGE_JPEG);
							Uri uri = null;
							try {
								uri = cr.insert(EXTERNAL_CONTENT_URI, values);
							} catch (UnsupportedOperationException e) {
								Log.d(LOG_TAG, "Can't write to external media storage.");
								try {
									uri = cr.insert(INTERNAL_CONTENT_URI, values);
								} catch (UnsupportedOperationException ex) {
									Log.d(LOG_TAG, "Can't write to internal media storage.");
									reject(callId, CAPTURE_INTERNAL_ERR, "Error capturing image - no media storage found.");
									return;
								}
							}
							FileInputStream fis = new FileInputStream(DirectoryManager.getTempDirectoryPath(getContext()) + "/Capture.jpg");
							OutputStream os = cr.openOutputStream(uri);
							byte[] buffer = new byte[4096];
							int len;
							while ((len = fis.read(buffer)) != -1) {
								os.write(buffer, 0, len);
							}
							os.flush();
							os.close();
							fis.close();

							// Add image to results
							results.put(createMediaFile(uri));

							checkForDuplicateImage();

							// Send Uri back to JavaScript for viewing image
							resolveWithResult(callId, results);

						} catch (IOException e) {
							if (BuildConfig.DEBUG) {
								e.printStackTrace();
							}
							reject(callId, CAPTURE_INTERNAL_ERR, "Error capturing image.");
						}
					} else if (requestCode == CAPTURE_VIDEO) {
						// Get the uri of the video clip
						Uri data = intent.getData();
						// create a file object from the uri
						results.put(createMediaFile(data));

						// Send Uri back to JavaScript for viewing video
						resolveWithResult(callId, results);
					}
				}
				// if cancelled or something else
				else {
					// user canceled the action
					rejectWithError(callId, CAPTURE_NO_MEDIA_FILES, "Canceled.");
				}
			}
		});

	}

	/**
	 * Creates a JSONObject that represents a File from the Uri
	 * 
	 * @param data
	 *            the Uri of the audio/image/video
	 * @return a JSONObject that represents a File
	 * @throws IOException
	 */
	private JSONObject createMediaFile(Uri data) {
		File fp = new File(getRealPathFromURI(data, getActivity()));
		JSONObject obj = new JSONObject();

		try {
			// File properties
			obj.put(MEDIA_FILE_NAME_FIELD, fp.getName());
			obj.put(FILE_PATH_FIELD, "file://" + fp.getAbsolutePath());
			// Because of an issue with MimeTypeMap.getMimeTypeFromExtension() all .3gpp files
			// are reported as video/3gpp. I'm doing this hacky check of the URI to see if it
			// is stored in the audio or video content store.
			if (fp.getAbsoluteFile().toString().endsWith(".3gp") || fp.getAbsoluteFile().toString().endsWith(".3gpp")) {
				if (data.toString().contains("/audio/")) {
					obj.put(FILE_TYPE_FIELD, AUDIO_3GPP);
				} else {
					obj.put(FILE_TYPE_FIELD, VIDEO_3GPP);
				}
			} else {
				obj.put(FILE_TYPE_FIELD, getMimeType(fp.getAbsolutePath()));
			}

			obj.put(FILE_MODIFIED_FIELD, fp.lastModified());
			obj.put(FILE_SIZE_FIELD, fp.length());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return obj;
	}

	/**
	 * Creates a cursor that can be used to determine how many images we have.
	 * 
	 * @return a cursor
	 */
	private Cursor queryImgDB(Uri contentStore) {
		return getContext().getContentResolver().query(contentStore, new String[] { MediaStore.Images.Media._ID }, null, null, null);
	}

	/**
	 * Used to find out if we are in a situation where the Camera Intent adds to images to the content store.
	 */
	private void checkForDuplicateImage() {
		Uri contentStore = whichContentStore();
		Cursor cursor = queryImgDB(contentStore);
		int currentNumOfImages = cursor.getCount();

		// delete the duplicate file if the difference is 2
		if ((currentNumOfImages - numPics) == 2) {
			cursor.moveToLast();
			int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID))) - 1;
			Uri uri = Uri.parse(contentStore + "/" + id);
			getContext().getContentResolver().delete(uri, null, null);
		}
	}

	/**
	 * Looks up the mime type of a given file name.
	 * 
	 * @param filename
	 * @return a mime type
	 */
	@SuppressLint("DefaultLocale")
	public static String getMimeType(String filename) {
		if (filename != null) {
			// Stupid bug in getFileExtensionFromUrl when the file name has a space
			// So we need to replace the space with a url encoded %20
			String url = filename.replace(" ", "%20").toLowerCase();
			MimeTypeMap map = MimeTypeMap.getSingleton();
			String extension = MimeTypeMap.getFileExtensionFromUrl(url);
			if (extension.toLowerCase().equals("3ga")) {
				return AUDIO_3GPP;
			} else {
				return map.getMimeTypeFromExtension(extension);
			}
		} else {
			return "";
		}
	}

	/**
	 * Queries the media store to find out what the file path is for the Uri we supply
	 * 
	 * @param contentUri
	 *            the Uri of the audio/image/video
	 * @param context
	 *            the current application context
	 * @return the full path to the file
	 */
	@SuppressWarnings("deprecation")
	public static String getRealPathFromURI(Uri contentUri, Activity context) {
		final String scheme = contentUri.getScheme();

		if (scheme == null) {
			return contentUri.toString();
		} else if (scheme.compareTo("content") == 0) {
			String[] proj = { DATA };
			Cursor cursor = context.managedQuery(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else if (scheme.compareTo("file") == 0) {
			return contentUri.getPath();
		} else {
			return contentUri.toString();
		}
	}

	/**
	 * Determine if we are storing the images in internal or external storage
	 * 
	 * @return Uri
	 */
	private Uri whichContentStore() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return EXTERNAL_CONTENT_URI;
		} else {
			return INTERNAL_CONTENT_URI;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle saveInInstance) {
		// TODO Auto-generated method stub

	}
}
