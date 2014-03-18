/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.android.settings.backup;

import android.util.Log;

import java.io.File;
import java.util.Date;

/**
 * Represents a single backup consisting of an Android Backup file.
 *
 * The file name is expected to be in the format "packagename-timestamp.ab",
 * with timestamp being milliseconds since epoch.
 *
 * Backup files are handled through BackupManager.
 * 
 */
public class Backup {
	
	private static final String TAG = "Backup";
	
	/**
	 * The package name of the backed up package.
	 */
	public final String packageName;
	
	/**
	 * The file containing the backup.
	 */
	public final File backupFile;
	
	/**
	 * Date the backup was taken.
	 */
	public final Date date;
	
	/**
	 * Total file size of the backup.
	 */
	public final long size;
	
	/**
	 * Observer for delete().
	 */
	public interface DeleteBackupObserver {
		public void onDeleteBackupCompleted(Backup backup);
	}
	
	/**
	 * Constructs a backup representing an Android Backup that already 
	 * exists on the file system.
	 */
	public Backup(String packageName, File backupFile, long timestamp) {
		this.packageName = packageName;
		this.backupFile = backupFile;
		this.date = new Date(timestamp);
		this.size = backupFile.length();
	}
	
	/**
	 * Deletes the file synchronously and returns success.
	 */
	public boolean delete() {
		Log.i(TAG, "Deleting backup " + backupFile.getAbsolutePath() + backupFile.getName());
		return backupFile.delete();
	}
	
	/**
	 * Deletes this backup file from the file system.
	 */
	public void delete(final DeleteBackupObserver observer) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (delete()) {
					observer.onDeleteBackupCompleted(Backup.this);
				}
			}
		}).run();
	}
	
}