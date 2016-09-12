package net.yourhome.app.views;

import android.annotation.SuppressLint;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.common.net.model.viewproperties.WebLink;

import org.json.JSONException;
import org.json.JSONObject;

public class WebLinkView extends WebsiteView {

	private String url;

	public WebLinkView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageItemId, viewProperties, bindingProperties);
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Properties
		url = this.properties.get(WebLink.URL).getValue();
        swipeLayout.setOnRefreshListener(this);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            public void onPageFinished(WebView view, String url) {
                if(swipeLayout != null) {
                    swipeLayout.setRefreshing(false);
                }
            }

        });
	}
    @Override public void onRefresh() {
        swipeLayout.setRefreshing(true);
        webView.loadUrl(url);
    }

	@Override
	public void refreshView() {
        swipeLayout.setRefreshing(true);
        webView.loadUrl(url);
	}
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        // Do nothing
    }
}
