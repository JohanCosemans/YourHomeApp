package net.yourhome.app.views.musicplayer;

import net.yourhome.app.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AccountSelectorTab extends Fragment {
	
	private Activity activity;
	private View view;
	private int setAccountNr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(
				R.layout.spotify_playlistselection_accountselector, container,
				false);
		
		activity = this.getActivity();
			/*	 
		// Fill radiobuttongroup with spotify accounts
		RadioGroup spotifyAccountSelector = (RadioGroup) view.findViewById(R.id.spotifyAccountSelector);
		//List<SpotifyAccount> spotifyAccounts = SpotifyBindingController.getInstance().getSpotifyAccounts();
		int checkedId = -1;;
		
		for(int i=0;i<spotifyAccounts.size();i++) {
			// Create radiobutton and add to the group
			final SpotifyAccount spotifyAccount = spotifyAccounts.get(i);
			RadioButton accountButton = new RadioButton(view.getContext());
			accountButton.setId(i);
			
			if(spotifyAccount.isCurrentAccount()) {
				checkedId = accountButton.getId();
			}
			
			accountButton.setText(spotifyAccount.getAccountLogin());
			accountButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("",spotifyAccount.getAccountLogin() +" clicked");
					// TODO: handle action of selecting the radiobutton
					setAccountNr = spotifyAccount.getId();
					
					// Initialize data loader
					AccountLoader loader = new AccountLoader();
					loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			});
			spotifyAccountSelector.addView(accountButton);
		}
		
		spotifyAccountSelector.check(checkedId);*/
		
		return view;
	}
	private class AccountLoader extends AsyncTask<Void, Void, Void> {

		final ProgressDialog dialog = new ProgressDialog(activity);

		@Override
		protected void onPreExecute() {
			dialog.setCancelable(true);
			dialog.setMessage("Loading...");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {

			// Get values from server
			//SetSpotifyAccountMessage message = new SetSpotifyAccountMessage();
			//message.accountNr = setAccountNr;
			//HomeServerConnector.getInstance().sendCommand(message);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

		

		}
	}
}
