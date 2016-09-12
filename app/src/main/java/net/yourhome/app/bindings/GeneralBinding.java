/*-
 * Copyright (c) 2016 Coteq, Johan Cosemans
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE NETBSD FOUNDATION, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.yourhome.app.bindings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import net.yourhome.app.R;
import net.yourhome.app.canvas.ipcamera.IPCameraActivity;
import net.yourhome.app.gcm.RefreshNotificationActivity;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.util.ThermostatEndDatePickerActivity;
import net.yourhome.app.util.Util;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.base.enums.MobileNotificationTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ClientMessageMessage;
import net.yourhome.common.net.messagestructures.general.ClientNotificationMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public class GeneralBinding extends AbstractBinding {

	protected Toast toastMessage;
	protected int notificationId = 1;

	private static volatile GeneralBinding instance;
	private static Object lock = new Object();

	private GeneralBinding() {
		this.controlIdentifier = new ControlIdentifiers(ControllerTypes.GENERAL.convert());
		this.stageElementId = "general";
	}

	public static GeneralBinding getInstance() {
		GeneralBinding r = GeneralBinding.instance;
		if (r == null) {
			synchronized (GeneralBinding.lock) { // while we were waiting for
													// the lock, another
				r = GeneralBinding.instance; // thread may have instantiated
				// the object
				if (r == null) {
					r = new GeneralBinding();
					GeneralBinding.instance = r;
				}
			}
		}
		return GeneralBinding.instance;
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if (message instanceof ClientNotificationMessage) {
			// Display notification
			ClientNotificationMessage clientMessage = (ClientNotificationMessage) message;
			this.displayNotification(clientMessage);
		} else if (message instanceof ClientMessageMessage) {
			// Display toast message
			ClientMessageMessage clientMessage = (ClientMessageMessage) message;
			this.displayToast(clientMessage.messageContent);
		}
	}

	@Override
	public void viewPressed(DynamicView v, UIEvent event) {
	}

	@Override
	public void viewLongPressed(DynamicView v, UIEvent event) {
	}

	private void displayToast(String text) {
		Context activeContext = HomeServerConnector.getInstance().getMainContext();
		if (activeContext != null) {
			// Cancel active toast message
			if (this.toastMessage != null) {
				this.toastMessage.cancel();
			}

			// Display the message
			if (text != null && !text.equals("")) {
				Log.d("GeneralBinding", "Displaying toast: " + text);
				this.toastMessage = Toast.makeText(activeContext, text, Toast.LENGTH_SHORT);
				this.toastMessage.show();
			}
		} else {
			Log.d("GeneralBinding", "Could not display toast as there is no active context");
		}
	}

	private Map<MobileNotificationTypes, List<Integer>> notifications = new HashMap<>();

	private void cancelNotificationsOfType(MobileNotificationTypes type) {
		Context activeContext = HomeServerConnector.getInstance().getMainContext();
		NotificationManager mNotificationManager = (NotificationManager) activeContext.getSystemService(Context.NOTIFICATION_SERVICE);
		List<Integer> notificationIds = this.notifications.get(type);
		if (notificationIds != null) {
			for (Integer i : notificationIds) {
				mNotificationManager.cancel(i);
			}
			notificationIds.clear();
		}
	}

	private void displayNotification(ClientNotificationMessage message) {

		Context activeContext = HomeServerConnector.getInstance().getMainContext();
		NotificationManager mNotificationManager = (NotificationManager) activeContext.getSystemService(Context.NOTIFICATION_SERVICE);

		if (activeContext != null) {
			Notification notification = null;
			switch (message.notificationType) {
			case DATE_TIME_PICKER:
				this.cancelNotificationsOfType(message.notificationType);
				if (!message.cancel) {
					notification = this.createDatePickerNotification(activeContext, message);
				}
				break;
			case IMAGE:
				notification = this.createImageNotification(activeContext, message);
				break;
			case TEXT:
				notification = this.createTextNotification(activeContext, message);
				break;
			default:
				break;
			}
			if (notification != null) {
				List<Integer> notificationIds = this.notifications.get(message.notificationType);
				if (notificationIds == null) {
					notificationIds = new ArrayList<>();
					this.notifications.put(message.notificationType, notificationIds);
				}
				notificationIds.add(this.notificationId);
				mNotificationManager.notify(this.notificationId++, notification);
			}
		}
	}

	private Notification createImageNotification(Context context, ClientNotificationMessage message) {
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentTitle(message.title).setContentText(message.message).setSmallIcon(R.drawable.ic_notification);
		Notification notification = null;
		if (message.imagePath != null) {
			// Create normal notification to show directly before loading
			// the image
			builder.setContentText("Loading image...");
			notification = builder.build();
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.defaults |= Notification.DEFAULT_SOUND;

			// Load the image - once it's loaded, update the active
			// notification
			Bitmap image = this.getImageFromUrl(message.imagePath);
			// Bitmap image = null;
			if (image != null) {
				// Actions
				if (message.videoPath != null) {

					// Intent resultIntent = new Intent(context,
					// IPCameraActivityDialog.class);
					Intent resultIntent = new Intent(context, IPCameraActivity.class);
					resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					resultIntent.putExtra("videoPath", message.videoPath);
					String[] splittedFileName = message.imagePath.split("/");
					if (splittedFileName.length > 0) {
						String filePath = Util.createImageFromBitmap(splittedFileName[splittedFileName.length - 1], image, context);
						resultIntent.putExtra("imagePath", filePath);
					}
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
					stackBuilder.addParentStack(IPCameraActivity.class);
					stackBuilder.addNextIntent(resultIntent);
					PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
					builder.setContentIntent(resultPendingIntent);
					builder.setContentText("Tap the image to see live feed");
				}
				builder.setLargeIcon(image);
				notification = new Notification.BigPictureStyle(builder).bigPicture(image).build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
			} else {

				builder.setContentText("Loading image failed. Tap to try again.");
				Intent refreshNotificationIntent = new Intent(context, RefreshNotificationActivity.class);
				refreshNotificationIntent.putExtra("videoPath", message.videoPath);
				refreshNotificationIntent.putExtra("imagePath", message.imagePath);
				refreshNotificationIntent.putExtra("title", message.title);
				refreshNotificationIntent.putExtra("message", message.message);

				PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, refreshNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				builder.setContentIntent(resultPendingIntent);
				notification = builder.build();
				notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
			}
		} else {
			notification = builder.build();
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.vibrate = new long[] { 0 };
			notification.defaults = Notification.DEFAULT_SOUND;
		}
		return notification;
	}

	private Notification createTextNotification(Context context, ClientNotificationMessage message) {
		Notification.Builder builder = new Notification.Builder(context);

		builder.setContentTitle(message.title).setContentText(message.message).setSmallIcon(R.drawable.ic_notification);

		Notification notification = null;
		notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.vibrate = new long[] { 0 };
		notification.defaults = Notification.DEFAULT_SOUND;
		return notification;
	}

	private Notification createDatePickerNotification(Context context, ClientNotificationMessage message) {
		Notification.Builder builder = new Notification.Builder(context);

		builder.setContentTitle(message.title).setContentText(message.message).setSmallIcon(R.drawable.ic_notification);

		Intent resultIntent = new Intent(context, ThermostatEndDatePickerActivity.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		resultIntent.putExtra("controllerIdentifier", message.controlIdentifiers.getControllerIdentifier().convert());
		resultIntent.putExtra("nodeIdentifier", message.controlIdentifiers.getNodeIdentifier());
		resultIntent.putExtra("valueIdentifier", message.controlIdentifiers.getValueIdentifier());
		resultIntent.putExtra("windowTitle", message.windowTitle);
		resultIntent.putExtra("subtitle", message.subtitle);
		resultIntent.putExtra("startDate", message.startDate);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(ThermostatEndDatePickerActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);

		Notification notification = null;
		notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.vibrate = new long[] { 0 };
		notification.defaults = Notification.DEFAULT_SOUND;
		return notification;
	}

	// private Bitmap getImageFromUrl(Context context, String imagePath) {
	private Bitmap getImageFromUrl(String imagePath) {
		Bitmap image = null;
		Context activeContext = HomeServerConnector.getInstance().getMainContext();
		if (!imagePath.equals("")) {
			// Get bitmap
			// GeneralController.getInstance().setActiveContext(context);
			String fullImageUrl = Configuration.getInstance().getHomeServerProtocol() + "://" + Configuration.getInstance().getHomeServerHostName(activeContext) + ":" + Configuration.getInstance().getHomeServerPort(activeContext) + imagePath;

			Log.d("GcmIntentService", "Retrieving image on " + imagePath);
			try {
				image = Util.getBitmapFromURL(fullImageUrl);
			} catch (IOException e) {
				e.printStackTrace();
				// Image retrieval failed: try once to switch from internal to
				// external address (or otherwise)
				try {
					Configuration.getInstance().toggleConnectionInternalExternal(activeContext);
					fullImageUrl = Configuration.getInstance().getHomeServerProtocol() + "://" + Configuration.getInstance().getHomeServerHostName(activeContext) + ":" + Configuration.getInstance().getHomeServerPort(activeContext) + imagePath;
					Log.d("GcmIntentService", "Retrieving image (retry 1) on " + imagePath);
					image = Util.getBitmapFromURL(fullImageUrl);
				} catch (IOException ex) {
					ex.printStackTrace();
					// Even this did not work: don't display the image...
				}
			}
		}
		return image;
	}

	@Override
	protected void buildBinding() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// do not destoy this like the other bindings.
	}

}
