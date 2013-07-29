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

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

/**
 * This class provides file directory utilities. All file operations are performed on the SD card.
 * 
 * It is used by the FileUtils class.
 */
public class DirectoryManager {

	@SuppressWarnings("unused")
	private static final String LOG_TAG = "DirectoryManager";

	/**
	 * Determine if a file or directory exists.
	 * 
	 * @param name
	 *            The name of the file to check.
	 * @return T=exists, F=not found
	 */
	protected static boolean testFileExists(String name) {
		boolean status;

		// If SD card exists
		if ((testSaveLocationExists()) && (!name.equals(""))) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = constructFilePaths(path.toString(), name);
			status = newPath.exists();
		}
		// If no SD card
		else {
			status = false;
		}
		return status;
	}

	/**
	 * Get the free disk space
	 * 
	 * @return Size in KB or -1 if not available
	 */
	protected static long getFreeDiskSpace(boolean checkInternal) {
		String status = Environment.getExternalStorageState();
		long freeSpace = 0;

		// If SD card exists
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			freeSpace = freeSpaceCalculation(Environment.getExternalStorageDirectory().getPath());
		} else if (checkInternal) {
			freeSpace = freeSpaceCalculation("/");
		}
		// If no SD card and we haven't been asked to check the internal directory then return -1
		else {
			return -1;
		}

		return freeSpace;
	}

	/**
	 * Given a path return the number of free KB
	 * 
	 * @param path
	 *            to the file system
	 * @return free space in KB
	 */
	private static long freeSpaceCalculation(String path) {
		StatFs stat = new StatFs(path);
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize / 1024;
	}

	/**
	 * Determine if SD card exists.
	 * 
	 * @return T=exists, F=not found
	 */
	protected static boolean testSaveLocationExists() {
		String sDCardStatus = Environment.getExternalStorageState();
		boolean status;

		// If SD card is mounted
		if (sDCardStatus.equals(Environment.MEDIA_MOUNTED)) {
			status = true;
		}

		// If no SD card
		else {
			status = false;
		}
		return status;
	}

	/**
	 * Create a new file object from two file paths.
	 * 
	 * @param file1
	 *            Base file path
	 * @param file2
	 *            Remaining file path
	 * @return File object
	 */
	private static File constructFilePaths(String file1, String file2) {
		File newPath;
		if (file2.startsWith(file1)) {
			newPath = new File(file2);
		} else {
			newPath = new File(file1 + "/" + file2);
		}
		return newPath;
	}

	/**
	 * Determine if we can use the SD Card to store the temporary file. If not then use the internal cache directory.
	 * 
	 * @return the absolute path of where to store the file
	 */
	public static String getTempDirectoryPath(Context ctx) {
		File cache = null;

		// SD Card Mounted
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cache = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/cache/");
		}
		// Use internal storage
		else {
			cache = ctx.getCacheDir();
		}

		// Create the cache directory if it doesn't exist
		if (!cache.exists()) {
			cache.mkdirs();
		}

		return cache.getAbsolutePath();
	}
}
