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

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.util.JSONMessageCaller;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.radio.RadioOnOffMessage;

public class RadioBinding extends ActivationBinding {

	private boolean isPlaying = false;

	public RadioBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	protected void buildBinding() {
		this.updateViews();
		this.setViewsAlpha((float) 0.5);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if (message instanceof RadioOnOffMessage) {
			if (message.controlIdentifiers.equals(this.controlIdentifier) && ((RadioOnOffMessage) message).status) {
				this.setViewsAlpha(1);
				this.isPlaying = true;
			} else {
				this.setViewsAlpha((float) 0.5);
				this.isPlaying = false;
			}

			// Also share this with the other radio bindings
			if (!((RadioOnOffMessage) message).stopSharing) {
				((RadioOnOffMessage) message).stopSharing = true;
				List<AbstractBinding> bindings = BindingController.getInstance().getBindingsFor(this.getControlIdentifier().getControllerIdentifier().convert());
				if (bindings != null) {
					for (AbstractBinding binding : bindings) {
						if (binding != this && binding instanceof RadioBinding) {
							binding.handleMessage(message);
						}
					}
				}
			}
		}
	}

    @Override
    public boolean viewPressed(DynamicView v, UIEvent event, JSONMessageCaller apiCaller) {
        JSONMessage radioMessage = buildMessage(v,event);
        apiCaller.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, radioMessage);
        return true;
    }
	@Override
	public boolean viewPressed(DynamicView v, UIEvent event) {
        JSONMessage radioMessage = buildMessage(v,event);
        BindingController.getInstance().sendMessage(radioMessage);
        return false;
	}
    private JSONMessage buildMessage(DynamicView v, UIEvent event) {
        // Send radio activation message to controller
        RadioOnOffMessage radioMessage = new RadioOnOffMessage();
        radioMessage.controlIdentifiers = this.getControlIdentifier();
        radioMessage.status = !this.isPlaying;
        if(event != null
                && event.getProperty("protected") != null
                && (Boolean)event.getProperty("protected")) {
            radioMessage.isProtected = (Boolean)event.getProperty("protected");
            radioMessage.protectionCode = (String)event.getProperty("protectionCode");
        }
        return radioMessage;
    }

	protected void setViewsAlpha(float alpha) {
		for (DynamicView v : this.viewListeners) {
			v.setAlpha(alpha);
		}
	}

	@Override
	public void addViewListener(DynamicView v) {
		this.viewListeners.add(v);
		v.refreshView();
		this.setViewsAlpha(this.isPlaying ? 1.0f : 0.5f);
	}
}
