package net.yourhome.app.canvas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.yourhome.app.bindings.BindingController;
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
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;
import net.yourhome.common.net.messagestructures.general.GCMRegistrationMessage;
import net.yourhome.app.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CanvasActivity extends FragmentActivity {
	public enum LoadingStatus {
		ERROR("error"),
		CONNECTING("connecting"),
		CONNECTED("connected"),
		UNKNOWN("unknown");
		
		LoadingStatus(String status) {
			this.status = status;
		}
		private String status;
		public String convert() {return status;}
	}
    private final String HIDE_STATUSBAR = "net.yourhome.app.canvas.HIDE_STATUSBAR";
    private Handler mHandler = new Handler();
	
	private CanvasFragmentAdapter canvasFragmentAdapter;
	private CanvasActivity me = this;
	private ViewPager mViewPager;
	private String TAG = "CanvasActivity";
	private HomeServerConnector homeServerConnector;
	//private boolean wifiConnected;

	private Map<String,List<ValueBinding>> activityResultListeners = new HashMap<String,List<ValueBinding>>();

	// Google cloud messaging
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private GoogleCloudMessaging gcm;
	//private AtomicInteger msgId = new AtomicInteger();
	private String regid;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String SENDER_ID = "11576394209"; // Google project API
															// id
															// https://console.developers.google.com/project/11576394209/apiui/credential

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
			String action = intent.getAction();
			Log.d(TAG, action);
			setLoadingStatus(action);
			if(action.equals(LoadingStatus.CONNECTED.convert())) {
				// Send Google Cloud Messaging registration ID
				GCMRegistrationMessage registrationMessage = new GCMRegistrationMessage(regid, Configuration.getInstance().getDeviceName());
				Point currentCanvasSize = new Point();
				WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    display.getRealSize(currentCanvasSize);
                }else {
                    display.getSize(currentCanvasSize);
                }
				registrationMessage.screenWidth = currentCanvasSize.x;
				registrationMessage.screenHeight = currentCanvasSize.y;
				
				try {
					homeServerConnector.sendCommand(registrationMessage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	};

	private LoadingStatus lastStatus = LoadingStatus.UNKNOWN;
	private void setLoadingStatus( String status ) {
		ImageView statusIcon = (ImageView) me.findViewById(R.id.connectionStatusIcon);
		if(!status.equals(lastStatus.convert())) {
			if (status.equals(LoadingStatus.ERROR.convert())
					|| status.equals(LoadingStatus.CONNECTING.convert())) {
				Log.d(TAG, "[UI] Received disconnect");
	
				Bitmap loadingIcon = Configuration.getInstance().getAppIcon(getBaseContext(), R.string.icon_spinner, 40, Color.WHITE);
				Bitmap loadingIconShadow = Util.addShadow(loadingIcon, loadingIcon.getHeight(), loadingIcon.getWidth(), Color.BLACK, 1, 1, 1);
				
				RotateAnimation rotate = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				rotate.setDuration(2000);
				rotate.setRepeatCount(Animation.INFINITE);
				rotate.setInterpolator(new LinearInterpolator());
	
				statusIcon.setImageBitmap(loadingIconShadow);
				statusIcon.setVisibility(View.VISIBLE);
				statusIcon.startAnimation(rotate);
			}else if (status.equals(LoadingStatus.CONNECTED.convert())) {
				Log.d(TAG, "[UI] Received connected");
				statusIcon.clearAnimation();
				statusIcon.setImageResource(0);
				statusIcon.setVisibility(View.GONE);
			}
		}
	}
	@Override
	protected void onRestoreInstanceState(Bundle inState) {
		Log.d(TAG, "onRestoreInstanceState - " + inState);
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
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, // Set
																			// fullscreen
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN); // Set fullscreen
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Set
																			// screen
																			// on
																			// landscape
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep
																				// screen
																				// awake
		View decorView = getWindow().getDecorView();
		int uiOptions = 0;
		//| View.SYSTEM_UI_FLAG_FULLSCREEN
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN ;
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE ;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}else {
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		decorView.setSystemUiVisibility(uiOptions);
        /* decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                                // Immersive mode does not exist - hide the action bars after a few seconds
                                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                          | View.SYSTEM_UI_FLAG_FULLSCREEN ;
                                            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                                        }}, 3000);
                                }
                            }
                        }
                    }
                });
        */
		Log.d(TAG, "OnCreate canvasActivity - " + savedInstanceState);

		SharedPreferences settings = me.getSharedPreferences("USER", MODE_PRIVATE);
		setContentView(R.layout.controller_canvas_sections);
		addLegacyOverflowButton(getWindow());
		super.onCreate(savedInstanceState);

		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(me);

			if (regid.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		//setLoadingStatus(LoadingStatus.CONNECTING.convert());

		// Navigation drawer - set a custom shadow that overlays the main
		// content when the drawer opens
		// set up the drawer's list view with items and click listener
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);

		navDrawerItems = new ArrayList<NavDrawerItem>();
		navDrawerItems.add(new NavDrawerItem(getString(R.string.drawer_settings), R.string.icon_cogs, false));
		navDrawerAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(navDrawerAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		
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
		// Check device for Play Services APK.
		checkPlayServices();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		// Disconnect from socket
		if (this.homeServerConnector != null) {
			this.homeServerConnector.disconnect();
		}
        BindingController.getInstance().destroy();
		// Unload configuration
		Configuration.getInstance().destroy();
		Log.d(TAG, "Activity destroyed");

		if (this.canvasFragmentAdapter != null) {
			//this.canvasFragmentAdapter.removeAllfragments();
			this.canvasFragmentAdapter = null;
		}

		if (this.homeServerConnector != null) {
			this.homeServerConnector.destroy();
			this.homeServerConnector = null;
		}
		activityResultListeners.clear();
		activityResultListeners = null;
        mDrawerList = null;
        me = null;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, DiscoveryActivity.MENU_OPEN_DISCOVERY, 0, R.string.server_discovery).setIcon(android.R.drawable.ic_menu_add);

		return true;
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		Log.d(TAG, "onAttachFragment - " + fragment.getTag());
		super.onAttachFragment(fragment);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DiscoveryActivity.MENU_OPEN_DISCOVERY:
			startActivity(new Intent(this, DiscoveryActivityImp.class));
			me.finish();
			return true;
		}
		return false;
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(me);
		int appVersion = getAppVersion(me);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.apply();
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(me);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;
					storeRegistrationId(me, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				Log.i(TAG, msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
			}
		}.execute(null, null, null);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
            if(context != null && context.getPackageManager() != null) {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionCode;
            }
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
        return 0;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		return getSharedPreferences(CanvasActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}
	// // End google cloud messaging
    public void removeActivityResultListener(ValueBinding binding) {
        List<ValueBinding> bindings = activityResultListeners.get(binding.getStageElementId());
        if(bindings != null) {
            bindings.remove(binding);
        }
    }
	public void addActivityResultListener(ValueBinding binding) {
        List<ValueBinding> bindings = activityResultListeners.get(binding.getControlIdentifier().getKey());
        if(bindings == null) { bindings = new ArrayList<>(); activityResultListeners.put(binding.getStageElementId(),bindings); }
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
                    if(listeningBindings != null) {
                        for (ValueBinding valueBinding : listeningBindings) {
                            //valueBinding.setValue(newColor+"");
                            UIEvent event = new UIEvent(UIEvent.Types.SET_VALUE);
                            event.setProperty("VALUE",newColor);
                            valueBinding.viewPressed(null, event);
                        }
                    }
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
			}
		}
	}
	public void nextFragment() {
		int currentItem = mViewPager.getCurrentItem()%canvasFragmentAdapter.getRealCount();
		int nextItem = currentItem+1;
		if(nextItem>canvasFragmentAdapter.getRealCount()-1) {
			nextItem = 0;
		}
        int circularNextItem = nextItem+canvasFragmentAdapter.getRealCount()*CanvasFragmentAdapter.LOOPS_COUNT/2;
        mViewPager.setCurrentItem(nextItem+canvasFragmentAdapter.getRealCount()*CanvasFragmentAdapter.LOOPS_COUNT/2, false);
	}
	public void previousFragment() {
		int currentItem = mViewPager.getCurrentItem()%canvasFragmentAdapter.getRealCount();
		int nextItem = currentItem-1;
		if(nextItem<0) {
			nextItem = canvasFragmentAdapter.getRealCount()-1;
		}
        int circularNextItem = nextItem+canvasFragmentAdapter.getRealCount()*CanvasFragmentAdapter.LOOPS_COUNT/2;
		mViewPager.setCurrentItem(circularNextItem, false);
	}
	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(position != parent.getChildCount()-1) {
				mViewPager.setCurrentItem(position+canvasFragmentAdapter.getRealCount()*CanvasFragmentAdapter.LOOPS_COUNT/2);
				mDrawerLayout.closeDrawer(mDrawerList);
			}else {
				mDrawerLayout.closeDrawer(mDrawerList);
				startActivity(new Intent(me, DiscoveryActivityImp.class));
				me.finish();
			}
		}
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
			configurationPresent = resumeFromSuspend && !(this.localConfigurationPath == null || this.configurationName == null);
		}

		final ProgressBar progress = (ProgressBar) findViewById(R.id.canvasActivityLoader);
		final TextView text = (TextView) findViewById(R.id.loadingHomeServerMessage);

		@Override
		protected void onPreExecute() {

			//SharedPreferences settings = me.getSharedPreferences("USER", MODE_PRIVATE);

			// Build loading message
			text.setText("Loading your homeserver");
			text.setVisibility(View.VISIBLE);
			progress.setVisibility(View.VISIBLE);

			// Prepare connection
			homeServerConnector = HomeServerConnector.getInstance();
			homeServerConnector.setMainContext(me);
		}

		@Override
		protected void onProgressUpdate(String[] status){
			if(status.length > 0) {
				text.setText(status[0]);
			}
	    }
		@Override
		protected HomeServerHost doInBackground(Void... arg0) {

			HomeServerHost checkedHost;
			checkedHost = super.doInBackground(arg0);

			// Check if configuration is still published
			if(checkedHost == null || checkedHost.getInfo().getConfigurations().get(configurationName) == null) {
				connectionTestResult = false;
			}

			// Load configuration
            JSONObject activeConfiguration = null;
			if (configurationPresent || connectionTestResult) {
                try {
                    if (configurationPresent) {
                        try {
                            activeConfiguration = Configuration.getInstance().parseConfiguration(new File(this.localConfigurationPath));
                            Configuration.getInstance().setHomeServerHostName(checkedHost.ipAddress);
                            Configuration.getInstance().setHomeServerPort(checkedHost.port);
                        } catch (FileNotFoundException | java.util.zip.ZipException e) {
                            // The configuration could not be read; Reload it!
                            configurationPresent = false;
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Configuration invalid+" + e.getMessage());
                            configurationPresent = false;
                        }
                    }
                    if(!configurationPresent) {
                        Configuration configuration = Configuration.getInstance();
                        activeConfiguration = configuration.initialize(getBaseContext(), checkedHost, configurationName);
                    }

                    // Fill collections with fragments based on configuration
                    // Generate canvascollection
                    canvasFragmentAdapter = new CanvasFragmentAdapter(getSupportFragmentManager(), activeConfiguration);
                    menuItems = canvasFragmentAdapter.initializeBindings();
                    Log.d(TAG, "canvasFragmentAdapter filled with " + canvasFragmentAdapter.getCount() + " fragments");

                    SharedPreferences settings = me.getSharedPreferences("USER", MODE_PRIVATE);
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
                }catch(Exception e) {
                    e.printStackTrace();
                    //Util.appendToLogFile(me, ExceptionUtils.getStackTrace(e));
                    connectionTestResult = false;
                    configurationPresent = false;
                    error = true;
                }
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(HomeServerHost result) {
            if(error) {
                Toast.makeText(me.getBaseContext(),
                        getResources().getString(R.string.error_loading_configuration), Toast.LENGTH_LONG).show();

                Thread thread = new Thread(){
                    @Override
                    public void run() {
                    try {
                        Thread.sleep(2000);
                        startActivity(new Intent(me, DiscoveryActivityImp.class));
                        me.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                };
                thread.start();
            }else if ((connectionTestResult || configurationPresent)) {
				// Set screen orientation
				switch(Configuration.getInstance().getOrientation()) {
				case landscape:
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
				case portrait:
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				}
				
				// Connect to websocket
				progress.setVisibility(View.GONE);
				text.setVisibility(View.GONE);
				mViewPager = (ViewPager) findViewById(R.id.pager);
//				mViewPager.setOffscreenPageLimit(15);
				mViewPager.setOffscreenPageLimit(1);
				mViewPager.setAdapter(canvasFragmentAdapter);
                mViewPager.setCurrentItem(canvasFragmentAdapter.getRealCount() * CanvasFragmentAdapter.LOOPS_COUNT / 2);

				if (menuItems != null && menuItems.size() > 0) {
					for (int i = menuItems.size()-1; i >=0; i--) {
						String menuItem = menuItems.get(i);
						// adding nav drawer items to array
						switch (i) {
						case 0:
							navDrawerItems.add(0,new NavDrawerItem(menuItem, R.string.icon_home, false));
							break;
						default:
							navDrawerItems.add(0,new NavDrawerItem(menuItem, R.string.icon_file_o, false));
							break;
						}	
					}
					navDrawerAdapter.notifyDataSetChanged();
				}
				
				Thread connectorThread = new Thread() {
					public void run() {
                    if(homeServerConnector != null) {
                        homeServerConnector.connect();
                    }
					}
				};
				
				// Register UI event listeners
				IntentFilter filter = new IntentFilter();
				filter.addAction(LoadingStatus.CONNECTED.convert());
				filter.addAction(LoadingStatus.ERROR.convert());
				filter.addAction(LoadingStatus.CONNECTING.convert());
				LocalBroadcastManager.getInstance(me).registerReceiver(mMessageReceiver, filter);
				
				connectorThread.start();

			} else {
				startActivity(new Intent(me, DiscoveryActivityImp.class));
				me.finish();
			}
		}
	}

}