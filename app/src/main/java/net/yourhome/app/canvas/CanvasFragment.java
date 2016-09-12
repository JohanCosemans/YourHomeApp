package net.yourhome.app.canvas;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.views.ColorPickerView;
import net.yourhome.app.views.WebLinkView;
import net.yourhome.app.views.WebRSSView;
import net.yourhome.app.views.WebStaticHtmlView;
import net.yourhome.common.base.enums.ViewTypes;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.ButtonView;
import net.yourhome.app.views.ClockAnalogView;
import net.yourhome.app.views.ClockDigitalView;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.IPCameraView;
import net.yourhome.app.views.MultiStateButtonView;
import net.yourhome.app.views.PictureView;
import net.yourhome.app.views.PlusMinView;
import net.yourhome.app.views.SensorView;
import net.yourhome.app.views.ShapeView;
import net.yourhome.app.views.SliderView;
import net.yourhome.app.views.TextView;
import net.yourhome.app.views.musicplayer.PlaylistSelectorActivity;
import net.yourhome.app.views.musicplayer.views.AlbumImageView;
import net.yourhome.app.views.musicplayer.views.PlaylistView;
import net.yourhome.app.views.musicplayer.views.TrackDisplayView;
import net.yourhome.app.views.musicplayer.views.TrackProgressView;
import net.yourhome.common.net.model.viewproperties.WebLink;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


@SuppressLint("ValidFragment")
public class CanvasFragment extends Fragment {

    private static CanvasFragmentAdapter fragmentAdapter;

	private RelativeLayout view;
	private JSONObject pageDefinition;
	private Bitmap background;
	private String backgroundColor;
	private String TAG = "CANVASFRAGMENT";
	//private boolean shouldBeRefreshed = true;
	private List<DynamicView> allViews = new ArrayList<DynamicView>();

    private Integer pageId;
    private String title;
	private Point relativeScreenSize;

