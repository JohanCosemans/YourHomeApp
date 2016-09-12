package net.yourhome.app.canvas.ipcamera;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import net.yourhome.app.R;
import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.IPCameraBinding;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
	   requestWindowFeature(Window.FEATURE_NO_TITLE);						 // Set fullscreen
	   getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,     // Set fullscreen
			   WindowManager.LayoutParams.FLAG_FULLSCREEN); // Set fullscreen
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);   // Set screen on landscape
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ipcamera_main);
		loading = (RelativeLayout) this.findViewById(R.id.ipcamera_loading);

		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

			mSurface = (SurfaceView) findViewById(R.id.surface);
			holder = mSurface.getHolder();
			//holder.addCallback(this);
			loading.setVisibility(View.VISIBLE);
			//mSurface.setVisibility(View.INVISIBLE);
			mSurface.setZOrderMediaOverlay(true);
            //mSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
            //mSurface.setZOrderOnTop(true);
			//mSurface.setBackgroundColor(Color.GRAY);
			//mSurface.bringToFront();
			// Attach close to action
			ImageButton closeButton = (ImageButton)this.findViewById(R.id.ip_camera_close);
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
					videoUrl = extras.getString(IPCameraBinding.VIDEO_PATH);
                    String stageElementId = extras.getString(IPCameraBinding.STAGE_ELEMENT_ID);

                    AbstractBinding binding = BindingController.getInstance().getBindingFor(stageElementId);
                    if(binding != null && binding instanceof IPCameraBinding) {
                        cameraBinding = (IPCameraBinding)binding;
                        loading.setBackground(new BitmapDrawable(getResources(),cameraBinding.getImage()));
                    }else {
                        // Try to read the image path from the extras
                        String imagePath = extras.getString("imagePath");
                        if(imagePath!=null) {
                            try {
                                Bitmap snapshotImage = BitmapFactory.decodeStream(getBaseContext().openFileInput(imagePath));
                                if(snapshotImage != null) {
                                    loading.setBackground(new BitmapDrawable(getResources(), snapshotImage));
                                    getBaseContext().deleteFile(imagePath);
                                }
                            } catch (FileNotFoundException e) {
                            }
                        }
                    }
				}
			}
			Log.d(TAG, "Playing back " + videoUrl);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setSize(mVideoWidth, mVideoHeight);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			createPlayer(videoUrl);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		releasePlayer();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releasePlayer();
	}

	/*************
	 * Surface
	 *************/
	private void setSize(int width, int height) {
		mVideoWidth = width;
		mVideoHeight = height;
		if (mVideoWidth * mVideoHeight <= 1)
			return;

        if(holder == null || mSurface == null)
            return;

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

		float videoAR = (float) mVideoWidth / (float) mVideoHeight;
		float screenAR = (float) w / (float) h;

		if (screenAR < videoAR)
			h = (int) (w / videoAR);
		else
			w = (int) (h * videoAR);

		// force surface buffer size
		holder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		LayoutParams lp = mSurface.getLayoutParams();
		lp.width = w;
		lp.height = h;
		mSurface.setLayoutParams(lp);
		mSurface.invalidate();
	}
	/*************
	 * Player
	 *************/

	private void createPlayer(String media) {
        releasePlayer();
        try {

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(options);
            libvlc.setOnHardwareAccelerationError(this);
            holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc, Uri.parse(media));
			mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
	}

    private void releasePlayer() {

		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
			final IVLCVout vout = mMediaPlayer.getVLCVout();
			vout.removeCallback(this);
			vout.detachViews();
		}
		if (libvlc != null) {
			holder = null;
			libvlc.release();
			libvlc = null;
			mVideoWidth = 0;
			mVideoHeight = 0;
		}
    }

    /*************
     * Events
     *************/

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
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
            mOwner = new WeakReference<IPCameraActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
        	IPCameraActivity player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
				case MediaPlayer.Event.Playing:
					Log.d(TAG, "Playing");
					//mSurface.setVisibility(View.VISIBLE);
					loading.setVisibility(View.INVISIBLE);
                    //mSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
					break;
                case MediaPlayer.Event.Paused:
					Log.d(TAG, "Playing");
					break;
                case MediaPlayer.Event.Stopped:
					Log.d(TAG, "Stopped");
					break;
                default:
					Log.d(TAG, "event:"+event.type);
                    break;
            }
        }
    }

    @Override
    public void eventHardwareAccelerationError() {
        // Handle errors with hardware acceleration
        Log.e(TAG, "Error with hardware acceleration");
        this.releasePlayer();
        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }
}