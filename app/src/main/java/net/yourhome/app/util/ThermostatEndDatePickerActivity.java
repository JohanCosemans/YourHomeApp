package net.yourhome.app.util;

import java.util.Date;

import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.thermostat.SetAwayMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.net.HomeServerConnector;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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
		identifiers = new ControlIdentifiers();
		identifiers.setControllerIdentifier(ControllerTypes.convert(controllerIdentifier));
		identifiers.setNodeIdentifier(nodeIdentifier);
		identifiers.setValueIdentifier(valueIdentifier);
		
		Intent intent = new Intent(this,DatePickerActivity.class);
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
				//SetAwayMessage
				JSONMessageCaller loader = new ThermostatCaller(this);
				SetAwayMessage message = new SetAwayMessage();
				message.controlIdentifiers = identifiers;
				message.value = "true";
				message.until = new Date(selectedTime);
		    	loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
		    	
				// Close this activity
				//finish();
			}
		}

	}
	protected class ThermostatCaller extends JSONMessageCaller {

		public ThermostatCaller(Context context) {
			super(context);
		}

		protected void onPreExecute() {
			//((ProgressBar) findViewById(R.id.widget_action_loading)).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(JSONMessage result) {

			Context activeContext = HomeServerConnector.getInstance().getMainContext();
			if(activeContext==null) { 
				HomeServerConnector.getInstance().setMainContext(me);
			}
			
			BindingController.getInstance().handleCommand(result);
			//((ProgressBar) findViewById(R.id.widget_action_loading)).setVisibility(View.INVISIBLE);

			Thread thread = new Thread(){
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
						me.finish();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}
	}
}
