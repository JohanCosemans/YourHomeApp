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

import com.larswerkman.lobsterpicker.adapters.BitmapColorAdapter;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

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
import net.yourhome.app.R;

public class AlternativeColorPickerActivity extends Activity {
	public static final int REQUEST_COLOR = 1;

	public static String STORE_COLOR_KEY = "storeColorKey";
	String stageElementId;

	boolean showShadeSlider = true;
	boolean showOpacitySlider = true;
	boolean showCirclePicker = false;

	private ColorSeekBar colorSeekBar = null;
	private ImageButton colorExampleButton = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alternative_color_picker_dialog);

		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		this.stageElementId = b.getString("stageElementId");

		final String storeColorKey = b.getString(AlternativeColorPickerActivity.STORE_COLOR_KEY);
		int initialColor = b.getInt(storeColorKey);

		this.colorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
		this.colorExampleButton = (ImageButton) findViewById(R.id.colorExampleButton);
		this.colorSeekBar.setColors(R.array.color_picker_colors); // material_colors
																	// is
																	// defalut
																	// included
																	// in
																	// res/color,just
																	// use it.

		this.colorSeekBar.setBarHeight(6);
		this.colorSeekBar.setThumbHeight(50); // 30dpi
		this.colorSeekBar.setShowAlphaBar(false);
		this.colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
			@Override
			public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
				PaintDrawable mDrawable = new PaintDrawable();
				mDrawable.getPaint().setColor(color);
				mDrawable.setCornerRadius(350f);
				AlternativeColorPickerActivity.this.colorExampleButton.setBackground(mDrawable);
			}
		});
		this.colorSeekBar.setColorBarValue(
				initialColor);/*
								 * PaintDrawable mDrawable = new
								 * PaintDrawable();
								 * mDrawable.getPaint().setColor(initialColor);
								 * mDrawable.setCornerRadius(350f);
								 * colorExampleButton.setBackground(mDrawable);
								 */

		Button select = (Button) findViewById(R.id.colorpicker_select);
		select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("storeColorKey", storeColorKey);
				intent.putExtra(storeColorKey, AlternativeColorPickerActivity.this.readColor());

				if (AlternativeColorPickerActivity.this.stageElementId != null) {
					intent.putExtra("stageElementId", AlternativeColorPickerActivity.this.stageElementId);
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
		return this.colorSeekBar.getColor();
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
