package net.yourhome.app.views;

import java.security.InvalidParameterException;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.R;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ClockAnalogView extends DynamicView {

	protected RelativeLayout relativeLayout;
	private ImageView minuteView; 
	private ImageView hourView;
	public ClockAnalogView(CanvasFragment canvas, String stageItemId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageItemId,viewProperties,bindingProperties);
		buildView(viewProperties);
		addBinding(bindingProperties);
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
		//params.topMargin = layoutParameters.top;
		relativeLayout = new RelativeLayout(canvas.getActivity());
		relativeLayout.setLayoutParams(params);
		relativeLayout.setRotation((float)layoutParameters.rotation);
		relativeLayout.setX(layoutParameters.left);
		relativeLayout.setY(layoutParameters.top);
		
		LayoutInflater inflater = canvas.getLayoutInflater(null);
		inflater.inflate(R.layout.view_analog_clock, relativeLayout);
		
		double factor = 1;
		Drawable hour = Configuration.getInstance().getAppIconDrawable(canvas.getActivity(), R.string.icon_Hourpointer, new Double(width*factor).intValue(), Color.BLACK);
		Drawable minute = Configuration.getInstance().getAppIconDrawable(canvas.getActivity(), R.string.icon_minute, new Double(width*factor).intValue(), Color.BLACK);
		//Drawable dial = Configuration.getInstance().getAppIconDrawable(canvas.getActivity(), R.string.icon_face1, new Double(width*factor).intValue(), Color.WHITE);

		//ImageView dialView = (ImageView) relativeLayout.findViewById(R.id.clock_face);
		//dialView.setBackground(dial);
		hourView = (ImageView) relativeLayout.findViewById(R.id.clock_hour);
		hourView.setBackground(hour);
		minuteView = (ImageView) relativeLayout.findViewById(R.id.clock_minute);
		minuteView.setBackground(minute);

        mCalendar = new Time();
        

        canvas.getActivity().getBaseContext().registerReceiver(mIntentReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        onTimeChanged();
	}

    private float mMinutes;
    private float mHour;
	private Time mCalendar;
	
	private void onTimeChanged() {
        mCalendar.setToNow();

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;

        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        

		hourView.setRotation(mHour / 12.0f * 360.0f);
		minuteView.setRotation(mMinutes / 60.0f * 360.0f);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }

            onTimeChanged();
        }
    };

    
	@Override
	public void addBinding(JSONObject bindingProperties) {
		// No binding possible
	}
    public static void createBinding(String stageItemId, JSONObject bindingProperties) {
        // Do nothing
    }
	@Override
	public void refreshView() {
		// Not needed	
	}
	@Override
	public void destroyView() {
		canvas.getActivity().getBaseContext().unregisterReceiver(mIntentReceiver);
	}

}
