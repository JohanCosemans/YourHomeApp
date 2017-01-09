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

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.util.JSONMessageCaller;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.SetValueMessage;
import net.yourhome.common.net.messagestructures.general.ValueChangedMessage;

public class ValueBinding extends AbstractBinding {

	String currentValue = null;

	public ValueBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if (message instanceof ValueChangedMessage) {
			ValueChangedMessage v = (ValueChangedMessage) message;
			this.currentValue = v.value;
			this.updateViews();
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
		this.updateViews();
	}

	@Override
	public boolean viewPressed(DynamicView v, UIEvent event) {
        JSONMessage valueMessage = buildMessage(v,event);
		if (event.getType() == UIEvent.Types.SET_VALUE) {
			BindingController.getInstance().sendMessage(valueMessage);
			this.setValue(((SetValueMessage)valueMessage).value);
		}
        return true;
	}

    @Override
    public boolean viewPressed(DynamicView v, UIEvent event, JSONMessageCaller apiCaller) {
        apiCaller.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buildMessage(v,event));
        return true;
    }
    private JSONMessage buildMessage(DynamicView v, UIEvent event) {
        if (event.getType() == UIEvent.Types.SET_VALUE) {
            SetValueMessage setValueMessage = new SetValueMessage();
            setValueMessage.controlIdentifiers = this.getControlIdentifier();
            setValueMessage.broadcast = false;
            setValueMessage.value = event.getProperty("VALUE").toString();
            if(event != null
                    && event.getProperty("protected") != null
                    && (Boolean)event.getProperty("protected")) {
                setValueMessage.isProtected = (Boolean)event.getProperty("protected");
                setValueMessage.protectionCode = (String)event.getProperty("protectionCode");
            }
            return setValueMessage;
        }
        return null;
    }
    @Override
	public void viewLongPressed(DynamicView v, UIEvent event) {

	}
}
