package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.model.viewproperties.ClockDigital;
import net.yourhome.common.net.model.viewproperties.PlusMinValue;
import net.yourhome.app.R;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlusMinView extends DynamicView {
	
	protected RelativeLayout relativeLayout;
	private TextView valueTextView;
	//private TextView valueTextViewDecimals;
	protected ImageButton left;
	protected ImageButton right;
	protected PlusMinView me = this;
	//private String TAG = "PlusMinView";
	
	private Double step;
	private int color;
	private Double size;
	
	private ValueBinding valueBinding;

    @Override
    public void destroyView() {
        super.destroyView();
        relativeLayout = null;
        valueTextView = null;
        left = null;
        right = null;
        me = null;
    }
	public PlusMinView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
		this.canvas = canvas;
		buildView(viewProperties);
		addBinding(bindingProperties);
	}
	
	private void setValue(String newValue) {
        if(newValue != null) {
            String[] splittedValue = newValue.replace(",", ".").split("\\.");
            String textValue = "";
            if (splittedValue.length > 0) {
                textValue = splittedValue[0];
            }
            if (splittedValue.length > 1) {
                //valueTextViewDecimals.setText();
                Spannable span = new SpannableString(textValue + new String("" + Math.round(Double.parseDouble(splittedValue[1]))).charAt(0));
                span.setSpan(new RelativeSizeSpan(0.3f), textValue.length(), span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                valueTextView.setText(span);
            }
        }
	}
	
	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Properties
		step = Double.parseDouble(this.properties.get(PlusMinValue.STEP).getValue());
		color = Color.parseColor(this.properties.get(PlusMinValue.COLOR).getValue());
		size = Double.parseDouble(this.properties.get(ClockDigital.SIZE).getValue());
		
		int textSize = (int) (Configuration.getInstance().convertPixtoDip(canvas.getActivity(), size.intValue()) * relativeWidthFactor);

		
		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		params.leftMargin = layoutParameters.left;
		params.topMargin = layoutParameters.top;
		relativeLayout = new RelativeLayout(canvas.getActivity());
		relativeLayout.setLayoutParams(params);
		relativeLayout.setRotation((float)layoutParameters.rotation);
		relativeLayout.setGravity(Gravity.CENTER);
		LayoutInflater inflater = canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_plus_min, relativeLayout);
		valueTextView = (TextView) relativeLayout.findViewById(R.id.view_plus_min_txt);
		valueTextView.setTextColor(color);
		valueTextView.setTextSize(textSize);
		
		left = (ImageButton) relativeLayout.findViewById(R.id.view_plus_min_left_btn);
		left.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						left.setAlpha((float)0.5);
						break;
					}case MotionEvent.ACTION_UP: {
						left.setAlpha((float)1);
						break;
					}case MotionEvent.ACTION_CANCEL: {
						left.setAlpha((float)1);
						break;
					}
				}
				return false;
			}
		});
		left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Let binding handle the action
				if(binding != null) {
					UIEvent event = new UIEvent(Types.SET_VALUE);
					Double currentValue = Double.parseDouble(valueBinding.getValue());
					Double newValue = currentValue-step;
            		event.setProperty("VALUE", newValue); //- step value
					binding.viewPressed(me,event);
					setValue(newValue+"");
				}
			}
		});
		
		right = (ImageButton) relativeLayout.findViewById(R.id.view_plus_min_right_btn);
		right.setBackground(Configuration.getInstance().getAppIconDrawable(canvas.getActivity(), R.string.icon_plus4, textSize/2, color));
		left.setBackground(Configuration.getInstance().getAppIconDrawable(canvas.getActivity(), R.string.icon_minus3, textSize/2, color));
		
		
		right.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						right.setAlpha((float)0.5);
						break;
					}case MotionEvent.ACTION_UP: {
						right.setAlpha((float)1);
						break;
					}case MotionEvent.ACTION_CANCEL: {
						right.setAlpha((float)1);
						break;
					}
				}
				return false;
			}
		});
		right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Let binding handle the action
				if(binding != null) {
					UIEvent event = new UIEvent(Types.SET_VALUE);
					Double currentValue = Double.parseDouble(valueBinding.getValue());
					Double newValue = currentValue+step;
            		event.setProperty("VALUE", newValue); //+ step value
					binding.viewPressed(me,event);
					setValue(newValue+"");
				}
			}
		});
		
		relativeLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(binding != null) {
					binding.viewLongPressed(me, new UIEvent(Types.EMPTY));
					
					Intent intent2 = new Intent(canvas.getActivity(),DataHistoryActivity.class);
					Bundle b2 = new Bundle();
					b2.putString("nodeIdentifier", binding.getControlIdentifier().getNodeIdentifier());
					b2.putString("valueIdentifier", binding.getControlIdentifier().getValueIdentifier());
					b2.putString("controllerIdentifier", binding.getControlIdentifier().getControllerIdentifier().convert());
					intent2.putExtras(b2);
					canvas.getActivity().startActivity(intent2);
				}
			}
		});
	}
    @Override
    public void addBinding(JSONObject bindingProperties) {
        this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
        if(this.binding != null) {
            valueBinding = (ValueBinding)binding;
            binding.addViewListener(this);
        }
        // Binding
        try {
            ValueTypes bindingType = ValueTypes.convert(bindingProperties.getString("valueType"));
            switch(bindingType) {
                case SENSOR_TEMPERATURE:
                    this.left.setVisibility(View.GONE);
                    this.right.setVisibility(View.GONE);
                    this.valueTextView.setTypeface(Configuration.getInstance().getAppIconFont(HomeServerConnector.getInstance().getMainContext()), Typeface.BOLD);
                    break;
            }
        } catch (Exception e) {
            Log.e("SensorView", "Could not create sensorbinding from "+bindingProperties);
        }
    }
    public static void createBinding(String stageElementId, JSONObject bindingProperties) {
        try {
            new ValueBinding(stageElementId, bindingProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void refreshView() {
        if(valueBinding != null) {
            this.setValue(valueBinding.getValue());
        }
	}
}
