package net.yourhome.app.views;


import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.common.net.model.viewproperties.WebStaticHtml;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;


public class WebStaticHtmlView extends WebsiteView {

	private String content;
    public WebStaticHtmlView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
        super(canvas,stageItemId, viewProperties, bindingProperties);
    }

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Properties
		content = this.properties.get(WebStaticHtml.CONTENT).getValue();
        setValue(content);
	}
    @Override public void onRefresh() {
       swipeLayout.setRefreshing(false);
    }
    public void setValue(String newValue) {
       webView.loadData(newValue, "text/html", "UTF-8");
	}
	@Override
	public void refreshView() {
		if(binding != null) {
			setValue(((ValueBinding)binding).getValue());
		}
	}

    @Override
    public void addBinding(JSONObject bindingProperties) {
        this.binding = BindingController.getInstance().getBindingFor(getStageElementId());
    }

    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        // Do nothing
        try {
            if(bindingProperties != null) {
                new ValueBinding(stageItemId, bindingProperties);
            }
		}catch (JSONException e) {
		}catch(InvalidParameterException ex) {
        }
    }
}
