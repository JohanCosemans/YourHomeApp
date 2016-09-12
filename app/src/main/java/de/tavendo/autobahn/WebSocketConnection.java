/******************************************************************************
 *
 *  Copyright 2011-2012 Tavendo GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package de.tavendo.autobahn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class WebSocketConnection implements WebSocket {

	private static final boolean DEBUG = true;
	private static final String TAG = WebSocketConnection.class.getName();

	protected Handler mMasterHandler;

	protected WebSocketReader mReader;
	protected WebSocketWriter mWriter;
	protected HandlerThread mWriterThread;

	protected SocketChannel mTransportChannel;

	private URI mWsUri;
	private String mWsScheme;
	private String mWsHost;
	private int mWsPort;
	private String mWsPath;
	private String mWsQuery;
	private String[] mWsSubprotocols;
	private List<BasicNameValuePair> mWsHeaders;

	private WebSocket.ConnectionHandler mWsHandler;

	protected WebSocketOptions mOptions;

	private boolean mActive;
	private boolean mPrevConnected;

	/**
	 * Asynchronous socket connector.
	 */
	private class WebSocketConnector extends Thread {

		@Override
		public void run() {
			Thread.currentThread().setName("WebSocketConnector");

			/*
			 * connect TCP socket
			 */
			try {
				WebSocketConnection.this.mTransportChannel = SocketChannel.open();

				// the following will block until connection was established or
				// an error occurred!
				WebSocketConnection.this.mTransportChannel.socket().connect(new InetSocketAddress(WebSocketConnection.this.mWsHost, WebSocketConnection.this.mWsPort), WebSocketConnection.this.mOptions.getSocketConnectTimeout());

				// before doing any data transfer on the socket, set socket
				// options
				WebSocketConnection.this.mTransportChannel.socket().setSoTimeout(WebSocketConnection.this.mOptions.getSocketReceiveTimeout());
				WebSocketConnection.this.mTransportChannel.socket().setTcpNoDelay(WebSocketConnection.this.mOptions.getTcpNoDelay());

			} catch (IOException e) {
				WebSocketConnection.this.onClose(ConnectionHandler.CLOSE_CANNOT_CONNECT, e.getMessage());
				return;
			}

			if (WebSocketConnection.this.mTransportChannel.isConnected()) {

				try {

					// create & start WebSocket reader
					WebSocketConnection.this.createReader();

					// create & start WebSocket writer
					WebSocketConnection.this.createWriter();

					// start WebSockets handshake
					WebSocketMessage.ClientHandshake hs = new WebSocketMessage.ClientHandshake(WebSocketConnection.this.mWsHost + ":" + WebSocketConnection.this.mWsPort);
					hs.mPath = WebSocketConnection.this.mWsPath;
					hs.mQuery = WebSocketConnection.this.mWsQuery;
					hs.mSubprotocols = WebSocketConnection.this.mWsSubprotocols;
					hs.mHeaderList = WebSocketConnection.this.mWsHeaders;
					WebSocketConnection.this.mWriter.forward(hs);

					WebSocketConnection.this.mPrevConnected = true;

				} catch (Exception e) {
					WebSocketConnection.this.onClose(ConnectionHandler.CLOSE_INTERNAL_ERROR, e.getMessage());
					return;
				}
			} else {
				WebSocketConnection.this.onClose(ConnectionHandler.CLOSE_CANNOT_CONNECT, "Could not connect to WebSocket server");
				return;
			}
		}

	}

	public WebSocketConnection() {
		if (WebSocketConnection.DEBUG) {
			Log.d(WebSocketConnection.TAG, "created");
		}

		// create WebSocket master handler
		this.createHandler();

		// set initial values
		this.mActive = false;
		this.mPrevConnected = false;
	}

	@Override
	public void sendTextMessage(String payload) {
		this.mWriter.forward(new WebSocketMessage.TextMessage(payload));
	}

	@Override
	public void sendRawTextMessage(byte[] payload) {
		this.mWriter.forward(new WebSocketMessage.RawTextMessage(payload));
	}

	@Override
	public void sendBinaryMessage(byte[] payload) {
		this.mWriter.forward(new WebSocketMessage.BinaryMessage(payload));
	}

	@Override
	public boolean isConnected() {
		return this.mTransportChannel != null && this.mTransportChannel.isConnected();
	}

	public void failConnection(int code, String reason) {

		if (WebSocketConnection.DEBUG) {
			Log.d(WebSocketConnection.TAG, "fail connection [code = " + code + ", reason = " + reason);
		}

		if (this.mReader != null) {
			this.mReader.quit();
			try {
				this.mReader.join();
			} catch (InterruptedException e) {
				if (WebSocketConnection.DEBUG) {
					e.printStackTrace();
				}
			}
			// mReader = null;
		} else {
			if (WebSocketConnection.DEBUG) {
				Log.d(WebSocketConnection.TAG, "mReader already NULL");
			}
		}

		if (this.mWriter != null) {
			// mWriterThread.getLooper().quit();
			this.mWriter.forward(new WebSocketMessage.Quit());
			try {
				this.mWriterThread.join();
			} catch (InterruptedException e) {
				if (WebSocketConnection.DEBUG) {
					e.printStackTrace();
				}
			}
			// mWriterThread = null;
		} else {
			if (WebSocketConnection.DEBUG) {
				Log.d(WebSocketConnection.TAG, "mWriter already NULL");
			}
		}

		if (this.mTransportChannel != null) {
			try {
				this.mTransportChannel.close();
			} catch (IOException e) {
				if (WebSocketConnection.DEBUG) {
					e.printStackTrace();
				}
			}
			// mTransportChannel = null;
		} else {
			if (WebSocketConnection.DEBUG) {
				Log.d(WebSocketConnection.TAG, "mTransportChannel already NULL");
			}
		}

		this.onClose(code, reason);

		if (WebSocketConnection.DEBUG) {
			Log.d(WebSocketConnection.TAG, "worker threads stopped");
		}
	}

	@Override
	public void connect(String wsUri, WebSocket.ConnectionHandler wsHandler) throws WebSocketException {
		this.connect(wsUri, null, wsHandler, new WebSocketOptions(), null);
	}

	@Override
	public void connect(String wsUri, WebSocket.ConnectionHandler wsHandler, WebSocketOptions options) throws WebSocketException {
		this.connect(wsUri, null, wsHandler, options, null);
	}

	public void connect(String wsUri, String[] wsSubprotocols, WebSocket.ConnectionHandler wsHandler, WebSocketOptions options, List<BasicNameValuePair> headers) throws WebSocketException {

		// don't connect if already connected .. user needs to disconnect first
		//
		if (this.mTransportChannel != null && this.mTransportChannel.isConnected()) {
			throw new WebSocketException("already connected");
		}

		// parse WebSockets URI
		//
		try {
			this.mWsUri = new URI(wsUri);

			if (!this.mWsUri.getScheme().equals("ws") && !this.mWsUri.getScheme().equals("wss")) {
				throw new WebSocketException("unsupported scheme for WebSockets URI");
			}

			if (this.mWsUri.getScheme().equals("wss")) {
				throw new WebSocketException("secure WebSockets not implemented");
			}

			this.mWsScheme = this.mWsUri.getScheme();

			if (this.mWsUri.getPort() == -1) {
				if (this.mWsScheme.equals("ws")) {
					this.mWsPort = 80;
				} else {
					this.mWsPort = 443;
				}
			} else {
				this.mWsPort = this.mWsUri.getPort();
			}

			if (this.mWsUri.getHost() == null) {
				throw new WebSocketException("no host specified in WebSockets URI");
			} else {
				this.mWsHost = this.mWsUri.getHost();
			}

			if (this.mWsUri.getRawPath() == null || this.mWsUri.getRawPath().equals("")) {
				this.mWsPath = "/";
			} else {
				this.mWsPath = this.mWsUri.getRawPath();
			}

			if (this.mWsUri.getRawQuery() == null || this.mWsUri.getRawQuery().equals("")) {
				this.mWsQuery = null;
			} else {
				this.mWsQuery = this.mWsUri.getRawQuery();
			}

		} catch (URISyntaxException e) {

			throw new WebSocketException("invalid WebSockets URI");
		}

		this.mWsSubprotocols = wsSubprotocols;
		this.mWsHeaders = headers;
		this.mWsHandler = wsHandler;

		// make copy of options!
		this.mOptions = new WebSocketOptions(options);

		// set connection active
		this.mActive = true;

		// use asynch connector on short-lived background thread
		new WebSocketConnector().start();
	}

	@Override
	public void disconnect() {
		if (this.mWriter != null) {
			this.mWriter.forward(new WebSocketMessage.Close(1000));
		} else {
			if (WebSocketConnection.DEBUG) {
				Log.d(WebSocketConnection.TAG, "could not send Close .. writer already NULL");
			}
		}
		if (this.mReader != null) {
			this.mReader.quit();
		} else {
			if (WebSocketConnection.DEBUG) {
				Log.d(WebSocketConnection.TAG, "could not send Close .. reader already NULL");
			}
		}
		this.mActive = false;
		this.mPrevConnected = false;
	}

	/**
	 * Reconnect to the server with the latest options
	 * 
	 * @return true if reconnection performed
	 */
	public boolean reconnect() {
		if (!this.isConnected() && (this.mWsUri != null)) {
			new WebSocketConnector().start();
			return true;
		}
		return false;
	}

	/**
	 * Perform reconnection
	 * 
	 * @return true if reconnection was scheduled
	 */
	protected boolean scheduleReconnect() {
		/**
		 * Reconnect only if: - connection active (connected but not
		 * disconnected) - has previous success connections - reconnect interval
		 * is set
		 */
		int interval = this.mOptions.getReconnectInterval();
		boolean need = this.mActive && this.mPrevConnected && (interval > 0);
		if (need) {
			if (WebSocketConnection.DEBUG) {
				Log.d(WebSocketConnection.TAG, "Reconnection scheduled");
			}
			this.mMasterHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (WebSocketConnection.DEBUG) {
						Log.d(WebSocketConnection.TAG, "Reconnecting...");
					}
					WebSocketConnection.this.reconnect();
				}
			}, interval);
		}
		return need;
	}

	/**
	 * Common close handler
	 * 
	 * @param code
	 *            Close code.
	 * @param reason
	 *            Close reason (human-readable).
	 */
	private void onClose(int code, String reason) {
		boolean reconnecting = false;

		if ((code == ConnectionHandler.CLOSE_CANNOT_CONNECT) || (code == ConnectionHandler.CLOSE_CONNECTION_LOST)) {
			reconnecting = this.scheduleReconnect();
		}

		if (this.mWsHandler != null) {
			try {
				if (reconnecting) {
					this.mWsHandler.onClose(ConnectionHandler.CLOSE_RECONNECT, reason);
				} else {
					this.mWsHandler.onClose(code, reason);
				}
			} catch (Exception e) {
				if (WebSocketConnection.DEBUG) {
					e.printStackTrace();
				}
			}
			// mWsHandler = null;
		} else {
			if (WebSocketConnection.DEBUG) {
				Log.d(WebSocketConnection.TAG, "mWsHandler already NULL");
			}
		}
	}

	/**
	 * Create master message handler.
	 */
	protected void createHandler() {

		this.mMasterHandler = new Handler(Looper.getMainLooper()) {

			public void handleMessage(Message msg) {

				if (msg.obj instanceof WebSocketMessage.TextMessage) {

					WebSocketMessage.TextMessage textMessage = (WebSocketMessage.TextMessage) msg.obj;

					if (WebSocketConnection.this.mWsHandler != null) {
						WebSocketConnection.this.mWsHandler.onTextMessage(textMessage.mPayload);
					} else {
						if (WebSocketConnection.DEBUG) {
							Log.d(WebSocketConnection.TAG, "could not call onTextMessage() .. handler already NULL");
						}
					}

				} else if (msg.obj instanceof WebSocketMessage.RawTextMessage) {

					WebSocketMessage.RawTextMessage rawTextMessage = (WebSocketMessage.RawTextMessage) msg.obj;

					if (WebSocketConnection.this.mWsHandler != null) {
						WebSocketConnection.this.mWsHandler.onRawTextMessage(rawTextMessage.mPayload);
					} else {
						if (WebSocketConnection.DEBUG) {
							Log.d(WebSocketConnection.TAG, "could not call onRawTextMessage() .. handler already NULL");
						}
					}

				} else if (msg.obj instanceof WebSocketMessage.BinaryMessage) {

					WebSocketMessage.BinaryMessage binaryMessage = (WebSocketMessage.BinaryMessage) msg.obj;

					if (WebSocketConnection.this.mWsHandler != null) {
						WebSocketConnection.this.mWsHandler.onBinaryMessage(binaryMessage.mPayload);
					} else {
						if (WebSocketConnection.DEBUG) {
							Log.d(WebSocketConnection.TAG, "could not call onBinaryMessage() .. handler already NULL");
						}
					}

				} else if (msg.obj instanceof WebSocketMessage.Ping) {

					WebSocketMessage.Ping ping = (WebSocketMessage.Ping) msg.obj;
					if (WebSocketConnection.DEBUG) {
						Log.d(WebSocketConnection.TAG, "WebSockets Ping received");
					}

					// reply with Pong
					WebSocketMessage.Pong pong = new WebSocketMessage.Pong();
					pong.mPayload = ping.mPayload;
					WebSocketConnection.this.mWriter.forward(pong);

				} else if (msg.obj instanceof WebSocketMessage.Pong) {

					@SuppressWarnings("unused")
					WebSocketMessage.Pong pong = (WebSocketMessage.Pong) msg.obj;

					if (WebSocketConnection.DEBUG) {
						Log.d(WebSocketConnection.TAG, "WebSockets Pong received");
					}

				} else if (msg.obj instanceof WebSocketMessage.Close) {

					WebSocketMessage.Close close = (WebSocketMessage.Close) msg.obj;

					if (WebSocketConnection.DEBUG) {
						Log.d(WebSocketConnection.TAG, "WebSockets Close received (" + close.mCode + " - " + close.mReason + ")");
					}

					final int tavendoCloseCode = (close.mCode == 1000) ? ConnectionHandler.CLOSE_NORMAL : ConnectionHandler.CLOSE_CONNECTION_LOST;

					if (WebSocketConnection.this.mActive) {
						WebSocketConnection.this.mWriter.forward(new WebSocketMessage.Close(1000));
					} else {
						// we've initiated disconnect, so ready to close the
						// channel
						try {
							WebSocketConnection.this.mTransportChannel.close();
						} catch (IOException e) {
							if (WebSocketConnection.DEBUG) {
								e.printStackTrace();
							}
						}
					}

					WebSocketConnection.this.onClose(tavendoCloseCode, close.mReason);

				} else if (msg.obj instanceof WebSocketMessage.ServerHandshake) {

					WebSocketMessage.ServerHandshake serverHandshake = (WebSocketMessage.ServerHandshake) msg.obj;

					if (WebSocketConnection.DEBUG) {
						Log.d(WebSocketConnection.TAG, "opening handshake received");
					}

					if (serverHandshake.mSuccess) {
						if (WebSocketConnection.this.mWsHandler != null) {
							WebSocketConnection.this.mWsHandler.onOpen();
						} else {
							if (WebSocketConnection.DEBUG) {
								Log.d(WebSocketConnection.TAG, "could not call onOpen() .. handler already NULL");
							}
						}
					}

				} else if (msg.obj instanceof WebSocketMessage.ConnectionLost) {

					@SuppressWarnings("unused")
					WebSocketMessage.ConnectionLost connnectionLost = (WebSocketMessage.ConnectionLost) msg.obj;
					WebSocketConnection.this.failConnection(ConnectionHandler.CLOSE_CONNECTION_LOST, "WebSockets connection lost");

				} else if (msg.obj instanceof WebSocketMessage.ProtocolViolation) {

					@SuppressWarnings("unused")
					WebSocketMessage.ProtocolViolation protocolViolation = (WebSocketMessage.ProtocolViolation) msg.obj;
					WebSocketConnection.this.failConnection(ConnectionHandler.CLOSE_PROTOCOL_ERROR, "WebSockets protocol violation");

				} else if (msg.obj instanceof WebSocketMessage.Error) {

					WebSocketMessage.Error error = (WebSocketMessage.Error) msg.obj;
					WebSocketConnection.this.failConnection(ConnectionHandler.CLOSE_INTERNAL_ERROR, "WebSockets internal error (" + error.mException.toString() + ")");

				} else if (msg.obj instanceof WebSocketMessage.ServerError) {

					WebSocketMessage.ServerError error = (WebSocketMessage.ServerError) msg.obj;
					WebSocketConnection.this.failConnection(ConnectionHandler.CLOSE_SERVER_ERROR, "Server error " + error.mStatusCode + " (" + error.mStatusMessage + ")");

				} else {

					WebSocketConnection.this.processAppMessage(msg.obj);

				}
			}
		};
	}

	protected void processAppMessage(Object message) {
	}

	/**
	 * Create WebSockets background writer.
	 */
	protected void createWriter() {

		this.mWriterThread = new HandlerThread("WebSocketWriter");
		this.mWriterThread.start();
		this.mWriter = new WebSocketWriter(this.mWriterThread.getLooper(), this.mMasterHandler, this.mTransportChannel, this.mOptions);

		if (WebSocketConnection.DEBUG) {
			Log.d(WebSocketConnection.TAG, "WS writer created and started");
		}
	}

	/**
	 * Create WebSockets background reader.
	 */
	protected void createReader() {

		this.mReader = new WebSocketReader(this.mMasterHandler, this.mTransportChannel, this.mOptions, "WebSocketReader");
		this.mReader.start();

		if (WebSocketConnection.DEBUG) {
			Log.d(WebSocketConnection.TAG, "WS reader created and started");
		}
	}
}
