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

import android.os.Environment;

import android.app.backup.IBackupManager;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.settings.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists and creates application backups.
 * 
 * Backups are created and restored using the system backup service, and 
 * are in the same format used by adb backup and adb restore.
 * 
 * All backups are stored in and retrieved from BACKUP_LOCATION.
 * 
 * The number of backups per package is limited by the shared preference 
 * "backup_history"  (can be changed in SecuritySettings);
 * 
 * See Backup for the naming convention for backup files.
 * 
 */
public class BackupManager {
	
	private static final String TAG = "BackupManager";

    private static IBackupManager mBackupService = IBackupManager.Stub.asInterface(
            ServiceManager.getService(Context.BACKUP_SERVICE));
	
	public static final File BACKUP_LOCATION = 
			new File(Environment.getExternalStorageDirectory(),  "Backup/");
	
	private Context mContext;
	
	public BackupManager(Context context) {
		mContext = context;
	}
	
	/**
	 * Observer for listBackups().
	 */
	public interface ListBackupsObserver {
		public void onListBackupsCompleted(Map<String, List<Backup>> backups);
	}
	
	/**
	 * Observer for createBackup().
	 */
	public interface CreateBackupObserver {
		public void onCreateBackupCompleted(Backup backup);
	}
	
	/**
	 * Observer for restore().
	 */
	public interface RestoreBackupObserver {
		public void onRestoreBackupCompleted(Backup backup);
	}
	
	/**
	 * Deletes the oldest backup(s), so that at most backup_history number 
	 * of backups exist.
	 */
	public class TrimBackupHistory implements ListBackupsObserver {
		public void onListBackupsCompleted(Map<String, List<Backup>> backups) {
			int backup_history = PreferenceManager.getDefaultSharedPreferences(mContext)
					.getInt("backup_history", mContext.getResources().getInteger(R.integer.backup_history_default));
			for (String s : backups.keySet()) {
				for (int i = backups.get(s).size() - 1; i > backup_history - 1; i--) {
					backups.get(s).get(i).delete();
				}
			}
		}
	}
	
	/**
	 * Compares backups by date so the newest backup comes first.
	 */
	private class BackupComparator implements Comparator<Backup> {
	    public int compare(Backup lhs, Backup rhs) {
	    	return rhs.date.compareTo(lhs.date);
	    }
	}
	
	/**
	 * Lists all backups for the specified packages.
	 * 
	 * Backups are sorted newest backup first.
	 * 
	 * @param packageNames List of packages for which backups should be listed. If
	 * 						this is null, all backups are listed.
	 * @param observer Object to be called with the backups.
	 */
	public void listBackups(final List<String> packageNames, final ListBackupsObserver observer) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				BACKUP_LOCATION.mkdirs();
				Map<String, List<Backup>> backups = new HashMap<String, List<Backup>>();
				String[] contents = BACKUP_LOCATION.list();
				for (String s : contents) {
					String[] split = s.replace(".ab", "").split("-", 2);
					if (split.length == 2) {
						if (!backups.containsKey(split[0])) {
							backups.put(split[0], new ArrayList<Backup>());
						}
						backups.get(split[0]).add(new Backup(split[0], new File(BACKUP_LOCATION, s), 
								Long.parseLong(split[1])));
					}
				}
				for (String s : backups.keySet()) {
					if (packageNames != null && !packageNames.contains(s)) {
						backups.remove(s);
					}
					else {
						Collections.sort(backups.get(s), new BackupComparator());						
					}
				}
				observer.onListBackupsCompleted(backups);
			}
		}).run();
	}
	
	/**
	 * Creates a new backup for the specified package and and saves it to BACKUP_LOCATION;
	 */
	public void createBackup(final String packageName, final CreateBackupObserver observer) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Log.i(TAG, "Backing up " + packageName);
					
					BACKUP_LOCATION.mkdirs();
					long timestamp = System.currentTimeMillis();
					File f = new File(BACKUP_LOCATION, 
							packageName + "-" + Long.toString(timestamp) + ".ab");
					ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, 
							ParcelFileDescriptor.MODE_CREATE | 
							ParcelFileDescriptor.MODE_READ_WRITE);
					mBackupService.fullBackup(pfd, true, false,
				            false, false, false, new String[]{packageName});
					listBackups(Arrays.asList(packageName), new TrimBackupHistory());
					observer.onCreateBackupCompleted(new Backup(packageName, f, timestamp));
				}
				catch (FileNotFoundException e) {	
					Log.d(TAG, "Backup failed", e);
				}
				catch (RemoteException e) {	
					Log.d(TAG, "Backup failed", e);				
				}
			}
		}).run();
	}
	
	/**
	 * Restores a backup that was previously retrieved using listBackups and is 
	 * located in BACKUP_LOCATION;
	 */
	public void restoreBackup(final Backup b, final RestoreBackupObserver observer) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Log.i(TAG, "Restoring " + b.backupFile.getName());
					
					BACKUP_LOCATION.mkdirs();
					ParcelFileDescriptor pfd = ParcelFileDescriptor.open(b.backupFile, 
							ParcelFileDescriptor.MODE_READ_ONLY);
					mBackupService.fullRestore(pfd);
					observer.onRestoreBackupCompleted(b);
				}
				catch (FileNotFoundException e) {	
					Log.d(TAG, "Restore failed", e);
				}
				catch (RemoteException e) {	
					Log.d(TAG, "Restore failed", e);				
				}
			}
		}).run();
	}
	
}