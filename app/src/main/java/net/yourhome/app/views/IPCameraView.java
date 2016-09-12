package net.yourhome.app.views;

import java.security.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.IPCameraBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.util.Util;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.app.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class IPCameraView extends ButtonView {
		
	public IPCameraView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties, bindingProperties);
		loader = new ProgressBar(canvas.getActivity());
		this.layout.addView(loader);
        setLoaderState(View.GONE);
	}
			
	public void setLoaderState(final int state) {
		if(canvas == null || canvas.getActivity() == null) {
			Context mainContext = HomeServerConnector.getInstance().getMainContext();
			if(mainContext != null && mainContext instanceof Activity) {
				((Activity)mainContext).runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		        		loader.setVisibility(state);
		            }
				});
			}
		}else {
			canvas.getActivity().runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	        		loader.setVisibility(state);
	            }
			});
		}
	}
	public void setOnClickListener(final Activity activity) {

		OnClickListener onClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(binding != null && binding instanceof IPCameraBinding) {
					binding.viewPressed(me, new UIEvent(Types.EMPTY));
				}
			}
		};
		this.button.setOnClickListener(onClickListener);
	}

	public void addBinding(JSONObject bindingProperties) {
		this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
        this.binding.addViewListener(this);
	}
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        try {
            new IPCameraBinding(stageItemId, bindingProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		Bitmap refreshBitmap = Configuration.getInstance().getAppIcon(HomeServerConnector.getInstance().getMainContext(), R.string.icon_refresh, 30, Color.WHITE);
		Bitmap refreshBitmapShadow = Util.addShadow(refreshBitmap, refreshBitmap.getHeight(), refreshBitmap.getWidth(), Color.BLACK, 1, 1, 1);
		
		ImageButton refreshButton = new ImageButton(canvas.getActivity());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.setMargins(0, 0, 10, 10);
		refreshButton.setBackground(new BitmapDrawable(HomeServerConnector.getInstance().getMainContext().getResources(),refreshBitmapShadow));
		layout.addView(refreshButton, params);
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((IPCameraBinding)binding).refreshSnapshot();
			}
		});
	}
	
	public void refreshView() {
		if(binding != null) {
			Bitmap bitmap = ((IPCameraBinding)binding).getImage();
			Drawable bitmapDrawable = new BitmapDrawable(button.getResources(),bitmap);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				button.setBackground(bitmapDrawable);
			}else {
				button.setBackgroundDrawable(bitmapDrawable);
			}
		}
	}
}
