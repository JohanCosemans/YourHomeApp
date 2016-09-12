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
package net.yourhome.app.views;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import net.yourhome.app.R;
import net.yourhome.app.canvas.CanvasFragment;

public abstract class WebsiteView extends DynamicView implements SwipeRefreshLayout.OnRefreshListener {

	protected RelativeLayout relativeLayout;
	protected SwipeRefreshLayout swipeLayout;
	protected android.webkit.WebView webView;

	@Override
	public void destroyView() {
		super.destroyView();
		this.relativeLayout = null;
		this.swipeLayout = null;
		this.webView = null;
	}

	public WebsiteView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
		this.refreshView();
	}

	@Override
	public View getView() {
		return this.relativeLayout;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(this.layoutParameters.width, this.layoutParameters.height);
		// params.leftMargin = layoutParameters.left;
		// params.topMargin = layoutParameters.left;
		this.relativeLayout = new RelativeLayout(this.canvas.getActivity());
		this.relativeLayout.setLayoutParams(params);
		this.relativeLayout.setRotation((float) this.layoutParameters.rotation);
		this.relativeLayout.setX(this.layoutParameters.left);
		this.relativeLayout.setY(this.layoutParameters.top);
		LayoutInflater inflater = this.canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_html, this.relativeLayout);
		this.webView = (android.webkit.WebView) this.relativeLayout.findViewById(R.id.webView);
		this.webView.getSettings().setJavaScriptEnabled(true);
		this.webView.setBackgroundColor(Color.TRANSPARENT);
		this.swipeLayout = (SwipeRefreshLayout) this.relativeLayout.findViewById(R.id.swipe_container);
		this.swipeLayout.setOnRefreshListener(this);

		this.webView.setScrollBarStyle(android.webkit.WebView.SCROLLBARS_OUTSIDE_OVERLAY);

		this.webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
				view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				return true;
			}

			public void onPageFinished(android.webkit.WebView view, String url) {
				if (WebsiteView.this.swipeLayout != null) {
					WebsiteView.this.swipeLayout.setRefreshing(false);
				}
			}

		});
	}

	public void addBinding(JSONObject jsonObject) {
	}

}
