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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import android.util.Log;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.common.base.enums.MessageTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public class BindingController {
	private Map<String, List<AbstractBinding>> bindingsByIdentifiers;
	private Map<String, List<AbstractBinding>> bindingsByController;
	private Map<String, AbstractBinding> bindingByStageElementId;
	private static volatile BindingController instance;
	private static Object lock = new Object();

	public static BindingController getInstance() {
		BindingController r = BindingController.instance;
		if (r == null) {
			synchronized (BindingController.lock) { // while we were waiting for
													// the lock, another
				r = BindingController.instance; // thread may have instantiated
												// the object
				if (r == null) {
					r = new BindingController();
					BindingController.instance = r;
				}
			}
		}
		return BindingController.instance;
	}

	private BindingController() {
		// Instantiate general binding (for toast, notifications, ...)
		this.bindingsByIdentifiers = new ConcurrentHashMap<String, List<AbstractBinding>>();
		this.bindingsByController = new ConcurrentHashMap<String, List<AbstractBinding>>();
		this.bindingByStageElementId = new ConcurrentHashMap<String, AbstractBinding>();
		this.addBinding(GeneralBinding.getInstance());
	}

	public void addBinding(AbstractBinding binding) {
		// Add binding to map with stage item id
		if (binding != null) {
			this.bindingByStageElementId.put(binding.getStageElementId(), binding);
			// Add binding to map by identifier (if it has a value)
			if (binding.getControlIdentifier().getValueIdentifier() != null) {
				List<AbstractBinding> bindingsForIdentifiers = this.bindingsByIdentifiers.get(binding.getControlIdentifier().getKey());
				if (bindingsForIdentifiers == null) {
					bindingsForIdentifiers = new ArrayList<AbstractBinding>();
					this.bindingsByIdentifiers.put(binding.getControlIdentifier().getKey(), bindingsForIdentifiers);
				}
				bindingsForIdentifiers.add(binding);
			}

			// Add binding to map by controller
			if (binding.getControlIdentifier().getControllerIdentifier() != null) {
				List<AbstractBinding> bindingsForController = this.bindingsByController.get(binding.getControlIdentifier().getControllerIdentifier().convert());
				if (bindingsForController == null) {
					bindingsForController = new ArrayList<AbstractBinding>();
					this.bindingsByController.put(binding.getControlIdentifier().getControllerIdentifier().convert(), bindingsForController);
				}
				bindingsForController.add(binding);
			}
		}
	}

	public AbstractBinding getBindingFor(String stageElementId) {
		if (stageElementId != null) {
			return this.bindingByStageElementId.get(stageElementId);
		}
		return null;
	}

	public List<AbstractBinding> getBindingsFor(ControlIdentifiers identifiers) {
		return this.bindingsByIdentifiers.get(identifiers.getKey());
	}

	public List<AbstractBinding> getBindingsFor(String controllerIdentifier) {
		return this.bindingsByController.get(controllerIdentifier);
	}

	public void handleCommand(String data) {
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONMessage message = MessageTypes.getMessage(jsonObject);
			if (message != null) {
				this.handleCommand(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("HomeServerConnector", "Could not parse incoming message: " + data);
		}
	}

	public void handleCommand(JSONMessage message) {
		if (message != null) {

			// Check if there is a value: if so, let the value binding handle
			// the message
			if (message.controlIdentifiers.getValueIdentifier() != null) {
				List<AbstractBinding> bindings = this.getBindingsFor(message.controlIdentifiers);
				// Let the bindings take care of the message
				if (bindings != null) {
					for (AbstractBinding binding : bindings) {
						binding.handleMessage(message);
					}
				}
				// ? needed to also process on node level?
			} else if (message.controlIdentifiers.getControllerIdentifier() != null) {
				// Fallback to the controller bindings
				List<AbstractBinding> bindings = this.getBindingsFor(message.controlIdentifiers.getControllerIdentifier().convert());
				// Let the bindings take care of the message
				if (bindings != null) {
					for (AbstractBinding binding : bindings) {
						binding.handleMessage(message);
					}
				}
			}
		}
	}

	public void sendMessage(JSONMessage message) {
		HomeServerConnector.getInstance().sendCommand(message);
	}

	public void destroy() {
		for (AbstractBinding binding : this.bindingByStageElementId.values()) {
			binding.destroy();
		}
		this.bindingsByController.clear();
		this.bindingsByIdentifiers.clear();
		this.bindingByStageElementId.clear();
		this.addBinding(GeneralBinding.getInstance());
	}

}
