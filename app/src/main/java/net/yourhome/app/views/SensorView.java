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

import java.security.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import net.yourhome.app.R;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.SensorBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.model.viewproperties.Property;
import net.yourhome.common.net.model.viewproperties.Sensor;
import net.yourhome.common.net.model.viewproperties.SensorWithIndicator;
import net.yourhome.common.net.model.viewproperties.Text;

public class SensorView extends DynamicView {

	protected RelativeLayout relativeLayout;
	private Button sensorText;
	protected ImageView icon;
	protected SensorView me = this;
	// private String TAG = "SensorView";
	private int color;

	// Properties
	private String downImage = null;
	private String upImage = null;
	private String neutralImage = null;
	private Double size;

	// UI Helpers
	private Double lastValue;

	protected SensorBinding sensorBinding;

	@Override
	public void destroyView() {
		super.destroyView();

		this.relativeLayout = null;
		this.sensorText = null;
		this.icon = null;
		this.me = null;
		this.downImage = null;
		this.upImage = null;
		this.neutralImage = null;
		this.size = null;
		this.lastValue = null;
		this.sensorBinding = null;
	}

	public SensorView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	public void setOnClickListener(final Activity activity) {
		OnTouchListener onTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				Log.d("SensorView", "Button event: " + event.getAction());
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					SensorView.this.me.icon.setAlpha((float) 0.5);
					SensorView.this.me.sensorText.setAlpha((float) 0.5);
					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					SensorView.this.me.icon.setAlpha((float) 1);
					SensorView.this.me.sensorText.setAlpha((float) 1);
					break;
				}
				case MotionEvent.ACTION_UP: {
					Intent intent2 = new Intent(activity, DataHistoryActivity.class);

					Bundle b2 = new Bundle();
					b2.putString("nodeIdentifier", SensorView.this.binding.getControlIdentifier().getNodeIdentifier());
					b2.putString("valueIdentifier", SensorView.this.binding.getControlIdentifier().getValueIdentifier());
					b2.putString("controllerIdentifier", SensorView.this.binding.getControlIdentifier().getControllerIdentifier().convert());
					intent2.putExtras(b2);
					activity.startActivity(intent2);

					SensorView.this.me.icon.setAlpha((float) 1);
					SensorView.this.me.sensorText.setAlpha((float) 1);

				}
				}
				return true;
			}
		};
		this.icon.setOnTouchListener(onTouchListener);
		this.sensorText.setOnTouchListener(onTouchListener);
	}

	private void setValue(double newValue, String unit) {
		this.lastValue = newValue;
		this.sensorText.setText(Double.toString(newValue).replace(".", ",") + " " + unit);
	}

	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		Property downImageProperty = this.properties.get(SensorWithIndicator.DOWN_IMAGE);
		Property upImageProperty = this.properties.get(SensorWithIndicator.UP_IMAGE);
		Property neutralImageProperty = this.properties.get(SensorWithIndicator.NEUTRAL_IMAGE);
		this.color = Color.parseColor(this.properties.get(Sensor.COLOR).getValue());
		this.size = (double) (Configuration.getInstance().convertPixtoDip(this.canvas.getActivity(), Double.parseDouble(this.properties.get(Text.SIZE).getValue())) * this.relativeWidthFactor);

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
		// sensorText = new Button(canvas.getActivity());

		LayoutInflater inflater = this.canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_sensor, this.relativeLayout);
		this.icon = (ImageView) this.relativeLayout.findViewById(R.id.view_sensor_image);
		this.sensorText = (Button) this.relativeLayout.findViewById(R.id.view_sensor_txt);
		this.sensorText.setTextColor(this.color);
		this.sensorText.setTextSize(this.size.floatValue());

		/*
		 * sensorText.setTextColor(Color.WHITE); icon = new
		 * ImageView(canvas.getActivity());
		 * relativeLayout.setGravity(Gravity.LEFT);
		 * relativeLayout.addView(icon);
		 * 
		 * icon.setId(1);
		 * 
		 * RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
		 * RelativeLayout.LayoutParams.WRAP_CONTENT,
		 * RelativeLayout.LayoutParams.WRAP_CONTENT);
		 * lp.addRule(RelativeLayout.RIGHT_OF, icon.getId() );
		 * 
		 * sensorText.setId(2); sensorText.setLayoutParams(lp);
		 * sensorText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		 * sensorText.setPadding(10, 0, 0, 0);
		 * sensorText.setBackgroundColor(color.transparent);
		 * sensorText.setText("?"); relativeLayout.addView(sensorText);
		 */

		// Properties
		if (downImageProperty != null) {
			this.downImage = downImageProperty.getValue();
		}
		if (upImageProperty != null) {
			this.upImage = upImageProperty.getValue();
		}
		if (neutralImageProperty != null) {
			this.neutralImage = neutralImageProperty.getValue();
		}
	}

	private void setImage(Bitmap image) {
		if (image != null) {
			Drawable bitmapDrawable = new BitmapDrawable(this.icon.getResources(), image);
			this.icon.setVisibility(View.VISIBLE);
			this.icon.setImageDrawable(bitmapDrawable);
		} else {
			this.icon.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
		this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
		if (this.binding != null) {
			this.sensorBinding = (SensorBinding) this.binding;
			this.binding.addViewListener(this);
		}
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		try {
			new SensorBinding(stageItemId, bindingProperties);
		} catch (JSONException e) {
		} catch (InvalidParameterException ex) {
		}
	}

	@Override
	public void refreshView() {
		if (this.sensorBinding != null) {
			String newValueString = this.sensorBinding.getValue();
			if (newValueString != null) {
				Double newValue = Double.parseDouble(this.sensorBinding.getValue());

				// Choose image
				String imageName = null;
				if (this.lastValue != null && this.lastValue > newValue) {
					imageName = this.downImage;
				} else if (this.lastValue != null && this.lastValue < newValue) {
					imageName = this.upImage;
				} else {
					imageName = this.neutralImage;
				}
				Bitmap bd = Configuration.getInstance().loadBitmap(imageName);

				this.setImage(bd);
				this.setValue(newValue, this.sensorBinding.getUnit());
			}
		}
	}
}
