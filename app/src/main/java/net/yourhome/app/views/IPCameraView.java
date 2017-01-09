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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import net.yourhome.app.R;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.IPCameraBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.util.Util;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.net.model.viewproperties.Camera;
import net.yourhome.common.net.model.viewproperties.Property;
import net.yourhome.common.net.model.viewproperties.SensorWithIndicator;

public class IPCameraView extends ButtonView {

    // Properties
    //private Boolean liveStream;
    private Boolean hideRefreshButton;
    private volatile Boolean hideLoadingIcon;
    private Integer refreshDelaySeconds;

	public IPCameraView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.loader = new ProgressBar(canvas.getActivity());
		this.layout.addView(this.loader);
        this.setLoaderState(View.GONE);
	}

	@Override
	public void setLoaderState(final int state) {
        if (state == View.GONE || !hideLoadingIcon) {
            if (this.canvas == null || this.canvas.getActivity() == null) {
                Context mainContext = HomeServerConnector.getInstance().getMainContext();
                if (mainContext != null && mainContext instanceof Activity) {
                    ((Activity) mainContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            IPCameraView.this.loader.setVisibility(state);
                        }
                    });
                }
            } else {
                this.canvas.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IPCameraView.this.loader.setVisibility(state);
                    }
                });
            }
        }
	}

	public void setOnClickListener(final Activity activity) {

		OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (IPCameraView.this.binding != null && IPCameraView.this.binding instanceof IPCameraBinding) {
					IPCameraView.this.binding.viewPressed(IPCameraView.this.me, new UIEvent(Types.EMPTY));
				}
			}
		};
		this.button.setOnClickListener(onClickListener);
	}

	public void addBinding(JSONObject bindingProperties) {
		this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
		this.binding.addViewListener(this);

        // Set properties in binding
        if(this.binding instanceof IPCameraBinding) {
            Log.d("IPCameraView", "addBinding: setting refresh delay in "+this.toString());
            ((IPCameraBinding)this.binding).setRefreshDelay(refreshDelaySeconds*1000);
        }
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

        Log.d("IPCameraView", "buildView: setting properties in "+this.toString());
        Property hideRefreshProperty = this.properties.get(Camera.HIDE_REFRESH_BUTTON);
        this.hideRefreshButton = hideRefreshProperty != null?hideRefreshProperty.getValue().toLowerCase().equals("true"):false;

        Property hideLoadingIconProperty = this.properties.get(Camera.HIDE_LOADING_ICON);
        this.hideLoadingIcon = hideLoadingIconProperty != null?hideLoadingIconProperty.getValue().toLowerCase().equals("true"):false;

        Property refreshDelaySecondsProperty = this.properties.get(Camera.REFRESH_DELAY_S);
        this.refreshDelaySeconds = refreshDelaySecondsProperty != null?((Long) Math.round(Double.valueOf(refreshDelaySecondsProperty.getValue()))).intValue():600;

        Bitmap refreshBitmap = Configuration.getInstance().getAppIcon(HomeServerConnector.getInstance().getMainContext(), R.string.icon_refresh, 30, Color.WHITE);
		Bitmap refreshBitmapShadow = Util.addShadow(refreshBitmap, refreshBitmap.getHeight(), refreshBitmap.getWidth(), Color.BLACK, 1, 1, 1);

        if(!hideRefreshButton) {
            ImageButton refreshButton = new ImageButton(this.canvas.getActivity());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(0, 0, 10, 10);
            refreshButton.setBackground(new BitmapDrawable(HomeServerConnector.getInstance().getMainContext().getResources(), refreshBitmapShadow));
            this.layout.addView(refreshButton, params);
            refreshButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IPCameraBinding) IPCameraView.this.binding).refreshSnapshot();
                }
            });
        }
	}

	@Override
	public void refreshView() {
		if (this.binding != null) {
			Bitmap bitmap = ((IPCameraBinding) this.binding).getImage();
			Drawable bitmapDrawable = new BitmapDrawable(this.button.getResources(), bitmap);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				this.button.setBackground(bitmapDrawable);
			} else {
				this.button.setBackgroundDrawable(bitmapDrawable);
			}
		}
	}
}
