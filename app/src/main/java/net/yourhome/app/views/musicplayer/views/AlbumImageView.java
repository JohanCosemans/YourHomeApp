package net.yourhome.app.views.musicplayer.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.canvas.CanvasFragment;
import net.yourhome.app.net.HomeServerConnector;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
public class AlbumImageView extends MusicPlayerView {

	private ImageView image;
	private RelativeLayout.LayoutParams params;
	private String lastLoadedImagePath = null;
	
	public AlbumImageView(CanvasFragment canvas, String stageElementId, JSONObject viewProperties, JSONObject bindingProperties) throws JSONException {
		super(canvas, stageElementId,viewProperties,bindingProperties);
		buildView(viewProperties);
		addBinding(bindingProperties);
	}
	
	/**private void setAlbumImage(byte[] imageAsBytes) {
		image.setImageBitmap(
	            BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
	    );
	}*/
	private void setAlbumImage(String url) {
		
		Configuration configuration = Configuration.getInstance();
		Context mainContext = HomeServerConnector.getInstance().getMainContext();
		configuration.getHomeServerHostName(HomeServerConnector.getInstance().getMainContext());
		
		
		String fullImageUrl = configuration.getHomeServerProtocol()+"://"+
				configuration.getHomeServerHostName(mainContext)+":"+
				configuration.getHomeServerPort(mainContext)+ url;
		
		AlbumImageLoader imageLoader = new AlbumImageLoader();
        imageLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,fullImageUrl);
    
	}
	
	@Override
	public View getView() {
		return image;
	}

	@Override
	public void buildView(JSONObject viewProperties) throws JSONException {
		super.buildView(viewProperties);

		// Layout
		params = new RelativeLayout.LayoutParams(layoutParameters.width,layoutParameters.height);
		params.leftMargin = layoutParameters.left;
		params.topMargin = layoutParameters.top;

        image = new ImageView(canvas.getActivity());
        image.setLayoutParams(params);
	}
	@Override
	public void refreshView() {
		String image = musicPlayerBinding.getStatus().imagePath;
		if(lastLoadedImagePath != image) {
			this.setAlbumImage(image);
		}
	}
	
	private void clear() {
		Bitmap imageBitmap = Configuration.getInstance().getAppIcon(HomeServerConnector.getInstance().getMainContext(), R.string.icon_music79, 200, Color.WHITE);
		//this.image.setImageResource(R.drawable.ic_album_note);
		this.image.setImageBitmap(imageBitmap);
		this.image.setBackgroundColor(Color.rgb(80, 80, 80));
	}

	private class AlbumImageLoader extends AsyncTask<String,String,Bitmap> {
		 //private Bitmap bitmap;
		 
		@Override
		protected Bitmap doInBackground(String... src) {
			return getBitmapFromURL(src[0]);
		}
		@Override
		protected void onPostExecute(Bitmap b) {
			if(b != null) {
				image.setImageBitmap(b);
			}else {
				clear();
			}
		}

		private Bitmap getBitmapFromURL(String URL)
		{       
		  Bitmap bitmap = null;
		  InputStream in = null;       
		     try {
		         //in = OpenHttpConnection(URL);
		         in = new URL(URL).openStream();
		         if(in != null) {
		        	 bitmap = BitmapFactory.decodeStream(in);
		        	 in.close();
		         }
		     } catch (IOException e1) {
		    	 e1.printStackTrace();
		     }
		     return bitmap;               
		}
	 }
	


}
