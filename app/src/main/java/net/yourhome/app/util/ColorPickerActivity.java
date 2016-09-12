package net.yourhome.app.util;

import net.yourhome.app.R;

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
        if(b.containsKey(SHOW_SHADE_SLIDER))   { showShadeSlider = b.getBoolean(SHOW_SHADE_SLIDER);     }
        if(b.containsKey(SHOW_OPACITY_SLIDER)) { showOpacitySlider = b.getBoolean(SHOW_OPACITY_SLIDER); }
        if(b.containsKey(SHOW_CIRCLE_PICKER))  { showCirclePicker = b.getBoolean(SHOW_CIRCLE_PICKER);   }
        stageElementId = b.getString("stageElementId");

		final String storeColorKey = b.getString(STORE_COLOR_KEY);
		int initialColor = b.getInt(storeColorKey);

        shadeSlider = (LobsterShadeSlider) findViewById(R.id.shadeslider);
        opacitySlider = (LobsterOpacitySlider) findViewById(R.id.opacityslider);
        lobsterPicker = (LobsterPicker) findViewById(R.id.lobsterpicker);
        colorExample = (LinearLayout) findViewById(R.id.colorExample);
        colorExampleButton = (ImageButton) findViewById(R.id.colorExampleButton);

        if(!showShadeSlider) {
            shadeSlider.setVisibility(View.GONE);
        }
        if(!showOpacitySlider) {
            opacitySlider.setVisibility(View.GONE);
        }
        if(!showCirclePicker) {
            lobsterPicker.setVisibility(View.GONE);
            if(showShadeSlider && shadeSlider != null) {
                shadeSlider.setColorAdapter(new CustomBitmapColorAdapter(this, R.drawable.default_shader_pallete));
                if(showOpacitySlider && opacitySlider != null) {
                    shadeSlider.addDecorator(opacitySlider);
                }
                shadeSlider.addOnColorListener(new OnColorListener() {
                    @Override
                    public void onColorChanged(@ColorInt int color) {
                        PaintDrawable mDrawable = new PaintDrawable();
                        mDrawable.getPaint().setColor(color);
                        mDrawable.setCornerRadius(350f);
                        colorExampleButton.setBackground(mDrawable);
                    }
                    @Override
                    public void onColorSelected(@ColorInt int color) {

                    }
                });

                PaintDrawable mDrawable = new PaintDrawable();
                mDrawable.getPaint().setColor(initialColor);
                mDrawable.setCornerRadius(350f);
                colorExampleButton.setBackground(mDrawable);
            }
        }else {
            colorExample.setVisibility(View.GONE);
            lobsterPicker.setColor(initialColor);
            lobsterPicker.setHistory(initialColor);
            lobsterPicker.setColorHistoryEnabled(true);
            if(showOpacitySlider && opacitySlider != null) { lobsterPicker.addDecorator(opacitySlider);    }
            if(showShadeSlider && shadeSlider != null)     { lobsterPicker.addDecorator(shadeSlider);      }
        }
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
        if(showCirclePicker) {
            return lobsterPicker.getColor();
        }else if(showShadeSlider) {
            return shadeSlider.getColor();
        }
        return Color.BLACK;
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
