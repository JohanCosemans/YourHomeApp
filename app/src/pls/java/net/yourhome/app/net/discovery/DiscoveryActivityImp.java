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
package net.yourhome.app.net.discovery;

import java.util.ArrayList;
import java.util.List;

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

final public class DiscoveryActivityImp extends DiscoveryActivity {

	public static void showConfigurationsDialog(final Activity activity, final HomeServerHost host) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setTitle(R.string.discover_action_title);

		final List<net.yourhome.common.net.model.Configuration> configurationList = new ArrayList<net.yourhome.common.net.model.Configuration>(host.getInfo().getConfigurations().values());
		String[] configurations = new String[configurationList.size()];
		int i = 0;
		for (net.yourhome.common.net.model.Configuration c : configurationList) {
			if (c.getName() == null) {
				c.setName("Unnamed");
			}
			configurations[i] = c.getName();
			i++;
		}
		if (i == 0) {
			TextView noConfigurationsMessage = new TextView(activity);
			String messageContent = activity.getResources().getString(R.string.discovery_no_published_configurations);
			messageContent += System.getProperty("line.separator");
			messageContent += host.getDesignerAddress();
			noConfigurationsMessage.setText(R.string.discovery_no_published_configurations);
			noConfigurationsMessage.setGravity(Gravity.LEFT);
			noConfigurationsMessage.setPadding(25, 25, 25, 80);

			SpannableString s = new SpannableString(messageContent);
			Linkify.addLinks(s, Linkify.WEB_URLS);
			noConfigurationsMessage.setText(s);
			noConfigurationsMessage.setMovementMethod(LinkMovementMethod.getInstance());

			dialog.setView(noConfigurationsMessage);
		} else {
			dialog.setItems(configurations, new DialogInterface.OnClickListener() {
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
