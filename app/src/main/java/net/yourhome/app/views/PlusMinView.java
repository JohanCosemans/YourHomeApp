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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import net.yourhome.app.R;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.model.viewproperties.ClockDigital;
import net.yourhome.common.net.model.viewproperties.PlusMinValue;

public class PlusMinView extends DynamicView {

	protected RelativeLayout relativeLayout;
	private android.widget.TextView valueTextView;
	// private TextView valueTextViewDecimals;
	protected ImageButton left;
	protected ImageButton right;
	protected PlusMinView me = this;
	// private String TAG = "PlusMinView";

	private Double step;
	private int color;
	private Double size;

	private ValueBinding valueBinding;

	@Override
	public void destroyView() {
		super.destroyView();
		this.relativeLayout = null;
		this.valueTextView = null;
		this.left = null;
		this.right = null;
		this.me = null;
	}

	public PlusMinView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.canvas = canvas;
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	private void setValue(String newValue) {
		if (newValue != null) {
			String[] splittedValue = newValue.replace(",", ".").split("\\.");
			String textValue = "";
			if (splittedValue.length > 0) {
				textValue = splittedValue[0];
			}
			if (splittedValue.length > 1) {
				// valueTextViewDecimals.setText();
				Spannable span = new SpannableString(textValue + new String("" + Math.round(Double.parseDouble(splittedValue[1]))).charAt(0));
				span.setSpan(new RelativeSizeSpan(0.3f), textValue.length(), span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				this.valueTextView.setText(span);
			}
		}
	}

	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		this.step = Double.parseDouble(this.properties.get(PlusMinValue.STEP).getValue());
		this.color = Color.parseColor(this.properties.get(PlusMinValue.COLOR).getValue());
		this.size = Double.parseDouble(this.properties.get(ClockDigital.SIZE).getValue());

		int textSize = (int) (Configuration.getInstance().convertPixtoDip(this.canvas.getActivity(), this.size.intValue()) * this.relativeWidthFactor);

		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		params.leftMargin = this.layoutParameters.left;
		params.topMargin = this.layoutParameters.top;
		this.relativeLayout = new RelativeLayout(this.canvas.getActivity());
		this.relativeLayout.setLayoutParams(params);
		this.relativeLayout.setRotation((float) this.layoutParameters.rotation);
		this.relativeLayout.setGravity(Gravity.CENTER);
		LayoutInflater inflater = this.canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_plus_min, this.relativeLayout);
		this.valueTextView = (android.widget.TextView) this.relativeLayout.findViewById(R.id.view_plus_min_txt);
		this.valueTextView.setTextColor(this.color);
		this.valueTextView.setTextSize(textSize);

		this.left = (ImageButton) this.relativeLayout.findViewById(R.id.view_plus_min_left_btn);
		this.left.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					PlusMinView.this.left.setAlpha((float) 0.5);
					break;
				}
				case MotionEvent.ACTION_UP: {
					PlusMinView.this.left.setAlpha((float) 1);
					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					PlusMinView.this.left.setAlpha((float) 1);
					break;
				}
				}
				return false;
			}
		});
		this.left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Let binding handle the action
				if (PlusMinView.this.binding != null) {
					UIEvent event = new UIEvent(Types.SET_VALUE);
					Double currentValue = Double.parseDouble(PlusMinView.this.valueBinding.getValue());
					Double newValue = currentValue - PlusMinView.this.step;
					event.setProperty("VALUE", newValue); // - step value
					PlusMinView.this.binding.viewPressed(PlusMinView.this.me, event);
					PlusMinView.this.setValue(newValue + "");
				}
			}
		});

		this.right = (ImageButton) this.relativeLayout.findViewById(R.id.view_plus_min_right_btn);
		this.right.setBackground(Configuration.getInstance().getAppIconDrawable(this.canvas.getActivity(), R.string.icon_plus4, textSize / 2, this.color));
		this.left.setBackground(Configuration.getInstance().getAppIconDrawable(this.canvas.getActivity(), R.string.icon_minus3, textSize / 2, this.color));

		this.right.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					PlusMinView.this.right.setAlpha((float) 0.5);
					break;
				}
				case MotionEvent.ACTION_UP: {
					PlusMinView.this.right.setAlpha((float) 1);
					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					PlusMinView.this.right.setAlpha((float) 1);
					break;
				}
				}
				return false;
			}
		});
		this.right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Let binding handle the action
				if (PlusMinView.this.binding != null) {
					UIEvent event = new UIEvent(Types.SET_VALUE);
					Double currentValue = Double.parseDouble(PlusMinView.this.valueBinding.getValue());
					Double newValue = currentValue + PlusMinView.this.step;
					event.setProperty("VALUE", newValue); // + step value
					PlusMinView.this.binding.viewPressed(PlusMinView.this.me, event);
					PlusMinView.this.setValue(newValue + "");
				}
			}
		});

		this.relativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PlusMinView.this.binding != null) {
					PlusMinView.this.binding.viewLongPressed(PlusMinView.this.me, new UIEvent(Types.EMPTY));

					Intent intent2 = new Intent(PlusMinView.this.canvas.getActivity(), DataHistoryActivity.class);
					Bundle b2 = new Bundle();
					b2.putString("nodeIdentifier", PlusMinView.this.binding.getControlIdentifier().getNodeIdentifier());
					b2.putString("valueIdentifier", PlusMinView.this.binding.getControlIdentifier().getValueIdentifier());
					b2.putString("controllerIdentifier", PlusMinView.this.binding.getControlIdentifier().getControllerIdentifier().convert());
					intent2.putExtras(b2);
					PlusMinView.this.canvas.getActivity().startActivity(intent2);
				}
			}
		});
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
		this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
		if (this.binding != null) {
			this.valueBinding = (ValueBinding) this.binding;
			this.binding.addViewListener(this);
		}
		// Binding
		try {
			ValueTypes bindingType = ValueTypes.convert(bindingProperties.getString("valueType"));
			switch (bindingType) {
			case SENSOR_TEMPERATURE:
				this.left.setVisibility(View.GONE);
				this.right.setVisibility(View.GONE);
				this.valueTextView.setTypeface(Configuration.getInstance().getAppIconFont(HomeServerConnector.getInstance().getMainContext()), Typeface.BOLD);
				break;
			}
		} catch (Exception e) {
			Log.e("SensorView", "Could not create sensorbinding from " + bindingProperties);
		}
	}

	public static void createBinding(String stageElementId, JSONObject bindingProperties) {
		try {
			new ValueBinding(stageElementId, bindingProperties);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refreshView() {
		if (this.valueBinding != null) {
			this.setValue(this.valueBinding.getValue());
		}
	}
}
