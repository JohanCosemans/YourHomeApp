package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.IPCameraBinding;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasActivity;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.AlternativeColorPickerActivity;
import net.yourhome.app.util.ColorPickerActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class ColorPickerView extends ButtonView {

	public static final int ACTION_GETCOLOR = 1;
	public static final String STORE_COLOR_KEY = "net.yourhome.app.views.COLOR_PICKER";
    public ValueBinding valueBinding;

    public ColorPickerView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
		if(canvas.getActivity() instanceof CanvasActivity && valueBinding != null) {
			((CanvasActivity)canvas.getActivity()).addActivityResultListener(valueBinding);
		}
	}

    public void destroy() {
        if(canvas.getActivity() instanceof CanvasActivity && valueBinding != null) {
            ((CanvasActivity)canvas.getActivity()).removeActivityResultListener(valueBinding);
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
                        button.setAlpha((float) 0.5);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        button.setAlpha((float) 1);
                        // Start dialog window
                        Intent intent = new Intent(activity, ColorPickerActivity.class);
                        //Intent intent = new Intent(activity, AlternativeColorPickerActivity.class);
                        Bundle intentExtras = new Bundle();
                        if (valueBinding != null && valueBinding.getValue() != null) {
                            intentExtras.putString(ColorPickerActivity.STORE_COLOR_KEY, STORE_COLOR_KEY);
                            intentExtras.putInt(STORE_COLOR_KEY, Integer.parseInt(valueBinding.getValue()));
                            intentExtras.putString("stageElementId", getStageElementId());
                            intentExtras.putString(ColorPickerActivity.STORE_COLOR_KEY, STORE_COLOR_KEY);
                        }
                        intent.putExtras(intentExtras);
                        activity.startActivityForResult(intent, ColorPickerActivity.REQUEST_COLOR);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL: {
                        button.setAlpha((float) 1);
                        break;
                    }
                }
                return true;
            }
        });
	}

    @Override
    public void refreshView() {
        if(valueBinding != null) {
            super.setBackgroundColor(Integer.parseInt(valueBinding.getValue()));
        }
    }

	public void setBackgroundColor(int c) {
        if(this.binding != null) {
            UIEvent event = new UIEvent(UIEvent.Types.SET_VALUE);
            event.setProperty("VALUE", c);
            binding.viewPressed(me, event);
        }
	}
    public void addBinding(JSONObject bindingProperties) {
        this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
        if(this.binding != null) {
            valueBinding = (ValueBinding)binding;
            if(valueBinding != null && valueBinding.getValue() == null) {
                valueBinding.setValue(Color.BLUE+"");
            }
            binding.addViewListener(this);
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
