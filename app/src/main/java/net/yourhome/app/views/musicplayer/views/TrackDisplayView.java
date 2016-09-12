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

import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.model.viewproperties.Text;

public class TrackDisplayView extends MusicPlayerView {

	private String artist = "";
	private String title = "";

	private int color;
	private Double size;

	private TextView trackDisplay;
	private RelativeLayout.LayoutParams params;

	public TrackDisplayView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);

		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	@Override
	public View getView() {
		if (this.artist != "" && this.title != "") {
			this.trackDisplay.setText(this.artist + " - " + this.title);
		}
		return this.trackDisplay;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Properties
		this.color = Color.parseColor(this.properties.get(Text.COLOR).getValue());
		Double textSize = Double.parseDouble(this.properties.get(Text.SIZE).getValue());
		this.size = (double) (Configuration.getInstance().convertPixtoDip(canvas.getActivity(), textSize.intValue()) * relativeWidthFactor);

		// Layout
		this.trackDisplay = new TextView(canvas.getActivity());
		this.params = new RelativeLayout.LayoutParams(layoutParameters.width, layoutParameters.height);
		this.params.leftMargin = layoutParameters.left;
		this.params.topMargin = layoutParameters.top;
		this.trackDisplay.setLayoutParams(this.params);

		this.trackDisplay.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		// trackDisplay.setTypeface(Configuration.getInstance().getApplicationFont(canvas.getActivity()));
		// trackDisplay.setTextColor(Color.WHITE);
		this.trackDisplay.setTextColor(this.color);
		// trackDisplay.setTextSize(18);
		this.trackDisplay.setTextSize(this.size.intValue());

		this.trackDisplay.setSingleLine();
		this.trackDisplay.setMarqueeRepeatLimit(-1);
		this.trackDisplay.setHorizontallyScrolling(true);
		this.trackDisplay.setEllipsize(TruncateAt.MARQUEE);
		this.trackDisplay.setFocusable(true);
		this.trackDisplay.setFocusableInTouchMode(true);
		// trackDisplay.setMovementMethod(new ScrollingMovementMethod());

	}

	private void clear() {
		this.trackDisplay.setText("");
	}

	private void setTrack(String artist, String title) {
		if (artist != "" && title != "") {
			this.artist = artist;
			this.title = title;

			this.trackDisplay.setText(artist + " - " + title);
		}
	}

	@Override
	public void refreshView() {
		if (this.trackDisplay != null) {
			String newArtist = this.musicPlayerBinding.getStatus().artist;
			String newTitle = this.musicPlayerBinding.getStatus().title;
			if ((newArtist == null && newTitle == null) || ((newArtist.equals("") && newTitle.equals("")))) {
				this.clear();
			} else if (!this.artist.equals(newArtist) || !this.title.equals(newTitle)) {
				this.setTrack(newArtist, newTitle);
			}
		}
	}
}
