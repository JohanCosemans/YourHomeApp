package net.yourhome.app.views.musicplayer.views;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.UIEvent;
import net.yourhome.app.views.UIEvent.Types;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistMessage.PlaylistItem;
import net.yourhome.app.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
public class PlaylistView extends MusicPlayerView implements OnItemClickListener {
	private RelativeLayout relativeLayout;
	private RelativeLayout.LayoutParams	relativeLayoutParams;
	private PlaylistView me = this;
	private PlaylistAdapter playlistAdapter;
	private ListView playlistView;
	private List<PlaylistItem> lastLoadedPlaylist;
	
	private int backgroundColor = Color.rgb(80, 80, 80);
	
	public PlaylistView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId,viewProperties,bindingProperties);
		buildView(viewProperties);
        addBinding(bindingProperties);
	}
	
	@Override
	public View getView() {
		return relativeLayout;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);
		
		// Layout
        relativeLayout = new RelativeLayout(canvas.getActivity());
		this.relativeLayoutParams = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		this.relativeLayoutParams.leftMargin = layoutParameters.left;
		this.relativeLayoutParams.topMargin = layoutParameters.top;
		relativeLayout.setLayoutParams(this.relativeLayoutParams);
		
		relativeLayout.setBackgroundColor(backgroundColor);

		// Create title textviews
		LinearLayout linearLayout = new LinearLayout(canvas.getActivity());
		TextView artistTitle =  new TextView(canvas.getActivity());
		artistTitle.setText(canvas.getActivity().getText(R.string.playlistsArtist));
		artistTitle.setTypeface(null, Typeface.BOLD);
		artistTitle.setWidth(this.relativeLayoutParams.width/3*1);
		artistTitle.setPadding(20, 20, 0, 0);
		artistTitle.setTextColor(Color.WHITE);
		linearLayout.addView(artistTitle);
		linearLayout.setBackgroundColor(backgroundColor);
		
		TextView titleTitle =  new TextView(canvas.getActivity());
        titleTitle.setText(canvas.getActivity().getText(R.string.playlistsTitle));
		//titleTitle.setTypeface(Configuration.getInstance().getApplicationFont(canvas.getActivity()),Typeface.BOLD);
        titleTitle.setTypeface(null, Typeface.BOLD);
        titleTitle.setWidth(this.relativeLayoutParams.width / 3 * 2);
        titleTitle.setTextColor(Color.WHITE);
        titleTitle.setPadding(10, 0, 0, 0);
        linearLayout.addView(titleTitle);
		linearLayout.setId(1);
        relativeLayout.addView(linearLayout);
		
		// Create artistView
		RelativeLayout.LayoutParams	listViewLayoutParams = new LayoutParams(this.relativeLayoutParams.width,this.relativeLayoutParams.height);
		listViewLayoutParams.addRule(RelativeLayout.BELOW, linearLayout.getId() );
		ArrayList<String> listContent = new ArrayList<String>(0);
		playlistAdapter = new PlaylistAdapter(canvas.getActivity(),listContent);
		playlistView = new ListView(canvas.getActivity());
		playlistView.setAdapter(playlistAdapter);
		playlistView.setLayoutParams(listViewLayoutParams);
		playlistView.setPadding(10, 10, 10, 20);
		playlistView.setOnItemClickListener(this);
		playlistView.setDivider(null);
		playlistView.setFastScrollEnabled(true);
		playlistView.setBackgroundColor(backgroundColor);
		relativeLayout.addView(playlistView);
				
	}
	/*public void setPlaylist(List<PlaylistMessage.PlaylistItem> playlist) {
		//this.playlist = playlist;
		
		// Update lists
		this.playlistAdapter.setPlaylist(playlist);
		this.scrollToTrack();
	}*/
	
	/*public List<PlaylistMessage.PlaylistItem> getPlaylist() {
		return playlist;
	}*/
	private void scrollToTrack() {
		int trackIndex = musicPlayerBinding.getStatus().trackIndex;
		if(musicPlayerBinding.getPlaylist() != null && trackIndex < musicPlayerBinding.getPlaylist().size()) {
			this.playlistAdapter.notifyDataSetChanged();
			
			int start = playlistView.getFirstVisiblePosition();
			int end = playlistView.getLastVisiblePosition();
			
			// Scroll to song if it is not visible
			if(trackIndex < start || trackIndex > end) {
				playlistView.setSelection(trackIndex);
			}		
		}
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		UIEvent event = new UIEvent(Types.SET_VALUE);
		event.setProperty("VALUE", position);
		binding.viewPressed(this, event);
	}
	

	@Override
	public void refreshView() {
		if(lastLoadedPlaylist != musicPlayerBinding.getPlaylist()) {
			playlistAdapter.setPlaylist(musicPlayerBinding.getPlaylist());
			lastLoadedPlaylist = musicPlayerBinding.getPlaylist();
		}
		scrollToTrack();
	}
	private class PlaylistAdapter extends ArrayAdapter<String> {
		private final Context context;
		ArrayList<String> values;
		
		public PlaylistAdapter(Context context, ArrayList<String> values) {
			super(context,0,values);
			this.context = context;
			this.values = values;
		}
		
		public void setPlaylist(List<PlaylistMessage.PlaylistItem> playlist) {
			if(playlist != null) {
				ArrayList<String> values = new ArrayList<String>();
				for(PlaylistMessage.PlaylistItem item : playlist) {
					values.add("");
				}
				setValues(values);
			}else {
				this.clear();
			}
		}
		
		public void setValues(ArrayList<String> values) {
			this.values = values;
			this.clear();
			this.addAll(values);
		}
	 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout linearLayout = new LinearLayout(parent.getContext());
			linearLayout.setBackgroundColor(backgroundColor);
			
			if(musicPlayerBinding.getPlaylist() != null && musicPlayerBinding.getPlaylist().size() > 0 && musicPlayerBinding.getPlaylist().size() > position) {
				TextView artistView = new TextView(parent.getContext());
				artistView.setText(musicPlayerBinding.getPlaylist().get(position).artist);
				artistView.setTextSize(14);
				artistView.setWidth(relativeLayoutParams.width/3*1);
				artistView.setPadding(10, 0, 0, 0);
				artistView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				artistView.setTextColor(Color.WHITE);
				if(position == me.musicPlayerBinding.getStatus().trackIndex) {
					artistView.setTypeface(Typeface.create(Configuration.getInstance().getApplicationFont(getContext().getAssets()), Typeface.BOLD));
				}else {
					//artistView.setTypeface(Configuration.getInstance().getApplicationFont(getContext()), Typeface.NORMAL);
				}
				
				TextView titleView = new TextView(parent.getContext());
				titleView.setText(musicPlayerBinding.getPlaylist().get(position).title);
				titleView.setTextSize(14);
				titleView.setWidth(relativeLayoutParams.width/3*2);
				titleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				titleView.setPadding(0, 0, 0, 10);
				titleView.setTextColor(Color.WHITE);
				if(position == me.musicPlayerBinding.getStatus().trackIndex) {
					titleView.setTypeface(Typeface.create(Configuration.getInstance().getApplicationFont(getContext().getAssets()), Typeface.BOLD));
				}else {
					//titleView.setTypeface(Configuration.getInstance().getApplicationFont(getContext()), Typeface.NORMAL);
				}
				
				linearLayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				linearLayout.addView(artistView);
				linearLayout.addView(titleView);
				
			}else {
				TextView artistView = new TextView(parent.getContext());
				artistView.setText(values.get(position));
				artistView.setTextSize(14);
				//artistView.setTypeface(Configuration.getInstance().getApplicationFont(getContext()));
			}
			
			
			return linearLayout;
		}
	}
}
