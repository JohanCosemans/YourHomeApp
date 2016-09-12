package net.yourhome.app.views.musicplayer.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.canvas.CanvasFragment;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class TrackProgressView extends MusicPlayerView  {

	private RelativeLayout.LayoutParams params;
	private ProgressBar progressBar;
	
	public TrackProgressView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId,viewProperties,bindingProperties);
		buildView(viewProperties);
        addBinding(bindingProperties);
	}
	
	@Override
	public View getView() {
		return progressBar;
	}
	private void setPercentage(double percentage) {
		this.progressBar.setProgress((int) (percentage*100));
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Layout
		params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		params.leftMargin = layoutParameters.left;
		params.topMargin = layoutParameters.top;
		
		progressBar = new ProgressBar(canvas.getActivity(),null,android.R.attr.progressBarStyleHorizontal);
		progressBar.setMax(10000);
		progressBar.setLayoutParams(params);
		progressBar.setHorizontalScrollBarEnabled(true);
//		progressBar.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				int progress = progressBar.getProgress();
//				TrackProgressMessage message = new TrackProgressMessage(null);
//				message.trackProgressPercentage = progress;
//				HomeServerConnector.getInstance().sendCommand(message);
//
//				return false;
//			}
//		});
		
		
	}

	@Override
	public void refreshView() {
		setPercentage(musicPlayerBinding.getStatus().trackProgressPercentage);
	}

}
