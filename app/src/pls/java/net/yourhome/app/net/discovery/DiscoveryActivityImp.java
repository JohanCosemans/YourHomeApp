package net.yourhome.app.net.discovery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.widget.TextView;

import net.yourhome.app.R;
import net.yourhome.app.canvas.CanvasActivity;

import java.util.ArrayList;
import java.util.List;

final public class DiscoveryActivityImp extends DiscoveryActivity {

	public static void showConfigurationsDialog(final Activity activity, final HomeServerHost host) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setTitle(R.string.discover_action_title);
		
		final List<net.yourhome.common.net.model.Configuration> configurationList = new ArrayList<net.yourhome.common.net.model.Configuration>(host.getInfo().getConfigurations().values());
		String[] configurations = new String[configurationList.size()];
		int i=0;
		for(net.yourhome.common.net.model.Configuration c : configurationList) {
			if(c.getName() == null) { c.setName("Unnamed"); }
			configurations[i] = c.getName();
			i++;
		}
		if(i==0) {
			TextView noConfigurationsMessage = new TextView(activity);
			String messageContent = activity.getResources().getString(R.string.discovery_no_published_configurations);
			messageContent += System.getProperty ("line.separator");
			messageContent += host.getDesignerAddress();
			noConfigurationsMessage.setText(R.string.discovery_no_published_configurations);
			noConfigurationsMessage.setGravity(Gravity.LEFT);
			noConfigurationsMessage.setPadding(25, 25, 25, 80);
			
			SpannableString s = new SpannableString(messageContent);
		    Linkify.addLinks(s, Linkify.WEB_URLS);
		    noConfigurationsMessage.setText(s);
		    noConfigurationsMessage.setMovementMethod(LinkMovementMethod.getInstance());
		  
			dialog.setView(noConfigurationsMessage);
		}else {
			dialog.setItems(configurations, 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int selected) {
						net.yourhome.common.net.model.Configuration selectedConfiguration = configurationList.get(selected);
						// Set selection in preferences
						SharedPreferences userDetails = activity.getSharedPreferences("USER", Context.MODE_PRIVATE);
						SharedPreferences.Editor edit = userDetails.edit();
						edit.putString("HOMESERVER_IP", host.ipAddress);
						edit.putInt("HOMESERVER_PORT", host.port);
						edit.putString("HOMESERVER_NAME", host.name);
						edit.putString("HOMESERVER_CONFIGURATION", selectedConfiguration.getFile());
						edit.commit();
	
						// Start canvas & load configuration
						dialog.dismiss();
						activity.startActivity(new Intent(activity, CanvasActivity.class));
	
						// Stop discovery activity
						activity.finish();
					}
			});
		}
		dialog.setNegativeButton(R.string.btn_discover_cancel, null);
		dialog.show();
	}
}
