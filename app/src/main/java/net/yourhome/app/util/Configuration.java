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

package net.yourhome.app.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import net.yourhome.app.net.discovery.HomeServerHost;

public class Configuration {

	public enum Orientations {
		portrait, landscape
	}

	public static final String CONFIGURATION_PATH = "/yourhome/configurations";
	private String TAG = "Configuration";
	private String configurationFileName;
	// private JSONObject activeConfiguration;
	private static Configuration configuration;
	private Map<String, Bitmap> images = new HashMap<String, Bitmap>();
	private Typeface applicationFont;
	private Typeface widgetIconFont;
	private Typeface appIconFont;
	private String hostName;
	private String connectionMode;
	private int port;
	private String configurationPath;
	private int defaultIconColor = Color.BLACK;
	private Orientations orientation;

	public Orientations getOrientation() {
		return this.orientation;
	}

	public String getHomeServerProtocol() {
		return "http";
	}

	public String getHomeServerHostName(Context context) {
		if (this.hostName == null || this.hostName.equals("")) {
			this.setDefaultConnectionDetails(context);
		}
		return this.hostName;
	}

	public String getConnectionMode(Context context) {
		if (this.connectionMode == null || this.connectionMode.equals("")) {
			this.setDefaultConnectionDetails(context);
		}
		return this.connectionMode;
	}

	public void setHomeServerHostName(String hostName) {
		this.hostName = hostName;
	}

