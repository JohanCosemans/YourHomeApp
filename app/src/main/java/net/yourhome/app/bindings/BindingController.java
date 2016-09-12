package net.yourhome.app.bindings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import net.yourhome.common.base.enums.MessageTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;
import net.yourhome.app.net.HomeServerConnector;
import android.util.Log;

public class BindingController {
	private Map<String,List<AbstractBinding>> bindingsByIdentifiers;
	private Map<String,List<AbstractBinding>> bindingsByController;
	private Map<String,AbstractBinding> bindingByStageElementId;
	private static volatile BindingController instance;
	private static Object lock = new Object();

	public static BindingController getInstance() {
		BindingController r = instance;
		if (r == null) {
			synchronized (lock) { // while we were waiting for the lock, another
				r = instance; // thread may have instantiated
												// the object
				if (r == null) {
					r = new BindingController();
					instance = r;
				}
			}
		}
		return instance;
	}
	private BindingController() {
		// Instantiate general binding (for toast, notifications, ...)
		bindingsByIdentifiers = new ConcurrentHashMap<String,List<AbstractBinding>>();
		bindingsByController = new ConcurrentHashMap<String,List<AbstractBinding>>();
        bindingByStageElementId = new ConcurrentHashMap<String,AbstractBinding>();
		addBinding(GeneralBinding.getInstance());
	}
	
	public void addBinding(AbstractBinding binding) {
        // Add binding to map with stage item id
        if(binding != null) {
            bindingByStageElementId.put(binding.getStageElementId(), binding);
            // Add binding to map by identifier (if it has a value)
            if (binding.getControlIdentifier().getValueIdentifier() != null) {
                List<AbstractBinding> bindingsForIdentifiers = bindingsByIdentifiers.get(binding.getControlIdentifier().getKey());
                if (bindingsForIdentifiers == null) {
                    bindingsForIdentifiers = new ArrayList<AbstractBinding>();
                    bindingsByIdentifiers.put(binding.getControlIdentifier().getKey(), bindingsForIdentifiers);
                }
                bindingsForIdentifiers.add(binding);
            }

            // Add binding to map by controller
            if (binding.getControlIdentifier().getControllerIdentifier() != null) {
                List<AbstractBinding> bindingsForController = bindingsByController.get(binding.getControlIdentifier().getControllerIdentifier().convert());
                if (bindingsForController == null) {
                    bindingsForController = new ArrayList<AbstractBinding>();
                    bindingsByController.put(binding.getControlIdentifier().getControllerIdentifier().convert(), bindingsForController);
                }
                bindingsForController.add(binding);
            }
        }
	}

	public AbstractBinding getBindingFor(String stageElementId) {
        if(stageElementId != null) {
            return bindingByStageElementId.get(stageElementId);
        }
        return null;
	}
	public List<AbstractBinding> getBindingsFor(ControlIdentifiers identifiers) {
		return bindingsByIdentifiers.get(identifiers.getKey());
	}
	public List<AbstractBinding> getBindingsFor(String controllerIdentifier) {
		return bindingsByController.get(controllerIdentifier);
	}
	public void handleCommand(String data) {
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONMessage message = MessageTypes.getMessage(jsonObject);
			if(message != null) {
				handleCommand(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("HomeServerConnector", "Could not parse incoming message: "+data);
		}
	}

	public void handleCommand(JSONMessage message) {
		if(message != null) {

			// Check if there is a value: if so, let the value binding handle the message
			if(message.controlIdentifiers.getValueIdentifier() != null) {
				List<AbstractBinding> bindings = this.getBindingsFor(message.controlIdentifiers);
				// Let the bindings take care of the message
				if(bindings != null) {
					for(AbstractBinding binding : bindings) {
						binding.handleMessage(message);
					}	
				}
			//? needed to also process on node level?	
			}else if(message.controlIdentifiers.getControllerIdentifier() != null) {
				// Fallback to the controller bindings
				List<AbstractBinding> bindings = this.getBindingsFor(message.controlIdentifiers.getControllerIdentifier().convert());
				// Let the bindings take care of the message
				if(bindings != null) {
					for(AbstractBinding binding : bindings) {
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
        for(AbstractBinding binding : bindingByStageElementId.values()) {
            binding.destroy();
        }
        this.bindingsByController.clear();
        this.bindingsByIdentifiers.clear();
        this.bindingByStageElementId.clear();
        addBinding(GeneralBinding.getInstance());
    }

}
