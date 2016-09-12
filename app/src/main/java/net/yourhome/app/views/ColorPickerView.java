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
 * THIS SOFTWARE IS PROVIDED BY THE NETBSD FOUNDATION, INC. AND CONTRIBUTORS
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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasActivity;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.ColorPickerActivity;

public class ColorPickerView extends ButtonView {

	public static final int ACTION_GETCOLOR = 1;
	public static final String STORE_COLOR_KEY = "net.yourhome.app.views.COLOR_PICKER";
	public ValueBinding valueBinding;

	public ColorPickerView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
		if (canvas.getActivity() instanceof CanvasActivity && this.valueBinding != null) {
			((CanvasActivity) canvas.getActivity()).addActivityResultListener(this.valueBinding);
		}
	}

	public void destroy() {
		if (this.canvas.getActivity() instanceof CanvasActivity && this.valueBinding != null) {
			((CanvasActivity) this.canvas.getActivity()).removeActivityResultListener(this.valueBinding);
		}
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
	}

	public void setOnClickListener(final Activity activity) {
		this.button.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					ColorPickerView.this.button.setAlpha((float) 0.5);
					break;
				}
				case MotionEvent.ACTION_UP: {
					ColorPickerView.this.button.setAlpha((float) 1);
					// Start dialog window
					Intent intent = new Intent(activity, ColorPickerActivity.class);
					// Intent intent = new Intent(activity,
					// AlternativeColorPickerActivity.class);
					Bundle intentExtras = new Bundle();
					if (ColorPickerView.this.valueBinding != null && ColorPickerView.this.valueBinding.getValue() != null) {
						intentExtras.putString(ColorPickerActivity.STORE_COLOR_KEY, ColorPickerView.STORE_COLOR_KEY);
						intentExtras.putInt(ColorPickerView.STORE_COLOR_KEY, Integer.parseInt(ColorPickerView.this.valueBinding.getValue()));
						intentExtras.putString("stageElementId", ColorPickerView.this.getStageElementId());
						intentExtras.putString(ColorPickerActivity.STORE_COLOR_KEY, ColorPickerView.STORE_COLOR_KEY);
					}
					intent.putExtras(intentExtras);
					activity.startActivityForResult(intent, ColorPickerActivity.REQUEST_COLOR);
					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					ColorPickerView.this.button.setAlpha((float) 1);
					break;
				}
				}
				return true;
			}
		});
	}

	@Override
	public void refreshView() {
		if (this.valueBinding != null) {
			super.setBackgroundColor(Integer.parseInt(this.valueBinding.getValue()));
		}
	}

	@Override
	public void setBackgroundColor(int c) {
		if (this.binding != null) {
			UIEvent event = new UIEvent(UIEvent.Types.SET_VALUE);
			event.setProperty("VALUE", c);
			this.binding.viewPressed(this.me, event);
		}
	}

	public void addBinding(JSONObject bindingProperties) {
		this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
		if (this.binding != null) {
			this.valueBinding = (ValueBinding) this.binding;
			if (this.valueBinding != null && this.valueBinding.getValue() == null) {
				this.valueBinding.setValue(Color.BLUE + "");
			}
			this.binding.addViewListener(this);
		}
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		try {
			new ValueBinding(stageItemId, bindingProperties);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
