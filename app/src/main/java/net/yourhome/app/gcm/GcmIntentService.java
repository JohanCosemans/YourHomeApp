package net.yourhome.app.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.base.enums.MobileNotificationTypes;
import net.yourhome.common.net.messagestructures.general.ClientNotificationMessage;
import net.yourhome.app.bindings.GeneralBinding;
import net.yourhome.app.net.HomeServerConnector;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    //private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GcmIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
            	ClientNotificationMessage notificationMessage = new ClientNotificationMessage();
    			Context activeContext = HomeServerConnector.getInstance().getMainContext();
    			if(activeContext==null) { 
    				HomeServerConnector.getInstance().setMainContext(this.getApplicationContext());
    			}
            	
            	notificationMessage.title = extras.getString("title");
            	notificationMessage.message = extras.getString("message");
            	notificationMessage.imagePath = extras.getString("imagePath");
            	notificationMessage.videoPath = extras.getString("videoPath");
            	notificationMessage.notificationType = MobileNotificationTypes.convert(extras.getString("notificationType"));
            	notificationMessage.windowTitle =  extras.getString("windowTitle");
            	notificationMessage.subtitle =  extras.getString("subtitle");
            	notificationMessage.startDate =  Long.parseLong(extras.getString("startDate"));
				String cancelString = extras.getString("cancel");
				if(cancelString != null) {
					notificationMessage.cancel = cancelString.toLowerCase().equals("true");
				}

            	String controllerIdentifier = extras.getString("controllerIdentifier");
            	if(controllerIdentifier != null) {
            		notificationMessage.controlIdentifiers.setControllerIdentifier(ControllerTypes.convert(controllerIdentifier));
                	String nodeIdentifier = extras.getString("nodeIdentifier");
            		notificationMessage.controlIdentifiers.setNodeIdentifier(nodeIdentifier);
                	String valueIdentifier = extras.getString("nodeIdentifier");	
            		notificationMessage.controlIdentifiers.setValueIdentifier(valueIdentifier);
            	}
            	
            	// Direct this message directly to the general binding
            	GeneralBinding.getInstance().handleMessage(notificationMessage);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
