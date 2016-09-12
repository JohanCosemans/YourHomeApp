package net.yourhome.app.bindings;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.base.enums.MessageTypes;
import net.yourhome.common.base.enums.zwave.DataHistoryOperations;
import net.yourhome.common.base.enums.zwave.DataHistoryPeriodTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ValueChangedMessage;
import net.yourhome.common.net.messagestructures.general.ValueHistoryMessage;
import net.yourhome.common.net.messagestructures.general.ValueHistoryRequest;
import net.yourhome.common.net.model.binding.ControlIdentifiers;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.views.DynamicView;

public class SensorBinding extends ValueBinding {
	
	String currentUnit = "?";

	public SensorBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if(message instanceof ValueChangedMessage) {
			ValueChangedMessage v = (ValueChangedMessage)message;
			currentUnit = v.unit;
			currentValue = v.value;
			updateViews();
		}
	}
	
	public String getUnit() {
		return currentUnit;
	}
	
	public static ValueHistoryMessage getHistoricValues(ControlIdentifiers controlIdentifiers, int offset, int historyAmount, DataHistoryPeriodTypes periodType, DataHistoryOperations operation) {
		
		ValueHistoryRequest requestMessage = new ValueHistoryRequest();
		requestMessage.offset = offset;
		requestMessage.controlIdentifiers = controlIdentifiers;
		requestMessage.historyAmount = historyAmount;
		requestMessage.periodType = periodType;
		requestMessage.operation = operation;

		// Send message
		String responseString;
		try {
			responseString = HomeServerConnector.getInstance().sendSyncMessage(requestMessage, 30000);
			if(responseString != null) {
				// Parse message
				JSONObject responseObject = new JSONObject(responseString);
				return (ValueHistoryMessage)MessageTypes.getMessage(responseObject);	
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
