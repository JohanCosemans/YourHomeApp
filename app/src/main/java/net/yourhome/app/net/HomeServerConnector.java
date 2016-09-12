package net.yourhome.app.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.canvas.CanvasActivity;
import net.yourhome.app.util.Configuration;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
public class HomeServerConnector {
	
	private String TAG = "HomeServerConnector";
	private HomeServerSocketHandler homeHandler;
	private Context mainContext;
	//private SpotifyBindingController spotifyController;
	//private RadioBindingController radioController;
	//private ZWaveBindingController zwaveBindingController;
	//private GeneralController generalController;
	//private IPCameraBindingController ipCameraController;
	private HomeServerConnector me = this;
	//private Map<Integer,IBinding> returningMessages;
	public static final int DEFAULT_HTTP_TIMEOUT = 6000;
	public static final int DEFAULT_SO_TIMEOUT = 4000;
	private boolean isConnected = false;
	private boolean initiateReconnection = true;
	private BindingController bindingController;
	
	private static HomeServerConnector HomeServerConnector;
	public static HomeServerConnector getInstance() {
		if(HomeServerConnector == null) {
			HomeServerConnector = new HomeServerConnector();
		}
		
		return HomeServerConnector;
	}
	
	
	private HomeServerConnector() {	
		/*spotifyController = SpotifyBindingController.getInstance();
		radioController = RadioBindingController.getInstance();
		zwaveBindingController = ZWaveBindingController.getInstance();
		generalController = GeneralController.getInstance();
		ipCameraController = IPCameraBindingController.getInstance();*/
		bindingController = BindingController.getInstance();
	}
	
	public void setMainContext(Context context) {
		this.mainContext = context;
		
		//this.mainContext.runOnUiThread(new Runnable() {
		//    public void run() {
				homeHandler = new HomeServerSocketHandler(me);
				//  }
				//});
	}

	public Context getMainContext() {
		return this.mainContext;
	}
	public void destroy() {
		this.mainContext = null;
		homeHandler = null;
//		spotifyController = null;
//		radioController = null;
//		zwaveBindingController = null;
//		generalController = null;
//		ipCameraController = null;
	}
	public void connect()  {

		initiateReconnection = true;
		isConnected = false;
		
		// Send connecting UI event
        Intent intent = new Intent(CanvasActivity.LoadingStatus.CONNECTING.convert());
        LocalBroadcastManager.getInstance(mainContext).sendBroadcast(intent);
        
        // Connect to websocket
        Configuration configuration = Configuration.getInstance();
        String hostName = configuration.getHomeServerHostName(mainContext);
        int port = configuration.getHomeServerPort(mainContext);
        if(homeHandler != null) {
        isConnected = homeHandler.connect(hostName,port);
        if(!isConnected) {
        	if(Configuration.getInstance().toggleConnectionInternalExternal(mainContext)) {
        		isConnected = homeHandler.connect(Configuration.getInstance().getHomeServerHostName(mainContext),Configuration.getInstance().getHomeServerPort(mainContext));
        	}
        }
        }else {
        	Log.e(TAG, "HomeHandler = null !");
        }
        
	}
	public boolean reconnect() {
		
		// Send connecting UI event
		initiateReconnection = true;
		
    	try {
			Thread.sleep(2000);
        	Intent intent = new Intent(CanvasActivity.LoadingStatus.CONNECTING.convert());
        	LocalBroadcastManager.getInstance(mainContext).sendBroadcast(intent);
      	} catch (InterruptedException e1) {}
    	
        Configuration.getInstance().toggleConnectionInternalExternal(mainContext);
        
		return homeHandler.reconnect(Configuration.getInstance().getHomeServerHostName(mainContext),Configuration.getInstance().getHomeServerPort(mainContext));
		
	}
	public void disconnect() {
		if(homeHandler != null) {
			homeHandler.disconnect();
			this.initiateReconnection = false;
			this.isConnected = false;
		}
	}
	
	public void sendCommand(JSONMessage command) {

		homeHandler.send(command.serialize().toString());
		
	}

