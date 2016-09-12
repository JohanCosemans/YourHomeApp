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
package net.yourhome.app.net;

import android.util.Log;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketOptions;

public class HomeServerSocketHandler extends WebSocketConnectionHandler {

	private String TAG = "HomeServerSocketHandler";
	private final WebSocketConnection mConnection;
	private String serverIp;
	private int serverPort;
	private HomeServerConnector connector;
	private WebSocketOptions options;

	public HomeServerSocketHandler(HomeServerConnector connector) {
		this.mConnection = new WebSocketConnection();
		this.options = new WebSocketOptions();
		this.connector = connector;
	}

	public boolean connect(String ip, int port) {
		this.serverIp = ip;
		this.serverPort = port;

		try {
			if (!this.mConnection.isConnected()) {
				// options.setReconnectInterval(500);
				this.mConnection.connect("ws://" + this.serverIp + ":" + this.serverPort + "/websocket", this, this.options);
				Log.d(this.TAG, "Connecting to " + this.serverIp);
			} else {
				// Request update
				Log.d(this.TAG, "Already connected to " + this.serverIp);
				this.mConnection.forcedDisconnect();
				this.mConnection.connect("ws://" + this.serverIp + ":" + this.serverPort, this, this.options);
			}
		} catch (WebSocketException e) {
			e.printStackTrace();
			return false;
		}

		try {
			Thread.sleep(HomeServerConnector.DEFAULT_SO_TIMEOUT);
		} catch (InterruptedException e) {
		}

		if (this.mConnection.isConnected()) {
			Log.d(this.TAG, "mConnection.isConnected ... ");
			this.connector.processConnected();
			return true;
		} else {
			Log.e(this.TAG, "mConnection.isConnected not connected ... ");
			return false;
		}
	}

	public boolean reconnect(String ip, int port) {

		if (this.options.getReconnectInterval() == 0) {
			Log.e(this.TAG, "Reconnecting ... ");
			return this.connect(ip, port);
		}

		return this.mConnection.isConnected();
	}

	public void disconnect() {
		this.mConnection.disconnect();
	}

	public boolean isConnected() {
		return this.mConnection.isConnected();
	}

	public void send(String payload) {
		this.mConnection.sendTextMessage(payload);
	}

	@Override
	public void onOpen() {
		this.connector.processConnected();
		Log.d(this.TAG, "Status: Connected to " + this.serverIp + ":" + this.serverPort);
	}

	@Override
	public void onTextMessage(String payload) {
		// Log.d(TAG, "Got message: " + payload);
		this.connector.handleCommand(payload);
	}

	@Override
	public void onClose(int code, String reason) {
		Log.d(this.TAG, "Connection lost.");
		this.connector.processDisconnected();
	}

}
