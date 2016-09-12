package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.SensorBinding;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.model.viewproperties.Property;
import net.yourhome.common.net.model.viewproperties.Sensor;
import net.yourhome.common.net.model.viewproperties.SensorWithIndicator;
import net.yourhome.common.net.model.viewproperties.Text;
import net.yourhome.app.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.security.InvalidParameterException;

public class SensorView extends DynamicView {
	
	protected RelativeLayout relativeLayout;
	private Button sensorText;
	protected ImageView icon;
	protected SensorView me = this;
	//private String TAG = "SensorView";
	private int color;
	
	//Properties
	private String downImage = null;
	private String upImage = null;
	private String neutralImage = null;
	private Double size;
	
	//UI Helpers
	private Double lastValue;
	
	protected SensorBinding sensorBinding;

    @Override
    public void destroyView() {
        super.destroyView();

        relativeLayout = null;
        sensorText = null;
        icon = null;
        me = null;
        downImage = null;
        upImage = null;
        neutralImage = null;
        size = null;
        lastValue = null;
        sensorBinding = null;
    }
	public SensorView(CanvasFragment canvas, String stageItemId,JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		buildView(viewProperties);
		addBinding(bindingProperties);
	}
	
	public void setOnClickListener(final Activity activity) {
		OnTouchListener onTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {		
				Log.d("SensorView","Button event: " + event.getAction());
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						me.icon.setAlpha((float)0.5);
						me.sensorText.setAlpha((float)0.5);
						break;
					}
					case MotionEvent.ACTION_CANCEL: {
						me.icon.setAlpha((float)1);
						me.sensorText.setAlpha((float)1);
						break;
					}
					case MotionEvent.ACTION_UP: {
						Intent intent2 = new Intent(activity,DataHistoryActivity.class);
						
						Bundle b2 = new Bundle();
						b2.putString("nodeIdentifier", binding.getControlIdentifier().getNodeIdentifier());
						b2.putString("valueIdentifier", binding.getControlIdentifier().getValueIdentifier());
						b2.putString("controllerIdentifier", binding.getControlIdentifier().getControllerIdentifier().convert());
						intent2.putExtras(b2);
						activity.startActivity(intent2);
						
						me.icon.setAlpha((float)1);
						me.sensorText.setAlpha((float)1);
						
					}
				}
				return true;
			}
		};
		this.icon.setOnTouchListener(onTouchListener);
		this.sensorText.setOnTouchListener(onTouchListener);
	}
	
	private void setValue(double newValue, String unit) {
		lastValue = newValue;
		sensorText.setText(Double.toString(newValue).replace(".", ",") + " " + unit);
	}
	
	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		Property downImageProperty = properties.get(SensorWithIndicator.DOWN_IMAGE);
		Property upImageProperty = properties.get(SensorWithIndicator.UP_IMAGE);
		Property neutralImageProperty = properties.get(SensorWithIndicator.NEUTRAL_IMAGE);
		color = Color.parseColor(this.properties.get(Sensor.COLOR).getValue());
		size = (double) (Configuration.getInstance().convertPixtoDip(canvas.getActivity(), Double.parseDouble(this.properties.get(Text.SIZE).getValue())) * relativeWidthFactor);

		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//params.leftMargin = layoutParameters.left;
		//params.topMargin = layoutParameters.top;
		relativeLayout = new RelativeLayout(canvas.getActivity());
		relativeLayout.setLayoutParams(params);
		relativeLayout.setRotation((float)layoutParameters.rotation);
		relativeLayout.setGravity(Gravity.CENTER);
		relativeLayout.setX(layoutParameters.left);
		relativeLayout.setY(layoutParameters.top);
		//sensorText = new Button(canvas.getActivity());
		
		LayoutInflater inflater = canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_sensor, relativeLayout);
		icon = (ImageView) relativeLayout.findViewById(R.id.view_sensor_image);
		sensorText = (Button) relativeLayout.findViewById(R.id.view_sensor_txt);
		sensorText.setTextColor(color);
		sensorText.setTextSize(size.floatValue());
		
		/*sensorText.setTextColor(Color.WHITE);
		icon = new ImageView(canvas.getActivity());
		relativeLayout.setGravity(Gravity.LEFT);
		relativeLayout.addView(icon);

        icon.setId(1);
        
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.RIGHT_OF, icon.getId() );

        sensorText.setId(2);
        sensorText.setLayoutParams(lp);
        sensorText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		sensorText.setPadding(10, 0, 0, 0);
		sensorText.setBackgroundColor(color.transparent);
		sensorText.setText("?");
		relativeLayout.addView(sensorText);
		 */
		
		// Properties
		if(downImageProperty != null) {
			this.downImage = downImageProperty.getValue();
		}
		if(upImageProperty != null) {
			this.upImage = upImageProperty.getValue();
		}
		if(neutralImageProperty != null) {
			this.neutralImage = neutralImageProperty.getValue();
		}
	}
	private void setImage(Bitmap image) {
		if(image != null) {
			Drawable bitmapDrawable = new BitmapDrawable(icon.getResources(),image);
			icon.setVisibility(View.VISIBLE);
			icon.setImageDrawable(bitmapDrawable);
		}else {
			icon.setVisibility(View.INVISIBLE);
		}
	}
    @Override
    public void addBinding(JSONObject bindingProperties) {
        this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
        if(this.binding != null) {
            sensorBinding = (SensorBinding)binding;
            binding.addViewListener(this);
        }
    }
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        try {
            new SensorBinding(stageItemId, bindingProperties);
        }
        catch(JSONException e) {}
        catch(InvalidParameterException ex) {}
    }
	@Override
	public void refreshView() {
        if(sensorBinding != null) {
            String newValueString = sensorBinding.getValue();
            if (newValueString != null) {
                Double newValue = Double.parseDouble(sensorBinding.getValue());

                // Choose image
                String imageName = null;
                if (lastValue != null && lastValue > newValue) {
                    imageName = downImage;
                } else if (lastValue != null && lastValue < newValue) {
                    imageName = upImage;
                } else {
                    imageName = neutralImage;
                }
                Bitmap bd = Configuration.getInstance().loadBitmap(imageName);

                setImage(bd);
                setValue(newValue, sensorBinding.getUnit());
            }
        }
	}
}
