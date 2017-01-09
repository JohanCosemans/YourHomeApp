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
package net.yourhome.app.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.canvas.CanvasActivity;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.messagestructures.JSONMessage;

public class HomeServerConnector {

    public static final String CONNECTION_STATUS="connectionStatus";
	private String TAG = "HomeServerConnector";
	private HomeServerSocketHandler homeHandler;
	private Context mainContext;
	private HomeServerConnector me = this;
	public static final int DEFAULT_HTTP_TIMEOUT = 6000;
	public static final int DEFAULT_SO_TIMEOUT = 4000;
	private boolean isConnected = false;
	private boolean initiateReconnection = true;
	private BindingController bindingController;

	private static HomeServerConnector HomeServerConnector;

	public static HomeServerConnector getInstance() {
		if (HomeServerConnector.HomeServerConnector == null) {
			HomeServerConnector.HomeServerConnector = new HomeServerConnector();
		}

		return HomeServerConnector.HomeServerConnector;
	}

	private HomeServerConnector() {
		this.bindingController = BindingController.getInstance();
	}

	public void setMainContext(Context context) {
		this.mainContext = context;
		this.homeHandler = new HomeServerSocketHandler(this.me);
	}

	public Context getMainContext() {
		return this.mainContext;
	}

	public void destroy() {
		this.mainContext = null;
		this.homeHandler = null;
	}

	public void connect() {

		this.initiateReconnection = true;
		this.isConnected = false;

		// Send connecting UI event
		Intent intent = new Intent(CanvasActivity.CanvasEvents.CONNECTION_STATUS_CHANGE.name());
        intent.putExtra(CONNECTION_STATUS,CanvasActivity.LoadingStatus.CONNECTING.name());
		LocalBroadcastManager.getInstance(this.mainContext).sendBroadcast(intent);

		// Connect to websocket
		Configuration configuration = Configuration.getInstance();
		String hostName = configuration.getHomeServerHostName(this.mainContext);
		int port = configuration.getHomeServerPort(this.mainContext);
		if (this.homeHandler != null) {
			this.isConnected = this.homeHandler.connect(hostName, port);
			if (!this.isConnected) {
				if (Configuration.getInstance().toggleConnectionInternalExternal(this.mainContext)) {
					this.isConnected = this.homeHandler.connect(Configuration.getInstance().getHomeServerHostName(this.mainContext), Configuration.getInstance().getHomeServerPort(this.mainContext));
				}
			}
		} else {
			Log.e(this.TAG, "HomeHandler = null !");
		}

	}

	public boolean reconnect() {

		// Send connecting UI event
		this.initiateReconnection = true;

		try {
			Thread.sleep(2000);

			Intent intent = new Intent(CanvasActivity.CanvasEvents.CONNECTION_STATUS_CHANGE.name());
            intent.putExtra(CONNECTION_STATUS,CanvasActivity.LoadingStatus.CONNECTING.name());
			LocalBroadcastManager.getInstance(this.mainContext).sendBroadcast(intent);
		} catch (InterruptedException e1) {
		}

		Configuration.getInstance().toggleConnectionInternalExternal(this.mainContext);

		return this.homeHandler.reconnect(Configuration.getInstance().getHomeServerHostName(this.mainContext), Configuration.getInstance().getHomeServerPort(this.mainContext));

	}

	public void disconnect() {
		if (this.homeHandler != null) {
			this.homeHandler.disconnect();
			this.initiateReconnection = false;
			this.isConnected = false;
		}
	}

	public void sendCommand(JSONMessage command) {

		this.homeHandler.send(command.serialize().toString());

	}

	public void handleCommand(String data) {
		this.processConnected();
		this.bindingController.handleCommand(data);
	}

	public void processConnected() {
		if (!this.isConnected) {
			// Send connected UI event
			Intent intent = new Intent(CanvasActivity.CanvasEvents.CONNECTION_STATUS_CHANGE.name());
            intent.putExtra(CONNECTION_STATUS,CanvasActivity.LoadingStatus.CONNECTED.name());
			LocalBroadcastManager.getInstance(this.mainContext).sendBroadcast(intent);
		}
		this.isConnected = true;
	}

