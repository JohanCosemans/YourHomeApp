package net.yourhome.app.views;

import java.security.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.common.base.enums.Alignments;
import net.yourhome.common.net.model.viewproperties.Text;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

public class TextView extends DynamicView {

	protected RelativeLayout relativeLayout;
	protected android.widget.TextView textTextView; 

	private int color;
	private Double size;
	private String content;
	private Alignments alignment;
	
	public TextView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageElementId,viewProperties,bindingProperties);
		buildView(viewProperties);
		addBinding(bindingProperties);
		refreshView();
	}
	
	@Override
	public View getView() {
		return relativeLayout;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Properties
		color = Color.parseColor(this.properties.get(Text.COLOR).getValue());
		size = (double) (Configuration.getInstance().convertPixtoDip(canvas.getActivity(), Double.parseDouble(this.properties.get(Text.SIZE).getValue())) * relativeWidthFactor);
		content = this.properties.get(Text.CONTENT).getValue();
		if(this.properties.get(Text.ALIGNMENT) != null) {
			alignment = Alignments.convert(this.properties.get(Text.ALIGNMENT).getValue());
		}else {
			alignment = Alignments.LEFT;
		}
		
		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//params.leftMargin = layoutParameters.left;
		//params.topMargin = layoutParameters.left;
		relativeLayout = new RelativeLayout(canvas.getActivity());
		relativeLayout.setLayoutParams(params);
		relativeLayout.setRotation((float)layoutParameters.rotation);
		relativeLayout.setX(layoutParameters.left);
		relativeLayout.setY(layoutParameters.top);
		textTextView = new android.widget.TextView(canvas.getActivity());
		textTextView.setTextColor(color);
		textTextView.setTextSize(size.intValue());
		
		switch(this.alignment) {
			case CENTER:
				textTextView.setGravity(Gravity.CENTER_HORIZONTAL);
				relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
				break;
			case LEFT:
				textTextView.setGravity(Gravity.LEFT);
				relativeLayout.setGravity(Gravity.LEFT);
				break;
			case RIGHT:
				textTextView.setGravity(Gravity.RIGHT);
				relativeLayout.setGravity(Gravity.RIGHT);
				break;
		}
		relativeLayout.addView(textTextView);
	}
	@Override
	public void addBinding(JSONObject bindingProperties) {
        this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
        if(bindingProperties != null) {
            binding.addViewListener(this);
        }else {
            // I can be my own binding!
            refreshView();
        }
	}
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        if(bindingProperties != null) {
            try {
                AbstractBinding binding = new ValueBinding(stageItemId, bindingProperties);
            } catch (JSONException e) {
            } catch (InvalidParameterException ex) {
            }
        }
    }

	public void setValue(String newValue) {
		textTextView.setText(newValue);
	}
	@Override
	public void refreshView() {
		if(binding != null) {
			setValue(((ValueBinding)binding).getValue());
		}else {
            setValue(content);
        }
	}
	
	@Override
	public void destroyView() {
	}
}
