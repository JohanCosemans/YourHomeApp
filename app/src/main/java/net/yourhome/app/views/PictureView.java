package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;


import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import net.yourhome.common.net.model.viewproperties.Property;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.*;

public class PictureView extends DynamicView {

	private ImageView image;
	private RelativeLayout.LayoutParams params;
	
	public PictureView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
		image = new ImageView(this.canvas.getActivity());
		buildView(viewProperties);
		addBinding(bindingProperties);
	}
	
	@Override
	public View getView() {
		return image;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		// Layout
		params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//params.leftMargin = layoutParameters.left;
		//params.topMargin = layoutParameters.top;
		image.setLayoutParams(params);
		image.setRotation((float)layoutParameters.rotation);
		image.setX(layoutParameters.left);
		image.setY(layoutParameters.top);
		
		// Properties
		Property imageSource = this.properties.get("imageSrc");
		Drawable bitmapDrawable = new BitmapDrawable(image.getResources(),Configuration.getInstance().loadBitmap(imageSource.getValue()));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			image.setBackground(bitmapDrawable);
		}else {
			image.setBackgroundDrawable(bitmapDrawable);
		}
	}
	@Override
	public void addBinding(JSONObject bindingProperties) {
		
	}
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        // Do nothing
    }
	@Override
	public void refreshView() {
		// TODO Auto-generated method stub
		
	}

}
