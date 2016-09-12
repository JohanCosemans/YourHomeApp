package net.yourhome.app.bindings;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ActivationMessage;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;

public class ActivationBinding extends AbstractBinding {
	

	public ActivationBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		// Nothing to do
	}

	@Override
	protected void buildBinding() {
		// Nothing to do - no specific parameters for a valuebinding
		updateViews();
	}

	@Override
	public void viewPressed(DynamicView v, UIEvent event) {
		// Send activation message to controller
		ActivationMessage activationMessage = new ActivationMessage();
		activationMessage.controlIdentifiers = this.getControlIdentifier();
		BindingController.getInstance().sendMessage(activationMessage);
	}

	@Override
	public void viewLongPressed(DynamicView v, UIEvent event) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void updateViews() {
		for(DynamicView v : this.viewListeners) {
			v.refreshView();
		}
	}

	
}
