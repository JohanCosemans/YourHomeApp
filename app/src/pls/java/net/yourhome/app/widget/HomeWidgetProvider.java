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
 * THIS SOFTWARE IS PROVIDED BY COTEQ AND CONTRIBUTORS
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
package net.yourhome.app.widget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import net.yourhome.app.R;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ActivationMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public class HomeWidgetProvider extends AppWidgetProvider {

	public static String WIDGET_ACTION = "net.yourhome.controller.widget.WIDGET_ACTION";
	private static List<String> icons;

	private SharedPreferences settings;
	private Context context;

	public static List<String> getIcons() {
		if (HomeWidgetProvider.icons == null) {
			HomeWidgetProvider.icons = new ArrayList<String>();
			// Build icon list
			Field[] fields = R.string.class.getFields();
			for (Field field : fields) {
				String name = field.getName();
				if (name.startsWith("widget_icon")) {
					HomeWidgetProvider.icons.add(name);
				}
			}
		}
		return HomeWidgetProvider.icons;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (this.settings == null) {
			this.settings = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
		}
		this.context = context;
		Log.d("HomeWidgetProvider onReceive", "got intent " + intent.getAction());

		if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DELETED")) {
			int i = intent.getExtras().getInt("appWidgetId", 0);
			this.settings.edit().remove("WIDGET_" + i).commit();
		} else if (intent.getAction().equals(HomeWidgetProvider.WIDGET_ACTION) & intent.hasExtra("command")) {
			Bundle b = intent.getExtras();
			String command = b.getString("command");
			if (command != null) {
				JSONMessage commandMessage = new ActivationMessage();
				if (command.startsWith("Scene_")) {
					short sceneId = Short.parseShort(command.substring(("Scene_").length()));
					commandMessage.controlIdentifiers = new ControlIdentifiers(ControllerTypes.GENERAL.convert(), "Scenes", sceneId + "");
				}
				if (commandMessage != null) {
					CommandRequester loader = new CommandRequester(context);
					loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commandMessage);
				}
			}
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		this.context = context;
		if (this.settings == null) {
			this.settings = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
		}
		Log.d("HomeWidgetProvider", "onUpdate");

		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			this.updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		if (this.settings == null) {
			this.settings = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
		}
		this.context = context;

		Set<String> widgetConfig = this.settings.getStringSet("WIDGET_" + appWidgetId, null);
		if (widgetConfig != null) {
			Log.d("HomeWidgetProvider", "Building widget " + appWidgetId + ": " + widgetConfig);

			if (widgetConfig != null) {

				WidgetConfigurationActivity.WidgetTypes widgetType = null;
				String widgetActionType = null;
				;
				String widgetIcon = null;
				String widgetLabel = null;
				int iconColor = 0;
				int iconBackgroundColor = Color.parseColor("#d2d2d2"); // light
																		// gray

				for (String widgetConfigLine : widgetConfig) {
					String[] widgetConfigLineSplit = widgetConfigLine.split("=");
					if (widgetConfigLineSplit.length == 2) {
						if (widgetConfigLineSplit[0].equals("WIDGET_TYPE")) {
							widgetType = WidgetConfigurationActivity.WidgetTypes.valueOf(widgetConfigLineSplit[1]);
						} else if (widgetConfigLineSplit[0].equals("WIDGET_ACTION_TYPE")) {
							widgetActionType = widgetConfigLineSplit[1];
						} else if (widgetConfigLineSplit[0].equals("WIDGET_ICON")) {
							widgetIcon = widgetConfigLineSplit[1];
						} else if (widgetConfigLineSplit[0].equals("WIDGET_LABEL")) {
							widgetLabel = widgetConfigLineSplit[1];
						} else if (widgetConfigLineSplit[0].equals("WIDGET_COLOR")) {
							iconColor = Integer.parseInt(widgetConfigLineSplit[1]);
						} else if (widgetConfigLineSplit[0].equals("WIDGET_BACKGROUND_COLOR")) {
							iconBackgroundColor = Integer.parseInt(widgetConfigLineSplit[1]);
						}
					}
					RemoteViews views = HomeWidgetProvider.buildWidgetView(context, appWidgetId, widgetType, widgetActionType, widgetLabel, widgetIcon, iconColor, iconBackgroundColor);

					appWidgetManager.updateAppWidget(appWidgetId, views);
				}

			}
			Log.d("HomeWidgetProvider", "UpdateAppWidget " + appWidgetId);
		}
	}

	public static RemoteViews buildWidgetView(Context context, Integer widgetId, WidgetConfigurationActivity.WidgetTypes widgetType, String actionType, String label, String icon, int iconColor, int iconBackgroundColor) {

		Log.d("HomeWidgetProvider", "Building view: " + widgetType + ", " + actionType + ", " + label + ", " + icon + ", " + iconColor + ", " + iconBackgroundColor);

		if (widgetType != null && actionType != null) {

			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single_button);
			PendingIntent pendingIntent = null;
			// Fill ui of widget based on settings
			switch (widgetType) {
			// case ZWAVE_GENERAL_COMMAND:
			// pendingIntent = addIntent(context,widgetId,actionType);
			// break;
			case SCENE:
				pendingIntent = HomeWidgetProvider.addIntent(context, widgetId, "Scene_" + actionType);
				break;
			}
			views.setImageViewBitmap(R.id.singleButton, Configuration.getInstance().getWidgetIcon(context, icon, 100, iconColor));

			if (label != null && !label.equals("")) {
				views.setTextViewText(R.id.singleButtonDescription, label);
				views.setViewVisibility(R.id.singleButtonDescription, View.VISIBLE);
			} else {
				views.setViewVisibility(R.id.singleButtonDescription, View.GONE);
			}
			if (pendingIntent != null) {
				views.setOnClickPendingIntent(R.id.singleButton, pendingIntent);
			}
			/* Set background color */
			views.setInt(R.id.widgetBackground, "setColorFilter", iconBackgroundColor);

			return views;
		}
		return null;
	}

	private static PendingIntent addIntent(Context context, int widgetId, String command) {
		Intent intentAction = new Intent();
		intentAction.setAction(HomeWidgetProvider.WIDGET_ACTION);
		intentAction.putExtra("command", command);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetId, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	protected class CommandRequester extends AsyncTask<JSONMessage, String, String> {

		private Configuration configuration;
		private Context context;

		public CommandRequester(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(JSONMessage... message) {
			this.configuration = Configuration.getInstance();
			String result = null;
			try {
				result = HomeServerConnector.getInstance().sendSyncMessage(message[0]);
			} catch (Exception e) {
				this.configuration.toggleConnectionInternalExternal(this.context);
				try {
					result = HomeServerConnector.getInstance().sendSyncMessage(message[0]);
				} catch (Exception ex) {
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				Toast.makeText(this.context, "Could not connect to homeserver", Toast.LENGTH_LONG).show();
			} else {

				String returnMessage = "Action finished";
				try {
					JSONObject returnObject = new JSONObject(result);
					returnMessage = returnObject.getString("messageContent");
				} catch (JSONException e) {
				}

				Toast.makeText(this.context, returnMessage, Toast.LENGTH_SHORT).show();
			}
		}

	}

}
