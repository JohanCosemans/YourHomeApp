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

import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import net.yourhome.app.canvas.CanvasFragment;

public class TrackProgressView extends MusicPlayerView {

	private RelativeLayout.LayoutParams params;
	private ProgressBar progressBar;

	public TrackProgressView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	@Override
	public View getView() {
		return this.progressBar;
	}

	private void setPercentage(double percentage) {
		this.progressBar.setProgress((int) (percentage * 100));
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Layout
		this.params = new RelativeLayout.LayoutParams(layoutParameters.width, layoutParameters.height);
		this.params.leftMargin = layoutParameters.left;
		this.params.topMargin = layoutParameters.top;

		this.progressBar = new ProgressBar(canvas.getActivity(), null, android.R.attr.progressBarStyleHorizontal);
		this.progressBar.setMax(10000);
		this.progressBar.setLayoutParams(this.params);
		this.progressBar.setHorizontalScrollBarEnabled(true);
		// progressBar.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// int progress = progressBar.getProgress();
		// TrackProgressMessage message = new TrackProgressMessage(null);
		// message.trackProgressPercentage = progress;
		// HomeServerConnector.getInstance().sendCommand(message);
		//
		// return false;
		// }
		// });

	}

	@Override
	public void refreshView() {
		this.setPercentage(this.musicPlayerBinding.getStatus().trackProgressPercentage);
	}

}
