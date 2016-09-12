package net.yourhome.app.bindings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.views.ButtonView;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ActivationMessage;
import net.yourhome.common.net.messagestructures.general.SetValueMessage;
import net.yourhome.common.net.messagestructures.musicplayer.MusicPlayerStatusMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistMessage.PlaylistItem;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.MultiStateButtonView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.app.views.musicplayer.views.PlaylistView;

public class MusicPlayerBinding extends AbstractBinding {
	
	
	private List<PlaylistItem> playlist = null;
	private MusicPlayerStatusMessage.MusicPlayerStatus playerStatus;
	private Map<String, ValueBinding> stateButtons = new HashMap<String, ValueBinding>();
	
	public MusicPlayerBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if(message instanceof PlaylistMessage) {
			PlaylistMessage playlistMessage = (PlaylistMessage) message;
			playlist = playlistMessage.playlist;
			for(DynamicView v : this.viewListeners) {
				if( v instanceof PlaylistView) {
					v.refreshView();
				}
			}
		}else if(message instanceof MusicPlayerStatusMessage) {
			// Update my state
			this.playerStatus = ((MusicPlayerStatusMessage) message).status;
			
			// Update state button bindings
			for(ValueBinding binding : this.stateButtons.values()) {
				switch(binding.getValueType()) {
					case MUSIC_PLAY_PAUSE:
						if(playerStatus.isPlaying) {
							binding.setValue("PLAY");
						}else if(playerStatus.isPaused || playerStatus.isStopped) {
							binding.setValue("PAUSE");
						}
						break;
					case MUSIC_RANDOM:
						if(playerStatus.randomStatus) {
							binding.setValue("true");
						}else {
							binding.setValue("false");
						}
					default:
					break;
				}	
			}
			
			// Update all UI elements
			this.updateViews();
		}
	}

	@Override
	protected void buildBinding() {
		// Initialize status
		MusicPlayerStatusMessage m = new MusicPlayerStatusMessage();
		this.playerStatus = m.new MusicPlayerStatus();
		
	}

	// Keep a seperate binding for multi state buttons
	public ValueBinding addStateButtonListener(DynamicView view, JSONObject bindingProperties) throws JSONException {
		ValueBinding valueBinding = new ValueBinding(view.getStageElementId(), bindingProperties);
		stateButtons.put(view.getStageElementId(), valueBinding);
        return valueBinding;
	}
	public MusicPlayerStatusMessage.MusicPlayerStatus getStatus() {
		return this.playerStatus;
	}
	

	public String getState(MultiStateButtonView view) {
		ValueBinding valueBinding = stateButtons.get(view.getStageElementId());
		if(valueBinding!= null) { return valueBinding.getValue(); } else { return null; }
	}
	@Override
	public void viewPressed(DynamicView v, UIEvent event) {
		if(v instanceof PlaylistView) {
			if(event.getType() == UIEvent.Types.SET_VALUE) {
				SetValueMessage setValueMessage = new SetValueMessage();
				setValueMessage.controlIdentifiers = this.getControlIdentifier();
				setValueMessage.controlIdentifiers.setValueIdentifier("Playlist");
				setValueMessage.broadcast = false;
				setValueMessage.value = event.getProperty("VALUE").toString();
				BindingController.getInstance().sendMessage(setValueMessage);
			}
		}else if(v instanceof MultiStateButtonView) {
            this.stateButtons.get(v).viewPressed(v, event);
        }else if(v instanceof ButtonView) {
            // Send activation message to controller
            ActivationMessage activationMessage = new ActivationMessage();
            activationMessage.controlIdentifiers = this.getControlIdentifier();
            BindingController.getInstance().sendMessage(activationMessage);
        }
	}

	@Override
	public void viewLongPressed(DynamicView v, UIEvent event) {
		// TODO Auto-generated method stub	
	}
	
	public List<PlaylistItem> getPlaylist() {
		return this.playlist;
	}
}
