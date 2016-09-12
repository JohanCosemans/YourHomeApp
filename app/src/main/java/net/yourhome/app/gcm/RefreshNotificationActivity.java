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
package net.yourhome.app.gcm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.common.net.messagestructures.general.ClientNotificationMessage;

public class RefreshNotificationActivity extends Activity {
	private Activity me = this;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		if (intent != null) {
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				/*
				 * final String title = extras.getString("title"); final String
				 * message = extras.getString("message"); final String imagePath
				 * = extras.getString("imagePath"); final String videoPath =
				 * extras.getString("videoPath");
				 */

				Thread t = new Thread() {
					@Override
					public void run() {

						HomeServerConnector.getInstance().setMainContext(RefreshNotificationActivity.this.me);
						ClientNotificationMessage notificationMessage = new ClientNotificationMessage();
						notificationMessage.title = extras.getString("title");
						notificationMessage.message = extras.getString("message");
						notificationMessage.imagePath = extras.getString("imagePath");
						notificationMessage.videoPath = extras.getString("videoPath");
						BindingController.getInstance().handleCommand(notificationMessage);

						// GeneralBinding.getInstance().displayNotification(me,
						// title, message, imagePath, videoPath);
					}
				};
				t.start();

				finish();
			}
		}
	}

}
