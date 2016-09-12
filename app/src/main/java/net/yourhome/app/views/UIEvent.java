package net.yourhome.app.views;

import java.util.HashMap;
import java.util.Map;

public class UIEvent {
	public enum Types {
		SET_VALUE,
		EMPTY
	}
	
	private Types eventType;
	
	public UIEvent(Types type) {
		this.eventType = type;
	}
	
	Map<String,Object> properties = new HashMap<String,Object>();
	
	public Object getProperty(String name) {
		return properties.get(name);
	}
	
	public void setProperty(String name, Object value) {
		this.properties.put(name, value);
	}
	
	public Types getType() {
		return eventType;
	}
}
