package net.yourhome.app.gcm;

import net.yourhome.common.net.messagestructures.general.ClientNotificationMessage;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.net.HomeServerConnector;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class RefreshNotificationActivity extends Activity {
	private Activity me = this;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		if (intent != null) {
			final Bundle extras = intent.getExtras();
			if (extras != null) {
            	/*final String title = extras.getString("title");
            	final String message = extras.getString("message");
            	final String imagePath = extras.getString("imagePath");
            	final String videoPath = extras.getString("videoPath");*/
            	
            	Thread t = new Thread() {
            		public void run() {

                    	HomeServerConnector.getInstance().setMainContext(me);
                    	ClientNotificationMessage notificationMessage = new ClientNotificationMessage();
                    	notificationMessage.title = extras.getString("title");
                    	notificationMessage.message = extras.getString("message");
                    	notificationMessage.imagePath = extras.getString("imagePath");
                    	notificationMessage.videoPath = extras.getString("videoPath");
                    	BindingController.getInstance().handleCommand(notificationMessage);
                    	
            			//GeneralBinding.getInstance().displayNotification(me, title, message, imagePath, videoPath);		
            		}
            	};
            	t.start();
            	
            	finish();
			}
		}
	}
	
}
