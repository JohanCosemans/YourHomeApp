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
package net.yourhome.app.util;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.thermostat.SetAwayMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public class ThermostatEndDatePickerActivity extends Activity {

	private ControlIdentifiers identifiers;
	private Activity me = this;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent myIntent = this.getIntent();
		Bundle extras = myIntent.getExtras();

		String controllerIdentifier = extras.getString("controllerIdentifier");
		String nodeIdentifier = extras.getString("nodeIdentifier");
		String valueIdentifier = extras.getString("valueIdentifier");
		this.identifiers = new ControlIdentifiers();
		this.identifiers.setControllerIdentifier(ControllerTypes.convert(controllerIdentifier));
		this.identifiers.setNodeIdentifier(nodeIdentifier);
		this.identifiers.setValueIdentifier(valueIdentifier);

		Intent intent = new Intent(this, DatePickerActivity.class);
		intent.putExtras(extras);
		startActivityForResult(intent, DatePickerActivity.REQUEST_DATE_TIME);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("widget", "Result from date picker!");

		// Check which request we're responding to
		if (requestCode == DatePickerActivity.REQUEST_DATE_TIME) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK && data != null) {
				Long selectedTime = data.getLongExtra("net.yourhome.controller.util.selectedDateTime", new Date().getTime());
				Log.d("widget", "Result from date picker: " + new Date(selectedTime));

				// Send message to homeserver to set away to this time
				// SetAwayMessage
				JSONMessageCaller loader = new ThermostatCaller(this);
				SetAwayMessage message = new SetAwayMessage();
				message.controlIdentifiers = this.identifiers;
				message.value = "true";
				message.until = new Date(selectedTime);
				loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);

				// Close this activity
				// finish();
			}
		}

	}

	protected class ThermostatCaller extends JSONMessageCaller {

		public ThermostatCaller(Context context) {
			super(context);
		}

		protected void onPreExecute() {
			// ((ProgressBar)
			// findViewById(R.id.widget_action_loading)).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(JSONMessage result) {

			Context activeContext = HomeServerConnector.getInstance().getMainContext();
			if (activeContext == null) {
				HomeServerConnector.getInstance().setMainContext(ThermostatEndDatePickerActivity.this.me);
			}

			BindingController.getInstance().handleCommand(result);
			// ((ProgressBar)
			// findViewById(R.id.widget_action_loading)).setVisibility(View.INVISIBLE);

			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
						ThermostatEndDatePickerActivity.this.me.finish();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}
	}
}
