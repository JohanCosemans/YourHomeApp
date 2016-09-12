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

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

/**
 * WebSocket reader, the receiving leg of a WebSockets connection. This runs on
 * it's own background thread and posts messages to master thread's message
 * queue for there to be consumed by the application. The only method that needs
 * to be called (from foreground thread) is quit(), which gracefully shuts down
 * the background receiver thread.
 */
public class WebSocketReader extends Thread {

	private static final boolean DEBUG = true;
	private static final String TAG = WebSocketReader.class.getName();

	private final Handler mMaster;
	private final SocketChannel mSocket;
	private final WebSocketOptions mOptions;

	private final ByteBuffer mFrameBuffer;
	private NoCopyByteArrayOutputStream mMessagePayload;

	private final static int STATE_CLOSED = 0;
	private final static int STATE_CONNECTING = 1;
	private final static int STATE_CLOSING = 2;
	private final static int STATE_OPEN = 3;

	private boolean mStopped = false;
	private int mState;

	private boolean mInsideMessage = false;
	private int mMessageOpcode;

	/// Frame currently being received.
	private FrameHeader mFrameHeader;

	private Utf8Validator mUtf8Validator = new Utf8Validator();

	/**
	 * WebSockets frame metadata.
	 */
	private static class FrameHeader {
		public int mOpcode;
		public boolean mFin;
		@SuppressWarnings("unused")
		public int mReserved;
		public int mHeaderLen;
		public int mPayloadLen;
		public int mTotalLen;
		public byte[] mMask;
	}

	/**
	 * Create new WebSockets background reader.
	 *
	 * @param master
	 *            The message handler of master (foreground thread).
	 * @param socket
	 *            The socket channel created on foreground thread.
	 */
	public WebSocketReader(Handler master, SocketChannel socket, WebSocketOptions options, String threadName) {

		super(threadName);

		this.mMaster = master;
		this.mSocket = socket;
		this.mOptions = options;

		this.mFrameBuffer = ByteBuffer.allocateDirect(options.getMaxFramePayloadSize() + 14);
		this.mMessagePayload = new NoCopyByteArrayOutputStream(options.getMaxMessagePayloadSize());

		this.mFrameHeader = null;
		this.mState = WebSocketReader.STATE_CONNECTING;

		if (WebSocketReader.DEBUG) {
			Log.d(WebSocketReader.TAG, "created");
		}
	}

	/**
	 * Graceful shutdown of background reader thread (called from master).
	 */
	public void quit() {
		this.mState = WebSocketReader.STATE_CLOSED;
		if (WebSocketReader.DEBUG) {
			Log.d(WebSocketReader.TAG, "quit");
		}
	}

	/**
	 * Notify the master (foreground thread) of WebSockets message received and
	 * unwrapped.
	 *
	 * @param message
	 *            Message to send to master.
	 */
	protected void notify(Object message) {

		Message msg = this.mMaster.obtainMessage();
		msg.obj = message;
		this.mMaster.sendMessage(msg);
	}

