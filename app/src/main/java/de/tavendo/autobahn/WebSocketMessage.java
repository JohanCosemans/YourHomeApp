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

import java.util.List;

import org.apache.http.message.BasicNameValuePair;

/**
 * WebSockets message classes. The master thread and the background
 * reader/writer threads communicate using these messages for WebSockets
 * connections.
 */
public class WebSocketMessage {

	/// Base message class.
	public static class Message {
	}

	/// Quite background thread.
	public static class Quit extends Message {
	}

	/// Initial WebSockets handshake (client request).
	public static class ClientHandshake extends Message {

		public String mHost;
		public String mPath;
		public String mQuery;
		public String mOrigin;
		public String[] mSubprotocols;
		public List<BasicNameValuePair> mHeaderList;

		ClientHandshake(String host) {
			this.mHost = host;
			this.mPath = "/";
			this.mOrigin = null;
			this.mSubprotocols = null;
			this.mHeaderList = null;
		}

		ClientHandshake(String host, String path, String origin) {
			this.mHost = host;
			this.mPath = path;
			this.mOrigin = origin;
			this.mSubprotocols = null;
		}

		ClientHandshake(String host, String path, String origin, String[] subprotocols) {
			this.mHost = host;
			this.mPath = path;
			this.mOrigin = origin;
			this.mSubprotocols = subprotocols;
		}
	}

	/// Initial WebSockets handshake (server response).
	public static class ServerHandshake extends Message {
		public boolean mSuccess;

		public ServerHandshake(boolean success) {
			this.mSuccess = success;
		}
	}

	/// WebSockets connection lost
	public static class ConnectionLost extends Message {
	}

	public static class ServerError extends Message {
		public int mStatusCode;
		public String mStatusMessage;

		public ServerError(int statusCode, String statusMessage) {
			this.mStatusCode = statusCode;
			this.mStatusMessage = statusMessage;
		}

	}

	/// WebSockets reader detected WS protocol violation.
	public static class ProtocolViolation extends Message {

		public WebSocketException mException;

		public ProtocolViolation(WebSocketException e) {
			this.mException = e;
		}
	}

	/// An exception occured in the WS reader or WS writer.
	public static class Error extends Message {

		public Exception mException;

		public Error(Exception e) {
			this.mException = e;
		}
	}

	/// WebSockets text message to send or received.
	public static class TextMessage extends Message {

		public String mPayload;

		TextMessage(String payload) {
			this.mPayload = payload;
		}
	}

	/// WebSockets raw (UTF-8) text message to send or received.
	public static class RawTextMessage extends Message {

		public byte[] mPayload;

		RawTextMessage(byte[] payload) {
			this.mPayload = payload;
		}
	}

	/// WebSockets binary message to send or received.
	public static class BinaryMessage extends Message {

		public byte[] mPayload;

		BinaryMessage(byte[] payload) {
			this.mPayload = payload;
		}
	}

	/// WebSockets close to send or received.
	public static class Close extends Message {

		public int mCode;
		public String mReason;

		Close() {
			this.mCode = -1;
			this.mReason = null;
		}

		Close(int code) {
			this.mCode = code;
			this.mReason = null;
		}

		Close(int code, String reason) {
			this.mCode = code;
			this.mReason = reason;
		}
	}

	/// WebSockets ping to send or received.
	public static class Ping extends Message {

		public byte[] mPayload;

		Ping() {
			this.mPayload = null;
		}

		Ping(byte[] payload) {
			this.mPayload = payload;
		}
	}

	/// WebSockets pong to send or received.
	public static class Pong extends Message {

		public byte[] mPayload;

		Pong() {
			this.mPayload = null;
		}

		Pong(byte[] payload) {
			this.mPayload = payload;
		}
	}

}
