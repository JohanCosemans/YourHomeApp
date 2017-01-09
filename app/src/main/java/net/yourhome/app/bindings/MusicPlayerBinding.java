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
 * THIS SOFTWARE IS PROVIDED BY COTEQ AND CONTRIBUTORS
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
package net.yourhome.app.bindings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.util.JSONMessageCaller;
import net.yourhome.app.views.ButtonView;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.MultiStateButtonView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.app.views.musicplayer.views.PlaylistView;
import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.general.ActivationMessage;
import net.yourhome.common.net.messagestructures.general.SetValueMessage;
import net.yourhome.common.net.messagestructures.musicplayer.MusicPlayerStatusMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistMessage.PlaylistItem;

public class MusicPlayerBinding extends AbstractBinding {

	private List<PlaylistItem> playlist = null;
	private MusicPlayerStatusMessage.MusicPlayerStatus playerStatus;
	private Map<String, ValueBinding> stateButtons = new HashMap<String, ValueBinding>();

	public MusicPlayerBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if (message instanceof PlaylistMessage) {
			PlaylistMessage playlistMessage = (PlaylistMessage) message;
			this.playlist = playlistMessage.playlist;
			for (DynamicView v : this.viewListeners) {
				if (v instanceof PlaylistView) {
					v.refreshView();
				}
			}
		} else if (message instanceof MusicPlayerStatusMessage) {
			// Update my state
			this.playerStatus = ((MusicPlayerStatusMessage) message).status;

			// Update state button bindings
			for (ValueBinding binding : this.stateButtons.values()) {
				switch (binding.getValueType()) {
				case MUSIC_PLAY_PAUSE:
					if (this.playerStatus.isPlaying) {
						binding.setValue("PLAY");
					} else if (this.playerStatus.isPaused || this.playerStatus.isStopped) {
						binding.setValue("PAUSE");
					}
					break;
				case MUSIC_RANDOM:
					if (this.playerStatus.randomStatus) {
						binding.setValue("true");
					} else {
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
		this.stateButtons.put(view.getStageElementId(), valueBinding);
		return valueBinding;
	}

	public MusicPlayerStatusMessage.MusicPlayerStatus getStatus() {
		return this.playerStatus;
	}

	public String getState(MultiStateButtonView view) {
		ValueBinding valueBinding = this.stateButtons.get(view.getStageElementId());
		if (valueBinding != null) {
			return valueBinding.getValue();
		} else {
			return null;
		}
	}

	@Override
	public boolean viewPressed(DynamicView v, UIEvent event) {
		if (v instanceof PlaylistView) {
			if (event.getType() == UIEvent.Types.SET_VALUE) {
				SetValueMessage setValueMessage = new SetValueMessage();
				setValueMessage.controlIdentifiers = this.getControlIdentifier();
				setValueMessage.controlIdentifiers.setValueIdentifier("Playlist");
				setValueMessage.broadcast = false;
				setValueMessage.value = event.getProperty("VALUE").toString();
				BindingController.getInstance().sendMessage(setValueMessage);
			}
		} else if (v instanceof MultiStateButtonView) {
			this.stateButtons.get(v).viewPressed(v, event);
		} else if (v instanceof ButtonView) {
			// Send activation message to controller
			ActivationMessage activationMessage = new ActivationMessage();
			activationMessage.controlIdentifiers = this.getControlIdentifier();
			BindingController.getInstance().sendMessage(activationMessage);
		}
        return true;
	}

    @Override
    public boolean viewPressed(DynamicView v, UIEvent event, JSONMessageCaller apiCaller) {
        return false;
    }

    @Override
	public void viewLongPressed(DynamicView v, UIEvent event) {
		// TODO Auto-generated method stub
	}

	public List<PlaylistItem> getPlaylist() {
		return this.playlist;
	}
}
