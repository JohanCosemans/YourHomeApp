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

import java.security.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.base.enums.Alignments;
import net.yourhome.common.net.model.viewproperties.Text;

public class TextView extends DynamicView {

	protected RelativeLayout relativeLayout;
	protected android.widget.TextView textTextView;

	private int color;
	private Double size;
	private String content;
	private Alignments alignment;

	public TextView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
		this.refreshView();
	}

	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		this.color = Color.parseColor(this.properties.get(Text.COLOR).getValue());
		this.size = (double) (Configuration.getInstance().convertPixtoDip(this.canvas.getActivity(), Double.parseDouble(this.properties.get(Text.SIZE).getValue())) * this.relativeWidthFactor);
		this.content = this.properties.get(Text.CONTENT).getValue();
		if (this.properties.get(Text.ALIGNMENT) != null) {
			this.alignment = Alignments.convert(this.properties.get(Text.ALIGNMENT).getValue());
		} else {
			this.alignment = Alignments.LEFT;
		}

		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		// params.leftMargin = layoutParameters.left;
		// params.topMargin = layoutParameters.left;
		this.relativeLayout = new RelativeLayout(this.canvas.getActivity());
		this.relativeLayout.setLayoutParams(params);
		this.relativeLayout.setRotation((float) this.layoutParameters.rotation);
		this.relativeLayout.setX(this.layoutParameters.left);
		this.relativeLayout.setY(this.layoutParameters.top);
		this.textTextView = new android.widget.TextView(this.canvas.getActivity());
		this.textTextView.setTextColor(this.color);
		this.textTextView.setTextSize(this.size.intValue());

		switch (this.alignment) {
		case CENTER:
			this.textTextView.setGravity(Gravity.CENTER_HORIZONTAL);
			this.relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
			break;
		case LEFT:
			this.textTextView.setGravity(Gravity.LEFT);
			this.relativeLayout.setGravity(Gravity.LEFT);
			break;
		case RIGHT:
			this.textTextView.setGravity(Gravity.RIGHT);
			this.relativeLayout.setGravity(Gravity.RIGHT);
			break;
		}
		this.relativeLayout.addView(this.textTextView);
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
		this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
		if (bindingProperties != null) {
			this.binding.addViewListener(this);
		} else {
			// I can be my own binding!
			this.refreshView();
		}
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		if (bindingProperties != null) {
			try {
				AbstractBinding binding = new ValueBinding(stageItemId, bindingProperties);
			} catch (JSONException e) {
			} catch (InvalidParameterException ex) {
			}
		}
	}

	public void setValue(String newValue) {
		this.textTextView.setText(newValue);
	}

	@Override
	public void refreshView() {
		if (this.binding != null) {
			this.setValue(((ValueBinding) this.binding).getValue());
		} else {
			this.setValue(this.content);
		}
	}

	@Override
	public void destroyView() {
	}
}
