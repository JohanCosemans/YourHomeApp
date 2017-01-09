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

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import net.yourhome.app.R;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.net.model.viewproperties.LineGraph;

public class SliderView extends DynamicView {
	private SeekBar seekBar;
	private RelativeLayout.LayoutParams params;
	private ValueBinding valueBinding;
	protected RelativeLayout relativeLayout;
	private SliderView me = this;

	// Property values
	private Double minimum;
	private Double maximum;

	@Override
	public void destroyView() {
		super.destroyView();
		this.seekBar = null;
		this.params = null;
		this.valueBinding = null;
		this.relativeLayout = null;
		this.me = null;
	}

	public SliderView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.canvas = canvas;
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	@Override
	public View getView() {
		return this.relativeLayout;
	}

	public void setValue(int newValue) {
		this.seekBar.setProgress((int) (newValue - this.minimum));
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		this.minimum = Double.valueOf(this.properties.get(LineGraph.MINIMUM).getValue().toString());
		this.maximum = Double.valueOf(this.properties.get(LineGraph.MAXIMUM).getValue().toString());

		Drawable thumbDrawable = this.canvas.getActivity().getResources().getDrawable(R.drawable.slider_thumb);
		Drawable scaledThumbDrawable = Configuration.getInstance().scaleImage(this.canvas.getActivity(), thumbDrawable, (float) Math.min(this.height / thumbDrawable.getIntrinsicHeight(), 1 * this.relativeWidthFactor));

		// Layout
		/*
		 * params = new
		 * RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.
		 * height); params.leftMargin = layoutParameters.left; params.topMargin
		 * = layoutParameters.top;
		 */
		this.seekBar = new SeekBar(this.canvas.getActivity());
		this.seekBar.setProgress(1);
		this.seekBar.setVisibility(View.VISIBLE);
		// seekBar.setBackgroundColor(Color.BLUE);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		this.seekBar.setLayoutParams(lp);
		this.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (SliderView.this.binding != null) {
					UIEvent event = new UIEvent(Types.SET_VALUE);
					event.setProperty("VALUE", seekBar.getProgress() - SliderView.this.minimum);
					SliderView.this.binding.viewPressed(SliderView.this.me, event);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int arg1, boolean arg2) {
			}
		});

		this.seekBar.setProgressDrawable(this.canvas.getActivity().getResources().getDrawable(R.drawable.slider_background));
		this.seekBar.setThumb(scaledThumbDrawable);
		this.seekBar.setMax((int) (this.maximum - this.minimum));

		this.relativeLayout = new RelativeLayout(this.canvas.getActivity());
		this.params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		// params.leftMargin = layoutParameters.left;
		// params.topMargin = layoutParameters.top;

		this.relativeLayout.setX(this.layoutParameters.left);
		this.relativeLayout.setY(this.layoutParameters.top);
		this.relativeLayout.setLayoutParams(this.params);
		this.relativeLayout.setRotation(this.layoutParameters.rotation);
		this.relativeLayout.addView(this.seekBar);
		// relativeLayout.setBackgroundColor(Color.CYAN);

	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
		this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
		if (this.binding != null) {
			this.valueBinding = (ValueBinding) this.binding;
			this.binding.addViewListener(this);
		}
	}

	public static void createBinding(String stageElementId, JSONObject bindingProperties) {
		try {
			new ValueBinding(stageElementId, bindingProperties);
		} catch (JSONException e) {
		} catch (InvalidParameterException e) {
		}
	}

	@Override
	public void refreshView() {
		if (this.valueBinding != null) {
			String valueString = this.valueBinding.getValue();
			if (valueString != null) {
				this.setValue((int) Math.round(Double.parseDouble(valueString)));
			}
		}
	}
}
