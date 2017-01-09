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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import net.yourhome.app.views.ButtonView;
import net.yourhome.app.views.ClockAnalogView;
import net.yourhome.app.views.ClockDigitalView;
import net.yourhome.app.views.ColorPickerView;
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
import net.yourhome.app.views.musicplayer.views.MusicPlayerView;
import net.yourhome.common.base.enums.ViewTypes;

public class CanvasFragmentAdapter extends FragmentPagerAdapter {

    private int realCount = -1;
	private JSONObject activeConfiguration;
    private Map<String,Integer> pageIndexes = new HashMap<>();

	public static int LOOPS_COUNT = 10000;

	public CanvasFragmentAdapter(FragmentManager fm, JSONObject activeConfiguration) {
		super(fm);
		this.activeConfiguration = activeConfiguration;
	}

	public JSONObject getPageDefinition(int i) throws JSONException {
		return this.activeConfiguration.getJSONArray("pages").getJSONObject(i);
	}

	public List<String> initializeBindings() {
		List<String> menuList = new ArrayList<String>();
		if (this.activeConfiguration != null) {
			JSONArray pages;
			try {
				pages = this.activeConfiguration.getJSONArray("pages");
				for (int i = 0; i < pages.length(); i++) {
					JSONObject page;
					try {
						page = pages.getJSONObject(i);
						String title = page.getString("title");
						menuList.add(title);
                        pageIndexes.put(page.getString("pageId"),i);

						// Initialize all bindings
						JSONArray viewObjects = page.getJSONArray("objects");
						for (int j = 0; j < viewObjects.length(); j++) {
							try {
								JSONObject viewObject = viewObjects.getJSONObject(j);
								JSONObject viewProperties = viewObject.getJSONObject("viewProperties");
								String stageObjectId = viewObject.getString("id");
								JSONObject bindingProperties = null;
								if (viewObject.has("bindingProperties")) {
									try {
										bindingProperties = viewObject.getJSONObject("bindingProperties");
									} catch (JSONException e) {
									}
								}
								ViewTypes type = ViewTypes.convert(viewProperties.getString("type"));
								switch (type) {
								case IMAGE:
									PictureView.createBinding(stageObjectId, bindingProperties);
									break;
								case SHAPE:
									ShapeView.createBinding(stageObjectId, bindingProperties);
									break;
								case IMAGE_BUTTON:
                                case PAGE_NAVIGATION:
									ButtonView.createBinding(stageObjectId, bindingProperties);
									break;
								case SLIDER:
									SliderView.createBinding(stageObjectId, bindingProperties);
									break;
								case SENSOR:
								case SENSOR_WITH_INDICATOR:
									SensorView.createBinding(stageObjectId, bindingProperties);
									break;
								case TRACK_DISPLAY:
									MusicPlayerView.createBinding(stageObjectId, bindingProperties);
									break;
								case TRACK_PROGRESS:
									MusicPlayerView.createBinding(stageObjectId, bindingProperties);
									break;
								case ALBUM_IMAGE:
									MusicPlayerView.createBinding(stageObjectId, bindingProperties);
									break;
								case PLAYLIST:
									MusicPlayerView.createBinding(stageObjectId, bindingProperties);
									break;
								case PLAYLISTS:
									ButtonView.createBinding(stageObjectId, bindingProperties);
									break;
								case CAMERA:
									IPCameraView.createBinding(stageObjectId, bindingProperties);
									break;
								case MULTI_STATE_BUTTON:
									MultiStateButtonView.createBinding(stageObjectId, bindingProperties);
									break;
								case PLUS_MIN_VALUE:
									PlusMinView.createBinding(stageObjectId, bindingProperties);
									break;
								case CLOCK_ANALOG:
									ClockAnalogView.createBinding(stageObjectId, bindingProperties);
									break;
								case CLOCK_DIGITAL:
									ClockDigitalView.createBinding(stageObjectId, bindingProperties);
									break;
								case TEXT:
									TextView.createBinding(stageObjectId, bindingProperties);
									break;
								case WEB_LINK:
									WebLinkView.createBinding(stageObjectId, bindingProperties);
									break;
								case WEB_RSS:
									WebRSSView.createBinding(stageObjectId, bindingProperties);
									break;
								case WEB_STATIC_HTML:
									WebStaticHtmlView.createBinding(stageObjectId, bindingProperties);
									break;
								case COLOR_PICKER:
									ColorPickerView.createBinding(stageObjectId, bindingProperties);
									break;
								default:
									break;
								}
							} catch (JSONException | InvalidParameterException e) {
								e.printStackTrace();
							}
						}
					} catch (JSONException e) {
					}
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// String orientationString =
			// activeConfiguration.getString("orientation");
			return menuList;
		} else {
			Log.d("Configuration", "activeConfiguration null!!!");
		}
		return null;
	}

    public Integer getPositionOf(String fragmentId) {
        return this.pageIndexes.get(fragmentId);
    }

	@Override
	public Fragment getItem(int position) {
		Log.d("CanvasFragmentAdapter", "Get fragment on position " + position);
		if (position < 2) {
			return new Fragment();
		}

		if (this.activeConfiguration != null && this.getRealCount() > 0) {
			int actualPosition = position % this.getRealCount();
			return CanvasFragment.newInstance(actualPosition, this);
		}
		return null;

	}

	@Override
	public int getCount() {
		return this.getRealCount() * CanvasFragmentAdapter.LOOPS_COUNT;
	}

	public int getRealCount() {
		if (this.realCount < 0) {
			Integer length = null;
			try {
				this.realCount = this.activeConfiguration.getJSONArray("pages").length();
			} catch (JSONException e) {
			}
		}
		return this.realCount;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		Log.d("CanvasFragmentAdapter", "Destroy fragment on position:  " + position);
		FragmentManager manager = ((Fragment) object).getFragmentManager();
		android.support.v4.app.FragmentTransaction trans = manager.beginTransaction();
		trans.remove((Fragment) object);
		trans.commit();

		super.destroyItem(container, position, object);
	}
}
