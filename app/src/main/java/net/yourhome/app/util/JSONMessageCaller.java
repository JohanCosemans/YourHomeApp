package net.yourhome.app.util;

import org.json.JSONObject;

import net.yourhome.common.base.enums.MessageTypes;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.app.net.HomeServerConnector;
import android.content.Context;
import android.os.AsyncTask;

public abstract class JSONMessageCaller extends AsyncTask<JSONMessage, Void, JSONMessage> {

	private Configuration configuration;
	private Context context;

	public JSONMessageCaller(Context context) {
		this.context = context;
	}

	@Override
	protected JSONMessage doInBackground(JSONMessage... message) {
		configuration = Configuration.getInstance();
		String result = null;
		try {
			result = HomeServerConnector.getInstance().sendSyncMessage(message[0]);
		} catch (Exception e) {
			configuration.toggleConnectionInternalExternal(context);
			try {			
				result = HomeServerConnector.getInstance().sendSyncMessage(message[0]);
			} catch (Exception ex) {
			}
		}
		try {
			JSONObject returnObject = new JSONObject(result);
			JSONMessage returnMessage = MessageTypes.getMessage(returnObject);
			return returnMessage;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
