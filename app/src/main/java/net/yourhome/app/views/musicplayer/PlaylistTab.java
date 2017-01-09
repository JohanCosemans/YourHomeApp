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
package net.yourhome.app.views.musicplayer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import net.yourhome.app.R;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.base.enums.MessageTypes;
import net.yourhome.common.net.messagestructures.general.SetValueMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistsMessage;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistsMessage.PlaylistDescription;
import net.yourhome.common.net.messagestructures.musicplayer.PlaylistsRequestMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;

public class PlaylistTab extends Fragment {

	private View view;
	private List<PlaylistDescription> playlistList = new ArrayList<PlaylistDescription>();

	private ControlIdentifiers identifiers;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getActivity().getIntent();
		Bundle extras = intent.getExtras();
		this.identifiers = new ControlIdentifiers(extras.getString("controllerIdentifier"), extras.getString("nodeIdentifier"), extras.getString("valueIdentifier"));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.view = inflater.inflate(R.layout.spotify_playlistselection_listview_list, container, false);

		// Initialize data loader
		PlaylistLoader loader = new PlaylistLoader();
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return this.view;
	}

	private class PlaylistLoader extends AsyncTask<Void, Void, Void> {

		private LinearLayout loading;

		@Override
		protected void onPreExecute() {
			this.loading = (LinearLayout) PlaylistTab.this.view.findViewById(R.id.playlists_loading);
			this.loading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... voids) {

			// Get values from server
			try {
				// Send message
				PlaylistsRequestMessage playlistsRequest = new PlaylistsRequestMessage();
				playlistsRequest.controlIdentifiers.setControllerIdentifier(ControllerTypes.SPOTIFY);
				String responseString = HomeServerConnector.getInstance().sendSyncMessage(playlistsRequest);

				// Process response
				JSONObject responseObject = new JSONObject(responseString);
				PlaylistsMessage returnMessage = (PlaylistsMessage) MessageTypes.getMessage(responseObject);
				PlaylistTab.this.playlistList = returnMessage.playlists;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			this.loading.setVisibility(View.GONE);

			String[] playlistNames = new String[PlaylistTab.this.playlistList.size()];

			for (int i = 0; i < PlaylistTab.this.playlistList.size(); i++) {
				playlistNames[i] = PlaylistTab.this.playlistList.get(i).name;
			}
			if (getActivity() != null) {
				ListView list = (ListView) getActivity().findViewById(R.id.playlistList);

				if (list != null) {
					list.setAdapter(new PlaylistAdapter(getActivity(), playlistNames));
					list.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> myAdapter, View myView, final int playlistNumber, long mylng) {

							// Set new playlist and refresh playlist content
							Thread t = new Thread() {
								List<PlaylistMessage.PlaylistItem> playlist;
								final ProgressDialog dialog = new ProgressDialog(getActivity());

								@Override
								public void run() {

									// Show loader
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											ListView playlistList = (ListView) PlaylistTab.this.view.findViewById(R.id.playlistList);
											playlistList.setVisibility(View.INVISIBLE);

											dialog.setCancelable(true);
											dialog.setMessage("Loading...");
											dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
											dialog.show();
										}
									});
									// Send set playlist message
									Log.d("PlaylistSelectorActivity", "Right before setting new playlist");

									try {
										// Get values from server
										SetValueMessage requestMessage = new SetValueMessage();
										requestMessage.controlIdentifiers = PlaylistTab.this.identifiers;
										requestMessage.value = playlistNumber + "";

										HomeServerConnector.getInstance().sendCommand(requestMessage);
									} catch (Exception e) {
										e.printStackTrace();
									}

									// Update UI
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											// Hide loader
											if (dialog.isShowing()) {
												dialog.dismiss();
											}
											getActivity().finish();
										}
									});
								}
							};
							t.start();
						}
					});
				}
			}
		}
	}

	private class PlaylistAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;

		public PlaylistAdapter(Context context, String[] values) {
			super(context, R.layout.spotify_playlistselection_listview_row, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView = inflater.inflate(R.layout.spotify_playlistselection_listview_row, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			textView.setText(this.values[position]);

			Drawable playlistIcon = Configuration.getInstance().getAppIconDrawable(getContext(), R.string.icon_music_note, 20);
			ImageView icon = (ImageView) rowView.findViewById(R.id.icon);
			icon.setImageDrawable(playlistIcon);
			// textView.setCompoundDrawablesWithIntrinsicBounds(playlistIcon,
			// null, null, null);
			// textView.setCompoundDrawablePadding(5);
			return rowView;
		}
	}
}