	/**
	 * Process incoming WebSockets data (after handshake).
	 */
	private boolean processData() throws Exception {

		// outside frame?
		if (this.mFrameHeader == null) {

			// need at least 2 bytes from WS frame header to start processing
			if (this.mFrameBuffer.position() >= 2) {

				byte b0 = this.mFrameBuffer.get(0);
				boolean fin = (b0 & 0x80) != 0;
				int rsv = (b0 & 0x70) >> 4;
				int opcode = b0 & 0x0f;

				byte b1 = this.mFrameBuffer.get(1);
				boolean masked = (b1 & 0x80) != 0;
				int payload_len1 = b1 & 0x7f;

				// now check protocol compliance

				if (rsv != 0) {
					throw new WebSocketException("RSV != 0 and no extension negotiated");
				}

				if (masked) {
					// currently, we don't allow this. need to see whats the
					// final spec.
					throw new WebSocketException("masked server frame");
				}

				if (opcode > 7) {
					// control frame
					if (!fin) {
						throw new WebSocketException("fragmented control frame");
					}
					if (payload_len1 > 125) {
						throw new WebSocketException("control frame with payload length > 125 octets");
					}
					if (opcode != 8 && opcode != 9 && opcode != 10) {
						throw new WebSocketException("control frame using reserved opcode " + opcode);
					}
					if (opcode == 8 && payload_len1 == 1) {
						throw new WebSocketException("received close control frame with payload len 1");
					}
				} else {
					// message frame
					if (opcode != 0 && opcode != 1 && opcode != 2) {
						throw new WebSocketException("data frame using reserved opcode " + opcode);
					}
					if (!this.mInsideMessage && opcode == 0) {
						throw new WebSocketException("received continuation data frame outside fragmented message");
					}
					if (this.mInsideMessage && opcode != 0) {
						throw new WebSocketException("received non-continuation data frame while inside fragmented message");
					}
				}

				int mask_len = masked ? 4 : 0;
				int header_len = 0;

				if (payload_len1 < 126) {
					header_len = 2 + mask_len;
				} else if (payload_len1 == 126) {
					header_len = 2 + 2 + mask_len;
				} else if (payload_len1 == 127) {
					header_len = 2 + 8 + mask_len;
				} else {
					// should not arrive here
					throw new Exception("logic error");
				}

				// continue when complete frame header is available
				if (this.mFrameBuffer.position() >= header_len) {

					// determine frame payload length
					int i = 2;
					long payload_len = 0;
					if (payload_len1 == 126) {
						payload_len = ((0xff & this.mFrameBuffer.get(i)) << 8) | (0xff & this.mFrameBuffer.get(i + 1));
						if (payload_len < 126) {
							throw new WebSocketException("invalid data frame length (not using minimal length encoding)");
						}
						i += 2;
					} else if (payload_len1 == 127) {
						if ((0x80 & this.mFrameBuffer.get(i + 0)) != 0) {
							throw new WebSocketException("invalid data frame length (> 2^63)");
						}
						payload_len = ((long) (0xff & this.mFrameBuffer.get(i + 0)) << 56) | ((long) (0xff & this.mFrameBuffer.get(i + 1)) << 48) | ((long) (0xff & this.mFrameBuffer.get(i + 2)) << 40) | ((long) (0xff & this.mFrameBuffer.get(i + 3)) << 32) | ((long) (0xff & this.mFrameBuffer.get(i + 4)) << 24) | ((long) (0xff & this.mFrameBuffer.get(i + 5)) << 16) | ((long) (0xff & this.mFrameBuffer.get(i + 6)) << 8) | (0xff & this.mFrameBuffer.get(i + 7));
						if (payload_len < 65536) {
							throw new WebSocketException("invalid data frame length (not using minimal length encoding)");
						}
						i += 8;
					} else {
						payload_len = payload_len1;
					}

					// immediately bail out on frame too large
					if (payload_len > this.mOptions.getMaxFramePayloadSize()) {
						throw new WebSocketException("frame payload too large");
					}

					// save frame header metadata
					this.mFrameHeader = new FrameHeader();
					this.mFrameHeader.mOpcode = opcode;
					this.mFrameHeader.mFin = fin;
					this.mFrameHeader.mReserved = rsv;
					this.mFrameHeader.mPayloadLen = (int) payload_len;
					this.mFrameHeader.mHeaderLen = header_len;
					this.mFrameHeader.mTotalLen = this.mFrameHeader.mHeaderLen + this.mFrameHeader.mPayloadLen;
					if (masked) {
						this.mFrameHeader.mMask = new byte[4];
						for (int j = 0; j < 4; ++j) {
							this.mFrameHeader.mMask[i] = (byte) (0xff & this.mFrameBuffer.get(i + j));
						}
						i += 4;
					} else {
						this.mFrameHeader.mMask = null;
					}

					// continue processing when payload empty or completely
					// buffered
					return this.mFrameHeader.mPayloadLen == 0 || this.mFrameBuffer.position() >= this.mFrameHeader.mTotalLen;

				} else {

					// need more data
					return false;
				}
			} else {

				// need more data
				return false;
			}

		} else {

			/// \todo refactor this for streaming processing, incl. fail fast on
			/// invalid UTF-8 within frame already

			// within frame

			// see if we buffered complete frame
			if (this.mFrameBuffer.position() >= this.mFrameHeader.mTotalLen) {

				// cut out frame payload
				byte[] framePayload = null;
				int oldPosition = this.mFrameBuffer.position();
				if (this.mFrameHeader.mPayloadLen > 0) {
					framePayload = new byte[this.mFrameHeader.mPayloadLen];
					this.mFrameBuffer.position(this.mFrameHeader.mHeaderLen);
					this.mFrameBuffer.get(framePayload, 0, this.mFrameHeader.mPayloadLen);
				}
				this.mFrameBuffer.position(this.mFrameHeader.mTotalLen);
				this.mFrameBuffer.limit(oldPosition);
				this.mFrameBuffer.compact();

				if (this.mFrameHeader.mOpcode > 7) {
					// control frame

					if (this.mFrameHeader.mOpcode == 8) {

						int code = 1005; // CLOSE_STATUS_CODE_NULL : no status
											// code received
						String reason = null;

						if (this.mFrameHeader.mPayloadLen >= 2) {

							// parse and check close code - see
							// http://tools.ietf.org/html/rfc6455#section-7.4
							code = (framePayload[0] & 0xff) * 256 + (framePayload[1] & 0xff);
							if (code < 1000 || (code >= 1000 && code <= 2999 && code != 1000 && code != 1001 && code != 1002 && code != 1003 && code != 1007 && code != 1008 && code != 1009 && code != 1010 && code != 1011) || code >= 5000) {

								throw new WebSocketException("invalid close code " + code);
							}

							// parse and check close reason
							if (this.mFrameHeader.mPayloadLen > 2) {

								byte[] ra = new byte[this.mFrameHeader.mPayloadLen - 2];
								System.arraycopy(framePayload, 2, ra, 0, this.mFrameHeader.mPayloadLen - 2);

								Utf8Validator val = new Utf8Validator();
								val.validate(ra);
								if (!val.isValid()) {
									throw new WebSocketException("invalid close reasons (not UTF-8)");
								} else {
									reason = new String(ra, "UTF-8");
								}
							}
						}
						this.onClose(code, reason);

					} else if (this.mFrameHeader.mOpcode == 9) {
						// dispatch WS ping
						this.onPing(framePayload);

					} else if (this.mFrameHeader.mOpcode == 10) {
						// dispatch WS pong
						this.onPong(framePayload);

					} else {

						// should not arrive here (handled before)
						throw new Exception("logic error");
					}

				} else {
					// message frame

					if (!this.mInsideMessage) {
						// new message started
						this.mInsideMessage = true;
						this.mMessageOpcode = this.mFrameHeader.mOpcode;
						if (this.mMessageOpcode == 1 && this.mOptions.getValidateIncomingUtf8()) {
							this.mUtf8Validator.reset();
						}
					}

					if (framePayload != null) {

						// immediately bail out on message too large
						if (this.mMessagePayload.size() + framePayload.length > this.mOptions.getMaxMessagePayloadSize()) {
							throw new WebSocketException("message payload too large");
						}

						// validate incoming UTF-8
						if (this.mMessageOpcode == 1 && this.mOptions.getValidateIncomingUtf8() && !this.mUtf8Validator.validate(framePayload)) {
							throw new WebSocketException("invalid UTF-8 in text message payload");
						}

						// buffer frame payload for message
						this.mMessagePayload.write(framePayload);
					}

					// on final frame ..
					if (this.mFrameHeader.mFin) {

						if (this.mMessageOpcode == 1) {

							// verify that UTF-8 ends on codepoint
							if (this.mOptions.getValidateIncomingUtf8() && !this.mUtf8Validator.isValid()) {
								throw new WebSocketException("UTF-8 text message payload ended within Unicode code point");
							}

							// deliver text message
							if (this.mOptions.getReceiveTextMessagesRaw()) {

								// dispatch WS text message as raw (but
								// validated) UTF-8
								this.onRawTextMessage(this.mMessagePayload.toByteArray());

							} else {

								// dispatch WS text message as Java String
								// (previously already validated)
								String s = new String(this.mMessagePayload.toByteArray(), "UTF-8");
								this.onTextMessage(s);
							}

						} else if (this.mMessageOpcode == 2) {

							// dispatch WS binary message
							this.onBinaryMessage(this.mMessagePayload.toByteArray());

						} else {

							// should not arrive here (handled before)
							throw new Exception("logic error");
						}

						// ok, message completed - reset all
						this.mInsideMessage = false;
						this.mMessagePayload.reset();
					}
				}

				// reset frame
				this.mFrameHeader = null;

				// reprocess if more data left
				return this.mFrameBuffer.position() > 0;

			} else {

				// need more data
				return false;
			}
		}
	}