	public void processDisconnected() {

		this.isConnected = false;

		if (this.initiateReconnection) {

			// Alert that the connection has dropped to the main activity UI
			Intent intent = new Intent(CanvasActivity.CanvasEvents.CONNECTION_STATUS_CHANGE.name());
            intent.putExtra(CONNECTION_STATUS,CanvasActivity.LoadingStatus.ERROR.name());
			LocalBroadcastManager.getInstance(this.mainContext).sendBroadcast(intent);
			this.isConnected = false;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
            Log.d(this.TAG, "Initiate reconnection");
			this.reconnect();

		}
	}

	public String sendSyncMessage(JSONMessage message) throws IOException, URISyntaxException {
		return this.sendSyncMessage(message, HomeServerConnector.DEFAULT_HTTP_TIMEOUT);
	}

	public String sendSyncMessage(JSONMessage message, int timeout) throws IOException, URISyntaxException {
		Configuration configuration = Configuration.getInstance();

		String url = configuration.getHomeServerProtocol() + "://" + configuration.getHomeServerHostName(this.mainContext) + ":" + configuration.getHomeServerPort(this.mainContext) + "/api/messagehandler";
		String postData = message.serialize().toString();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ContentType", "application/json");

		Log.d(this.TAG, "[Net-Http] Sending sync message: " + postData);
		String returnString = "";
		returnString = this.getStringContent(url, postData, headers, timeout);
		Log.d(this.TAG, "[Net-Http] Received sync message: " + returnString);
		return returnString;
	}

	public String sendApiCall(String ip, int port, String apiPath) throws Exception {
		return this.sendApiCall(ip, port, apiPath, HomeServerConnector.DEFAULT_HTTP_TIMEOUT);
	}

	public String sendApiCall(String apiPath) throws Exception {
		return this.sendApiCall(apiPath, HomeServerConnector.DEFAULT_HTTP_TIMEOUT);
	}

	public String sendApiCall(String apiPath, int timeout) throws Exception {
		Configuration configuration = Configuration.getInstance();
		return this.sendApiCall(configuration.getHomeServerHostName(this.mainContext), configuration.getHomeServerPort(this.mainContext), apiPath, timeout);
	}

	public String sendApiCall(String ip, int port, String apiPath, int timeout) throws Exception {
		Configuration configuration = Configuration.getInstance();
		String url = configuration.getHomeServerProtocol() + "://" + ip + ":" + port + "/api" + apiPath;
		Log.d(this.TAG, "[Net-Http] Sending api message to: " + url);
		String returnString = "";
		returnString = this.getStringContent(url, timeout);
		Log.d(this.TAG, "[Net-Http] Received sync message: " + returnString);
		return returnString;
	}

	// POST
	public String getStringContent(String uri, String postData, HashMap<String, String> headers, int timeout) throws URISyntaxException, IOException {

		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpPost request = new HttpPost();
		request.setURI(new URI(uri));
		StringEntity entity = new StringEntity(postData);
		entity.setContentType("application/json");
		request.setEntity(entity);
		if (headers != null) {
			for (Entry<String, String> s : headers.entrySet()) {
				request.setHeader(s.getKey(), s.getValue());
			}
		}
		HttpResponse response = client.execute(request);
		return this.processResponse(response);
	}

	// GET
	public String getStringContent(String uri, int timeout) throws Exception {
		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpGet request = new HttpGet();
		request.setURI(new URI(uri));
		HttpResponse response = client.execute(request);
		return this.processResponse(response);
	}

	private String processResponse(HttpResponse response) throws IOException {
		InputStream ips = response.getEntity().getContent();
		BufferedReader buf = new BufferedReader(new InputStreamReader(ips, "UTF-8"));
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException(response.getStatusLine().getReasonPhrase());
		}
		StringBuilder sb = new StringBuilder();
		String s;
		while (true) {
			s = buf.readLine();
			if (s == null || s.length() == 0) {
				break;
			}
			sb.append(s);

		}
		buf.close();
		ips.close();
		return sb.toString();
	}

}
