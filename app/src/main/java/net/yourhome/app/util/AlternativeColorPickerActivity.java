package net.yourhome.app.util;

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

import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.adapters.BitmapColorAdapter;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

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
        stageElementId = b.getString("stageElementId");

		final String storeColorKey = b.getString(STORE_COLOR_KEY);
		int initialColor = b.getInt(storeColorKey);

        colorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
        colorExampleButton = (ImageButton) findViewById(R.id.colorExampleButton);
        colorSeekBar.setColors(R.array.color_picker_colors); // material_colors is defalut included in res/color,just use it.

        colorSeekBar.setBarHeight(6);
        colorSeekBar.setThumbHeight(50); //30dpi
        colorSeekBar.setShowAlphaBar(false);
        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                PaintDrawable mDrawable = new PaintDrawable();
                mDrawable.getPaint().setColor(color);
                mDrawable.setCornerRadius(350f);
                colorExampleButton.setBackground(mDrawable);
            }
        });
        colorSeekBar.setColorBarValue(initialColor);/*
        PaintDrawable mDrawable = new PaintDrawable();
        mDrawable.getPaint().setColor(initialColor);
        mDrawable.setCornerRadius(350f);
        colorExampleButton.setBackground(mDrawable);*/

		Button select = (Button)findViewById(R.id.colorpicker_select);
		select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("storeColorKey", storeColorKey);
                intent.putExtra(storeColorKey, readColor());

                if (stageElementId != null) {
                    intent.putExtra("stageElementId", stageElementId);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
		Button cancel = (Button)findViewById(R.id.colorpicker_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
	}
	private int readColor() {
        return colorSeekBar.getColor();
    }

    // This version of the bitmapColorAdapter will add black and white to the pallet.
    class CustomBitmapColorAdapter extends BitmapColorAdapter{
        public CustomBitmapColorAdapter(Context context, @DrawableRes int resource) {
            super(context,resource);
        }
        public int shades(int position) {
            if(position==0) {
                return super.shades(position)+2;
            }else {
                return super.shades(position);
            }
        }
        @Override
        public @ColorInt int color(int position, int shade) {
            if(position != 0 || (position == 0 && shade < super.shades(0))) {
                return super.color(position,shade);
            }else if(position == 0 && shade == super.shades(0)) {
                return Color.BLACK;
            }else {
                return Color.WHITE;
            }
        }
    }
}
