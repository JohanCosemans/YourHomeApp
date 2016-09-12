package net.yourhome.app.bindings;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.base.enums.ValueTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;

public abstract class AbstractBinding {
	
	//protected HomeServerConnector connector = HomeServerConnector.getInstance();	

	//protected BindingController bindingController = BindingController.getInstance();
	
	// Value type: dimmer, switch, sensor, music control, ...
	protected ValueTypes valueType = ValueTypes.UNKNOWN;
	protected String stageElementId;
	
	// Identification of controller
	protected ControlIdentifiers controlIdentifier;
	
	protected List<DynamicView> viewListeners = new ArrayList<DynamicView>();

    public void destroy() {
        this.viewListeners.clear();
        controlIdentifier = null;
    }
	public AbstractBinding() {};
	public AbstractBinding(String stageElementId, JSONObject bindingProperties) throws JSONException, InvalidParameterException {
		if(bindingProperties == null) {
			throw new InvalidParameterException("Bindingproperties cannot be empty");
		}
		this.stageElementId = stageElementId;
		this.valueType = ValueTypes.convert(bindingProperties.getString("valueType"));
		controlIdentifier = new ControlIdentifiers(bindingProperties);		

		//this.addViewListener(view);
		
		// parse properties and initialize binding
		buildBinding();
		
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
		return stageElementId;
	}

	public abstract void handleMessage(JSONMessage message);
	public abstract void viewPressed(DynamicView v, UIEvent event);
	public abstract void viewLongPressed(DynamicView v, UIEvent event);

	// Handle value changes in the ui
	protected void updateViews() {
		for(DynamicView v : this.viewListeners) {
            if(v != null) {
                v.refreshView();
            }else {
                this.viewListeners.remove(v);
            }
		}
	}
	protected void setLoaderState(final int state) {
        if(this.viewListeners != null) {
            for (DynamicView v : this.viewListeners) {
                if (v != null) {
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
		return valueType;
	}


	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(ValueTypes valueType) {
		this.valueType = valueType;
	}
	/**
	 * @return the controlIdentifier
	 */
	public ControlIdentifiers getControlIdentifier() {
		return controlIdentifier;
	}
	/**
	 * @param controlIdentifier the controlIdentifier to set
	 */
	public void setControlIdentifier(ControlIdentifiers controlIdentifier) {
		this.controlIdentifier = controlIdentifier;
	}


	
	


}
