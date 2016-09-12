package net.yourhome.app.net.discovery;

import net.yourhome.app.net.HomeServerConnector;

import android.os.AsyncTask;
import android.util.Log;

public class IPHostnameChecker extends AsyncTask<Void,String,HomeServerHost> {
	
	protected boolean connectionTestResult = false;
    
    protected String ipAddress;	// LAN
    protected int port;			// LAN

    protected String ipAddressExt;	// Internet
    protected int portExt;			// Internet
    
    protected boolean localFirst;
    protected boolean resumeFromSuspend = false;
    protected String connectionModeBeforeSuspend = null;
    
    //protected HomeServerHost host = null;
    
    public IPHostnameChecker(String ipAddress, int port, String ipAddressExt, int portExt, boolean localFirst) {
    	this.ipAddress = ipAddress;
    	this.port = port;
    	
    	this.ipAddressExt = ipAddressExt;
    	this.portExt = portExt;
    	
    	this.localFirst = localFirst;
    }
    public IPHostnameChecker(String ipAddress, int port, String ipAddressExt, int portExt, boolean localFirst, boolean resumeFromSuspend, String connectionModeBeforeSuspend) {
    	this(ipAddress, port, ipAddressExt, portExt, localFirst);
    	this.resumeFromSuspend = resumeFromSuspend;
    	this.connectionModeBeforeSuspend = connectionModeBeforeSuspend;
    }

	@Override
	protected HomeServerHost doInBackground(Void... arg0) {
		HomeServerHost host = null;
		// Try to connect to homeserverip
		if (ipAddress == "" && ipAddressExt == "") {
			// Don't bother checking
			connectionTestResult = false;
		} else {
			// Disconnect all other sessions first (if there are any)
			HomeServerConnector homeServerConnector = HomeServerConnector.getInstance();
			homeServerConnector.disconnect();

			host = new HomeServerHost(ipAddress, port);
			if (localFirst) {
				host.ipAddress = ipAddress;
				host.port = port;
				if(host.ipAddress != null) {
					publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
                    if (!host.getDetails()) {
                        host.ipAddress = ipAddressExt;
                        host.port = portExt;
                        if(host.ipAddress != null) {
                            publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
                            host.getDetails();
                        }
                    }
                }
			} else {
				host.ipAddress = ipAddressExt;
				host.port = portExt;
				if(host.ipAddress != null) {
                    publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
                    if (!host.getDetails()) {
                        host.ipAddress = ipAddress;
                        host.port = port;
                        if (host.ipAddress != null) {
                            publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
                            host.getDetails();
                        }
                    }
                }
			}

			if (host.getInfo() == null) {
				connectionTestResult = false;
				return null;
			} else {
				connectionTestResult = true;
			}

			Log.d("IPHostnameChecker", "connectionTestResult: " + connectionTestResult);
		}
		return host;
	}
	@Override
	protected void onProgressUpdate(String... status) {
		
	}      
}