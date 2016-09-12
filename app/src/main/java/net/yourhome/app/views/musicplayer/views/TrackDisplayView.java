package net.yourhome.app.views.musicplayer.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.model.viewproperties.Text;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TrackDisplayView extends MusicPlayerView  {

	private String artist = "";
	private String title = "";

	private int color;
	private Double size;
	
	private TextView trackDisplay;
	private RelativeLayout.LayoutParams params;
	
	public TrackDisplayView(CanvasFragment canvas, String stageElementId,JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas,stageElementId,viewProperties,bindingProperties);
		
		buildView(viewProperties);
        addBinding(bindingProperties);
	}

	@Override
	public View getView() {
		if(artist != "" && title != "") {
			trackDisplay.setText(artist + " - " + title);
		}
		return trackDisplay;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		color = Color.parseColor(this.properties.get(Text.COLOR).getValue());
		Double textSize = Double.parseDouble(this.properties.get(Text.SIZE).getValue());
		size = (double) (Configuration.getInstance().convertPixtoDip(canvas.getActivity(), textSize.intValue()) * relativeWidthFactor);

		// Layout
		this.trackDisplay = new TextView(canvas.getActivity());
		params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		params.leftMargin = layoutParameters.left;
		params.topMargin = layoutParameters.top;
		trackDisplay.setLayoutParams(params);
		

		trackDisplay.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		//trackDisplay.setTypeface(Configuration.getInstance().getApplicationFont(canvas.getActivity()));
//		trackDisplay.setTextColor(Color.WHITE);
		trackDisplay.setTextColor(color);
//		trackDisplay.setTextSize(18);
		trackDisplay.setTextSize(size.intValue());
		
		trackDisplay.setSingleLine();
		trackDisplay.setMarqueeRepeatLimit(-1);
		trackDisplay.setHorizontallyScrolling(true);
		trackDisplay.setEllipsize(TruncateAt.MARQUEE);
		trackDisplay.setFocusable(true);
		trackDisplay.setFocusableInTouchMode(true);
		//trackDisplay.setMovementMethod(new ScrollingMovementMethod());
		
		
	}
	private void clear() {
		this.trackDisplay.setText("");
	}
	
	private void setTrack(String artist, String title) {
		if(artist != "" && title != "") {
			this.artist = artist;
			this.title = title;		
			
			trackDisplay.setText(artist + " - " + title);
		}
	}
	
	@Override
	public void refreshView() {
		if(trackDisplay != null) {
			String newArtist = musicPlayerBinding.getStatus().artist;
			String newTitle = musicPlayerBinding.getStatus().title;
			if((newArtist == null && newTitle == null ) || ((newArtist.equals("") && newTitle.equals("")))) {
				clear();
			}else if(!this.artist.equals(newArtist) || !this.title.equals(newTitle)) {
				setTrack(newArtist, newTitle);
			}
		}
	}
}