    public static CanvasFragment newInstance(int pageId, CanvasFragmentAdapter adapter) {
        CanvasFragment df = new CanvasFragment();
        Bundle args = new Bundle();
        args.putInt("PAGE", pageId);
        df.setArguments(args);
        fragmentAdapter = adapter;
        return df;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageId = getArguments().getInt("PAGE");
        Log.d(TAG, "onCreate CanvasFragment "+title+","+this.getId()+","+savedInstanceState);

        if(this.pageDefinition== null) {
            try {
                this.pageDefinition = fragmentAdapter.getPageDefinition(pageId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Assign everything besides the view hierarchy here.

        try {
            // Check if there is a background set
            String backgroundColor = this.pageDefinition.getString("backgroundColor");
            if(backgroundColor != null && !backgroundColor.equals("")) {
                this.backgroundColor = backgroundColor;
            }
            this.title = this.pageDefinition.getString("title");
            relativeScreenSize = new Point(Integer.parseInt(this.pageDefinition.getString("width")), Integer.parseInt(this.pageDefinition.getString("height")));

            String backgroundSrc = this.pageDefinition.getString("backgroundImageSrc");
            if(backgroundSrc != null && !backgroundSrc.equals("")) {
                background = Configuration.getInstance().loadBitmap(backgroundSrc);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView CanvasFragment " + title + "," + this.getId() + " savedstate: " + savedInstanceState);

        if(allViews != null) {
            this.view = new RelativeLayout(getActivity());
            if (backgroundColor != null) {
                try {
                    view.setBackgroundColor(Color.parseColor(backgroundColor));
                    if (background != null) {
                        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{view.getBackground(), new BitmapDrawable(view.getResources(), background)});
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            view.setBackground(layerDrawable);
                        } else {
                            view.setBackgroundDrawable(layerDrawable);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    view.setBackgroundColor(Color.BLACK);
                }
            } else {
                if (background != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                        view.setBackground(new BitmapDrawable(view.getResources(), background));
                    } else {
                        view.setBackgroundDrawable(new BitmapDrawable(view.getResources(), background));
                    }
                }
            }
            try {
                // Read subnodes with type "View"
                JSONArray viewObjects = this.pageDefinition.getJSONArray("objects");
                for (int i = 0; i < viewObjects.length(); i++) {
                    try {
                        JSONObject viewObject = viewObjects.getJSONObject(i);
                        JSONObject viewProperties = viewObject.getJSONObject("viewProperties");
                        String stageElementId = viewObject.getString("id");
                        JSONObject bindingProperties = null;
                        if (!viewObject.isNull("bindingProperties")) {
                            bindingProperties = viewObject.getJSONObject("bindingProperties");
                        }
                        ViewTypes type = ViewTypes.convert(viewProperties.getString("type"));
                        switch (type) {
                            case IMAGE:
                                PictureView pictureView = new PictureView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(pictureView.getView());
                                allViews.add(pictureView);
                                break;
                            case SHAPE:
                                ShapeView shapeView = new ShapeView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(shapeView.getView());
                                allViews.add(shapeView);
                                break;
                            case IMAGE_BUTTON:
                                ButtonView buttonView = new ButtonView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(buttonView.getView());
                                allViews.add(buttonView);
                                break;
                            case SLIDER:
                                SliderView sliderView = new SliderView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(sliderView.getView());
                                allViews.add(sliderView);
                                break;
                            case SENSOR:
                            case SENSOR_WITH_INDICATOR:
                                SensorView sensorView = new SensorView(this, stageElementId, viewProperties, bindingProperties);
                                sensorView.setOnClickListener(getActivity());
                                this.view.addView(sensorView.getView());
                                allViews.add(sensorView);
                                break;
					        case COLOR_PICKER:
                                ColorPickerView colorpickerView = new ColorPickerView(this, stageElementId, viewProperties, bindingProperties);
                                colorpickerView.setOnClickListener(getActivity());
                                this.view.addView(colorpickerView.getView());
                                allViews.add(colorpickerView);
                                break;
                            case TRACK_DISPLAY:
                                TrackDisplayView trackDisplayView = new TrackDisplayView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(trackDisplayView.getView());
                                allViews.add(trackDisplayView);
                                break;
                            case TRACK_PROGRESS:
                                TrackProgressView trackProgressView = new TrackProgressView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(trackProgressView.getView());
                                allViews.add(trackProgressView);
                                break;
                            case ALBUM_IMAGE:
                                AlbumImageView albumImageView = new AlbumImageView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(albumImageView.getView());
                                allViews.add(albumImageView);
                                break;
                            case PLAYLIST:
                                PlaylistView playlistView = new PlaylistView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(playlistView.getView());
                                allViews.add(playlistView);
                                break;
                            case PLAYLISTS:
                                ButtonView playlistsButtonView = new ButtonView(this, stageElementId, viewProperties, bindingProperties);
                                playlistsButtonView.setOnClickListener(getActivity(), PlaylistSelectorActivity.class);
                                this.view.addView(playlistsButtonView.getView());
                                allViews.add(playlistsButtonView);
                                break;
                            case CAMERA:
                                IPCameraView ipcameraView = new IPCameraView(this, stageElementId, viewProperties, bindingProperties);
                                ipcameraView.setOnClickListener(getActivity());
                                this.view.addView(ipcameraView.getView());
                                allViews.add(ipcameraView);
                                break;
                            case MULTI_STATE_BUTTON:
                                MultiStateButtonView multiStateButtonView = new MultiStateButtonView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(multiStateButtonView.getView());
                                allViews.add(multiStateButtonView);
                                break;
                            case PLUS_MIN_VALUE:
                                PlusMinView plusMinView = new PlusMinView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(plusMinView.getView());
                                allViews.add(plusMinView);
                                break;
                            case CLOCK_ANALOG:
                                ClockAnalogView analogClock = new ClockAnalogView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(analogClock.getView());
                                allViews.add(analogClock);
                                break;
                            case CLOCK_DIGITAL:
                                ClockDigitalView digitalClock = new ClockDigitalView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(digitalClock.getView());
                                allViews.add(digitalClock);
                                break;
                            case TEXT:
                                TextView textView = new TextView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(textView.getView());
                                allViews.add(textView);
                                break;
                            case WEB_LINK:
                                WebLinkView webLinkViewView = new WebLinkView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(webLinkViewView.getView());
                                allViews.add(webLinkViewView);
                                break;
                            case WEB_RSS:
                                WebRSSView webRSSView = new WebRSSView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(webRSSView.getView());
                                allViews.add(webRSSView);
                                break;
                            case WEB_STATIC_HTML:
                                WebStaticHtmlView webStaticHtmlView = new WebStaticHtmlView(this, stageElementId, viewProperties, bindingProperties);
                                this.view.addView(webStaticHtmlView.getView());
                                allViews.add(webStaticHtmlView);
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
		return view;
	}
	public String getTitle() {
		return this.title;
	}

	/**
	 * @return the relativeScreenSize
	 */
	public Point getRelativeScreenSize() {
		return relativeScreenSize;
	}

	/**
	 * @param relativeScreenSize the relativeScreenSize to set
	 */
	public void setRelativeScreenSize(Point relativeScreenSize) {
		this.relativeScreenSize = relativeScreenSize;
	}


	@Override
	public void onResume() {
		Log.d(TAG, "onResume CanvasFragment "+title+","+this.getId());
		super.onResume();
		View view = getView();
		if(view != null) {
			view.setFocusableInTouchMode(true);
			view.requestFocus();
			view.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {

					if (event.getAction()!=KeyEvent.ACTION_DOWN)
						return false;

					Activity activity = getActivity();
					if(activity instanceof CanvasActivity) {
						CanvasActivity canvasActivity = (CanvasActivity)activity;
						switch(event.getKeyCode()) {
							case KeyEvent.KEYCODE_PAGE_DOWN:
                            case KeyEvent.KEYCODE_VOLUME_DOWN:
								canvasActivity.nextFragment();
								return true;
							case KeyEvent.KEYCODE_PAGE_UP:
                            case KeyEvent.KEYCODE_VOLUME_UP:
								canvasActivity.previousFragment();
								return true;
						}
					}
					return false;
				}
			});
		}

	}
	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView CanvasFragment " + title + "," + this.getId());
		super.onDestroyView();
		this.view = null;
        if(allViews != null) {
            for (DynamicView v : allViews) {
                if(v != null) {
                    v.destroyView();
                }
            }
            allViews.clear();
            allViews = null;
        }
	}
	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy CanvasFragment "+this.getId());
		super.onDestroy();
		this.background = null;
		this.pageDefinition = null;
		this.backgroundColor = null;
	}
}
