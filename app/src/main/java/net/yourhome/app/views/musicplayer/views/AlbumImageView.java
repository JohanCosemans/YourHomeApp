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
package net.yourhome.app.views.musicplayer.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import net.yourhome.app.R;
import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;

public class AlbumImageView extends MusicPlayerView {

	private ImageView image;
	private RelativeLayout.LayoutParams params;
	private String lastLoadedImagePath = null;

	public AlbumImageView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId, viewProperties, bindingProperties);
		this.buildView(viewProperties);
		this.addBinding(bindingProperties);
	}

	/**
	 * private void setAlbumImage(byte[] imageAsBytes) { image.setImageBitmap(
	 * BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length) ); }
	 */
	private void setAlbumImage(String url) {

		Configuration configuration = Configuration.getInstance();
		Context mainContext = HomeServerConnector.getInstance().getMainContext();
		configuration.getHomeServerHostName(HomeServerConnector.getInstance().getMainContext());

		String fullImageUrl = configuration.getHomeServerProtocol() + "://" + configuration.getHomeServerHostName(mainContext) + ":" + configuration.getHomeServerPort(mainContext) + url;

		AlbumImageLoader imageLoader = new AlbumImageLoader();
		imageLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fullImageUrl);

	}

	@Override
	public View getView() {
		return this.image;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Layout
		this.params = new RelativeLayout.LayoutParams(layoutParameters.width, layoutParameters.height);
		this.params.leftMargin = layoutParameters.left;
		this.params.topMargin = layoutParameters.top;

		this.image = new ImageView(canvas.getActivity());
		this.image.setLayoutParams(this.params);
	}

	@Override
	public void refreshView() {
		String image = this.musicPlayerBinding.getStatus().imagePath;
		if (this.lastLoadedImagePath != image) {
			this.setAlbumImage(image);
		}
	}

	private void clear() {
		Bitmap imageBitmap = Configuration.getInstance().getAppIcon(HomeServerConnector.getInstance().getMainContext(), R.string.icon_music79, 200, Color.WHITE);
		// this.image.setImageResource(R.drawable.ic_album_note);
		this.image.setImageBitmap(imageBitmap);
		this.image.setBackgroundColor(Color.rgb(80, 80, 80));
	}

	private class AlbumImageLoader extends AsyncTask<String, String, Bitmap> {
		// private Bitmap bitmap;

		@Override
		protected Bitmap doInBackground(String... src) {
			return this.getBitmapFromURL(src[0]);
		}

		@Override
		protected void onPostExecute(Bitmap b) {
			if (b != null) {
				AlbumImageView.this.image.setImageBitmap(b);
			} else {
				AlbumImageView.this.clear();
			}
		}

		private Bitmap getBitmapFromURL(String URL) {
			Bitmap bitmap = null;
			InputStream in = null;
			try {
				// in = OpenHttpConnection(URL);
				in = new URL(URL).openStream();
				if (in != null) {
					bitmap = BitmapFactory.decodeStream(in);
					in.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return bitmap;
		}
	}

}