	public void handleCommand(String data) {
		this.processConnected();
		bindingController.handleCommand(data);
	}
	public void processConnected() {
		if(!isConnected) {
			// Send connected UI event
			Intent connectedintent = new Intent(CanvasActivity.LoadingStatus.CONNECTED.convert());
			LocalBroadcastManager.getInstance(mainContext).sendBroadcast(connectedintent);
		}
		isConnected = true;
	}
	public void processDisconnected() {
		
		isConnected = false;
		
		if(initiateReconnection) {
		
	        // Alert that the connection has dropped to the main activity UI
	        Intent intent = new Intent(CanvasActivity.LoadingStatus.ERROR.convert());
	        LocalBroadcastManager.getInstance(mainContext).sendBroadcast(intent);
	        isConnected = false;
	        Log.d(TAG, "Initiate reconnection");
	        try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
			this.reconnect();
			
		}
	}
	public String sendSyncMessage(JSONMessage message) throws Exception {
		return sendSyncMessage(message,DEFAULT_HTTP_TIMEOUT);
	}
	public String sendSyncMessage(JSONMessage message, int timeout) throws Exception {
		Configuration configuration = Configuration.getInstance();
		
		String url = configuration.getHomeServerProtocol()+"://"+configuration.getHomeServerHostName(mainContext)+":"+configuration.getHomeServerPort(mainContext)+"/api/messagehandler";
		String postData = message.serialize().toString();
		HashMap<String, String> headers = new HashMap<String,String>();
		headers.put("ContentType", "application/json");
		
		Log.d(TAG, "[Net-Http] Sending sync message: "+postData);
		String returnString = "";
		returnString = this.getStringContent(url, postData, headers, timeout);
		Log.d(TAG, "[Net-Http] Received sync message: "+returnString);
		return returnString;
	}
	public String sendApiCall(String ip, int port, String apiPath) throws Exception {
		return sendApiCall(ip, port, apiPath,DEFAULT_HTTP_TIMEOUT);
	}
	public String sendApiCall(String apiPath) throws Exception {
		return sendApiCall(apiPath,DEFAULT_HTTP_TIMEOUT);
	}
	public String sendApiCall(String apiPath, int timeout) throws Exception {
		Configuration configuration = Configuration.getInstance();
		return sendApiCall(configuration.getHomeServerHostName(mainContext), configuration.getHomeServerPort(mainContext), apiPath, timeout);
	}
	public String sendApiCall(String ip, int port, String apiPath, int timeout) throws Exception {
		Configuration configuration = Configuration.getInstance();
		String url = configuration.getHomeServerProtocol()+"://"+ip+":"+port+"/api"+apiPath;
		Log.d(TAG, "[Net-Http] Sending api message to: "+url);
		String returnString = "";
		returnString = this.getStringContent(url, timeout);
		Log.d(TAG, "[Net-Http] Received sync message: "+returnString);
		return returnString;
	}
	// POST
	public String getStringContent(String uri, String postData, 
	        HashMap<String, String> headers, int timeout) throws Exception {

	        HttpClient client = new DefaultHttpClient();
	        HttpParams httpParams = client.getParams();
	        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
	        HttpConnectionParams.setSoTimeout(httpParams, timeout);
	        HttpPost request = new HttpPost();
	        request.setURI(new URI(uri));
	        StringEntity entity = new StringEntity(postData); 
	        entity.setContentType("application/json");
	        request.setEntity(entity);
	        if(headers != null) {
		        for(Entry<String, String> s : headers.entrySet())
		        {
		            request.setHeader(s.getKey(), s.getValue());
		        }
	        }
	        HttpResponse response = client.execute(request);
	        return processResponse(response);
	 } 
	// GET
	public String getStringContent(String uri, int timeout) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpGet request = new HttpGet();
        request.setURI(new URI(uri));
        HttpResponse response = client.execute(request);
        return processResponse(response);
	 } 
	
	private String processResponse(HttpResponse response) throws Exception {
		InputStream ips  = response.getEntity().getContent();
        BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));
        if(response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK)
        {
            throw new Exception(response.getStatusLine().getReasonPhrase());
        }
        StringBuilder sb = new StringBuilder();
        String s;
        while(true )
        {
            s = buf.readLine();
            if(s==null || s.length()==0)
                break;
            sb.append(s);

        }
        buf.close();
        ips.close();
        return sb.toString();
	}
	
}

