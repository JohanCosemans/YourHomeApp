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
package net.yourhome.app.views.musicplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.yourhome.app.R;

public class AccountSelectorTab extends Fragment {

	private Activity activity;
	private View view;
	private int setAccountNr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.view = inflater.inflate(R.layout.spotify_playlistselection_accountselector, container, false);

		this.activity = this.getActivity();
		/*
		 * // Fill radiobuttongroup with spotify accounts RadioGroup
		 * spotifyAccountSelector = (RadioGroup)
		 * view.findViewById(R.id.spotifyAccountSelector);
		 * //List<SpotifyAccount> spotifyAccounts =
		 * SpotifyBindingController.getInstance().getSpotifyAccounts(); int
		 * checkedId = -1;;
		 * 
		 * for(int i=0;i<spotifyAccounts.size();i++) { // Create radiobutton and
		 * add to the group final SpotifyAccount spotifyAccount =
		 * spotifyAccounts.get(i); RadioButton accountButton = new
		 * RadioButton(view.getContext()); accountButton.setId(i);
		 * 
		 * if(spotifyAccount.isCurrentAccount()) { checkedId =
		 * accountButton.getId(); }
		 * 
		 * accountButton.setText(spotifyAccount.getAccountLogin());
		 * accountButton.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * Log.d("",spotifyAccount.getAccountLogin() +" clicked"); // TODO:
		 * handle action of selecting the radiobutton setAccountNr =
		 * spotifyAccount.getId();
		 * 
		 * // Initialize data loader AccountLoader loader = new AccountLoader();
		 * loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); } });
		 * spotifyAccountSelector.addView(accountButton); }
		 * 
		 * spotifyAccountSelector.check(checkedId);
		 */

		return this.view;
	}

	private class AccountLoader extends AsyncTask<Void, Void, Void> {

		final ProgressDialog dialog = new ProgressDialog(AccountSelectorTab.this.activity);

		@Override
		protected void onPreExecute() {
			this.dialog.setCancelable(true);
			this.dialog.setMessage("Loading...");
			this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			this.dialog.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			// Get values from server
			// SetSpotifyAccountMessage message = new
			// SetSpotifyAccountMessage();
			// message.accountNr = setAccountNr;
			// HomeServerConnector.getInstance().sendCommand(message);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

		}
	}
}
