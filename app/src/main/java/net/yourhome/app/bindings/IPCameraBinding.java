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
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import net.yourhome.app.canvas.ipcamera.IPCameraActivity;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.util.JSONMessageCaller;
import net.yourhome.app.util.Util;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.ipcamera.SnapshotMessage;
import net.yourhome.common.net.messagestructures.ipcamera.SnapshotRequestMessage;

public class IPCameraBinding extends AbstractBinding {

	public static String VIDEO_PATH = "videoPath";
	public static String STAGE_ELEMENT_ID = "stageElementId";

	private Bitmap currentImage;
	private String videoPath;
    private Timer refreshTimer;
    private IPCameraBinding me = this;

	@Override
	public void destroy() {
		super.destroy();
		this.currentImage = null;
		this.videoPath = null;
	}

	public IPCameraBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if (message instanceof SnapshotMessage) {
			this.videoPath = ((SnapshotMessage) message).videoPath;
			String fullImageUrl = Configuration.getInstance().getHomeServerProtocol() + "://" + Configuration.getInstance().getHomeServerHostName(HomeServerConnector.getInstance().getMainContext()) + ":" + Configuration.getInstance().getHomeServerPort(HomeServerConnector.getInstance().getMainContext()) + ((SnapshotMessage) message).snapshotUrl;
			this.loadSnapshot(fullImageUrl);
		}
	}

	@Override
	protected void buildBinding() {
	}

	@Override
	public void viewPressed(DynamicView v, UIEvent event) {

		// Start dialog window
		if (this.videoPath != null) {
			Intent intent = new Intent(v.getView().getContext(), IPCameraActivity.class);
			intent.putExtra(IPCameraBinding.VIDEO_PATH, this.videoPath);
			intent.putExtra(IPCameraBinding.STAGE_ELEMENT_ID, this.getStageElementId());
			v.getView().getContext().startActivity(intent);
		}
	}

	@Override
	public void viewLongPressed(DynamicView v, UIEvent event) {
	}

	public Bitmap getImage() {
		return this.currentImage;
	}

	private void loadSnapshot(String snapshotUrl) {
		// Initialize data loader
		IPCameraSnapshotLoader loader = new IPCameraSnapshotLoader();
		String[] urlParam = new String[] { snapshotUrl };
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String[]) urlParam);
	}

	public void refreshSnapshot() {
		// Request a snapshot from the server (async)
		JSONMessageCaller loader = new RequestImageCaller(HomeServerConnector.getInstance().getMainContext());
		SnapshotRequestMessage snapshotMessage = new SnapshotRequestMessage();
		snapshotMessage.controlIdentifiers = this.getControlIdentifier();
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, snapshotMessage);
	}

    public void setRefreshDelay(int delayMs) {
        if(refreshTimer != null) {
            refreshTimer.cancel();
        }
        refreshTimer = new Timer();
		// Refresh every 10 minutes (default time)
        refreshTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				me.refreshSnapshot();
			}
		}, new Date(), delayMs);
	}
	private class IPCameraSnapshotLoader extends AsyncTask<String, Void, Void> {

		private Bitmap snapshot = null;

		protected void onPreExecute() {
			me.setLoaderState(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(String... snapshotUrls) {
			if (snapshotUrls.length > 0) {
				String snapshotUrl = snapshotUrls[0];
				try {
					this.snapshot = Util.getBitmapFromURL(snapshotUrl);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			// Update loader of all listeners
			me.setLoaderState(View.INVISIBLE);
			if (this.snapshot != null) {
				me.currentImage = this.snapshot;
				me.updateViews();
			}
		}
	}

	protected class RequestImageCaller extends JSONMessageCaller {

		public RequestImageCaller(Context context) {
			super(context);
		}

		protected void onPreExecute() {
			me.setLoaderState(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(JSONMessage result) {

			me.setLoaderState(View.GONE);
			me.handleMessage(result);

		}
	}

}
