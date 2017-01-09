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

import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.adapters.BitmapColorAdapter;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import net.yourhome.app.R;

public class ColorPickerActivity extends Activity {
	public static final int REQUEST_COLOR = 1;

	public static String STORE_COLOR_KEY = "storeColorKey";
	public static String SHOW_SHADE_SLIDER = "show_shade_slider";
	public static String SHOW_OPACITY_SLIDER = "show_opacity_slider";
	public static String SHOW_CIRCLE_PICKER = "show_circle_picker";
	String stageElementId;

	boolean showShadeSlider = true;
	boolean showOpacitySlider = true;
	boolean showCirclePicker = false;

	private LobsterOpacitySlider opacitySlider = null;
	private LobsterShadeSlider shadeSlider = null;
	private LobsterPicker lobsterPicker = null;
	private LinearLayout colorExample = null;
	private ImageButton colorExampleButton = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.color_picker_dialog);

		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		if (b.containsKey(ColorPickerActivity.SHOW_SHADE_SLIDER)) {
			this.showShadeSlider = b.getBoolean(ColorPickerActivity.SHOW_SHADE_SLIDER);
		}
		if (b.containsKey(ColorPickerActivity.SHOW_OPACITY_SLIDER)) {
			this.showOpacitySlider = b.getBoolean(ColorPickerActivity.SHOW_OPACITY_SLIDER);
		}
		if (b.containsKey(ColorPickerActivity.SHOW_CIRCLE_PICKER)) {
			this.showCirclePicker = b.getBoolean(ColorPickerActivity.SHOW_CIRCLE_PICKER);
		}
		this.stageElementId = b.getString("stageElementId");

		final String storeColorKey = b.getString(ColorPickerActivity.STORE_COLOR_KEY);
		int initialColor = b.getInt(storeColorKey);

		this.shadeSlider = (LobsterShadeSlider) findViewById(R.id.shadeslider);
		this.opacitySlider = (LobsterOpacitySlider) findViewById(R.id.opacityslider);
		this.lobsterPicker = (LobsterPicker) findViewById(R.id.lobsterpicker);
		this.colorExample = (LinearLayout) findViewById(R.id.colorExample);
		this.colorExampleButton = (ImageButton) findViewById(R.id.colorExampleButton);

		if (!this.showShadeSlider) {
			this.shadeSlider.setVisibility(View.GONE);
		}
		if (!this.showOpacitySlider) {
			this.opacitySlider.setVisibility(View.GONE);
		}
		if (!this.showCirclePicker) {
			this.lobsterPicker.setVisibility(View.GONE);
			if (this.showShadeSlider && this.shadeSlider != null) {
				this.shadeSlider.setColorAdapter(new CustomBitmapColorAdapter(this, R.drawable.default_shader_pallete));
				if (this.showOpacitySlider && this.opacitySlider != null) {
					this.shadeSlider.addDecorator(this.opacitySlider);
				}
				this.shadeSlider.addOnColorListener(new OnColorListener() {
					@Override
					public void onColorChanged(@ColorInt int color) {
						PaintDrawable mDrawable = new PaintDrawable();
						mDrawable.getPaint().setColor(color);
						mDrawable.setCornerRadius(350f);
						ColorPickerActivity.this.colorExampleButton.setBackground(mDrawable);
					}

					@Override
					public void onColorSelected(@ColorInt int color) {

					}
				});

				PaintDrawable mDrawable = new PaintDrawable();
				mDrawable.getPaint().setColor(initialColor);
				mDrawable.setCornerRadius(350f);
				this.colorExampleButton.setBackground(mDrawable);
			}
		} else {
			this.colorExample.setVisibility(View.GONE);
			this.lobsterPicker.setColor(initialColor);
			this.lobsterPicker.setHistory(initialColor);
			this.lobsterPicker.setColorHistoryEnabled(true);
			if (this.showOpacitySlider && this.opacitySlider != null) {
				this.lobsterPicker.addDecorator(this.opacitySlider);
			}
			if (this.showShadeSlider && this.shadeSlider != null) {
				this.lobsterPicker.addDecorator(this.shadeSlider);
			}
		}
		Button select = (Button) findViewById(R.id.colorpicker_select);
		select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("storeColorKey", storeColorKey);
				intent.putExtra(storeColorKey, ColorPickerActivity.this.readColor());

				if (ColorPickerActivity.this.stageElementId != null) {
					intent.putExtra("stageElementId", ColorPickerActivity.this.stageElementId);
				}
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		Button cancel = (Button) findViewById(R.id.colorpicker_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private int readColor() {
		if (this.showCirclePicker) {
			return this.lobsterPicker.getColor();
		} else if (this.showShadeSlider) {
			return this.shadeSlider.getColor();
		}
		return Color.BLACK;
	}

	// This version of the bitmapColorAdapter will add black and white to the
	// pallet.
	class CustomBitmapColorAdapter extends BitmapColorAdapter {
		public CustomBitmapColorAdapter(Context context, @DrawableRes int resource) {
			super(context, resource);
		}

		public int shades(int position) {
			if (position == 0) {
				return super.shades(position) + 2;
			} else {
				return super.shades(position);
			}
		}

		@Override
		public @ColorInt int color(int position, int shade) {
			if (position != 0 || (position == 0 && shade < super.shades(0))) {
				return super.color(position, shade);
			} else if (position == 0 && shade == super.shades(0)) {
				return Color.BLACK;
			} else {
				return Color.WHITE;
			}
		}
	}
}
