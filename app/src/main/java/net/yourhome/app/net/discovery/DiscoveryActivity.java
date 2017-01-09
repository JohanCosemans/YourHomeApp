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
package net.yourhome.app.net.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import net.yourhome.app.R;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.net.Discovery;

public class DiscoveryActivity extends Activity implements OnItemClickListener {

	public final static long VIBRATE = 250;
	public final static int SCAN_PORT_RESULT = 1;

	public static final int MENU_OPEN_DISCOVERY = 0;
	public static final int MENU_MANUAL_INPUT = 1;

	protected static LayoutInflater mInflater;;
	protected List<HomeServerHost> hosts = null;
	protected HostsAdapter adapter;
	protected Button btn_manual;
	protected Button btn_search;
	protected HomeServerDiscovery mDiscoveryTask = null;

	protected DiscoveryActivity me = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.discovery_discovery_activity);

		DiscoveryActivity.mInflater = LayoutInflater.from(getApplicationContext());

		Drawable manualIconDrawable = Configuration.getInstance().getAppIconDrawable(this.getBaseContext(), R.string.icon_gear, 30);
		this.btn_manual = (Button) findViewById(R.id.btn_manual);
		this.btn_manual.setCompoundDrawablesWithIntrinsicBounds(manualIconDrawable, null, null, null);
		this.btn_manual.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(DiscoveryActivity.this.me, ManualEntryActivity.class));
			}
		});

		Drawable searchIconDrawable = Configuration.getInstance().getAppIconDrawable(this.getBaseContext(), R.string.icon_search, 30);
		this.btn_search = (Button) findViewById(R.id.btn_search);
		this.btn_search.setCompoundDrawablesWithIntrinsicBounds(searchIconDrawable, null, null, null);
		this.btn_search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DiscoveryActivity.this.startDiscovering();
			}
		});
		// Hosts list
		this.adapter = new HostsAdapter(getApplicationContext());
		ListView list = (ListView) findViewById(R.id.output);
		list.setAdapter(this.adapter);
		list.setItemsCanFocus(false);
		list.setOnItemClickListener(this);

		this.startDiscovering();
	}

	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		final HomeServerHost host = this.hosts.get(position);
		DiscoveryActivityImp.showConfigurationsDialog(DiscoveryActivity.this, host);
	}

	/*
	 * public static void showConfigurationsDialog(final Activity activity,
	 * final HomeServerHost host) { // Template for DiscoveryActivityImp }
	 */

	/*
	 * public static void showConfigurationsDialog(final Activity activity,
	 * final HomeServerHost host) { AlertDialog.Builder dialog = new
	 * AlertDialog.Builder(activity);
	 * dialog.setTitle(R.string.discover_action_title);
	 * 
	 * final List<net.yourhome.common.net.model.Configuration> configurationList
	 * = new
	 * ArrayList<net.yourhome.common.net.model.Configuration>(host.getInfo().
	 * getConfigurations().values()); String[] configurations = new
	 * String[configurationList.size()]; int i=0;
	 * for(net.yourhome.common.net.model.Configuration c : configurationList) {
	 * if(c.getName() == null) { c.setName("Unnamed"); } configurations[i] =
	 * c.getName(); i++; } if(i==0) { TextView noConfigurationsMessage = new
	 * TextView(activity); String messageContent =
	 * activity.getResources().getString(R.string.
	 * discovery_no_published_configurations); messageContent +=
	 * System.getProperty ("line.separator"); messageContent +=
	 * host.getDesignerAddress(); noConfigurationsMessage.setText(R.string.
	 * discovery_no_published_configurations);
	 * noConfigurationsMessage.setGravity(Gravity.LEFT);
	 * noConfigurationsMessage.setPadding(25, 25, 25, 80);
	 * 
	 * SpannableString s = new SpannableString(messageContent);
	 * Linkify.addLinks(s, Linkify.WEB_URLS);
	 * noConfigurationsMessage.setText(s);
	 * noConfigurationsMessage.setMovementMethod(LinkMovementMethod.getInstance(
	 * ));
	 * 
	 * dialog.setView(noConfigurationsMessage); }else {
	 * dialog.setItems(configurations, new OnClickListener() { public void
	 * onClick(DialogInterface dialog, int selected) {
	 * net.yourhome.common.net.model.Configuration selectedConfiguration =
	 * configurationList.get(selected); // Set selection in preferences
	 * SharedPreferences userDetails = activity.getSharedPreferences("USER",
	 * Context.MODE_PRIVATE); Editor edit = userDetails.edit();
	 * edit.putString("HOMESERVER_IP", host.ipAddress);
	 * edit.putInt("HOMESERVER_PORT", host.port);
	 * edit.putString("HOMESERVER_NAME", host.name);
	 * edit.putString("HOMESERVER_CONFIGURATION",
	 * selectedConfiguration.getFile()); edit.commit();
	 * 
	 * // Start canvas & load configuration dialog.dismiss();
	 * activity.startActivity(new Intent(activity, CanvasActivity.class));
	 * 
	 * // Stop discovery activity activity.finish(); } }); }
	 * dialog.setNegativeButton(R.string.btn_discover_cancel, null);
	 * dialog.show(); }
	 */

	static class ViewHolder {
		TextView name;
		TextView ipAddress;
		ImageView serverIcon;
		ImageView arrowIcon;
	}

	// Custom ArrayAdapter
	protected class HostsAdapter extends ArrayAdapter<Void> {
		public HostsAdapter(Context ctxt) {
			super(ctxt, R.layout.discovery_hostlist, R.id.discovery_hostlist_name);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = DiscoveryActivity.mInflater.inflate(R.layout.discovery_hostlist, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.discovery_hostlist_name);
				// holder.name.setTypeface(Configuration.getInstance().getApplicationFont(getBaseContext()));
				holder.ipAddress = (TextView) convertView.findViewById(R.id.discovery_hostlist_ip_address);
				// holder.ipAddress.setTypeface(Configuration.getInstance().getApplicationFont(getBaseContext()));
				holder.serverIcon = (ImageView) convertView.findViewById(R.id.discovery_hostlist_icon);
				holder.arrowIcon = (ImageView) convertView.findViewById(R.id.discovery_hostlist_arrow);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final HomeServerHost host = DiscoveryActivity.this.hosts.get(position);
			holder.name.setText(host.getInfo().getName());
			holder.ipAddress.setText(host.ipAddress);
			holder.serverIcon.setImageBitmap(Configuration.getInstance().getAppIcon(DiscoveryActivity.this.me, R.string.icon_home, 30));
			holder.arrowIcon.setImageBitmap(Configuration.getInstance().getAppIcon(DiscoveryActivity.this.me, R.string.icon_arrow_right, 30));

			return convertView;
		}
	}

	/**
	 * Discover hosts
	 */
	protected void startDiscovering() {
		this.mDiscoveryTask = new HomeServerDiscovery();
		this.mDiscoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		setProgressBarVisibility(true);
		setProgressBarIndeterminateVisibility(true);
		this.initList();
	}

	protected void initList() {
		this.adapter.clear();
		this.hosts = new ArrayList<HomeServerHost>();
	}

	public void addHost(HomeServerHost host) {
		this.hosts.add(host);
		this.adapter.add(null);
	}

	protected class HomeServerDiscovery extends AsyncTask<Void, HomeServerHost, Void> {

		protected final String TAG = "HomeServerDiscovery";
		protected LinearLayout loadingLayout = null;
		protected LinearLayout discoveryDescription = null;

		protected WifiManager mWifi;

		@Override
		protected void onPreExecute() {
			this.mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

			this.loadingLayout = (LinearLayout) findViewById(R.id.discovery_loading);
			this.discoveryDescription = (LinearLayout) findViewById(R.id.discovery_description);
			this.loadingLayout.setVisibility(View.VISIBLE);
			this.discoveryDescription.setVisibility(View.GONE);

			Drawable searchIconDrawable = Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_search, 30, Color.GRAY);
			DiscoveryActivity.this.btn_search.setCompoundDrawablesWithIntrinsicBounds(searchIconDrawable, null, null, null);
			DiscoveryActivity.this.btn_search.setText(R.string.btn_discover_searching);
			DiscoveryActivity.this.btn_search.setEnabled(false);
		}

		@Override
		protected void onProgressUpdate(HomeServerHost... host) {
			if (host != null && host.length > 0) {
				DiscoveryActivity.this.addHost(host[0]);
				this.loadingLayout.setVisibility(View.GONE);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				try {
					DatagramSocket socket = new DatagramSocket(null);
					socket.setReuseAddress(true);
					socket.setBroadcast(true);
					socket.bind(new InetSocketAddress(Discovery.BROADCAST_PORT));
					socket.setBroadcast(true);
					socket.setSoTimeout(2000);

					this.sendDiscoveryRequest(socket);
					this.listenForResponses(socket);
				} catch (SocketTimeoutException e) {
				}
			} catch (IOException e) {
				Log.e(this.TAG, "Could not send discovery request", e);
			}
			return null;
		}

		/**
		 * Send a broadcast UDP packet containing a request for homeservers to
		 * announce themselves.
		 * 
		 * @throws IOException
		 */
		protected void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
			String data = Discovery.BROADCAST_SERVICE;
			Log.d(this.TAG, "Sending data " + data);

			DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), this.getBroadcastAddress(), Discovery.BROADCAST_PORT);
			socket.send(packet);
		}

		/**
		 * Calculate the broadcast IP we need to send the packet along.
		 */
		protected InetAddress getBroadcastAddress() throws IOException {
			DhcpInfo dhcp = this.mWifi.getDhcpInfo();
			if (dhcp == null) {
				Log.d(this.TAG, "Could not get dhcp info");
				return null;
			}

			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			byte[] quads = new byte[4];
			for (int k = 0; k < 4; k++) {
				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			}
			return InetAddress.getByAddress(quads);
		}

		/**
		 * Listen on socket for responses, timing out after TIMEOUT_MS
		 * 
		 * @param socket
		 *            socket on which the announcement request was sent
		 * @throws IOException
		 */
		protected void listenForResponses(DatagramSocket socket) throws IOException {
			byte[] buf = new byte[65508];
			try {
				while (true) {
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					String message = new String(packet.getData(), 0, packet.getLength());
					if (!message.equals(Discovery.BROADCAST_SERVICE)) {
						Log.d(this.TAG, "Received response " + message);
						HomeServerHost newHost;
						try {
							newHost = new HomeServerHost(packet.getAddress().getHostAddress(), message);
							publishProgress(newHost);
						} catch (Exception e) {
							Log.e(this.TAG, "Could not parse home server configuration from: " + message);
						}
					}
				}
			} catch (SocketTimeoutException e) {
				Log.d(this.TAG, "Receive timed out");
			}
		}

		@Override
		protected void onPostExecute(Void unused) {
			DiscoveryActivity.this.btn_search.setText(R.string.btn_discover);
			Drawable searchIconDrawable = Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_search, 30);
			DiscoveryActivity.this.btn_search.setCompoundDrawablesWithIntrinsicBounds(searchIconDrawable, null, null, null);
			DiscoveryActivity.this.btn_search.setEnabled(true);

			this.loadingLayout.setVisibility(View.GONE);
			if (DiscoveryActivity.this.hosts.size() == 0) {
				this.discoveryDescription.setVisibility(View.VISIBLE);
			}
		}
	}
}
