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
package net.yourhome.app.canvas.ipcamera;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import net.yourhome.app.R;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.IPCameraBinding;

public class IPCameraActivity extends Activity implements IVLCVout.Callback, LibVLC.HardwareAccelerationError {

	private static final String TAG = "IPCameraActivity";

	private String videoUrl = null;

	// display surface
	private static SurfaceView mSurface;
	private SurfaceHolder holder;

	protected IPCameraBinding cameraBinding = null;

	// media player
	private LibVLC libvlc;
	private MediaPlayer mMediaPlayer = null;
	private int mVideoWidth;
	private int mVideoHeight;
	private final static int VideoSizeChanged = -1;

	private static RelativeLayout loading;

	/*************
	 * Activity
	 *************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Set fullscreen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, // Set
																			// fullscreen
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // Set fullscreen
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Set
																			// screen
																			// on
																			// landscape
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ipcamera_main);
		IPCameraActivity.loading = (RelativeLayout) this.findViewById(R.id.ipcamera_loading);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

			IPCameraActivity.mSurface = (SurfaceView) findViewById(R.id.surface);
			this.holder = IPCameraActivity.mSurface.getHolder();
			// holder.addCallback(this);
			IPCameraActivity.loading.setVisibility(View.VISIBLE);
			// mSurface.setVisibility(View.INVISIBLE);
			IPCameraActivity.mSurface.setZOrderMediaOverlay(true);
			// mSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
			// mSurface.setZOrderOnTop(true);
			// mSurface.setBackgroundColor(Color.GRAY);
			// mSurface.bringToFront();
			// Attach close to action
			ImageButton closeButton = (ImageButton) this.findViewById(R.id.ip_camera_close);
			closeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					finish();
				}

			});
			closeButton.bringToFront();
			// Receive path to play from intent
			Intent intent = this.getIntent();
			if (intent != null) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					this.videoUrl = extras.getString(IPCameraBinding.VIDEO_PATH);
					String stageElementId = extras.getString(IPCameraBinding.STAGE_ELEMENT_ID);

					AbstractBinding binding = BindingController.getInstance().getBindingFor(stageElementId);
					if (binding != null && binding instanceof IPCameraBinding) {
						this.cameraBinding = (IPCameraBinding) binding;
						IPCameraActivity.loading.setBackground(new BitmapDrawable(getResources(), this.cameraBinding.getImage()));
					} else {
						// Try to read the image path from the extras
						String imagePath = extras.getString("imagePath");
						if (imagePath != null) {
							try {
								Bitmap snapshotImage = BitmapFactory.decodeStream(getBaseContext().openFileInput(imagePath));
								if (snapshotImage != null) {
									IPCameraActivity.loading.setBackground(new BitmapDrawable(getResources(), snapshotImage));
									getBaseContext().deleteFile(imagePath);
								}
							} catch (FileNotFoundException e) {
							}
						}
					}
				}
			}
			Log.d(IPCameraActivity.TAG, "Playing back " + this.videoUrl);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.setSize(this.mVideoWidth, this.mVideoHeight);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			this.createPlayer(this.videoUrl);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.releasePlayer();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.releasePlayer();
	}

	/*************
	 * Surface
	 *************/
	private void setSize(int width, int height) {
		this.mVideoWidth = width;
		this.mVideoHeight = height;
		if (this.mVideoWidth * this.mVideoHeight <= 1) {
			return;
		}

		if (this.holder == null || IPCameraActivity.mSurface == null) {
			return;
		}

		// get screen size
		int w = getWindow().getDecorView().getWidth();
		int h = getWindow().getDecorView().getHeight();

		// getWindow().getDecorView() doesn't always take orientation into
		// account, we have to correct the values
		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (w > h && isPortrait || w < h && !isPortrait) {
			int i = w;
			w = h;
			h = i;
		}

		float videoAR = (float) this.mVideoWidth / (float) this.mVideoHeight;
		float screenAR = (float) w / (float) h;

		if (screenAR < videoAR) {
			h = (int) (w / videoAR);
		} else {
			w = (int) (h * videoAR);
		}

		// force surface buffer size
		this.holder.setFixedSize(this.mVideoWidth, this.mVideoHeight);

		// set display size
		LayoutParams lp = IPCameraActivity.mSurface.getLayoutParams();
		lp.width = w;
		lp.height = h;
		IPCameraActivity.mSurface.setLayoutParams(lp);
		IPCameraActivity.mSurface.invalidate();
	}

