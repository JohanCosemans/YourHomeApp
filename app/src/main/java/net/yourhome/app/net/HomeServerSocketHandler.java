package net.yourhome.app.net;

import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketOptions;
import android.util.Log;

public class HomeServerSocketHandler extends WebSocketConnectionHandler {
	
	private String TAG = "HomeServerSocketHandler";
	private final WebSocketConnection mConnection;
	private String serverIp;
	private int serverPort;
	private HomeServerConnector connector;
	private WebSocketOptions options;
	
	public HomeServerSocketHandler(HomeServerConnector connector) {
		mConnection = new WebSocketConnection();
		options = new WebSocketOptions();
		this.connector = connector;
	}
	
	public boolean connect(String ip, int port) {
		this.serverIp = ip;
		this.serverPort = port;

		try {
			if(!mConnection.isConnected()) {
				//options.setReconnectInterval(500);
				mConnection.connect("ws://"+serverIp+":"+serverPort+"/websocket", this, options);
			    Log.d(TAG, "Connecting to "+serverIp);
			}else {
				// Request update
			    Log.d(TAG, "Already connected to "+serverIp);
			    mConnection.forcedDisconnect();
				mConnection.connect("ws://"+serverIp+":"+serverPort, this, options);
			}
		} catch (WebSocketException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			Thread.sleep(HomeServerConnector.DEFAULT_SO_TIMEOUT);
		} catch (InterruptedException e) {
		}
		
		if(mConnection.isConnected()) {
		    Log.d(TAG, "mConnection.isConnected ... ");
		    connector.processConnected();
			return true;
		}else {
		    Log.e(TAG, "mConnection.isConnected not connected ... ");
			return false;
		}
	}
	public boolean reconnect(String ip, int port) {

		if(options.getReconnectInterval() == 0) {
		    Log.e(TAG, "Reconnecting ... ");
			return this.connect(ip,port);
		}
		
		return mConnection.isConnected();
	}
    public void disconnect() {
    	mConnection.disconnect();
    }
    public boolean isConnected() {
    	return mConnection.isConnected();
    }
    public void send(String payload) {
    	mConnection.sendTextMessage(payload);
    }
	   
	@Override
    public void onOpen() {
		connector.processConnected();
       Log.d(TAG, "Status: Connected to " + serverIp + ":" +serverPort);
    }

    @Override
    public void onTextMessage(String payload) {
       //Log.d(TAG, "Got message: " + payload);
       connector.handleCommand(payload);
    }

    @Override
    public void onClose(int code, String reason) {
       Log.d(TAG, "Connection lost.");
       connector.processDisconnected();
    }
    
}
