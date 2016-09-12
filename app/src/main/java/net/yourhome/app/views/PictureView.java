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

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.*;
import net.yourhome.common.net.model.viewproperties.Property;

public class PictureView extends DynamicView {

	private ImageView image;
	private RelativeLayout.LayoutParams params;

	public PictureView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.image = new ImageView(this.canvas.getActivity());
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	@Override
	public View getView() {
		return this.image;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		// Layout
		this.params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		// params.leftMargin = layoutParameters.left;
		// params.topMargin = layoutParameters.top;
		this.image.setLayoutParams(this.params);
		this.image.setRotation((float) this.layoutParameters.rotation);
		this.image.setX(this.layoutParameters.left);
		this.image.setY(this.layoutParameters.top);

		// Properties
		Property imageSource = this.properties.get("imageSrc");
		Drawable bitmapDrawable = new BitmapDrawable(this.image.getResources(), Configuration.getInstance().loadBitmap(imageSource.getValue()));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			this.image.setBackground(bitmapDrawable);
		} else {
			this.image.setBackgroundDrawable(bitmapDrawable);
		}
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {

	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		// Do nothing
	}

	@Override
	public void refreshView() {
		// TODO Auto-generated method stub

	}

}