	/*************
	 * Player
	 *************/

	private void createPlayer(String media) {
		this.releasePlayer();
		try {

			// Create LibVLC
			// TODO: make this more robust, and sync with audio demo
			ArrayList<String> options = new ArrayList<String>();
			// options.add("--subsdec-encoding <encoding>");
			options.add("--aout=opensles");
			options.add("--audio-time-stretch"); // time stretching
			options.add("-vvv"); // verbosity
			this.libvlc = new LibVLC(options);
			this.libvlc.setOnHardwareAccelerationError(this);
			this.holder.setKeepScreenOn(true);

			// Create media player
			this.mMediaPlayer = new MediaPlayer(this.libvlc);
			this.mMediaPlayer.setEventListener(this.mPlayerListener);

			// Set up video output
			final IVLCVout vout = this.mMediaPlayer.getVLCVout();
			vout.setVideoView(IPCameraActivity.mSurface);
			vout.addCallback(this);
			vout.attachViews();

			Media m = new Media(this.libvlc, Uri.parse(media));
			this.mMediaPlayer.setMedia(m);
			this.mMediaPlayer.play();
		} catch (Exception e) {
			Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
		}
	}

	private void releasePlayer() {

		if (this.mMediaPlayer != null) {
			this.mMediaPlayer.stop();
			final IVLCVout vout = this.mMediaPlayer.getVLCVout();
			vout.removeCallback(this);
			vout.detachViews();
		}
		if (this.libvlc != null) {
			this.holder = null;
			this.libvlc.release();
			this.libvlc = null;
			this.mVideoWidth = 0;
			this.mVideoHeight = 0;
		}
	}

	/*************
	 * Events
	 *************/

	private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

	@Override
	public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
		if (width * height == 0) {
			return;
		}

		// store video size
		this.mVideoWidth = width;
		this.mVideoHeight = height;
		this.setSize(this.mVideoWidth, this.mVideoHeight);
	}

	@Override
	public void onSurfacesCreated(IVLCVout vout) {

	}

	@Override
	public void onSurfacesDestroyed(IVLCVout vout) {

	}

	private static class MyPlayerListener implements MediaPlayer.EventListener {
		private WeakReference<IPCameraActivity> mOwner;

		public MyPlayerListener(IPCameraActivity owner) {
			this.mOwner = new WeakReference<IPCameraActivity>(owner);
		}

		@Override
		public void onEvent(MediaPlayer.Event event) {
			IPCameraActivity player = this.mOwner.get();

			switch (event.type) {
			case MediaPlayer.Event.EndReached:
				Log.d(IPCameraActivity.TAG, "MediaPlayerEndReached");
				player.releasePlayer();
				break;
			case MediaPlayer.Event.Playing:
				Log.d(IPCameraActivity.TAG, "Playing");
				// mSurface.setVisibility(View.VISIBLE);
				IPCameraActivity.loading.setVisibility(View.INVISIBLE);
				// mSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
				break;
			case MediaPlayer.Event.Paused:
				Log.d(IPCameraActivity.TAG, "Playing");
				break;
			case MediaPlayer.Event.Stopped:
				Log.d(IPCameraActivity.TAG, "Stopped");
				break;
			default:
				Log.d(IPCameraActivity.TAG, "event:" + event.type);
				break;
			}
		}
	}

	@Override
	public void eventHardwareAccelerationError() {
		// Handle errors with hardware acceleration
		Log.e(IPCameraActivity.TAG, "Error with hardware acceleration");
		this.releasePlayer();
		Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
	}
}