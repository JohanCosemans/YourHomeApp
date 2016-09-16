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
 * THIS SOFTWARE IS PROVIDED BY THE NETBSD FOUNDATION, INC. AND CONTRIBUTORS
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

import android.util.Log;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public abstract class AbstractBinding {

	// Value type: dimmer, switch, sensor, music control, ...
	protected ValueTypes valueType = ValueTypes.UNKNOWN;
	protected String stageElementId;

	// Identification of controller
	protected ControlIdentifiers controlIdentifier;

	protected List<DynamicView> viewListeners = new ArrayList<DynamicView>();

	public void destroy() {
		this.viewListeners.clear();
		this.controlIdentifier = null;
	}

	public AbstractBinding() {
	};

	public AbstractBinding(String stageElementId, JSONObject bindingProperties) throws JSONException, InvalidParameterException {
		if (bindingProperties == null) {
			throw new InvalidParameterException("Bindingproperties cannot be empty");
		}
		this.stageElementId = stageElementId;
		this.valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
		this.controlIdentifier = new ControlIdentifiers(bindingProperties);

		// parse properties and initialize binding
		this.buildBinding();

		BindingController.getInstance().addBinding(this);

	}

	public void addViewListener(DynamicView v) {
		this.viewListeners.add(v);
		v.refreshView();
	}

	public void removeViewListener(DynamicView v) {
		this.viewListeners.remove(v);
	}

	public String getStageElementId() {
		return this.stageElementId;
	}

	public abstract void handleMessage(JSONMessage message);

	public abstract void viewPressed(DynamicView v, UIEvent event);

	public abstract void viewLongPressed(DynamicView v, UIEvent event);

	// Handle value changes in the ui
	protected void updateViews() {
		for (DynamicView v : this.viewListeners) {
			if (v != null) {
				v.refreshView();
			} else {
				this.viewListeners.remove(v);
			}
		}
	}

	protected void setLoaderState(final int state) {
		if (this.viewListeners != null) {
			for (DynamicView v : this.viewListeners) {
				if (v != null) {
                    Log.d("AbstractBinding", "setLoaderState: setting loader state"+v.toString());
					v.setLoaderState(state);
				} else {
					this.viewListeners.remove(v);
				}
			}
		}
	}

	// Set all binding-specific properties
	protected abstract void buildBinding();

	/**
	 * @return the valueType
	 */
	public ValueTypes getValueType() {
		return this.valueType;
	}

	/**
	 * @param valueType
	 *            the valueType to set
	 */
	public void setValueType(ValueTypes valueType) {
		this.valueType = valueType;
	}

	/**
	 * @return the controlIdentifier
	 */
	public ControlIdentifiers getControlIdentifier() {
		return this.controlIdentifier;
	}

	/**
	 * @param controlIdentifier
	 *            the controlIdentifier to set
	 */
	public void setControlIdentifier(ControlIdentifiers controlIdentifier) {
		this.controlIdentifier = controlIdentifier;
	}

}
