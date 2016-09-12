package net.yourhome.app.views.musicplayer.views;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.MusicPlayerBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.views.DynamicView;

public abstract class MusicPlayerView extends DynamicView {

	protected MusicPlayerBinding musicPlayerBinding;
	
	public MusicPlayerView(CanvasFragment canvas, String  stageElementId,JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
        try {
            String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
            binding = BindingController.getInstance().getBindingFor(controllerIdentifier);
            musicPlayerBinding = (MusicPlayerBinding)binding;
            binding.addViewListener(this);
        } catch (JSONException e) {}
	}

    public static void createBinding(String stageElementId, JSONObject bindingProperties) {
        // Music player views have only one binding per music player controller.
        if(bindingProperties != null) {
            try {
                // Check if a music player controller was created already
                String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
                AbstractBinding existingBinding = BindingController.getInstance().getBindingFor(controllerIdentifier);
                if(existingBinding == null) {
                    new MusicPlayerBinding(controllerIdentifier, bindingProperties);
                }
            } catch (JSONException e) {}

            /*
            String controllerIdentifier;
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
                        binding = new MusicPlayerBinding(stageElementId, bindingProperties);
                    }
                    binding.addViewListener(this);
                }else {
                    binding = new MusicPlayerBinding(stageElementId, bindingProperties);
                }
*/
        }
    }

}
