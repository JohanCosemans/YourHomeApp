package net.yourhome.app.views;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.IPCameraBinding;
import net.yourhome.common.base.enums.PropertyTypes;
import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.model.viewproperties.Property;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.MusicPlayerBinding;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;


public class MultiStateButtonView extends ButtonView {
	
	protected List<Property> states;
	protected int currentStateIndex;
	
	public MultiStateButtonView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId, viewProperties, bindingProperties);
	}
    @Override
    public void destroyView() {
        super.destroyView();
        states = null;
    }
	@Override
	public void initialize() {
		states = new ArrayList<Property>();
		currentStateIndex = 0;
	}
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		// Layout
		//params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//params.leftMargin = layoutParameters.left;
		//params.topMargin = layoutParameters.top;
		//button.setLayoutParams(buttonParams);		
		//layout.setLayoutParams(params);		 
		//layout.setRotation(layoutParameters.rotation);
		
		// Properties
			// Read states (in order!)
		for(Property p : this.properties.values()) {
			if(p.getType().equals(PropertyTypes.IMAGE_STATE.convert())) {
				this.states.add(p);
			}
		}
	}
	private int getIndexByDescription(Object key) {
		int i=0;
		for(Property p : this.states) {
			if(p.getDescription().equals(key)) {
				return i;
			}
			i++;
		}
		return 0;
	}
	@Override
	public void refreshView() {
		/* Find state that is closest to the current binding value */
		if(binding != null && binding instanceof ValueBinding) {

				String valueStr = ((ValueBinding) binding).getValue();
                if(valueStr != null) {
                    try {

                        double value = Double.valueOf(valueStr);
                        // Get status from value
                        boolean valueAsExistingStatusFound = false;
                        double closestDistance = Integer.MAX_VALUE;
                        int i=0;
                        int closestStatusIndex = i;

                        while(!valueAsExistingStatusFound && i<states.size()) {
                            Property next = states.get(i);
                            double nextValue = Double.parseDouble(next.getDescription());
                            if( nextValue == value) {
                                valueAsExistingStatusFound = true;
                                closestStatusIndex = i;
                            }

                            // Calculate distance between new & current value
                            double distance = Math.abs(value-nextValue);
                            if(nextValue != 0 && distance < closestDistance) {
                                closestStatusIndex = i;
                                closestDistance = distance;
                            }
                            i++;
                        }
                        this.currentStateIndex = closestStatusIndex;
                    }catch(NumberFormatException e) {
                        // Fallback - check only the keys
                        this.currentStateIndex = getIndexByDescription(valueStr);
                    }
                }

		}else if(binding != null && binding instanceof MusicPlayerBinding) {
			this.currentStateIndex = getIndexByDescription(((MusicPlayerBinding)binding).getState(this));
		}
		
		// update view with the new state index
		Property currentState = this.states.get(currentStateIndex);
		Bitmap currentStateImage = Configuration.getInstance().loadBitmap(currentState.getValue());
		Drawable bitmapDrawable = new BitmapDrawable(button.getResources(),currentStateImage);
        button.setBackground(bitmapDrawable);
	}
    public void addBinding(JSONObject bindingProperties) {
        try {
            ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
            switch(valueType) {
                case MUSIC_PLAY_PAUSE:
                case MUSIC_RANDOM:
                    String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
                    AbstractBinding musicBinding = BindingController.getInstance().getBindingFor(controllerIdentifier);
                    if(musicBinding != null && musicBinding instanceof MusicPlayerBinding) {
                        binding = ((MusicPlayerBinding) musicBinding).addStateButtonListener(this, bindingProperties);
                        binding.addViewListener(this);
                    }
                    break;
                default:
                    this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
                    this.binding.addViewListener(this);
            }
        } catch (JSONException e) {}
    }

    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        if(bindingProperties != null) {
            try {
                ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
                switch(valueType) {
                    case MUSIC_PLAY_PAUSE:
                    case MUSIC_RANDOM:
                            try {
                                // Check if a music player controller was created already
                                String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
                                AbstractBinding existingBinding = BindingController.getInstance().getBindingFor(controllerIdentifier);
                                if(existingBinding == null) {
                                    new MusicPlayerBinding(controllerIdentifier, bindingProperties);
                                }
                            } catch (JSONException e) {}

/*                            String controllerIdentifier;
                            try {
                                controllerIdentifier = bindingProperties.getString("controllerIdentifier");
                                List<AbstractBinding> bindings = BindingController.getInstance().getBindingsFor(controllerIdentifier);
                                if(bindings != null && bindings.size() > 0) {
                                    int i=0; boolean found = false;
                                    while(!found & i<bindings.size()) {
                                        if(bindings.get(i) instanceof MusicPlayerBinding) {
                                            found = true;
                                        }else { i++; }
                                    }
                                    if(found) {
                                        //(MusicPlayerBinding) bindings.get(i);
                                    }else {
                                        new MusicPlayerBinding(stageItemId, bindingProperties);
                                    }
                                }else {
                                    new MusicPlayerBinding(stageItemId, bindingProperties)
                                            .addStateButtonListener(stageItemId,bindingProperties);
                                }
                            } catch (JSONException e) {
                            }*/

                        break;
                    default:
                        new ValueBinding(stageItemId, bindingProperties);
                        // Default value of first state
                        //((ValueBinding)binding).setValue(states.get(currentStateIndex).getDescription());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }catch(InvalidParameterException e) {
                e.printStackTrace();
            }
        }
    }/*
	@Override
	public void addBinding(JSONObject bindingProperties) {
		if(bindingProperties != null) {
			try {
				ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
				switch(valueType) {
					case MUSIC_PLAY_PAUSE:
					case MUSIC_RANDOM:
						if(bindingProperties != null) {
							String controllerIdentifier;
							try {
								controllerIdentifier = bindingProperties.getString("controllerIdentifier");
								List<AbstractBinding> bindings = BindingController.getInstance().getBindingsFor(controllerIdentifier);
								if(bindings != null && bindings.size() > 0) {
									int i=0; boolean found = false;
									while(!found & i<bindings.size()) {
										if(bindings.get(i) instanceof MusicPlayerBinding) {
											found = true;
										}else { i++; }
									}
									if(found) {
										binding = (MusicPlayerBinding) bindings.get(i);
									}else {
										binding = new MusicPlayerBinding(bindingId, bindingProperties);
									}
								}else {
									binding = new MusicPlayerBinding(this, bindingProperties);
								}
								((MusicPlayerBinding) binding).addStateButtonListener(this,bindingProperties);
							} catch (JSONException e) {
							}
						}
						refreshView();
						break;
					default:
						binding = new ValueBinding(this, bindingProperties);
						// Default value of first state
						((ValueBinding)binding).setValue(states.get(currentStateIndex).getDescription());	
						break;
					}
			} catch (JSONException e) {
				e.printStackTrace();
			}catch(InvalidParameterException e) {
				e.printStackTrace();
			}	
		}
	}*/

	@Override
	public void setListener() {
		
		button.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {
				if(binding != null) {
					binding.viewLongPressed(me, new UIEvent(Types.EMPTY));
				}
				return false;
			}
		});

		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						button.setAlpha((float)0.5);
						
						break;
					}case MotionEvent.ACTION_UP: {
						button.setAlpha((float)1);
						
						break;
					}case MotionEvent.ACTION_CANCEL: {
						button.setAlpha((float)1);
						break;
					}
				}
				return false;
			}
		});
		
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Proceed to next state
				currentStateIndex = currentStateIndex==0 && states.size()>0?states.size()-1:currentStateIndex-1;
				Property nextState = states.get(currentStateIndex);
				
				// Let binding handle the action
				UIEvent event = new UIEvent(Types.SET_VALUE);
				event.setProperty("VALUE", nextState.getDescription());

				if(binding != null) {
					binding.viewPressed(me,event);
					refreshView();
				}
			}
			
		});
	}

}
