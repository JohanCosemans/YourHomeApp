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
package net.yourhome.app.net.discovery;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.common.net.model.ServerInfo;

public class HomeServerHost {

	// Set paths on server
	// public static final String CONFIGURATIONS = "configurations";
	public static final String WEBSOCKET = "websocket";
	// public static final String SERVER_INFO = "serverinfo.xml";

	public int configurationVersion;
	public String configurationFileName;

	public String ipAddress = null;
	public int port = 80;
	public String name = "";
	private ServerInfo info;

	public HomeServerHost(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public HomeServerHost(String ip, String serverInfoString) throws JSONException {
		this.ipAddress = ip;
		this.info = new ServerInfo(new JSONObject(serverInfoString));
		this.port = this.info.getPort();
	}

	public URI getDesignerAddress() {
		String designerAddress = "http://" + this.ipAddress;
		if (this.port != 80) {
			designerAddress += ":" + this.port;
		}
		;
		designerAddress += "/YourHomeDesigner";
		try {
			return new URI(designerAddress);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public boolean getDetails() {
		try {
			// Build url
			HomeServerConnector connector = HomeServerConnector.getInstance();
			String result = connector.sendApiCall(this.ipAddress, this.port, "/Info");
			if (result != null) {
				this.info = new ServerInfo(new JSONObject(result));
				this.port = this.info.getPort();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @return the info
	 */
	public ServerInfo getInfo() {
		return this.info;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	public void setInfo(ServerInfo info) {
		this.info = info;
	}

}
