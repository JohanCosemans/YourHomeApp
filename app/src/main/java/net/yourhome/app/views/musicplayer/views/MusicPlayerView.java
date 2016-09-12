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
 * THIS SOFTWARE IS PROVIDED BY THE NETBSD FOUNDATION, INC. AND CONTRIBUTORS
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
package net.yourhome.app.views.musicplayer.views;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.MusicPlayerBinding;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.views.DynamicView;

public abstract class MusicPlayerView extends DynamicView {

	protected MusicPlayerBinding musicPlayerBinding;

	public MusicPlayerView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
	}

	@Override
	public void addBinding(JSONObject bindingProperties) {
		try {
			String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
			binding = BindingController.getInstance().getBindingFor(controllerIdentifier);
			this.musicPlayerBinding = (MusicPlayerBinding) binding;
			binding.addViewListener(this);
		} catch (JSONException e) {
		}
	}

	public static void createBinding(String stageElementId, JSONObject bindingProperties) {
		// Music player views have only one binding per music player controller.
		if (bindingProperties != null) {
			try {
				// Check if a music player controller was created already
				String controllerIdentifier = bindingProperties.getString("controllerIdentifier");
				AbstractBinding existingBinding = BindingController.getInstance().getBindingFor(controllerIdentifier);
				if (existingBinding == null) {
					new MusicPlayerBinding(controllerIdentifier, bindingProperties);
				}
			} catch (JSONException e) {
			}

			/*
			 * String controllerIdentifier; controllerIdentifier =
			 * bindingProperties.getString("controllerIdentifier");
			 * List<AbstractBinding> bindings =
			 * BindingController.getInstance().getBindingsFor(
			 * controllerIdentifier); if(bindings != null && bindings.size() >
			 * 0) { int i=0; boolean found = false; while(!found &
			 * i<bindings.size()) { if(bindings.get(i) instanceof
			 * MusicPlayerBinding) { found = true; }else { i++; } } if(found) {
			 * binding = (MusicPlayerBinding) bindings.get(i); }else { binding =
			 * new MusicPlayerBinding(stageElementId, bindingProperties); }
			 * binding.addViewListener(this); }else { binding = new
			 * MusicPlayerBinding(stageElementId, bindingProperties); }
			 */
		}
	}

}
