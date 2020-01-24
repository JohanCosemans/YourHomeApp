package net.yourhome.app.fcm;

import android.content.Context;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import net.yourhome.app.bindings.GeneralBinding;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.base.enums.MobileNotificationTypes;
import net.yourhome.common.net.messagestructures.general.ClientNotificationMessage;

import java.util.Map;

/**
 * Created by johan on 5/14/2017.
 */
public class NotificationCreatorService extends FirebaseMessagingService  {

    private static final String TAG = "NotificationCreatorService";

    public static final String NOTIFICATION_CHANNEL="net.yourhome.app.fcm.main";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                // Handle message within 10 seconds
            showNotification(remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void showNotification(Map<String,String> data) {
        // Post notification of received message.
        ClientNotificationMessage notificationMessage = new ClientNotificationMessage();
        Context activeContext = HomeServerConnector.getInstance().getMainContext();
        if (activeContext == null) {
            HomeServerConnector.getInstance().setMainContext(this.getApplicationContext());
        }

        notificationMessage.title = data.get("title");
        notificationMessage.message = data.get("message");
        notificationMessage.imagePath = data.get("imagePath");
        notificationMessage.videoPath = data.get("videoPath");
        notificationMessage.notificationType = MobileNotificationTypes.convert(data.get("notificationType"));
        notificationMessage.windowTitle = data.get("windowTitle");
        notificationMessage.subtitle = data.get("subtitle");
        notificationMessage.startDate = Long.parseLong(data.get("startDate"));
        String cancelString = data.get("cancel");
        if (cancelString != null) {
            notificationMessage.cancel = cancelString.toLowerCase().equals("true");
        }

        String controllerIdentifier = data.get("controllerIdentifier");
        if (controllerIdentifier != null) {
            notificationMessage.controlIdentifiers.setControllerIdentifier(ControllerTypes.convert(controllerIdentifier));
            String nodeIdentifier = data.get("nodeIdentifier");
            notificationMessage.controlIdentifiers.setNodeIdentifier(nodeIdentifier);
            String valueIdentifier = data.get("nodeIdentifier");
            notificationMessage.controlIdentifiers.setValueIdentifier(valueIdentifier);
        }

        // Direct this message directly to the general binding
        GeneralBinding.getInstance().handleMessage(notificationMessage);

    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 , notificationBuilder.build());
    }
     */
}