	/**
	 * WebSockets handshake reply from server received, default notifies master.
	 * 
	 * @param success
	 *            Success handshake flag
	 */
	protected void onHandshake(boolean success) {

		this.notify(new WebSocketMessage.ServerHandshake(success));
	}

	/**
	 * WebSockets close received, default notifies master.
	 */
	protected void onClose(int code, String reason) {

		this.notify(new WebSocketMessage.Close(code, reason));
	}

	/**
	 * WebSockets ping received, default notifies master.
	 *
	 * @param payload
	 *            Ping payload or null.
	 */
	protected void onPing(byte[] payload) {

		this.notify(new WebSocketMessage.Ping(payload));
	}

	/**
	 * WebSockets pong received, default notifies master.
	 *
	 * @param payload
	 *            Pong payload or null.
	 */
	protected void onPong(byte[] payload) {

		this.notify(new WebSocketMessage.Pong(payload));
	}

	/**
	 * WebSockets text message received, default notifies master. This will only
	 * be called when the option receiveTextMessagesRaw HAS NOT been set.
	 *
	 * @param payload
	 *            Text message payload as Java String decoded from raw UTF-8
	 *            payload or null (empty payload).
	 */
	protected void onTextMessage(String payload) {

		this.notify(new WebSocketMessage.TextMessage(payload));
	}

