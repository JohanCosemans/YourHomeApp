package net.yourhome.app.bindings;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.radio.RadioOnOffMessage;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;

public class RadioBinding extends ActivationBinding {
	
	private boolean isPlaying = false;
	
	public RadioBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}
	@Override
	protected void buildBinding() {
		updateViews();
		setViewsAlpha((float) 0.5);
	}
	@Override
	public void handleMessage(JSONMessage message) {
		if(message instanceof RadioOnOffMessage) {
			if(message.controlIdentifiers.equals(this.controlIdentifier) && ((RadioOnOffMessage)message).status) {
				setViewsAlpha(1);
				isPlaying = true;
			}else {
				setViewsAlpha((float)0.5);
				isPlaying = false;
			}
			
			// Also share this with the other radio bindings
			if(!((RadioOnOffMessage)message).stopSharing) {
				((RadioOnOffMessage)message).stopSharing = true; 
				List<AbstractBinding> bindings = BindingController.getInstance().getBindingsFor(this.getControlIdentifier().getControllerIdentifier().convert());
				if(bindings != null) {
					for(AbstractBinding binding : bindings) {
						if(binding != this && binding instanceof RadioBinding) {
							binding.handleMessage(message);
						}
					}
				}
			}
		}
	}
	@Override
	public void viewPressed(DynamicView v, UIEvent event) {
		// Send radio activation message to controller
		RadioOnOffMessage radioMessage = new RadioOnOffMessage();
		radioMessage.controlIdentifiers = this.getControlIdentifier();
		radioMessage.status = !isPlaying;
		BindingController.getInstance().sendMessage(radioMessage);
	}
	
	protected void setViewsAlpha(float alpha) {
		for(DynamicView v : this.viewListeners) {
			v.setAlpha(alpha);
		}
	}
    @Override
    public void addViewListener(DynamicView v) {
        this.viewListeners.add(v);
        v.refreshView();
        setViewsAlpha(isPlaying?1.0f:0.5f);
    }
}
