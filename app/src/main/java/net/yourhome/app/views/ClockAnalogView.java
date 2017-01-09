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
package net.yourhome.app.views;

import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import net.yourhome.app.R;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;

public class ClockAnalogView extends DynamicView {

	protected RelativeLayout relativeLayout;
	private ImageView minuteView;
	private ImageView hourView;

	public ClockAnalogView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		// params.leftMargin = layoutParameters.left;
		// params.topMargin = layoutParameters.top;
		this.relativeLayout = new RelativeLayout(this.canvas.getActivity());
		this.relativeLayout.setLayoutParams(params);
		this.relativeLayout.setRotation((float) this.layoutParameters.rotation);
		this.relativeLayout.setX(this.layoutParameters.left);
		this.relativeLayout.setY(this.layoutParameters.top);

		LayoutInflater inflater = this.canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_analog_clock, this.relativeLayout);

		double factor = 1;
		Drawable hour = Configuration.getInstance().getAppIconDrawable(this.canvas.getActivity(), R.string.icon_Hourpointer, new Double(this.width * factor).intValue(), Color.BLACK);
		Drawable minute = Configuration.getInstance().getAppIconDrawable(this.canvas.getActivity(), R.string.icon_minute, new Double(this.width * factor).intValue(), Color.BLACK);
		// Drawable dial =
		// Configuration.getInstance().getAppIconDrawable(canvas.getActivity(),
		// R.string.icon_face1, new Double(width*factor).intValue(),
		// Color.WHITE);

		// ImageView dialView = (ImageView)
		// relativeLayout.findViewById(R.id.clock_face);
		// dialView.setBackground(dial);
		this.hourView = (ImageView) this.relativeLayout.findViewById(R.id.clock_hour);
		this.hourView.setBackground(hour);
		this.minuteView = (ImageView) this.relativeLayout.findViewById(R.id.clock_minute);
		this.minuteView.setBackground(minute);

		this.mCalendar = new Time();

		this.canvas.getActivity().getBaseContext().registerReceiver(this.mIntentReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		this.onTimeChanged();
	}

	private float mMinutes;
	private float mHour;
	private Time mCalendar;

	private void onTimeChanged() {
		this.mCalendar.setToNow();

		int hour = this.mCalendar.hour;
		int minute = this.mCalendar.minute;
		int second = this.mCalendar.second;

		this.mMinutes = minute + second / 60.0f;
		this.mHour = hour + this.mMinutes / 60.0f;

		this.hourView.setRotation(this.mHour / 12.0f * 360.0f);
		this.minuteView.setRotation(this.mMinutes / 60.0f * 360.0f);
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				String tz = intent.getStringExtra("time-zone");
				ClockAnalogView.this.mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
			}

			ClockAnalogView.this.onTimeChanged();
		}
	};

	@Override
	public void addBinding(JSONObject bindingProperties) {
		// No binding possible
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		// Do nothing
	}

	@Override
	public void refreshView() {
		// Not needed
	}

	@Override
	public void destroyView() {
		this.canvas.getActivity().getBaseContext().unregisterReceiver(this.mIntentReceiver);
	}

}
