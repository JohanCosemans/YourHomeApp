/*-
 * Copyright (c) 2016 Coteq, Johan Cosemans
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY COTEQ AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.yourhome.app.bindings;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.common.base.enums.MessageTypes;
import net.yourhome.common.base.enums.zwave.DataHistoryOperations;
import net.yourhome.common.base.enums.zwave.DataHistoryPeriodTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ValueChangedMessage;
import net.yourhome.common.net.messagestructures.general.ValueHistoryMessage;
import net.yourhome.common.net.messagestructures.general.ValueHistoryRequest;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public class SensorBinding extends ValueBinding {

	String currentUnit = "?";

	public SensorBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if (message instanceof ValueChangedMessage) {
			ValueChangedMessage v = (ValueChangedMessage) message;
			this.currentUnit = v.unit;
			this.currentValue = v.value;
			this.updateViews();
		}
	}

	public String getUnit() {
		return this.currentUnit;
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
			if (responseString != null) {
				// Parse message
				JSONObject responseObject = new JSONObject(responseString);
				return (ValueHistoryMessage) MessageTypes.getMessage(responseObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
