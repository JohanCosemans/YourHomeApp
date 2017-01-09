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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.common.net.model.viewproperties.Property;

public abstract class DynamicView {


	public DynamicView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		this.stageElementId = stageElementId;
		this.canvas = canvas;
		this.initialize();
	}

	protected String stageElementId;
	protected DynamicView me = this;
	protected CanvasFragment canvas;
	protected Map<String, Property> properties = new HashMap<String, Property>();
	protected ViewLayoutParams layoutParameters;
	protected ProgressBar loader;
	protected AbstractBinding binding;
	protected float relativeWidthFactor;
	protected float relativeHeightFactor;

	protected Double width;
	protected Double height;
	protected Double left;
	protected Double top;

    public boolean hasBinding() {
        return this.binding!=null;
    }

	public abstract View getView();

	public abstract void refreshView();

	public void destroyView() {
		if (this.binding != null) {
			this.binding.removeViewListener(this);
		}
		this.properties.clear();
		this.properties = null;
		this.binding = null;
		this.canvas = null;
		this.layoutParameters = null;
		this.me = null;
	}

	public void buildView(JSONObject viewProperties) throws JSONException {
		// Read settings for this view
		JSONArray propertiesArray = viewProperties.getJSONArray("properties");
		for (int i = 0; i < propertiesArray.length(); i++) {
			Property p = new Property(propertiesArray.getJSONObject(i));
			this.properties.put(p.getKey(), p);
		}

		/* Parse layout parameters */
		// Get current width,height
		WindowManager wm = (WindowManager) this.canvas.getActivity().getSystemService(Context.WINDOW_SERVICE);

		Point currentCanvasSize = new Point();
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			display.getRealSize(currentCanvasSize);
		} else {
			display.getSize(currentCanvasSize);
		}

		// Get original width,height
		Point originalCanvasSize = this.canvas.getRelativeScreenSize();

		/*
		 * switch(Configuration.getInstance().getOrientation()) { case
		 * landscape: relativeWidthFactor = (float)currentCanvasSize.x /
		 * (float)originalCanvasSize.x; relativeHeightFactor =
		 * (float)currentCanvasSize.y / (float)originalCanvasSize.y; break; case
		 * portrait: relativeHeightFactor = (float)currentCanvasSize.x /
		 * (float)originalCanvasSize.x; relativeWidthFactor =
		 * (float)currentCanvasSize.y / (float)originalCanvasSize.y; break; }
		 */

		this.relativeWidthFactor = (float) currentCanvasSize.x / (float) originalCanvasSize.x;
		this.relativeHeightFactor = (float) currentCanvasSize.y / (float) originalCanvasSize.y;
		this.layoutParameters = new ViewLayoutParams();
		this.width = viewProperties.getDouble("width");
		this.height = viewProperties.getDouble("height");
		this.left = viewProperties.getDouble("left");
		this.top = viewProperties.getDouble("top");
		this.layoutParameters.width = (int) (this.width != null ? Math.round(this.width * this.relativeWidthFactor) : 0);
		this.layoutParameters.height = (int) (this.width != null ? Math.round(this.height * this.relativeHeightFactor) : 0);
		this.layoutParameters.top = (int) (this.width != null ? Math.round(this.top * this.relativeHeightFactor) : 0);
		this.layoutParameters.left = (int) (this.width != null ? Math.round(this.left * this.relativeWidthFactor) : 0);
		this.layoutParameters.rotation = (float) viewProperties.getDouble("rotation");
	};

	public abstract void addBinding(JSONObject bindingProperties);

	protected void initialize() {
	};

	public void setLoaderState(final int state) {
		if (this.loader != null && this.canvas != null) {
			this.canvas.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DynamicView.this.loader.setVisibility(state);
				}
			});
		}
	}

	public String getStageElementId() {
		return this.stageElementId;
	}

	public void setAlpha(float alpha) {
		if (this.getView() != null) {
			this.getView().setAlpha(alpha);
		}
	}

    public Context getContext() {
        if(canvas != null) {
            return canvas.getContext();
        }
        return null;
    }
	public class ViewLayoutParams {
		public int top;
		public int left;
		public int width;
		public int height;
		public float rotation;
	}
}
