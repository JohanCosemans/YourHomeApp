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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.MusicPlayerBinding;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.base.enums.PropertyTypes;
import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.model.viewproperties.Property;

public class MultiStateButtonView extends ButtonView {

	protected List<Property> states;
	protected int currentStateIndex;

	public MultiStateButtonView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
	}

	@Override
	public void destroyView() {
		super.destroyView();
		this.states = null;
	}

	@Override
	public void initialize() {
		this.states = new ArrayList<Property>();
		this.currentStateIndex = 0;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		// Layout
		// params = new
		// RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		// RelativeLayout.LayoutParams buttonParams = new
		// RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		// params.leftMargin = layoutParameters.left;
		// params.topMargin = layoutParameters.top;
		// button.setLayoutParams(buttonParams);
		// layout.setLayoutParams(params);
		// layout.setRotation(layoutParameters.rotation);

		// Properties
		// Read states (in order!)
		for (Property p : this.properties.values()) {
			if (p.getType().equals(PropertyTypes.IMAGE_STATE.convert())) {
				this.states.add(p);
			}
		}
	}

	private int getIndexByDescription(Object key) {
		int i = 0;
		for (Property p : this.states) {
			if (p.getDescription().equals(key)) {
				return i;
			}
			i++;
		}
		return 0;
	}

	@Override
	public void refreshView() {
		/* Find state that is closest to the current binding value */
		if (this.binding != null && this.binding instanceof ValueBinding) {

			String valueStr = ((ValueBinding) this.binding).getValue();
			if (valueStr != null) {
				try {

					double value = Double.valueOf(valueStr);
					// Get status from value
					boolean valueAsExistingStatusFound = false;
					double closestDistance = Integer.MAX_VALUE;
					int i = 0;
					int closestStatusIndex = i;

					while (!valueAsExistingStatusFound && i < this.states.size()) {
						Property next = this.states.get(i);
						double nextValue = Double.parseDouble(next.getDescription());
						if (nextValue == value) {
							valueAsExistingStatusFound = true;
							closestStatusIndex = i;
						}

						// Calculate distance between new & current value
						double distance = Math.abs(value - nextValue);
						if (nextValue != 0 && distance < closestDistance) {
							closestStatusIndex = i;
							closestDistance = distance;
						}
						i++;
					}
					this.currentStateIndex = closestStatusIndex;
				} catch (NumberFormatException e) {
					// Fallback - check only the keys
					this.currentStateIndex = this.getIndexByDescription(valueStr);
				}
			}

		} else if (this.binding != null && this.binding instanceof MusicPlayerBinding) {
			this.currentStateIndex = this.getIndexByDescription(((MusicPlayerBinding) this.binding).getState(this));
		}

		// update view with the new state index
		Property currentState = this.states.get(this.currentStateIndex);
		Bitmap currentStateImage = Configuration.getInstance().loadBitmap(currentState.getValue());
		Drawable bitmapDrawable = new BitmapDrawable(this.button.getResources(), currentStateImage);
		this.button.setBackground(bitmapDrawable);
	}

	public void addBinding(JSONObject bindingProperties) {
		try {
			ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
			switch (valueType) {
			case MUSIC_PLAY_PAUSE:
			case MUSIC_RANDOM:
				String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
				AbstractBinding musicBinding = BindingController.getInstance().getBindingFor(controllerIdentifier);
				if (musicBinding != null && musicBinding instanceof MusicPlayerBinding) {
					this.binding = ((MusicPlayerBinding) musicBinding).addStateButtonListener(this, bindingProperties);
					this.binding.addViewListener(this);
				}
				break;
			default:
				this.binding = BindingController.getInstance().getBindingFor(this.getStageElementId());
				this.binding.addViewListener(this);
			}
		} catch (JSONException e) {
		}
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		if (bindingProperties != null) {
			try {
				ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
				switch (valueType) {
				case MUSIC_PLAY_PAUSE:
				case MUSIC_RANDOM:
					try {
						// Check if a music player controller was created
						// already
						String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
						AbstractBinding existingBinding = BindingController.getInstance().getBindingFor(controllerIdentifier);
						if (existingBinding == null) {
							new MusicPlayerBinding(controllerIdentifier, bindingProperties);
						}
					} catch (JSONException e) {
					}

					/*
					 * String controllerIdentifier; try { controllerIdentifier =
					 * bindingProperties.getString("controllerIdentifier");
					 * List<AbstractBinding> bindings =
					 * BindingController.getInstance().getBindingsFor(
					 * controllerIdentifier); if(bindings != null &&
					 * bindings.size() > 0) { int i=0; boolean found = false;
					 * while(!found & i<bindings.size()) { if(bindings.get(i)
					 * instanceof MusicPlayerBinding) { found = true; }else {
					 * i++; } } if(found) { //(MusicPlayerBinding)
					 * bindings.get(i); }else { new
					 * MusicPlayerBinding(stageItemId, bindingProperties); }
					 * }else { new MusicPlayerBinding(stageItemId,
					 * bindingProperties)
					 * .addStateButtonListener(stageItemId,bindingProperties); }
					 * } catch (JSONException e) { }
					 */

					break;
				default:
					new ValueBinding(stageItemId, bindingProperties);
					// Default value of first state
					// ((ValueBinding)binding).setValue(states.get(currentStateIndex).getDescription());
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (InvalidParameterException e) {
				e.printStackTrace();
			}
		}
	}/*
		 * @Override public void addBinding(JSONObject bindingProperties) {
		 * if(bindingProperties != null) { try { ValueTypes valueType =
		 * ValueTypes.convert(bindingProperties.getString("valueType"));
		 * switch(valueType) { case MUSIC_PLAY_PAUSE: case MUSIC_RANDOM:
		 * if(bindingProperties != null) { String controllerIdentifier; try {
		 * controllerIdentifier =
		 * bindingProperties.getString("controllerIdentifier");
		 * List<AbstractBinding> bindings =
		 * BindingController.getInstance().getBindingsFor(controllerIdentifier);
		 * if(bindings != null && bindings.size() > 0) { int i=0; boolean found
		 * = false; while(!found & i<bindings.size()) { if(bindings.get(i)
		 * instanceof MusicPlayerBinding) { found = true; }else { i++; } }
		 * if(found) { binding = (MusicPlayerBinding) bindings.get(i); }else {
		 * binding = new MusicPlayerBinding(bindingId, bindingProperties); }
		 * }else { binding = new MusicPlayerBinding(this, bindingProperties); }
		 * ((MusicPlayerBinding)
		 * binding).addStateButtonListener(this,bindingProperties); } catch
		 * (JSONException e) { } } refreshView(); break; default: binding = new
		 * ValueBinding(this, bindingProperties); // Default value of first
		 * state ((ValueBinding)binding).setValue(states.get(currentStateIndex).
		 * getDescription()); break; } } catch (JSONException e) {
		 * e.printStackTrace(); }catch(InvalidParameterException e) {
		 * e.printStackTrace(); } } }
		 */

	@Override
	public void setListener() {

		this.button.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (MultiStateButtonView.this.binding != null) {
					MultiStateButtonView.this.binding.viewLongPressed(MultiStateButtonView.this.me, new UIEvent(Types.EMPTY));
				}
				return false;
			}
		});

		this.button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					MultiStateButtonView.this.button.setAlpha((float) 0.5);

					break;
				}
				case MotionEvent.ACTION_UP: {
					MultiStateButtonView.this.button.setAlpha((float) 1);

					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					MultiStateButtonView.this.button.setAlpha((float) 1);
					break;
				}
				}
				return false;
			}
		});

		this.button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// Proceed to next state
				MultiStateButtonView.this.currentStateIndex = MultiStateButtonView.this.currentStateIndex == 0 && MultiStateButtonView.this.states.size() > 0 ? MultiStateButtonView.this.states.size() - 1 : MultiStateButtonView.this.currentStateIndex - 1;
				Property nextState = MultiStateButtonView.this.states.get(MultiStateButtonView.this.currentStateIndex);

				// Let binding handle the action
				UIEvent event = new UIEvent(Types.SET_VALUE);
				event.setProperty("VALUE", nextState.getDescription());

				if (MultiStateButtonView.this.binding != null) {
					MultiStateButtonView.this.binding.viewPressed(MultiStateButtonView.this.me, event);
					MultiStateButtonView.this.refreshView();
				}
			}

		});
	}

}
