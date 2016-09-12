package net.yourhome.app.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import net.yourhome.app.R;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.model.viewproperties.WebRSS;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

public class WebRSSView extends WebsiteView  {

    private final static String RSS_FEED_PATH = "/rss/rss.html";
	private String url;
	private String fullFeedUrl;
    private String color;

	public WebRSSView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
	}
	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Properties
		url = this.properties.get(WebRSS.URL).getValue();
		color = this.properties.get(WebRSS.COLOR).getValue();
        try {
            fullFeedUrl = Configuration.getInstance().getHomeServerProtocol()
                            + "://"
                            + Configuration.getInstance().getHomeServerHostName(canvas.getActivity().getBaseContext())
                            + WebRSSView.RSS_FEED_PATH
                            + "?feedUrl="+ URLEncoder.encode(url,"UTF-8")
                            + "&textColor="+color.substring(1);
        } catch (UnsupportedEncodingException e) {}
        swipeLayout.setRefreshing(true);

	}
    @Override public void onRefresh() {
        swipeLayout.setRefreshing(true);
        webView.loadUrl(fullFeedUrl);
    }

	@Override
	public void refreshView() {
        swipeLayout.setRefreshing(true);
        webView.loadUrl(fullFeedUrl);
	}
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        // Do nothing
    }
}