	public boolean setDefaultConnectionDetails(Context context) {

		if (context == null) {
			return false;
		}
		SharedPreferences settings = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
		/*** Get connection data ***/
		try {
			this.connectionMode = settings.getString("HOMESERVER_CONNECTION_MODE", "LAN");
			if (this.connectionMode != null && this.connectionMode.equals("LAN")) {
				this.setHomeServerHostName(settings.getString("HOMESERVER_IP", null));
				this.setHomeServerPort((int) settings.getInt("HOMESERVER_PORT", 0));
			} else if (this.connectionMode != null && this.connectionMode.equals("INTERNET")) {
				this.setHomeServerHostName(settings.getString("HOMESERVER_EXT_IP", null));
				this.setHomeServerPort((int) settings.getInt("HOMESERVER_EXT_PORT", 0));
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public boolean toggleConnectionInternalExternal(Context context) {

		if (context == null) {
			return false;
		}

		SharedPreferences settings = context.getSharedPreferences("USER", Context.MODE_PRIVATE);

		/*** LAN connection data ***/
		String homeserverIP = "";
		int homeserverPort = 0;
		String oldConnectionMode = "";

		try {
			homeserverIP = settings.getString("HOMESERVER_IP", null);
			homeserverPort = (int) settings.getInt("HOMESERVER_PORT", 80);
			oldConnectionMode = settings.getString("HOMESERVER_CONNECTION_MODE", "LAN");
		} catch (Exception e) {
		}

		/*** Internet connection data ***/
		String homeserverExtIP = "";
		int homeserverExtPort = 0;
		try {
			homeserverExtIP = settings.getString("HOMESERVER_EXT_IP", null);
			homeserverExtPort = settings.getInt("HOMESERVER_EXT_PORT", 0);
		} catch (Exception e) {
		}

		Editor edit = settings.edit();
		if (oldConnectionMode.equals("LAN") && homeserverExtIP != null && !homeserverExtIP.equals("")) {
			this.setHomeServerHostName(homeserverExtIP);
			this.setHomeServerPort(homeserverExtPort);
			this.connectionMode = "INTERNET";
			edit.putString("HOMESERVER_CONNECTION_MODE", this.connectionMode);
			edit.commit();
			return true;
		} else if (oldConnectionMode.equals("INTERNET") && homeserverIP != null && !homeserverIP.equals("")) {
			this.setHomeServerHostName(homeserverIP);
			this.setHomeServerPort(homeserverPort);
			this.connectionMode = "LAN";
			edit.putString("HOMESERVER_CONNECTION_MODE", this.connectionMode);
			edit.commit();
			return true;
		}
		return false;
	}

	public int getHomeServerPort(Context context) {
		if (this.hostName == null || this.hostName.equals("")) {
			this.setDefaultConnectionDetails(context);
		}
		return this.port;
	}

	public void setHomeServerPort(int port) {
		this.port = port;
	}

	public String getConfigurationPath() {
		return this.configurationPath;
	}

	public String getConfigurationName() {
		return this.configurationFileName;
	}

	// public void initialize(String configurationName, String hostName, int
	// port, String configFileName) {
	public JSONObject initialize(Context context, HomeServerHost host, String configFileName) throws java.util.zip.ZipException, IOException {
		Log.d("Configuration", "Start initialization of configuration" + configFileName);

		this.configurationFileName = configFileName;
		this.hostName = host.ipAddress;
		this.port = host.port;

		// Read the file size of the buffered version
		// File root = android.os.Environment.getExternalStorageDirectory();
		// File bufferedConfiguration = new File(root.getAbsolutePath() +
		// CONFIGURATION_PATH, configFileName);
		File bufferedConfiguration = new File(context.getFilesDir() + Configuration.CONFIGURATION_PATH, configFileName);
		// Read file size of buffered server configuration
		net.yourhome.common.net.model.Configuration remoteConfiguration = null;
		if (bufferedConfiguration.exists()) {
			remoteConfiguration = host.getInfo().getConfigurations().get(bufferedConfiguration.getName());
			/*
			 * String serverInfoString; try { serverInfoString =
			 * HomeServerConnector.getInstance().sendApiCall("/Info");
			 * ServerInfo serverInfo = new ServerInfo(new
			 * JSONObject(serverInfoString)); remoteConfiguration =
			 * serverInfo.getConfigurations().get(bufferedConfiguration.getName(
			 * )); } catch (Exception e) { e.printStackTrace(); }
			 */
		}
		if (bufferedConfiguration != null && remoteConfiguration != null && remoteConfiguration.getSize() == bufferedConfiguration.length()) {
			// Read configuration from the bufferFile root =
			// android.os.Environment.getExternalStorageDirectory();
			Log.d("Configuration", "Reading configuration from local filesystem ");
			this.configurationPath = bufferedConfiguration.getAbsolutePath();
			try {
				return this.parseConfiguration(bufferedConfiguration);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			// Read new version from the server
			try {
				Log.d("Configuration", "Reading configuration from remote filesystem");
				File configurationPath = this.downloadConfiguration(context, this.getHomeServerProtocol() + "://" + this.hostName + ":" + this.port + "/api/Project/" + URLEncoder.encode(this.configurationFileName, "UTF-8"), this.configurationFileName);
				return this.parseConfiguration(configurationPath);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public JSONObject parseConfiguration(File configurationFile) throws IOException, FileNotFoundException, JSONException, java.util.zip.ZipException {
		Log.d(this.TAG, "Parsing configuration on path " + configurationFile.getAbsolutePath());
		// Parse the zip file
		JSONObject activeConfiguration = null;
		if (!configurationFile.exists()) {
			Log.e("Configuration", "Configuration file not found on path " + configurationFile.getAbsolutePath());
		} else {
			FileInputStream configurationInput = new FileInputStream(configurationFile);
			if (configurationInput != null) {
				ZipInputStream zin = new ZipInputStream(configurationInput);

				ZipEntry currentEntry;
				boolean cont = true;
				String configurationJson = null;
				while (cont && (currentEntry = zin.getNextEntry()) != null) {
					String entryName = currentEntry.getName();

					// First parse images folder, then load layout xml
					if (entryName.startsWith("images/") && entryName.length() > 7) {
						// Parse image
						this.preloadImage(entryName, zin);
					} else if (entryName.endsWith(".json")) {
						configurationJson = new Scanner(zin, "UTF-8").useDelimiter("\\A").next();
					}
				}
				zin.close();
				activeConfiguration = new JSONObject(configurationJson);

				String orientationString = activeConfiguration.getString("orientation");
				if (orientationString == null) {
					this.orientation = Orientations.landscape;
				} else {
					this.orientation = Orientations.valueOf(orientationString);
				}
			}
		}
		// Log.d(TAG, "Parsed configuration on path
		// "+configurationFile.getAbsolutePath());
		return activeConfiguration;
	}

	private Configuration() {
	}

	public static Configuration getInstance() {
		if (Configuration.configuration == null) {
			Configuration.configuration = new Configuration();
		}
		return Configuration.configuration;
	}

	public void destroy() {
		this.images.clear();
	}

	private void preloadImage(String name, ZipInputStream image) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(image);
			this.images.put(name, bitmap);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
	}

	private File downloadConfiguration(Context context, String configurationUrl, String fileName) throws IOException {
		// File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(context.getFilesDir() + Configuration.CONFIGURATION_PATH);
		if (dir.exists() == false) {
			// Create folder
			dir.mkdirs();
		} else {
			// Delete existing configuration files
			String[] children = dir.list();
			if (children != null) {
				for (String element : children) {
					File file = new File(dir, element);
					if (file.getName().contains(".zip")) {
						file.delete();
					}
				}
			}
		}
		configurationUrl = configurationUrl.replace("+", "%20");
		URL url = new URL(configurationUrl);
		File file = new File(dir, fileName);

		// long startTime = System.currentTimeMillis();
		Log.d("Configuration", "download url:" + url);

		URLConnection uconn = url.openConnection();
		uconn.setReadTimeout(10000);
		uconn.setConnectTimeout(700);

		InputStream is = uconn.getInputStream();
		BufferedInputStream bufferinstream = new BufferedInputStream(is);

		ByteArrayBuffer baf = new ByteArrayBuffer(5000);
		int current = 0;
		while ((current = bufferinstream.read()) != -1) {
			baf.append((byte) current);
		}

		FileOutputStream fos = new FileOutputStream(file);
		fos.write(baf.toByteArray());
		fos.flush();
		fos.close();

		if (!file.exists()) {
			Toast.makeText(context, "Failed to download layout configuration.", Toast.LENGTH_SHORT).show();
		}

		return file;
	}

	public Bitmap loadBitmap(final String imageFileName) {
		// return this.images.get("images/"+imageFileName);
		Bitmap returnBitmap = this.images.get(imageFileName);
		// String ok = "ok";
		// if(returnBitmap == null) {
		// ok = "nok";
		// }
		// Log.d("Configuration" , "loadBitmap: "+imageFileName+": "+ok);

		return returnBitmap;
	}

	/*
	 * public JSONObject getPage(int pageId) throws JSONException { return
	 * activeConfiguration.getJSONArray("pages").getJSONObject(pageId); } public
	 * int getPageCount(){ try { return
	 * activeConfiguration.getJSONArray("pages").length(); } catch
	 * (JSONException e) { return 0; } }
	 */

	public Typeface getApplicationFont(AssetManager assets) {
		if (this.applicationFont == null) {
			// this.applicationFont = Typeface.createFromAsset(assets,
			// "fonts/KozGoPro-Regular.otf");
			this.applicationFont = Typeface.createFromAsset(assets, "fonts/OpenSans-Regular.ttf");
		}
		return this.applicationFont;
	}

	public Typeface getWidgetIconFont(Context context) {
		if (this.widgetIconFont == null) {
			this.widgetIconFont = Typeface.createFromAsset(context.getAssets(), "fonts/icomoon-widget.ttf");
		}
		return this.widgetIconFont;
	}

	public Typeface getAppIconFont(Context context) {
		if (this.appIconFont == null) {
			this.appIconFont = Typeface.createFromAsset(context.getAssets(), "fonts/icomoon-app.ttf");
		}
		return this.appIconFont;
	}

	public Bitmap drawText(Context context, String text, int fontSizeSP, int color, boolean bold, Typeface typeface) {

		int fontSizePX = this.convertDiptoPix(context, fontSizeSP);

		Paint paint = new Paint();
		// this is a must
		paint.setAntiAlias(true);
		// this is a must if you have tiny text or really thin font like i do
		paint.setSubpixelText(true);
		paint.setStyle(Style.FILL);
		paint.setTypeface(typeface);
		paint.setTextSize(fontSizePX);
		paint.setColor(color);
		paint.setFakeBoldText(bold);

		int padding = (fontSizePX / 10);
		int width = (int) (paint.measureText(text) + padding * 2);
		int height = fontSizePX + padding * 2;

		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		Canvas canvas = new Canvas(result);

		int theY = fontSizePX;

		// fills the bitmap with inverted "color" for debugging
		/*
		 * if(DEBUG_MODE) { canvas.drawColor(color^0x00FFFFFF); }
		 */

		canvas.drawText(text, padding, theY, paint);

		return result;
	}

	public int convertDiptoPix(Context context, float dip) {
		if (dip == 0) {
			return 0;
		}
		float scale = this.getDensity(context);
		return (int) (dip * scale + 0.5f);
	}

	public int convertPixtoDip(Context context, double pixel) {
		if (pixel == 0) {
			return 0;
		}
		float scale = this.getDensity(context);
		return (int) ((pixel - 0.5f) / scale);
	}

	private float getDensity(Context context) {
		float scale = context.getResources().getDisplayMetrics().density;
		return scale;
	}

	public Drawable scaleImage(Context context, Drawable image, float scaleFactor) {

		if ((image == null) || !(image instanceof BitmapDrawable)) {
			return image;
		}

		Bitmap b = ((BitmapDrawable) image).getBitmap();

		int sizeX = Math.round(image.getIntrinsicWidth() * scaleFactor);
		int sizeY = Math.round(image.getIntrinsicHeight() * scaleFactor);

		Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);

		image = new BitmapDrawable(context.getResources(), bitmapResized);

		return image;

	}

	public Drawable getAppIconDrawable(Context context, int icon, int sizeSp, int color) {
		Bitmap iconBitmap = this.getAppIcon(context, icon, sizeSp, color);
		return new BitmapDrawable(context.getResources(), iconBitmap);
	}

	public Drawable getAppIconDrawable(Context context, int icon, int sizeSp) {
		Bitmap iconBitmap = this.getAppIcon(context, icon, sizeSp);
		return new BitmapDrawable(context.getResources(), iconBitmap);
	}

	public Bitmap getAppIcon(Context context, int icon, int sizeSp) {
		return this.getAppIcon(context, icon, sizeSp, this.defaultIconColor);
	}

	public Bitmap getAppIcon(Context context, int icon, int sizeSp, int textColor) {
		if (icon != 0 && context != null && context.getResources() != null) {
			String iconText = (String) context.getResources().getText(icon);
			return this.drawText(context, iconText, sizeSp, textColor, false, this.getAppIconFont(context));
		}
		return null;
	}

	public Drawable getWidgetIconDrawable(Context context, int icon, int sizeSp, int textColor) {
		Bitmap iconBitmap = this.getWidgetIcon(context, icon, sizeSp, textColor);
		return new BitmapDrawable(context.getResources(), iconBitmap);
	}

	public Bitmap getWidgetIcon(Context context, int icon, int sizeSp, int textColor) {
		if (icon != 0 && context != null && context.getResources() != null) {
			String iconText = (String) context.getResources().getText(icon);
			return this.drawText(context, iconText, sizeSp, textColor, false, this.getWidgetIconFont(context));
		}
		return null;
	}

	public Bitmap getWidgetIcon(Context context, String iconName, int sizeSp, int textColor) {
		if (iconName != null && !iconName.equals("") && context != null && context.getResources() != null) {
			int identifier = context.getResources().getIdentifier(iconName, "string", context.getPackageName());
			return this.getWidgetIcon(context, identifier, sizeSp, textColor);
		}
		return null;
	}

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return this.capitalize(model);
		} else {
			return this.capitalize(manufacturer) + " " + model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
}
