package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.model.viewproperties.Shape;
import net.yourhome.app.canvas.CanvasFragment;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;

public class ShapeView extends ButtonView {
	
	//private RelativeLayout layout;
	//private RelativeLayout.LayoutParams params;
	private int color;
	private float cornerRadius;

	
	public ShapeView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
	}
	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		// Properties
		color = Color.parseColor(this.properties.get(Shape.COLOR).getValue());
		cornerRadius = Float.valueOf(this.properties.get(Shape.CORNER_RADIUS).getValue());
		
        PaintDrawable mDrawable = new PaintDrawable();
        mDrawable.getPaint().setColor(color);
        mDrawable.setCornerRadius(cornerRadius);
        button.setBackground(mDrawable);
	}
	@Override
	public void refreshView() {
	}

        
}
