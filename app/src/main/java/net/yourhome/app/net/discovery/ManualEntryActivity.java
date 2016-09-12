package net.yourhome.app.net.discovery;

import net.yourhome.app.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


public class ManualEntryActivity extends Activity {
	
	private ManualEntryActivity me = this; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//		setProgressBarIndeterminateVisibility(false);
		this.setContentView(R.layout.discovery_ip_input_popup);
        
        // Get reference to settings
        SharedPreferences settings = this.getSharedPreferences("USER", MODE_PRIVATE);
        
    	// Default value of field with current active server:
    	EditText text = (EditText)this.findViewById(R.id.discovery_ip_input_ip_hostname);
        final String homeserverIP = settings.getString("HOMESERVER_IP", "");
        final int homeserverPort = settings.getInt("HOMESERVER_PORT",0);
        if(homeserverPort == 0 || homeserverPort == 80) {
        	text.setText(homeserverIP);
        }else {
        	text.setText(homeserverIP + ":" + homeserverPort);
        }
        
        // Default value of external access
    	EditText textExt = (EditText)this.findViewById(R.id.discovery_ip_input_ext_hostname);
    	
        String homeserverExtIP = "";
        int homeserverExtPort = 0;
        
        try{
        	homeserverExtIP = settings.getString("HOMESERVER_EXT_IP", "");
        	homeserverExtPort = settings.getInt("HOMESERVER_EXT_PORT",0);
        }catch(Exception e) {}
        
        if(homeserverExtPort == 0 || homeserverExtPort == 80) {
        	textExt.setText(homeserverExtIP);
        }else {
        	textExt.setText(homeserverExtIP + ":" + homeserverExtPort);
        }
    	
    	// Assign binding to cancel button
    	Button cancelButton = (Button)this.findViewById(R.id.discovery_ip_input_popup_cancel);
    	cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
    	});
    	
    	// Assing binding to ok button
    	Button okButton = (Button)me.findViewById(R.id.discovery_ip_input_popup_ok);
    	okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// Parse LAN details
				EditText text = (EditText)me.findViewById(R.id.discovery_ip_input_ip_hostname);
				String[] hostnameTab = text.getText().toString().split(":");
				String hostname = "";
				int port = 80;
				if(hostnameTab.length > 0) {
	            	hostname = hostnameTab[0];
	            	if(hostnameTab.length > 1 && hostnameTab[1] != "") {
	            		port = Integer.parseInt(hostnameTab[1]);
	            	}
				}
				
				// Parse internet details
				EditText textExt = (EditText)me.findViewById(R.id.discovery_ip_input_ext_hostname);
				String[] hostnameTabExt = textExt.getText().toString().split(":");
				String hostnameExt = "";
				int portExt = 80;
				if(hostnameTabExt.length > 0) {
	            	hostnameExt = hostnameTabExt[0];
	            	if(hostnameTabExt.length > 1 && hostnameTabExt[1] != "") {
	            		portExt = Integer.parseInt(hostnameTabExt[1]);
	            	}
				}
				
				// TODO: Check if ip or hostname is valid (textual)
				
				// Check if ip or hostname is homeserver, set it in preferences, load it
				
				// Check if connected to lan or to internet
	        	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	        	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	        	boolean wifiConnected = mWifi.isConnected();
				IPHostnameChecker checker = new ActivityHostnameChecker(hostname, port, hostnameExt, portExt,wifiConnected);
				checker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
    	});

    	
    }
    
    private class ActivityHostnameChecker extends IPHostnameChecker {
		
		final ProgressBar progress;
		
		public ActivityHostnameChecker(String ipAddress, int port,
				String ipAddressExt, int portExt, boolean localFirst) {
			super(ipAddress, port, ipAddressExt, portExt, localFirst);
	    	progress = (ProgressBar)me.findViewById(R.id.discoveryLoader);
		}
		protected void onPreExecute(){
	    	progress.setVisibility(View.VISIBLE);
			//setProgressBarIndeterminateVisibility(true);
	    }
	    protected void onPostExecute(HomeServerHost homeServerHost){
	    	progress.setVisibility(View.INVISIBLE);
			//setProgressBarIndeterminateVisibility(false);
	    	super.onPostExecute(homeServerHost);
	        if(connectionTestResult) {
	          // Save in preferences & load configuration
	     	   SharedPreferences userDetails = getSharedPreferences("USER", Context.MODE_PRIVATE);
	     	   Editor edit = userDetails.edit();
	           edit.putString("HOMESERVER_IP", ipAddress);
	           edit.putInt("HOMESERVER_PORT", this.port);
	           edit.putInt("HOMESERVER_SOCKETPORT", this.port+1);
	            
	           edit.putString("HOMESERVER_EXT_IP", ipAddressExt);
	           edit.putInt("HOMESERVER_EXT_PORT", this.portExt);
	           edit.putInt("HOMESERVER_EXT_SOCKETPORT", this.portExt+1);
	            
	           edit.commit();
	         	
	           // Close window
	           //me.finish();
	 			
	           // Start canvas & load configuration
	           // Show configuration dialog
	           DiscoveryActivityImp.showConfigurationsDialog(me, homeServerHost);
	           //startActivity(new Intent(me, CanvasActivity.class));
	          	
	           // Stop discovery activity
	           //finish();
	        }else {
	     	 // Error message
	     	   Toast.makeText(me, "Failed to find HomeServer on "+ipAddress+":"+this.port + " or " + ipAddressExt + ":" + portExt, Toast.LENGTH_LONG).show();
	        }
	     }     
}	    
    
}
