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
package net.yourhome.app.canvas;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.ButtonView;
import net.yourhome.app.views.ClockAnalogView;
import net.yourhome.app.views.ClockDigitalView;
import net.yourhome.app.views.ColorPickerView;
import net.yourhome.app.views.DynamicView;
import net.yourhome.app.views.IPCameraView;
import net.yourhome.app.views.MultiStateButtonView;
import net.yourhome.app.views.PictureView;
import net.yourhome.app.views.PlusMinView;
import net.yourhome.app.views.SensorView;
import net.yourhome.app.views.ShapeView;
import net.yourhome.app.views.SliderView;
import net.yourhome.app.views.TextView;
import net.yourhome.app.views.WebLinkView;
import net.yourhome.app.views.WebRSSView;
import net.yourhome.app.views.WebStaticHtmlView;
import net.yourhome.app.views.musicplayer.PlaylistSelectorActivity;
import net.yourhome.app.views.musicplayer.views.AlbumImageView;
import net.yourhome.app.views.musicplayer.views.PlaylistView;
import net.yourhome.app.views.musicplayer.views.TrackDisplayView;
import net.yourhome.app.views.musicplayer.views.TrackProgressView;
import net.yourhome.common.base.enums.ViewTypes;

@SuppressLint("ValidFragment")
public class CanvasFragment extends Fragment {

	private static CanvasFragmentAdapter fragmentAdapter;

	private RelativeLayout view;
	private JSONObject pageDefinition;
	private Bitmap background;
	private String backgroundColor;
	private String TAG = "CANVASFRAGMENT";
	// private boolean shouldBeRefreshed = true;
	private List<DynamicView> allViews = new ArrayList<DynamicView>();

	private Integer pageId;
	private String title;
	private Point relativeScreenSize;

