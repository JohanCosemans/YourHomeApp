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

import android.os.AsyncTask;
import android.util.Log;
import net.yourhome.app.net.HomeServerConnector;

public class IPHostnameChecker extends AsyncTask<Void, String, HomeServerHost> {

	protected boolean connectionTestResult = false;

	protected String ipAddress; // LAN
	protected int port; // LAN

	protected String ipAddressExt; // Internet
	protected int portExt; // Internet

	protected boolean localFirst;
	protected boolean resumeFromSuspend = false;
	protected String connectionModeBeforeSuspend = null;

	// protected HomeServerHost host = null;

	public IPHostnameChecker(String ipAddress, int port, String ipAddressExt, int portExt, boolean localFirst) {
		this.ipAddress = ipAddress;
		this.port = port;

		this.ipAddressExt = ipAddressExt;
		this.portExt = portExt;

		this.localFirst = localFirst;
	}

	public IPHostnameChecker(String ipAddress, int port, String ipAddressExt, int portExt, boolean localFirst, boolean resumeFromSuspend, String connectionModeBeforeSuspend) {
		this(ipAddress, port, ipAddressExt, portExt, localFirst);
		this.resumeFromSuspend = resumeFromSuspend;
		this.connectionModeBeforeSuspend = connectionModeBeforeSuspend;
	}

	@Override
	protected HomeServerHost doInBackground(Void... arg0) {
		HomeServerHost host = null;
		// Try to connect to homeserverip
		if (this.ipAddress == "" && this.ipAddressExt == "") {
			// Don't bother checking
			this.connectionTestResult = false;
		} else {
			// Disconnect all other sessions first (if there are any)
			HomeServerConnector homeServerConnector = HomeServerConnector.getInstance();
			homeServerConnector.disconnect();

			host = new HomeServerHost(this.ipAddress, this.port);
			if (this.localFirst) {
				host.ipAddress = this.ipAddress;
				host.port = this.port;
				if (host.ipAddress != null) {
					publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
					if (!host.getDetails()) {
						host.ipAddress = this.ipAddressExt;
						host.port = this.portExt;
						if (host.ipAddress != null) {
							publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
							host.getDetails();
						}
					}
				}
			} else {
				host.ipAddress = this.ipAddressExt;
				host.port = this.portExt;
				if (host.ipAddress != null) {
					publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
					if (!host.getDetails()) {
						host.ipAddress = this.ipAddress;
						host.port = this.port;
						if (host.ipAddress != null) {
							publishProgress("Connecting to " + host.ipAddress + ":" + host.port);
							host.getDetails();
						}
					}
				}
			}

			if (host.getInfo() == null) {
				this.connectionTestResult = false;
				return null;
			} else {
				this.connectionTestResult = true;
			}

			Log.d("IPHostnameChecker", "connectionTestResult: " + this.connectionTestResult);
		}
		return host;
	}

	@Override
	protected void onProgressUpdate(String... status) {

	}
}