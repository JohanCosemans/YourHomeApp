package net.yourhome.app.views;

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
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;

public abstract class WebsiteView extends DynamicView implements SwipeRefreshLayout.OnRefreshListener {

	protected RelativeLayout relativeLayout;
    protected SwipeRefreshLayout swipeLayout;
	protected android.webkit.WebView webView;

    @Override
    public void destroyView() {
        super.destroyView();
        relativeLayout = null;
        swipeLayout = null;
        webView = null;
    }
	public WebsiteView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
		buildView(viewProperties);
		addBinding(bindingProperties);
		refreshView();
	}
	
	@Override
	public View getView() {
		return relativeLayout;
	}

	@SuppressLint("NewApi")
	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//params.leftMargin = layoutParameters.left;
		//params.topMargin = layoutParameters.left;
		relativeLayout = new RelativeLayout(canvas.getActivity());
		relativeLayout.setLayoutParams(params);
		relativeLayout.setRotation((float) layoutParameters.rotation);
		relativeLayout.setX(layoutParameters.left);
		relativeLayout.setY(layoutParameters.top);
		LayoutInflater inflater = canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_html, relativeLayout);
		webView = (android.webkit.WebView)relativeLayout.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        swipeLayout = (SwipeRefreshLayout) relativeLayout.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        webView.setScrollBarStyle(android.webkit.WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }

            public void onPageFinished(android.webkit.WebView view, String url) {
                if(swipeLayout != null) {
                    swipeLayout.setRefreshing(false);
                }
            }

        });
	}
    public void addBinding(JSONObject jsonObject) {}


}