	public static CanvasFragment newInstance(int pageId, CanvasFragmentAdapter adapter) {
		CanvasFragment df = new CanvasFragment();
		Bundle args = new Bundle();
		args.putInt("PAGE", pageId);
		df.setArguments(args);
		CanvasFragment.fragmentAdapter = adapter;
		return df;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.pageId = getArguments().getInt("PAGE");
		Log.d(this.TAG, "onCreate CanvasFragment " + this.title + "," + this.getId() + "," + savedInstanceState);

		if (this.pageDefinition == null) {
			try {
				this.pageDefinition = CanvasFragment.fragmentAdapter.getPageDefinition(this.pageId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// Assign everything besides the view hierarchy here.

		try {
			// Check if there is a background set
			String backgroundColor = this.pageDefinition.getString("backgroundColor");
			if (backgroundColor != null && !backgroundColor.equals("")) {
				this.backgroundColor = backgroundColor;
			}
			this.title = this.pageDefinition.getString("title");
			this.relativeScreenSize = new Point(Integer.parseInt(this.pageDefinition.getString("width")), Integer.parseInt(this.pageDefinition.getString("height")));

			String backgroundSrc = this.pageDefinition.getString("backgroundImageSrc");
			if (backgroundSrc != null && !backgroundSrc.equals("")) {
				this.background = Configuration.getInstance().loadBitmap(backgroundSrc);
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
		Log.d(this.TAG, "onCreateView CanvasFragment " + this.title + "," + this.getId() + " savedstate: " + savedInstanceState);

		if (this.allViews != null) {
			this.view = new RelativeLayout(getActivity());
			if (this.backgroundColor != null) {
				try {
					this.view.setBackgroundColor(Color.parseColor(this.backgroundColor));
					if (this.background != null) {
						LayerDrawable layerDrawable = new LayerDrawable(new Drawable[] { this.view.getBackground(), new BitmapDrawable(this.view.getResources(), this.background) });
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							this.view.setBackground(layerDrawable);
						} else {
							this.view.setBackgroundDrawable(layerDrawable);
						}
					}
				} catch (IllegalArgumentException e) {
					this.view.setBackgroundColor(Color.BLACK);
				}
			} else {
				if (this.background != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

						this.view.setBackground(new BitmapDrawable(this.view.getResources(), this.background));
					} else {
						this.view.setBackgroundDrawable(new BitmapDrawable(this.view.getResources(), this.background));
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
							this.allViews.add(pictureView);
							break;
						case SHAPE:
							ShapeView shapeView = new ShapeView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(shapeView.getView());
							this.allViews.add(shapeView);
							break;
						case IMAGE_BUTTON:
							ButtonView buttonView = new ButtonView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(buttonView.getView());
							this.allViews.add(buttonView);
							break;
						case SLIDER:
							SliderView sliderView = new SliderView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(sliderView.getView());
							this.allViews.add(sliderView);
							break;
						case SENSOR:
						case SENSOR_WITH_INDICATOR:
							SensorView sensorView = new SensorView(this, stageElementId, viewProperties, bindingProperties);
							sensorView.setOnClickListener(getActivity());
							this.view.addView(sensorView.getView());
							this.allViews.add(sensorView);
							break;
						case COLOR_PICKER:
							ColorPickerView colorpickerView = new ColorPickerView(this, stageElementId, viewProperties, bindingProperties);
							colorpickerView.setOnClickListener(getActivity());
							this.view.addView(colorpickerView.getView());
							this.allViews.add(colorpickerView);
							break;
						case TRACK_DISPLAY:
							TrackDisplayView trackDisplayView = new TrackDisplayView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(trackDisplayView.getView());
							this.allViews.add(trackDisplayView);
							break;
						case TRACK_PROGRESS:
							TrackProgressView trackProgressView = new TrackProgressView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(trackProgressView.getView());
							this.allViews.add(trackProgressView);
							break;
						case ALBUM_IMAGE:
							AlbumImageView albumImageView = new AlbumImageView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(albumImageView.getView());
							this.allViews.add(albumImageView);
							break;
						case PLAYLIST:
							PlaylistView playlistView = new PlaylistView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(playlistView.getView());
							this.allViews.add(playlistView);
							break;
						case PLAYLISTS:
							ButtonView playlistsButtonView = new ButtonView(this, stageElementId, viewProperties, bindingProperties);
							playlistsButtonView.setOnClickListener(getActivity(), PlaylistSelectorActivity.class);
							this.view.addView(playlistsButtonView.getView());
							this.allViews.add(playlistsButtonView);
							break;
						case CAMERA:
							IPCameraView ipcameraView = new IPCameraView(this, stageElementId, viewProperties, bindingProperties);
							ipcameraView.setOnClickListener(getActivity());
							this.view.addView(ipcameraView.getView());
							this.allViews.add(ipcameraView);
							break;
						case MULTI_STATE_BUTTON:
							MultiStateButtonView multiStateButtonView = new MultiStateButtonView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(multiStateButtonView.getView());
							this.allViews.add(multiStateButtonView);
							break;
						case PLUS_MIN_VALUE:
							PlusMinView plusMinView = new PlusMinView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(plusMinView.getView());
							this.allViews.add(plusMinView);
							break;
						case CLOCK_ANALOG:
							ClockAnalogView analogClock = new ClockAnalogView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(analogClock.getView());
							this.allViews.add(analogClock);
							break;
						case CLOCK_DIGITAL:
							ClockDigitalView digitalClock = new ClockDigitalView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(digitalClock.getView());
							this.allViews.add(digitalClock);
							break;
						case TEXT:
							TextView textView = new TextView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(textView.getView());
							this.allViews.add(textView);
							break;
						case WEB_LINK:
							WebLinkView webLinkViewView = new WebLinkView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(webLinkViewView.getView());
							this.allViews.add(webLinkViewView);
							break;
						case WEB_RSS:
							WebRSSView webRSSView = new WebRSSView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(webRSSView.getView());
							this.allViews.add(webRSSView);
							break;
						case WEB_STATIC_HTML:
							WebStaticHtmlView webStaticHtmlView = new WebStaticHtmlView(this, stageElementId, viewProperties, bindingProperties);
							this.view.addView(webStaticHtmlView.getView());
							this.allViews.add(webStaticHtmlView);
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
		return this.view;
	}

	public String getTitle() {
		return this.title;
	}

	/**
	 * @return the relativeScreenSize
	 */
	public Point getRelativeScreenSize() {
		return this.relativeScreenSize;
	}

	/**
	 * @param relativeScreenSize
	 *            the relativeScreenSize to set
	 */
	public void setRelativeScreenSize(Point relativeScreenSize) {
		this.relativeScreenSize = relativeScreenSize;
	}

	@Override
	public void onResume() {
		Log.d(this.TAG, "onResume CanvasFragment " + this.title + "," + this.getId());
		super.onResume();
		View view = getView();
		if (view != null) {
			view.setFocusableInTouchMode(true);
			view.requestFocus();
			view.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {

					if (event.getAction() != KeyEvent.ACTION_DOWN) {
						return false;
					}

					Activity activity = getActivity();
					if (activity instanceof CanvasActivity) {
						CanvasActivity canvasActivity = (CanvasActivity) activity;
						switch (event.getKeyCode()) {
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
		Log.d(this.TAG, "onDestroyView CanvasFragment " + this.title + "," + this.getId());
		super.onDestroyView();
		this.view = null;
		if (this.allViews != null) {
			for (DynamicView v : this.allViews) {
				if (v != null) {
					v.destroyView();
				}
			}
			this.allViews.clear();
			this.allViews = null;
		}
	}

	@Override
	public void onDestroy() {
		Log.d(this.TAG, "onDestroy CanvasFragment " + this.getId());
		super.onDestroy();
		this.background = null;
		this.pageDefinition = null;
		this.backgroundColor = null;
	}
}