	/**
	 * WebSockets text message received, default notifies master. This will only
	 * be called when the option receiveTextMessagesRaw HAS been set.
	 *
	 * @param payload
	 *            Text message payload as raw UTF-8 octets or null (empty
	 *            payload).
	 */
	protected void onRawTextMessage(byte[] payload) {

		this.notify(new WebSocketMessage.RawTextMessage(payload));
	}

	/**
	 * WebSockets binary message received, default notifies master.
	 *
	 * @param payload
	 *            Binary message payload or null (empty payload).
	 */
	protected void onBinaryMessage(byte[] payload) {

		this.notify(new WebSocketMessage.BinaryMessage(payload));
	}

	/**
	 * Process WebSockets handshake received from server.
	 */
	private boolean processHandshake() throws UnsupportedEncodingException {

		boolean res = false;
		for (int pos = this.mFrameBuffer.position() - 4; pos >= 0; --pos) {
			if (this.mFrameBuffer.get(pos + 0) == 0x0d && this.mFrameBuffer.get(pos + 1) == 0x0a && this.mFrameBuffer.get(pos + 2) == 0x0d && this.mFrameBuffer.get(pos + 3) == 0x0a) {

				/// \todo process & verify handshake from server
				/// \todo forward subprotocol, if any

				int oldPosition = this.mFrameBuffer.position();

				// Check HTTP status code
				boolean serverError = false;
				if (this.mFrameBuffer.get(0) == 'H' && this.mFrameBuffer.get(1) == 'T' && this.mFrameBuffer.get(2) == 'T' && this.mFrameBuffer.get(3) == 'P') {

					Pair<Integer, String> status = this.parseHttpStatus();
					if (status.first >= 300) {
						// Invalid status code for success connection
						this.notify(new WebSocketMessage.ServerError(status.first, status.second));
						serverError = true;
					}
				}

				this.mFrameBuffer.position(pos + 4);
				this.mFrameBuffer.limit(oldPosition);
				this.mFrameBuffer.compact();

				if (!serverError) {
					// process further when data after HTTP headers left in
					// buffer
					res = this.mFrameBuffer.position() > 0;

					this.mState = WebSocketReader.STATE_OPEN;
				} else {
					res = true;
					this.mState = WebSocketReader.STATE_CLOSED;
					this.mStopped = true;
				}

				this.onHandshake(!serverError);
				break;
			}
		}
		return res;
	}

	@SuppressWarnings("unused")
	private Map<String, String> parseHttpHeaders(byte[] buffer) throws UnsupportedEncodingException {
		// TODO: use utf-8 validator?
		String s = new String(buffer, "UTF-8");
		Map<String, String> headers = new HashMap<String, String>();

		String[] lines = s.split("\r\n");
		for (String line : lines) {
			if (line.length() > 0) {
				String[] h = line.split(": ");
				if (h.length == 2) {
					headers.put(h[0], h[1]);
					Log.w(WebSocketReader.TAG, String.format("'%s'='%s'", h[0], h[1]));
				}
			}
		}

		return headers;
	}

