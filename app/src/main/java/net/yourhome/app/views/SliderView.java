package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.SensorBinding;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.net.model.viewproperties.LineGraph;
import net.yourhome.app.R;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.security.InvalidParameterException;

public class SliderView extends DynamicView {
	private SeekBar seekBar;
	private RelativeLayout.LayoutParams params;
	private ValueBinding valueBinding;
	protected RelativeLayout relativeLayout;
	private SliderView me = this;
	
	// Property values
	private Double minimum;
	private Double maximum;
    @Override
    public void destroyView() {
        super.destroyView();
        seekBar = null;
        params = null;
        valueBinding = null;
        relativeLayout = null;
        me = null;
    }
	public SliderView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
		this.canvas = canvas;
		buildView(viewProperties);
		addBinding(bindingProperties);
	}
	
	@Override
	public View getView() {
		return relativeLayout;
	}
	
	public void setValue(int newValue) {
		this.seekBar.setProgress((int)(newValue-minimum));
	}
	
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Properties		
        this.minimum = Double.valueOf(properties.get(LineGraph.MINIMUM).getValue().toString());
		this.maximum = Double.valueOf(properties.get(LineGraph.MAXIMUM).getValue().toString());
		
	    Drawable thumbDrawable = this.canvas.getActivity().getResources().getDrawable(R.drawable.slider_thumb);
	    Drawable scaledThumbDrawable = Configuration.getInstance().scaleImage(canvas.getActivity(),thumbDrawable, (float)Math.min(height/thumbDrawable.getIntrinsicHeight(), 1*relativeWidthFactor));

		// Layout
		/*params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		params.leftMargin = layoutParameters.left;
		params.topMargin = layoutParameters.top;
*/
		seekBar = new SeekBar(canvas.getActivity());
        seekBar.setProgress(1);
        seekBar.setVisibility(View.VISIBLE);		
        //seekBar.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        seekBar.setLayoutParams(lp);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            	if(binding != null) {
            		UIEvent event = new UIEvent(Types.SET_VALUE);
            		event.setProperty("VALUE", seekBar.getProgress()-minimum);
            		binding.viewPressed(me,event);
            	}
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onProgressChanged(SeekBar seekBar, int arg1, boolean arg2) {}
        });

	    seekBar.setProgressDrawable(this.canvas.getActivity().getResources().getDrawable(R.drawable.slider_background));
        seekBar.setThumb(scaledThumbDrawable);
        seekBar.setMax((int)(maximum-minimum));

		relativeLayout = new RelativeLayout(canvas.getActivity());
		params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//params.leftMargin = layoutParameters.left;
		//params.topMargin = layoutParameters.top;

		relativeLayout.setX(layoutParameters.left);
		relativeLayout.setY(layoutParameters.top);
		relativeLayout.setLayoutParams(params);
        relativeLayout.setRotation(layoutParameters.rotation);
		relativeLayout.addView(seekBar);
		//relativeLayout.setBackgroundColor(Color.CYAN);
        
	}
	@Override
	public void addBinding(JSONObject bindingProperties) {
        this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
        if(this.binding != null) {
            valueBinding = (ValueBinding)binding;
            binding.addViewListener(this);
        }
	}
    public static void createBinding(String stageElementId, JSONObject bindingProperties) {
        try {
            new ValueBinding(stageElementId, bindingProperties);
        } catch (JSONException e) {
        } catch (InvalidParameterException e) {
        }
    }
	@Override
	public void refreshView() {
		if(valueBinding != null) {
            String valueString = valueBinding.getValue();
            if(valueString != null) {
                setValue((int) Math.round(Double.parseDouble(valueString)));
            }
		}
	}
}
