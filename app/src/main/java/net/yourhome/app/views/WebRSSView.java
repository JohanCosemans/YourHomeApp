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
package net.yourhome.app.views;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.model.viewproperties.WebRSS;

public class WebRSSView extends WebsiteView {

	private final static String RSS_FEED_PATH = "/rss/rss.html";
	private String url;
	private String fullFeedUrl;
	private String color;

	public WebRSSView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		this.url = this.properties.get(WebRSS.URL).getValue();
		this.color = this.properties.get(WebRSS.COLOR).getValue();
		try {
			this.fullFeedUrl = Configuration.getInstance().getHomeServerProtocol() + "://" + Configuration.getInstance().getHomeServerHostName(this.canvas.getActivity().getBaseContext()) + WebRSSView.RSS_FEED_PATH + "?feedUrl=" + URLEncoder.encode(this.url, "UTF-8") + "&textColor=" + this.color.substring(1);
		} catch (UnsupportedEncodingException e) {
		}
		this.swipeLayout.setRefreshing(true);

	}

	@Override
	public void onRefresh() {
		this.swipeLayout.setRefreshing(true);
		this.webView.loadUrl(this.fullFeedUrl);
	}

	@Override
	public void refreshView() {
		this.swipeLayout.setRefreshing(true);
		this.webView.loadUrl(this.fullFeedUrl);
	}

	public static void createBinding(String stageItemId, JSONObject bindingProperties) {
		// Do nothing
	}
}
