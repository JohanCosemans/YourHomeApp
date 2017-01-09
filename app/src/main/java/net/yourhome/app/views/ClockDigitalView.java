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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import net.yourhome.app.R;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.model.viewproperties.ClockDigital;

public class ClockDigitalView extends DynamicView {

	protected RelativeLayout relativeLayout;
	protected android.widget.TextView clockTextView;
	protected SimpleDateFormat clockFormat;
	protected SimpleDateFormat amPmFormat;
	protected BroadcastReceiver broadcastReceiver;

	private int color;
	private boolean amPm;
	private Double size;

	public ClockDigitalView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
		this.refreshView();
	}

	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		this.color = Color.parseColor(this.properties.get(ClockDigital.COLOR).getValue());
		this.size = Double.parseDouble(this.properties.get(ClockDigital.SIZE).getValue());
		this.amPm = Boolean.parseBoolean(this.properties.get(ClockDigital.AMPM).getValue());

		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		// params.leftMargin = layoutParameters.left;
		// params.topMargin = layoutParameters.top;
		this.relativeLayout = new RelativeLayout(this.canvas.getActivity());
		this.relativeLayout.setLayoutParams(params);
		this.relativeLayout.setRotation((float) this.layoutParameters.rotation);
		this.relativeLayout.setGravity(Gravity.CENTER);
		this.relativeLayout.setX(this.layoutParameters.left);
		this.relativeLayout.setY(this.layoutParameters.top);

		LayoutInflater inflater = this.canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_digital_clock, this.relativeLayout);
		int textSize = (int) (Configuration.getInstance().convertPixtoDip(this.canvas.getActivity(), this.size.intValue()) * this.relativeWidthFactor);

		this.clockTextView = (android.widget.TextView) this.relativeLayout.findViewById(R.id.digital_clock_time_txt);
		this.clockTextView.setTextColor(this.color);
		this.clockTextView.setTextSize(textSize);
		// clockTextView.setTypeface(Configuration.getInstance().getAppIconFont(HomeServerConnector.getInstance().getMainContext()),
		// Typeface.BOLD);
		if (this.amPm) {
			this.clockFormat = new SimpleDateFormat("hh:mm");
			this.amPmFormat = new SimpleDateFormat("aa");
		} else {
			this.clockFormat = new SimpleDateFormat("HH:mm");
		}

		this.broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent intent) {
				if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
					ClockDigitalView.this.setTime(new Date());
				}
			}
		};
		this.canvas.getActivity().getBaseContext().registerReceiver(this.broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
	}

	protected void setTime(Date date) {
		String timeFormatted = this.clockFormat.format(date);
		if (this.amPm) {
			Spannable span = new SpannableString(timeFormatted + this.amPmFormat.format(date));
			span.setSpan(new RelativeSizeSpan(0.3f), timeFormatted.length(), span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			this.clockTextView.setText(span);
		} else {
			this.clockTextView.setText(timeFormatted);
		}
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
		// No binding possible
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		// Do nothing
	}

	@Override
	public void refreshView() {
		this.setTime(new Date());
	}

	@Override
	public void destroyView() {
		this.canvas.getActivity().getBaseContext().unregisterReceiver(this.broadcastReceiver);
		this.relativeLayout = null;
		this.clockTextView = null;
		this.clockFormat = null;
		this.amPmFormat = null;
		this.broadcastReceiver = null;
		super.destroyView();
	}
}
