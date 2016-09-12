package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.MusicPlayerBinding;
import net.yourhome.app.bindings.SensorBinding;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.model.viewproperties.Property;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.ActivationBinding;
import net.yourhome.app.bindings.RadioBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.security.InvalidParameterException;


public class ButtonView extends DynamicView {
	
	protected RelativeLayout layout;
	protected ImageButton button;
	protected RelativeLayout.LayoutParams params;
	//protected AbstractBinding binding;

	public ButtonView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
		buildView(viewProperties);
		addBinding(bindingProperties);
		//refreshView();
	}
	public void setBackgroundColor(int c) {
		//this.button.setBackgroundColor(c);
        PaintDrawable mDrawable = new PaintDrawable();
        mDrawable.getPaint().setColor(c);
        button.setBackground(mDrawable);
	}
	public RelativeLayout.LayoutParams getLayoutParams() {
		return params;
	}
		
	// Set silent button listener
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
                // Let binding handle the action
                if (binding != null) {
                    binding.viewPressed(me, new UIEvent(Types.EMPTY));
                }
            }

        });
	}
	
	// Set button listener with on click action
	public void setOnClickListener(final Activity listenerContext, final Class activityToStart) {
		button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b2 = new Bundle();
                b2.putString("nodeIdentifier", binding.getControlIdentifier().getNodeIdentifier());
                b2.putString("valueIdentifier", binding.getControlIdentifier().getValueIdentifier());
                b2.putString("controllerIdentifier", binding.getControlIdentifier().getControllerIdentifier().convert());

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
                        button.setAlpha((float) 0.5);

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        button.setAlpha((float) 1);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL: {
                        button.setAlpha((float) 1);
                        break;
                    }
                }
                return false;
            }
        });
	}
	
	@Override
	public View getView() {
		//return this.button;
		return this.layout;
	}
	
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Layout
		RelativeLayout subLayout = new RelativeLayout(canvas.getActivity());
		RelativeLayout.LayoutParams subLayoutParams = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);

		button = new ImageButton(canvas.getActivity());
		layout = new RelativeLayout(canvas.getActivity());
		layout.addView(button);
		params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		button.setLayoutParams(buttonParams);		
		layout.setLayoutParams(params);		 
		layout.setRotation(layoutParameters.rotation);
		setListener();
		
		layout.setX(layoutParameters.left);
		layout.setY(layoutParameters.top);
	}

    @Override
    public void addBinding(JSONObject bindingProperties) {
        if(bindingProperties != null) {
            try {
                ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
                switch (valueType) {
                    //case MUSIC_PLAYLISTS:
                    //case MUSIC_ACTION:
                    /*    try {
                            String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
                            binding = BindingController.getInstance().getBindingFor(controllerIdentifier);
                            binding.addViewListener(this);
                        } catch (JSONException e) {
                        }
                        break;*/
                    default:
                        this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
                        if (this.binding != null) {
                            binding.addViewListener(this);
                        }
                        break;
                }
            } catch (JSONException e1) {
            }
        } else {
            // I can be my own binding!
            refreshView();
            this.button.setOnClickListener(null);
            this.button.setOnTouchListener(null);
        }
    }
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        try {

            try {
                ValueTypes valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
                switch(valueType) {
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
        if(this.properties != null) {
            Property imageSource = this.properties.get("imageSrc");
            if (imageSource != null) {
                Drawable bitmapDrawable = new BitmapDrawable(button.getResources(), Configuration.getInstance().loadBitmap(imageSource.getValue()));
                button.setBackground(bitmapDrawable);
            }
        }
	}

    public void destroyView() {
        super.destroyView();
        button.setBackground(null);
        button = null;
        layout = null;
        params = null;
    }


}
