/*-
 * Copyright (c) 2016 Coteq, Johan Cosemans
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY COTEQ AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.yourhome.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import net.yourhome.app.R;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public class DatePickerActivity extends Activity {

	public static int REQUEST_DATE_TIME = 1;
	private ControlIdentifiers identifiers;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.date_time_picker_dialog);

		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		String controllerIdentifier = extras.getString("controllerIdentifier");
		String nodeIdentifier = extras.getString("nodeIdentifier");
		String valueIdentifier = extras.getString("valueIdentifier");

		String windowTitle = extras.getString("windowTitle");
		String subtitle = extras.getString("subtitle");
		Long startDateLong = extras.getLong("startDate", new Date().getTime());

		// Set window title
		if (windowTitle != null) {
			this.setTitle(extras.getString("windowTitle"));
		}
		// Set window subtitle
		TextView titleView = (TextView) findViewById(R.id.date_timepicker_title);
		if (subtitle != null) {
			titleView.setText(subtitle);
		} else {
			titleView.setVisibility(View.GONE);
		}
		// Get default date
		final Date startDate = new Date(startDateLong);

		// Controller identifiers
		this.identifiers = new ControlIdentifiers();
		this.identifiers.setControllerIdentifier(ControllerTypes.convert(controllerIdentifier));
		this.identifiers.setNodeIdentifier(nodeIdentifier);
		this.identifiers.setValueIdentifier(valueIdentifier);

		// Initialize number pickers
		final NumberPicker dayPicker = (NumberPicker) findViewById(R.id.date_timepicker_day_picker);

		dayPicker.setMinValue(0);
		dayPicker.setMaxValue(29);
		dayPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		String[] dayLabels = new String[30];

		Calendar today = Calendar.getInstance();
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_MONTH, 1);

		Calendar day = Calendar.getInstance();
		day.setTime(startDate);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd  ", Locale.getDefault());
		// dayLabels[0] = "Today";
		// dayLabels[1] = "Tomorrow";
		// day.add(Calendar.DAY_OF_MONTH, 2);
		for (int i = 0; i < dayLabels.length; i++) {
			if (day.get(Calendar.YEAR) == today.get(Calendar.YEAR) && day.get(Calendar.MONTH) == today.get(Calendar.MONTH) && day.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
				dayLabels[i] = "Today";
			} else if (day.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) && day.get(Calendar.MONTH) == tomorrow.get(Calendar.MONTH) && day.get(Calendar.DAY_OF_MONTH) == tomorrow.get(Calendar.DAY_OF_MONTH)) {
				dayLabels[i] = "Tomorrow";
			} else {
				dayLabels[i] = dateFormat.format(day.getTime());
			}
			day.add(Calendar.DAY_OF_MONTH, 1);
		}
		dayPicker.setValue(0);
		dayPicker.setDisplayedValues(dayLabels);

		final NumberPicker hourPicker = (NumberPicker) findViewById(R.id.date_timepicker_hour_picker);
		hourPicker.setMinValue(0);
		hourPicker.setMaxValue(23);
		hourPicker.setValue(Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(startDate)));
		String[] hourLabels = new String[24];
		for (int i = 0; i < 24; i++) {
			hourLabels[i] = String.format("%02d", i);
		}
		hourPicker.setDisplayedValues(hourLabels);
		hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		final NumberPicker minutePicker = (NumberPicker) findViewById(R.id.date_timepicker_minute_picker);
		minutePicker.setMinValue(0);
		minutePicker.setMaxValue(3);
		minutePicker.setDisplayedValues(new String[] { "00", "15", "30", "45" });
		minutePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		Button select = (Button) findViewById(R.id.date_timepicker_select);
		select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// Get dates
				int dayValue = dayPicker.getValue();

				Calendar day = Calendar.getInstance();
				day.setTime(startDate);
				day.add(Calendar.DAY_OF_MONTH, dayValue);
				int hourValue = hourPicker.getValue();
				day.set(Calendar.HOUR_OF_DAY, hourValue);
				int minuteValue = minutePicker.getValue();
				day.set(Calendar.MINUTE, minuteValue * 15);

				Intent intent = new Intent();
				intent.putExtra("net.yourhome.controller.util.selectedDateTime", day.getTimeInMillis());
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		Button cancel = (Button) findViewById(R.id.date_timepicker_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
