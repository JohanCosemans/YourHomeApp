package net.yourhome.app.util;

import net.yourhome.app.net.HomeServerConnector;

import android.content.Context;
import android.os.AsyncTask;

public abstract class APICaller extends AsyncTask<String, Void, String> {

	private Configuration configuration;
	private Context context;

	public APICaller(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(String... apiAddress) {
		configuration = Configuration.getInstance();
		String result = null;
		try {
			result = HomeServerConnector.getInstance().sendApiCall(apiAddress[0]);
		} catch (Exception e) {
			configuration.toggleConnectionInternalExternal(context);
			try {
				result = HomeServerConnector.getInstance().sendApiCall(apiAddress[0]);
			} catch (Exception ex) {
			}
		}
		return result;
	}

}
