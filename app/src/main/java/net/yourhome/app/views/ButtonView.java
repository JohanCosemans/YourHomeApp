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

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import net.yourhome.app.bindings.ActivationBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.RadioBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.model.viewproperties.Property;

public class ButtonView extends DynamicView {

	protected RelativeLayout layout;
	protected ImageButton button;
	protected RelativeLayout.LayoutParams params;
	// protected AbstractBinding binding;

	public ButtonView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
		// refreshView();
	}

	public void setBackgroundColor(int c) {
		// this.button.setBackgroundColor(c);
		PaintDrawable mDrawable = new PaintDrawable();
		mDrawable.getPaint().setColor(c);
		this.button.setBackground(mDrawable);
	}

	public RelativeLayout.LayoutParams getLayoutParams() {
		return this.params;
	}

	// Set silent button listener
	public void setListener() {
		this.button.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (ButtonView.this.binding != null) {
					ButtonView.this.binding.viewLongPressed(ButtonView.this.me, new UIEvent(Types.EMPTY));
				}
				return false;
			}
		});

		this.button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					ButtonView.this.button.setAlpha((float) 0.5);

					break;
				}
				case MotionEvent.ACTION_UP: {
					ButtonView.this.button.setAlpha((float) 1);

					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					ButtonView.this.button.setAlpha((float) 1);
					break;
				}
				}
				return false;
			}
		});

		this.button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Let binding handle the action
				if (ButtonView.this.binding != null) {
					ButtonView.this.binding.viewPressed(ButtonView.this.me, new UIEvent(Types.EMPTY));
				}
			}

		});
	}

	// Set button listener with on click action
	public void setOnClickListener(final Activity listenerContext, final Class activityToStart) {
		this.button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle b2 = new Bundle();
				b2.putString("nodeIdentifier", ButtonView.this.binding.getControlIdentifier().getNodeIdentifier());
				b2.putString("valueIdentifier", ButtonView.this.binding.getControlIdentifier().getValueIdentifier());
				b2.putString("controllerIdentifier", ButtonView.this.binding.getControlIdentifier().getControllerIdentifier().convert());

				// Start dialog window
				Intent intent = new Intent(listenerContext, activityToStart);
				intent.putExtras(b2);

				listenerContext.startActivity(intent);
			}
		});

		this.button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					ButtonView.this.button.setAlpha((float) 0.5);

					break;
				}
				case MotionEvent.ACTION_UP: {
					ButtonView.this.button.setAlpha((float) 1);
					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					ButtonView.this.button.setAlpha((float) 1);
					break;
				}
				}
				return false;
			}
		});
	}

	@Override
	public View getView() {
		// return this.button;
		return this.layout;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Layout
		RelativeLayout subLayout = new RelativeLayout(this.canvas.getActivity());
		RelativeLayout.LayoutParams subLayoutParams = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);

		this.button = new ImageButton(this.canvas.getActivity());
		this.layout = new RelativeLayout(this.canvas.getActivity());
		this.layout.addView(this.button);
		this.params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		this.button.setLayoutParams(buttonParams);
		this.layout.setLayoutParams(this.params);
		this.layout.setRotation(this.layoutParameters.rotation);
		this.setListener();

		this.layout.setX(this.layoutParameters.left);
		this.layout.setY(this.layoutParameters.top);
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
		if (bindingProperties != null) {
			try {
				ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
				switch (valueType) {
				// case MUSIC_PLAYLISTS:
				// case MUSIC_ACTION:
				/*
				 * try { String controllerIdentifier =
				 * bindingProperties.getString("controllerIdentifier"); binding
				 * = BindingController.getInstance().getBindingFor(
				 * controllerIdentifier); binding.addViewListener(this); } catch
				 * (JSONException e) { } break;
				 */
				default:
					this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
					if (this.binding != null) {
						this.binding.addViewListener(this);
					}
					break;
				}
			} catch (JSONException e1) {
			}
		} else {
			// I can be my own binding!
			this.refreshView();
			this.button.setOnClickListener(null);
			this.button.setOnTouchListener(null);
		}
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		try {

			try {
				ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
				switch (valueType) {
				case RADIO_STATION:
					new RadioBinding(stageItemId, bindingProperties);
					break;
				default:
					new ActivationBinding(stageItemId, bindingProperties);
					break;
				}
			} catch (JSONException e1) {
				new ActivationBinding(stageItemId, bindingProperties);
			}
		} catch (Exception e) {
			// I can be my own binding!
		}
	}

	@Override
	public void refreshView() {
		// Properties
		if (this.properties != null) {
			Property imageSource = this.properties.get("imageSrc");
			if (imageSource != null) {
				Drawable bitmapDrawable = new BitmapDrawable(this.button.getResources(), Configuration.getInstance().loadBitmap(imageSource.getValue()));
				this.button.setBackground(bitmapDrawable);
			}
		}
	}

	@Override
	public void destroyView() {
		super.destroyView();
		this.button.setBackground(null);
		this.button = null;
		this.layout = null;
		this.params = null;
	}

}
