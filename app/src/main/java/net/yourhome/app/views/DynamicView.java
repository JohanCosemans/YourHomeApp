package net.yourhome.app.views;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.model.viewproperties.Property;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.canvas.CanvasFragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

public abstract class DynamicView {
	
	public DynamicView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
        this.stageElementId = stageElementId;
		this.canvas = canvas;
		initialize();
	}
	protected String stageElementId;
	protected DynamicView me = this;
	protected CanvasFragment canvas; 
	protected Map<String,Property> properties = new HashMap<String,Property>();
	protected ViewLayoutParams layoutParameters;
	protected ProgressBar loader;
	protected AbstractBinding binding;
	protected float relativeWidthFactor;
	protected float relativeHeightFactor;
	
	protected Double width;
	protected Double height;
	protected Double left;
	protected Double top;
	
	public abstract View getView();
	public abstract void refreshView();
	public void destroyView() {
        if(binding != null) {
            binding.removeViewListener(this);
        }
		properties.clear();
		properties = null;
		binding=null;
		canvas=null;
		layoutParameters=null;
        me = null;
	}
	public void buildView(JSONObject viewProperties) throws JSONException {
		// Read settings for this view
		JSONArray propertiesArray = viewProperties.getJSONArray("properties");
		for(int i=0;i<propertiesArray.length();i++) {
			Property p = new Property(propertiesArray.getJSONObject(i));
			properties.put(p.getKey(), p);
		}
		
		/* Parse layout parameters */
		// Get current width,height
		WindowManager wm = (WindowManager) canvas.getActivity().getSystemService(Context.WINDOW_SERVICE);
		
		Point currentCanvasSize = new Point();
		Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(currentCanvasSize);
        }else {
            display.getSize(currentCanvasSize);
        }

		// Get original width,height
		Point originalCanvasSize = canvas.getRelativeScreenSize();



/*		switch(Configuration.getInstance().getOrientation()) {
		case landscape:
			relativeWidthFactor = (float)currentCanvasSize.x / (float)originalCanvasSize.x;
			relativeHeightFactor = (float)currentCanvasSize.y / (float)originalCanvasSize.y;
			break;
		case portrait:
			relativeHeightFactor = (float)currentCanvasSize.x / (float)originalCanvasSize.x;
			relativeWidthFactor = (float)currentCanvasSize.y / (float)originalCanvasSize.y;
			break;
		}*/

		relativeWidthFactor = (float)currentCanvasSize.x / (float)originalCanvasSize.x;
		relativeHeightFactor = (float)currentCanvasSize.y / (float)originalCanvasSize.y;
		layoutParameters = new ViewLayoutParams();
		width = 	viewProperties.getDouble("width");
		height =	viewProperties.getDouble("height");
		left = 	viewProperties.getDouble("left");
		top = 	viewProperties.getDouble("top");
		layoutParameters.width = (int) (width!=null?Math.round(width * relativeWidthFactor):0);
		layoutParameters.height = (int) (width!=null?Math.round(height * relativeHeightFactor):0);
		layoutParameters.top = (int) (width!=null?Math.round(top * relativeHeightFactor):0);
		layoutParameters.left = (int) (width!=null?Math.round(left * relativeWidthFactor):0);
		layoutParameters.rotation = (float)viewProperties.getDouble("rotation");
	};
	public abstract void addBinding(JSONObject bindingProperties);
	protected void initialize() {};
	public void setLoaderState(final int state) {
		if(this.loader != null && this.canvas != null) {
			canvas.getActivity().runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	        		loader.setVisibility(state);
	            }
			});
		}
	}

	public String getStageElementId() {
		return stageElementId;
	}

	public void setAlpha(float alpha) {
        if(getView() != null) {
            getView().setAlpha(alpha);
        }
	}
	
	public class ViewLayoutParams {
		public int top;
		public int left;
		public int width;
		public int height;
		public float rotation;
	}
}
