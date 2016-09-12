package net.yourhome.app.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.model.viewproperties.ClockDigital;
import net.yourhome.app.R;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ClockDigitalView extends DynamicView {

	protected RelativeLayout relativeLayout;
	protected TextView clockTextView;
	protected SimpleDateFormat clockFormat;
	protected SimpleDateFormat amPmFormat;
	protected BroadcastReceiver broadcastReceiver; 

	private int color;
	private boolean amPm;
	private Double size;
	
	public ClockDigitalView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId,viewProperties,bindingProperties);
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
		
		// Properties
		color = Color.parseColor(this.properties.get(ClockDigital.COLOR).getValue());
		size = Double.parseDouble(this.properties.get(ClockDigital.SIZE).getValue());
		amPm = Boolean.parseBoolean(this.properties.get(ClockDigital.AMPM).getValue());

		// Layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		//params.leftMargin = layoutParameters.left;
		//params.topMargin = layoutParameters.top;
		relativeLayout = new RelativeLayout(canvas.getActivity());
		relativeLayout.setLayoutParams(params);
		relativeLayout.setRotation((float)layoutParameters.rotation);
		relativeLayout.setGravity(Gravity.CENTER);
		relativeLayout.setX(layoutParameters.left);
		relativeLayout.setY(layoutParameters.top);
		
		LayoutInflater inflater = canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_digital_clock, relativeLayout);
		int textSize = (int) (Configuration.getInstance().convertPixtoDip(canvas.getActivity(), size.intValue()) * relativeWidthFactor);
		
		clockTextView = (TextView) relativeLayout.findViewById(R.id.digital_clock_time_txt);
		clockTextView.setTextColor(color);
		clockTextView.setTextSize(textSize);
		//clockTextView.setTypeface(Configuration.getInstance().getAppIconFont(HomeServerConnector.getInstance().getMainContext()), Typeface.BOLD);
		if(amPm) {
			clockFormat = new SimpleDateFormat("hh:mm");
			amPmFormat =  new SimpleDateFormat("aa");
		}else {
			clockFormat = new SimpleDateFormat("HH:mm");
		}
		
		broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                	setTime(new Date());
            	}
            }
        };
        canvas.getActivity().getBaseContext().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
	}
	protected void setTime(Date date) {
		String timeFormatted = clockFormat.format(date);
    	if(amPm) {
    		Spannable span = new SpannableString(timeFormatted+amPmFormat.format(date));
    		span.setSpan(new RelativeSizeSpan(0.3f), timeFormatted.length(), span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		clockTextView.setText(span);
    	}else {
        	clockTextView.setText(timeFormatted);
    	}
	}
	@Override
	public void addBinding(JSONObject bindingProperties) {
		// No binding possible
	}
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        // Do nothing
    }
	@Override
	public void refreshView() {
    	setTime(new Date());
	}
	
	@Override
	public void destroyView() {
		canvas.getActivity().getBaseContext().unregisterReceiver(broadcastReceiver);
        relativeLayout = null;
        clockTextView = null;
        clockFormat = null;
        amPmFormat = null;
        broadcastReceiver = null;
        super.destroyView();
    }
}
