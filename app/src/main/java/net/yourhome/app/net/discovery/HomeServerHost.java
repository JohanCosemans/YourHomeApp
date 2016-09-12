package net.yourhome.app.net.discovery;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.model.ServerInfo;
import net.yourhome.app.net.HomeServerConnector;

public class HomeServerHost {
	
	// Set paths on server
	//public static final String CONFIGURATIONS = "configurations";
	public static final String WEBSOCKET = "websocket";
	//public static final String SERVER_INFO = "serverinfo.xml";

	public int configurationVersion;
	public String configurationFileName;
    
    public String ipAddress = null;
    public int port = 80;
    public String name = "";
    private ServerInfo info;
    
    public HomeServerHost(String ipAddress, int port) {
    	this.ipAddress = ipAddress;
    	this.port = port;
    }
   
    public HomeServerHost(String ip, String serverInfoString) throws JSONException {
    	this.ipAddress = ip;
    	info = new ServerInfo(new JSONObject(serverInfoString));
    	this.port = info.getPort();
    }
    public URI getDesignerAddress()  {
    	String designerAddress = "http://"+ipAddress;
    	if(port!=80) { designerAddress += ":"+port; };
    	designerAddress += "/YourHomeDesigner";
    	try {
			return new URI(designerAddress);
		} catch (URISyntaxException e) {
			return null;
		}
    }
    public boolean getDetails() {
		try {
			// Build url
			HomeServerConnector connector = HomeServerConnector.getInstance();
			String result = connector.sendApiCall(ipAddress, port, "/Info");
			if(result != null) {
				info = new ServerInfo(new JSONObject(result));
				port  = info.getPort();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @return the info
	 */
	public ServerInfo getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(ServerInfo info) {
		this.info = info;
	}
    

}
