package net.yourhome.app.bindings;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.SetValueMessage;
import net.yourhome.common.net.messagestructures.general.ValueChangedMessage;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;

public class ValueBinding extends AbstractBinding {
	
	String currentValue = null; 

	public ValueBinding(String bindingId,JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if(message instanceof ValueChangedMessage) {
			ValueChangedMessage v = (ValueChangedMessage)message;
			currentValue = v.value;			
			updateViews();
		}
	}

	@Override
	protected void buildBinding() {
		// Nothing to do - no specific parameters for a valuebinding
	}
	
	public String getValue() {
		return this.currentValue;
	}
	public void setValue(String value) {
		this.currentValue = value;
		updateViews();
	}
	
	@Override
	public void viewPressed(DynamicView v, UIEvent event) {
		if(event.getType() == UIEvent.Types.SET_VALUE) {
			SetValueMessage setValueMessage = new SetValueMessage();
			setValueMessage.controlIdentifiers = this.getControlIdentifier();
			setValueMessage.broadcast = false;
			setValueMessage.value = event.getProperty("VALUE").toString();
			BindingController.getInstance().sendMessage(setValueMessage);

			setValue(setValueMessage.value);
		}
	}

	@Override
	public void viewLongPressed(DynamicView v, UIEvent event) {
		
	}
}