	private Pair<Integer, String> parseHttpStatus() throws UnsupportedEncodingException {
		int beg, end;
		// Find first space
		for (beg = 4; beg < this.mFrameBuffer.position(); ++beg) {
			if (this.mFrameBuffer.get(beg) == ' ') {
				break;
			}
		}
		// Find second space
		for (end = beg + 1; end < this.mFrameBuffer.position(); ++end) {
			if (this.mFrameBuffer.get(end) == ' ') {
				break;
			}
		}
		// Parse status code between them
		++beg;
		int statusCode = 0;
		for (int i = 0; beg + i < end; ++i) {
			int digit = (this.mFrameBuffer.get(beg + i) - 0x30);
			statusCode *= 10;
			statusCode += digit;
		}
		// Find end of line to extract error message
		++end;
		int eol;
		for (eol = end; eol < this.mFrameBuffer.position(); ++eol) {
			if (this.mFrameBuffer.get(eol) == 0x0d) {
				break;
			}
		}
		int statusMessageLength = eol - end;
		byte[] statusBuf = new byte[statusMessageLength];
		this.mFrameBuffer.position(end);
		this.mFrameBuffer.get(statusBuf, 0, statusMessageLength);
		String statusMessage = new String(statusBuf, "UTF-8");
		if (WebSocketReader.DEBUG) {
			Log.w(WebSocketReader.TAG, String.format("Status: %d (%s)", statusCode, statusMessage));
		}
		return new Pair<Integer, String>(statusCode, statusMessage);
	}

	/**
	 * Consume data buffered in mFrameBuffer.
	 */
	private boolean consumeData() throws Exception {

		if (this.mState == WebSocketReader.STATE_OPEN || this.mState == WebSocketReader.STATE_CLOSING) {

			return this.processData();

		} else if (this.mState == WebSocketReader.STATE_CONNECTING) {

			return this.processHandshake();

		} else if (this.mState == WebSocketReader.STATE_CLOSED) {

			return false;

		} else {
			// should not arrive here
			return false;
		}

	}

	/**
	 * Run the background reader thread loop.
	 */
	@Override
	public void run() {

		if (WebSocketReader.DEBUG) {
			Log.d(WebSocketReader.TAG, "running");
		}

		try {

			this.mFrameBuffer.clear();
			do {
				// blocking read on socket
				int len = this.mSocket.read(this.mFrameBuffer);
				if (len > 0) {
					// process buffered data
					while (this.consumeData()) {
					}
				} else if (this.mState == WebSocketReader.STATE_CLOSED) {
					this.notify(new WebSocketMessage.Close(1000)); // Connection
																	// has been
																	// closed
																	// normally
					this.mStopped = true;
				} else if (len < 0) {

					if (WebSocketReader.DEBUG) {
						Log.d(WebSocketReader.TAG, "run() : ConnectionLost");
					}

					this.notify(new WebSocketMessage.ConnectionLost());
					this.mStopped = true;
				}
			} while (!this.mStopped);

		} catch (WebSocketException e) {

			if (WebSocketReader.DEBUG) {
				Log.d(WebSocketReader.TAG, "run() : WebSocketException (" + e.toString() + ")");
			}

			// wrap the exception and notify master
			this.notify(new WebSocketMessage.ProtocolViolation(e));

		} catch (SocketException e) {

			if (WebSocketReader.DEBUG) {
				Log.d(WebSocketReader.TAG, "run() : SocketException (" + e.toString() + ")");
			}

			// wrap the exception and notify master
			this.notify(new WebSocketMessage.ConnectionLost());
			;

		} catch (Exception e) {

			if (WebSocketReader.DEBUG) {
				Log.d(WebSocketReader.TAG, "run() : Exception (" + e.toString() + ")");
			}

			// wrap the exception and notify master
			this.notify(new WebSocketMessage.Error(e));

		} finally {

			this.mStopped = true;
		}

		if (WebSocketReader.DEBUG) {
			Log.d(WebSocketReader.TAG, "ended");
		}
	}
}
