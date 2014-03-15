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

import com.android.settings.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.AttributeSet;

import java.text.DateFormat;

/**
 * View that represents a single Backup object.
 * 
 * It displays date and size of the backup and buttons 
 * to restore or delete it.
 * 
 */
public class BackupView extends RelativeLayout {
	
	private Backup mBackup;
	
	public BackupView(Context context) {
        super(context);
	}
	
	public BackupView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public BackupView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	/**
	 * Sets the backup whose information should be displayed in this view.
	 *
	 * @param b The backup to display.
	 * @param sizeString A string that shows Backup.size in a human readable format.
	 * @param listener An OnClickListener that is invoked when either button is pressed
	 * 					(use their ids, R.id.delete and R.id.restore, to distinguish them).
	 */
	public void setBackup(Backup backup, String sizeString, 
			View.OnClickListener listener) {
		mBackup = backup;
		
		TextView date = (TextView) findViewById(R.id.date);
		DateFormat df = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, DateFormat.MEDIUM);
		date.setText(df.format(backup.date));
		
		TextView size = (TextView) findViewById(R.id.size);
		size.setText(sizeString);

		TextView restore = (Button) findViewById(R.id.restore);
		restore.setOnClickListener(listener);
		
		TextView delete = (Button) findViewById(R.id.delete);
		delete.setOnClickListener(listener);
	}
	
	/**
	 * Returns the backup last passed to setBackup(), or null if none was set.
	 */
	public Backup getBackup() {
		return mBackup;
	}
	
}