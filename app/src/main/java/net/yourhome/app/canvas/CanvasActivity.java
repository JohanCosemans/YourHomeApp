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
package net.yourhome.app.canvas;

import android.app.Activity;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.iid.FirebaseInstanceId;
import net.yourhome.app.R;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.PageNavigationBinding;
import net.yourhome.app.bindings.ValueBinding;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.net.discovery.DiscoveryActivity;
import net.yourhome.app.net.discovery.DiscoveryActivityImp;
import net.yourhome.app.net.discovery.HomeServerHost;
import net.yourhome.app.net.discovery.IPHostnameChecker;
import net.yourhome.app.util.ColorPickerActivity;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.util.Util;
import net.yourhome.app.views.ColorPickerView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.net.messagestructures.general.GCMRegistrationMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanvasActivity extends FragmentActivity {
    public enum CanvasEvents {
        CONNECTION_STATUS_CHANGE,
        PAGE_NAVIGATION
    }
	public enum LoadingStatus {
		ERROR, CONNECTING, CONNECTED, UNKNOWN;
	}

	private final String HIDE_STATUSBAR = "net.yourhome.app.canvas.HIDE_STATUSBAR";
	private Handler mHandler = new Handler();

	private CanvasFragmentAdapter canvasFragmentAdapter;
	private CanvasActivity me = this;
	private ViewPager mViewPager;
	private String TAG = "CanvasActivity";
	private HomeServerConnector homeServerConnector;

	private Map<String, List<ValueBinding>> activityResultListeners = new HashMap<String, List<ValueBinding>>();

	// Google cloud messaging
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	// Navigation drawer
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private List<String> menuItems;
	private List<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter navDrawerAdapter;

	// Events coming from HomeServerConnector and current activity
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
            CanvasActivity.CanvasEvents intentAction = CanvasEvents.valueOf(intent.getAction());
            switch(intentAction) {
                case CONNECTION_STATUS_CHANGE:
                    LoadingStatus status = LoadingStatus.valueOf(intent.getStringExtra(HomeServerConnector.CONNECTION_STATUS));
                    CanvasActivity.this.setLoadingStatus(status);
                    if (status.equals(LoadingStatus.CONNECTED)) {
                        // Send Google Cloud Messaging registration ID
                        GCMRegistrationMessage registrationMessage = new GCMRegistrationMessage(FirebaseInstanceId.getInstance().getToken(), Configuration.getInstance().getDeviceName());
                        Point currentCanvasSize = new Point();
                        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        Display display = wm.getDefaultDisplay();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            display.getRealSize(currentCanvasSize);
                        } else {
                            display.getSize(currentCanvasSize);
                        }
                        registrationMessage.screenWidth = currentCanvasSize.x;
                        registrationMessage.screenHeight = currentCanvasSize.y;

                        try {
                            CanvasActivity.this.homeServerConnector.sendCommand(registrationMessage);
                        } catch (Exception e) {
                            Log.e(TAG,"Could not register device for notifications",e);
                        }
                    }
                    break;
                case PAGE_NAVIGATION:
                    Integer fragmentPosition = canvasFragmentAdapter.getPositionOf(intent.getStringExtra(PageNavigationBinding.PAGE_ID));
                    if(fragmentPosition != null) {
                        me.goToFragment(fragmentPosition);
                    }else {
                        Log.e(TAG,"Could not find fragment with pageId "+intent.getStringExtra(PageNavigationBinding.PAGE_ID));
                    }
                    break;
            }

		};
	};

	private LoadingStatus lastStatus = LoadingStatus.UNKNOWN;

	private void setLoadingStatus(LoadingStatus status) {
		ImageView statusIcon = (ImageView) this.me.findViewById(R.id.connectionStatusIcon);
		if (!status.equals(this.lastStatus)) {
			if (status.equals(LoadingStatus.ERROR) || status.equals(LoadingStatus.CONNECTING)) {
				Log.d(this.TAG, "[UI] Received disconnect");

				Bitmap loadingIcon = Configuration.getInstance().getAppIcon(getBaseContext(), R.string.icon_spinner, 40, Color.WHITE);
				Bitmap loadingIconShadow = Util.addShadow(loadingIcon, loadingIcon.getHeight(), loadingIcon.getWidth(), Color.BLACK, 1, 1, 1);

				RotateAnimation rotate = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				rotate.setDuration(2000);
				rotate.setRepeatCount(Animation.INFINITE);
				rotate.setInterpolator(new LinearInterpolator());

				statusIcon.setImageBitmap(loadingIconShadow);
				statusIcon.setVisibility(View.VISIBLE);
				statusIcon.startAnimation(rotate);
			} else if (status.equals(LoadingStatus.CONNECTED)) {
				Log.d(this.TAG, "[UI] Received connected");
				statusIcon.clearAnimation();
				statusIcon.setImageResource(0);
				statusIcon.setVisibility(View.GONE);
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle inState) {
		Log.d(this.TAG, "onRestoreInstanceState - " + inState);
		super.onRestoreInstanceState(inState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("MainActivity", "onSaveInstanceState - " + outState);
		String currentConfigurationPath = Configuration.getInstance().getConfigurationPath();
		Log.d("MainActivity", "OnSaveInstanceState: Saving config path at " + currentConfigurationPath);
		outState.putString("configurationPath", currentConfigurationPath);
		outState.putString("HOMESERVER_CONFIGURATION", Configuration.getInstance().getConfigurationName());

		// / !! Remove the saved fragment state: This will cause the creation of
		// five empty fragments (black screens)
		if (outState.containsKey("android:support:fragments")) {
			outState.remove("android:support:fragments");
		}
	}

	public static void addLegacyOverflowButton(Window window) {
		if (window.peekDecorView() == null) {
			throw new RuntimeException("Must call addLegacyOverflowButton() after setContentView()");
		}

		try {
			window.addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
		} catch (NoSuchFieldException e) {
			// Ignore since this field won't exist in most versions of Android
		} catch (IllegalAccessException e) {
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Set fullscreen
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, //
		// Set
		// fullscreen
		// WindowManager.LayoutParams.FLAG_FULLSCREEN); // Set fullscreen
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// // Set
		// screen
		// on
		// landscape
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep
																				// screen
																				// awake
		View decorView = getWindow().getDecorView();
		int uiOptions = 0;
		// | View.SYSTEM_UI_FLAG_FULLSCREEN
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            //uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		} else {
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		decorView.setSystemUiVisibility(uiOptions);
		/*
		 * decorView.setOnSystemUiVisibilityChangeListener (new
		 * View.OnSystemUiVisibilityChangeListener() {
		 * 
		 * @Override public void onSystemUiVisibilityChange(int visibility) { //
		 * Note that system bars will only be "visible" if none of the //
		 * LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set. if
		 * ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) { // TODO: The
		 * system bars are visible. Make any desired if(Build.VERSION.SDK_INT <
		 * Build.VERSION_CODES.KITKAT) { // Immersive mode does not exist - hide
		 * the action bars after a few seconds if ((visibility &
		 * View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) { mHandler.postDelayed(new
		 * Runnable() {
		 * 
		 * @Override public void run() { int uiOptions =
		 * View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
		 * View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
		 * View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
		 * ; getWindow().getDecorView().setSystemUiVisibility(uiOptions); }},
		 * 3000); } } } } });
		 */
		Log.d(this.TAG, "OnCreate canvasActivity - " + savedInstanceState);

		SharedPreferences settings = this.me.getSharedPreferences("USER", MODE_PRIVATE);
		setContentView(R.layout.controller_canvas_sections);
		CanvasActivity.addLegacyOverflowButton(getWindow());
		super.onCreate(savedInstanceState);

		// Navigation drawer - set a custom shadow that overlays the main
		// content when the drawer opens
		// set up the drawer's list view with items and click listener
		this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		this.mDrawerList = (ListView) findViewById(R.id.left_drawer);
		this.mDrawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);

		this.navDrawerItems = new ArrayList<NavDrawerItem>();
		this.navDrawerItems.add(new NavDrawerItem(getString(R.string.drawer_settings), R.string.icon_cogs, false));
		this.navDrawerAdapter = new NavDrawerListAdapter(getApplicationContext(), this.navDrawerItems);
		this.mDrawerList.setAdapter(this.navDrawerAdapter);
		this.mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		/*** LAN connection data ***/
		String homeserverIP = settings.getString("HOMESERVER_IP", null);
		int homeserverPort = (int) settings.getInt("HOMESERVER_PORT", 0);

		/*** Internet connection data ***/
		String homeserverExtIP = "";
		int homeserverExtPort = 0;
		try {
			homeserverExtIP = settings.getString("HOMESERVER_EXT_IP", null);
			homeserverExtPort = settings.getInt("HOMESERVER_EXT_PORT", 0);
		} catch (Exception e) {
		}

		boolean resumeFromSuspend = false;
		String connectionModeBeforeSuspend = null;
		String localConfigurationPath = null;
		String configurationName = settings.getString("HOMESERVER_CONFIGURATION", null);

		/*** Check if connected to lan or to internet ***/
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean wifiConnected = mWifi.isConnected();

		// After suspend refresh the images before loading the fragments!
		if (savedInstanceState != null) {

			// Load directly the configuration without downloading it first.
			localConfigurationPath = savedInstanceState.getString("configurationPath", null);
			configurationName = savedInstanceState.getString("HOMESERVER_CONFIGURATION", null);
			resumeFromSuspend = true;
			connectionModeBeforeSuspend = settings.getString("HOMESERVER_CONNECTION_MODE", null);
		}

		/*** Verify server & load configuration ***/
		// This will only connect to the websocket in case of resuming the
		// application
		CanvasActivityLoader loader = new CanvasActivityLoader(homeserverIP, homeserverPort, homeserverExtIP, homeserverExtPort, wifiConnected, resumeFromSuspend, connectionModeBeforeSuspend, localConfigurationPath, configurationName);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mMessageReceiver);
		// Disconnect from socket
		if (this.homeServerConnector != null) {
			this.homeServerConnector.disconnect();
		}
		BindingController.getInstance().destroy();
		// Unload configuration
		Configuration.getInstance().destroy();
		Log.d(this.TAG, "Activity destroyed");

		if (this.canvasFragmentAdapter != null) {
			// this.canvasFragmentAdapter.removeAllfragments();
			this.canvasFragmentAdapter = null;
		}

		if (this.homeServerConnector != null) {
			this.homeServerConnector.destroy();
			this.homeServerConnector = null;
		}
		this.activityResultListeners.clear();
		this.activityResultListeners = null;
		this.mDrawerList = null;
		this.me = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, DiscoveryActivity.MENU_OPEN_DISCOVERY, 0, R.string.server_discovery).setIcon(android.R.drawable.ic_menu_add);

		return true;
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		Log.d(this.TAG, "onAttachFragment - " + fragment.getTag());
		super.onAttachFragment(fragment);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DiscoveryActivity.MENU_OPEN_DISCOVERY:
			startActivity(new Intent(this, DiscoveryActivityImp.class));
			this.me.finish();
			return true;
		}
		return false;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			if (context != null && context.getPackageManager() != null) {
				PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				return packageInfo.versionCode;
			}
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
		return 0;
	}

	public void removeActivityResultListener(ValueBinding binding) {
		List<ValueBinding> bindings = this.activityResultListeners.get(binding.getStageElementId());
		if (bindings != null) {
			bindings.remove(binding);
		}
	}

	public void addActivityResultListener(ValueBinding binding) {
		List<ValueBinding> bindings = this.activityResultListeners.get(binding.getControlIdentifier().getKey());
		if (bindings == null) {
			bindings = new ArrayList<>();
			this.activityResultListeners.put(binding.getStageElementId(), bindings);
		}
		bindings.add(binding);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == ColorPickerView.ACTION_GETCOLOR) {

			if (resultCode == Activity.RESULT_OK) {
				// Send to colorpicker
				Bundle b = intent.getExtras();
				if (b != null) {
					String storeColorKey = b.getString(ColorPickerActivity.STORE_COLOR_KEY);
					int newColor = b.getInt(storeColorKey);
					String stageElementId = b.getString("stageElementId");
					List<ValueBinding> listeningBindings = this.activityResultListeners.get(stageElementId);
					if (listeningBindings != null) {
						for (ValueBinding valueBinding : listeningBindings) {
							// valueBinding.setValue(newColor+"");
							UIEvent event = new UIEvent(UIEvent.Types.SET_VALUE);
							event.setProperty("VALUE", newColor);
							valueBinding.viewPressed(null, event);
						}
					}
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
			}
		}
	}

	public void nextFragment() {
		int currentItem = this.mViewPager.getCurrentItem() % this.canvasFragmentAdapter.getRealCount();
		int nextItem = currentItem + 1;
		if (nextItem > this.canvasFragmentAdapter.getRealCount() - 1) {
			nextItem = 0;
		}
		int circularNextItem = nextItem + this.canvasFragmentAdapter.getRealCount() * CanvasFragmentAdapter.LOOPS_COUNT / 2;
		this.mViewPager.setCurrentItem(nextItem + this.canvasFragmentAdapter.getRealCount() * CanvasFragmentAdapter.LOOPS_COUNT / 2, false);
	}

	public void previousFragment() {
		int currentItem = this.mViewPager.getCurrentItem() % this.canvasFragmentAdapter.getRealCount();
		int nextItem = currentItem - 1;
		if (nextItem < 0) {
			nextItem = this.canvasFragmentAdapter.getRealCount() - 1;
		}
		int circularNextItem = nextItem + this.canvasFragmentAdapter.getRealCount() * CanvasFragmentAdapter.LOOPS_COUNT / 2;
		this.mViewPager.setCurrentItem(circularNextItem, false);
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CanvasActivity.this.mDrawerLayout.closeDrawer(CanvasActivity.this.mDrawerList);
			if (position != parent.getChildCount() - 1) {
                goToFragment(position);
			} else {
				startActivity(new Intent(CanvasActivity.this.me, DiscoveryActivityImp.class));
				CanvasActivity.this.me.finish();
			}
		}
	}
    public void goToFragment(int position) {
        this.mViewPager.setCurrentItem(position + CanvasActivity.this.canvasFragmentAdapter.getRealCount() * CanvasFragmentAdapter.LOOPS_COUNT / 2);

    }

	private class CanvasActivityLoader extends IPHostnameChecker {

		String localConfigurationPath;
		String configurationName;
		boolean configurationPresent;
		boolean error = false;

		public CanvasActivityLoader(String ipAddress, int port, String ipAddressExt, int portExt, boolean localFirst, boolean resumeFromSuspend, String connectionModeBeforeSuspend, String localConfigurationPath, String configurationName) {
			super(ipAddress, port, ipAddressExt, portExt, localFirst, resumeFromSuspend, connectionModeBeforeSuspend);
			this.localConfigurationPath = localConfigurationPath;
			this.configurationName = configurationName;

			// We assume that there is a configuration present if we are
			// resuming the application and the path is filled
			this.configurationPresent = resumeFromSuspend && !(this.localConfigurationPath == null || this.configurationName == null);
		}

		final ProgressBar progress = (ProgressBar) findViewById(R.id.canvasActivityLoader);
		final TextView text = (TextView) findViewById(R.id.loadingHomeServerMessage);

		@Override
		protected void onPreExecute() {

			// SharedPreferences settings = me.getSharedPreferences("USER",
			// MODE_PRIVATE);

			// Build loading message
			this.text.setText("Loading your homeserver");
			this.text.setVisibility(View.VISIBLE);
			this.progress.setVisibility(View.VISIBLE);

			// Prepare connection
			CanvasActivity.this.homeServerConnector = HomeServerConnector.getInstance();
			CanvasActivity.this.homeServerConnector.setMainContext(CanvasActivity.this.me);
		}

		@Override
		protected void onProgressUpdate(String[] status) {
			if (status.length > 0) {
				this.text.setText(status[0]);
			}
		}

		@Override
		protected HomeServerHost doInBackground(Void... arg0) {

			HomeServerHost checkedHost;
			checkedHost = super.doInBackground(arg0);

			// Check if configuration is still published
			if (checkedHost == null || checkedHost.getInfo().getConfigurations().get(this.configurationName) == null) {
				connectionTestResult = false;
			}

			// Load configuration
			JSONObject activeConfiguration = null;
			if (this.configurationPresent || connectionTestResult) {
				try {
					if (this.configurationPresent) {
						try {
							activeConfiguration = Configuration.getInstance().parseConfiguration(new File(this.localConfigurationPath));
							Configuration.getInstance().setHomeServerHostName(checkedHost.ipAddress);
							Configuration.getInstance().setHomeServerPort(checkedHost.port);
						} catch (FileNotFoundException | java.util.zip.ZipException e) {
							// The configuration could not be read; Reload it!
							this.configurationPresent = false;
						} catch (JSONException e) {
							Log.e(CanvasActivity.this.TAG, "JSON Configuration invalid+" + e.getMessage());
							this.configurationPresent = false;
						}
					}
					if (!this.configurationPresent) {
						Configuration configuration = Configuration.getInstance();
						activeConfiguration = configuration.initialize(getBaseContext(), checkedHost, this.configurationName);
					}

					// Fill collections with fragments based on configuration
					// Generate canvascollection
					CanvasActivity.this.canvasFragmentAdapter = new CanvasFragmentAdapter(getSupportFragmentManager(), activeConfiguration);
					CanvasActivity.this.menuItems = CanvasActivity.this.canvasFragmentAdapter.initializeBindings();
					Log.d(CanvasActivity.this.TAG, "canvasFragmentAdapter filled with " + CanvasActivity.this.canvasFragmentAdapter.getCount() + " fragments");

					SharedPreferences settings = CanvasActivity.this.me.getSharedPreferences("USER", MODE_PRIVATE);
					Editor edit = settings.edit();
					if (checkedHost != null && this.ipAddress.equals(checkedHost.ipAddress)) {
						Configuration.getInstance().setHomeServerHostName(checkedHost.ipAddress);
						Configuration.getInstance().setHomeServerPort(checkedHost.port);
						edit.putString("HOMESERVER_CONNECTION_MODE", "LAN");
						edit.commit();
					} else if (checkedHost != null && this.ipAddressExt.equals(checkedHost.ipAddress)) {
						Configuration.getInstance().setHomeServerHostName(checkedHost.ipAddress);
						Configuration.getInstance().setHomeServerPort(checkedHost.port);
						edit.putString("HOMESERVER_CONNECTION_MODE", "INTERNET");
						edit.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
					// Util.appendToLogFile(me,
					// ExceptionUtils.getStackTrace(e));
					connectionTestResult = false;
					this.configurationPresent = false;
					this.error = true;
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(HomeServerHost result) {
			if (this.error) {
				Toast.makeText(CanvasActivity.this.me.getBaseContext(), getResources().getString(R.string.error_loading_configuration), Toast.LENGTH_LONG).show();

				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
							startActivity(new Intent(CanvasActivity.this.me, DiscoveryActivityImp.class));
							CanvasActivity.this.me.finish();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			} else if ((connectionTestResult || this.configurationPresent)) {
				// Set screen orientation
				switch (Configuration.getInstance().getOrientation()) {
				case landscape:
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
				case portrait:
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				}

				// Connect to websocket
				this.progress.setVisibility(View.GONE);
				this.text.setVisibility(View.GONE);
				CanvasActivity.this.mViewPager = (ViewPager) findViewById(R.id.pager);
				// mViewPager.setOffscreenPageLimit(15);
				CanvasActivity.this.mViewPager.setOffscreenPageLimit(1);
				CanvasActivity.this.mViewPager.setAdapter(CanvasActivity.this.canvasFragmentAdapter);
				CanvasActivity.this.mViewPager.setCurrentItem(CanvasActivity.this.canvasFragmentAdapter.getRealCount() * CanvasFragmentAdapter.LOOPS_COUNT / 2);

				if (CanvasActivity.this.menuItems != null && CanvasActivity.this.menuItems.size() > 0) {
					for (int i = CanvasActivity.this.menuItems.size() - 1; i >= 0; i--) {
						String menuItem = CanvasActivity.this.menuItems.get(i);
						// adding nav drawer items to array
						switch (i) {
						case 0:
							CanvasActivity.this.navDrawerItems.add(0, new NavDrawerItem(menuItem, R.string.icon_home, false));
							break;
						default:
							CanvasActivity.this.navDrawerItems.add(0, new NavDrawerItem(menuItem, R.string.icon_file_o, false));
							break;
						}
					}
					CanvasActivity.this.navDrawerAdapter.notifyDataSetChanged();
				}

				Thread connectorThread = new Thread() {
					@Override
					public void run() {
						if (CanvasActivity.this.homeServerConnector != null) {
							CanvasActivity.this.homeServerConnector.connect();
						}
					}
				};

				// Register UI event listeners
				IntentFilter filter = new IntentFilter();
                for(CanvasActivity.CanvasEvents event : CanvasActivity.CanvasEvents.values()){
                    filter.addAction(event.name());
                }
                LocalBroadcastManager.getInstance(CanvasActivity.this.me).registerReceiver(CanvasActivity.this.mMessageReceiver, filter);

				connectorThread.start();

			} else {
				startActivity(new Intent(CanvasActivity.this.me, DiscoveryActivityImp.class));
				CanvasActivity.this.me.finish();
			}
		}
	}

}