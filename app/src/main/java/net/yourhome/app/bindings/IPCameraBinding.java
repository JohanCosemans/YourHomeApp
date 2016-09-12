package net.yourhome.app.bindings;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.common.net.messagestructures.JSONMessage;
import net.yourhome.common.net.messagestructures.ipcamera.SnapshotMessage;
import net.yourhome.common.net.messagestructures.ipcamera.SnapshotRequestMessage;
import net.yourhome.app.canvas.ipcamera.IPCameraActivity;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.util.JSONMessageCaller;
import net.yourhome.app.util.Util;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.UIEvent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

public class IPCameraBinding extends AbstractBinding {

    public static String VIDEO_PATH="videoPath";
    public static String STAGE_ELEMENT_ID="stageElementId";

	private Bitmap currentImage;
	private String videoPath;

    public void destroy() {
        super.destroy();
        currentImage = null;
        videoPath = null;
    }

	public IPCameraBinding(String bindingId, JSONObject bindingProperties) throws JSONException {
		super(bindingId, bindingProperties);

		scheduleRefreshSnapshots();
	}

	@Override
	public void handleMessage(JSONMessage message) {
		if(message instanceof SnapshotMessage) {
            this.videoPath = ((SnapshotMessage) message).videoPath;
            String fullImageUrl = Configuration.getInstance().getHomeServerProtocol() + "://" +
                    Configuration.getInstance().getHomeServerHostName(HomeServerConnector.getInstance().getMainContext()) + ":" +
                    Configuration.getInstance().getHomeServerPort(HomeServerConnector.getInstance().getMainContext()) +
                    ((SnapshotMessage) message).snapshotUrl;
            loadSnapshot(fullImageUrl);
		}
	}

	@Override
	protected void buildBinding() {
	}

	@Override
	public void viewPressed(DynamicView v, UIEvent event) {
		
		// Start dialog window
		if(videoPath != null) {
			Intent intent = new Intent(v.getView().getContext(),IPCameraActivity.class);
			intent.putExtra(VIDEO_PATH,videoPath);
			intent.putExtra(STAGE_ELEMENT_ID,getStageElementId());
			v.getView().getContext().startActivity(intent);
		}
	}
	
	@Override
	public void viewLongPressed(DynamicView v, UIEvent event) {
	}
	public Bitmap getImage() {
		return currentImage;
	}
	private void loadSnapshot(String snapshotUrl) {
		// Initialize data loader
		IPCameraSnapshotLoader loader = new IPCameraSnapshotLoader();
		String[] urlParam = new String[] { snapshotUrl };
    	loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,(String[]) urlParam);
	}
	public void refreshSnapshot() {
		// Request a snapshot from the server (async)
		JSONMessageCaller loader = new RequestImageCaller(HomeServerConnector.getInstance().getMainContext());
		SnapshotRequestMessage snapshotMessage = new SnapshotRequestMessage();
		snapshotMessage.controlIdentifiers = this.getControlIdentifier();
    	loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, snapshotMessage);
    	
    	/*
		SnapshotRequestMessage snapshotMessage = new SnapshotRequestMessage();
		snapshotMessage.controlIdentifiers = this.getControlIdentifier();
		try {
			BindingController.getInstance().handleCommand(HomeServerConnector.getInstance().sendSyncMessage(snapshotMessage).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	private void scheduleRefreshSnapshots() {
		// Refresh every 10 minutes
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				refreshSnapshot();
			}}, new Date(), 600000);
	}
	
	private class IPCameraSnapshotLoader extends AsyncTask<String,Void,Void> {
        
		private Bitmap snapshot = null;
		
        protected void onPreExecute(){
			setLoaderState(View.VISIBLE);	
        }
        
		@Override
		protected Void doInBackground(String... snapshotUrls) {
			if(snapshotUrls.length > 0) {
				String snapshotUrl = snapshotUrls[0];
				try {
					snapshot = Util.getBitmapFromURL(snapshotUrl);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		
        protected void onPostExecute(Void result){
    		// Update loader of all listeners
			setLoaderState(View.INVISIBLE);
			if(snapshot != null) {
				currentImage = snapshot;
				updateViews();
			}
        }        
    }
	protected class RequestImageCaller extends JSONMessageCaller {

		public RequestImageCaller(Context context) {
			super(context);
		}

		protected void onPreExecute() {
			setLoaderState(View.VISIBLE);	
		}

		@Override
		protected void onPostExecute(JSONMessage result) {

			setLoaderState(View.GONE);	
			handleMessage(result);

		}
	}

}
