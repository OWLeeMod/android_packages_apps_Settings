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
 
package com.android.settings.preference;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.android.settings.R;

/**
 * A preference item that opens a dialog with a NumberPicker when tapped.
 */
public class NumberPickerPreference extends DialogPreference {

    private static final String TAG = "NumberPickerPreference";

    private int mMinValue;
    private int mMaxValue;
    private int mValue;
    
    private NumberPicker mNumberPicker;

    public NumberPickerPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_picker_dialog);
    }

    public NumberPickerPreference(final Context context, final AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDialogLayoutResource(R.layout.number_picker_dialog);
    }

    @Override
    protected View onCreateDialogView() {
        final View view = super.onCreateDialogView();
        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
		mNumberPicker.setMinValue(mMinValue);
		mNumberPicker.setMaxValue(mMaxValue);
		mNumberPicker.setWrapSelectorWheel(false);
		mNumberPicker.setValue(mValue);
		return view;
    }

    @Override
    protected void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        builder.setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, this);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE && callChangeListener(mNumberPicker.getValue())) {
            mValue = mNumberPicker.getValue();
            persistInt(mValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            mValue = getPersistedInt(0);
        } 
        else {
            int temp = 0;
            try {
                temp = (Integer) defaultValue;
            } catch(Exception ex) {
                Log.e(TAG, "Invalid default value: " + defaultValue.toString());
            }
            persistInt(temp);
            mValue = temp;
        }
    }
    
    public void setMinValue(int value) {
		mMinValue = value;
    }
    
    public void setMaxValue(int value) {
		mMaxValue = value;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public int getValue() {
		return mValue;
    }
    
}
