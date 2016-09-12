package net.yourhome.app.canvas;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import net.yourhome.app.util.Configuration;
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
import net.yourhome.app.views.musicplayer.PlaylistSelectorActivity;
import net.yourhome.app.views.musicplayer.views.AlbumImageView;
import net.yourhome.app.views.musicplayer.views.MusicPlayerView;
import net.yourhome.app.views.musicplayer.views.PlaylistView;
import net.yourhome.app.views.musicplayer.views.TrackDisplayView;
import net.yourhome.app.views.musicplayer.views.TrackProgressView;
import net.yourhome.common.base.enums.ViewTypes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CanvasFragmentAdapter extends FragmentPagerAdapter  {

    //private List<Fragment> fragments = new ArrayList<Fragment>();
    private JSONObject activeConfiguration;
    public static int LOOPS_COUNT = 10000;
    private int realCount = -1;

    public CanvasFragmentAdapter(FragmentManager fm, JSONObject activeConfiguration) {
        super(fm);
        this.activeConfiguration = activeConfiguration;
    }
    public JSONObject getPageDefinition(int i) throws JSONException {
        return activeConfiguration.getJSONArray("pages").getJSONObject(i);
    }
    public List<String> initializeBindings() {
        List<String> menuList = new ArrayList<String>();
        if(activeConfiguration != null) {
            JSONArray pages;
            try {
                pages = activeConfiguration.getJSONArray("pages");
                for(int i=0;i<pages.length();i++) {
                    JSONObject page;
                    try {
                        page = pages.getJSONObject(i);
                        String title = page.getString("title");
                        menuList.add(title);

                        // Initialize all bindings when we're at it
                        JSONArray viewObjects = page.getJSONArray("objects");
                        for (int j = 0; j < viewObjects.length(); j++) {
                            try {
                                JSONObject viewObject = viewObjects.getJSONObject(j);
                                JSONObject viewProperties = viewObject.getJSONObject("viewProperties");
                                String stageObjectId = viewObject.getString("id");
                                JSONObject bindingProperties = null;
                                if(viewObject.has("bindingProperties")) {
                                    try {
                                        bindingProperties = viewObject.getJSONObject("bindingProperties");
                                    }catch(JSONException e) {}
                                }
                                ViewTypes type = ViewTypes.convert(viewProperties.getString("type"));
                                switch(type) {
                                    case IMAGE:
                                        PictureView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case SHAPE:
                                        ShapeView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case IMAGE_BUTTON:
                                        ButtonView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case SLIDER:
                                        SliderView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case SENSOR:
                                    case SENSOR_WITH_INDICATOR:
                                        SensorView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case  TRACK_DISPLAY:
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
                                        IPCameraView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case MULTI_STATE_BUTTON:
                                        MultiStateButtonView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case PLUS_MIN_VALUE:
                                        PlusMinView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case CLOCK_ANALOG:
                                        ClockAnalogView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case CLOCK_DIGITAL:
                                        ClockDigitalView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case TEXT:
                                        TextView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case WEB_LINK:
                                        WebLinkView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case WEB_RSS:
                                        WebRSSView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case WEB_STATIC_HTML:
                                        WebStaticHtmlView.createBinding(stageObjectId,bindingProperties);
                                        break;
                                    case COLOR_PICKER:
                                        ColorPickerView.createBinding(stageObjectId, bindingProperties);
                                        break;
                                    default:
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }catch(JSONException e) {}
                }
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //String orientationString = activeConfiguration.getString("orientation");
            return menuList;
        }else {
            Log.d("Configuration" , "activeConfiguration null!!!");
        }
        return null;
    }
    @Override
    public Fragment getItem(int position) {
        Log.d("CanvasFragmentAdapter", "Get fragment on position "+position);
        if(position < 2) {
            return new Fragment();
        }

        if (activeConfiguration != null && getRealCount() > 0)
        {
            int actualPosition = position % getRealCount();
            return CanvasFragment.newInstance(actualPosition, this);
        }
        return null;

    }

    @Override
    public int getCount() {
        return getRealCount()*LOOPS_COUNT;
    }
    public int getRealCount() {
        if(realCount < 0) {
            Integer length = null;
            try {
                realCount = activeConfiguration.getJSONArray("pages").length();
            } catch (JSONException e) {}
        }
        return realCount;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d("CanvasFragmentAdapter", "Destroy fragment on position:  "+position);
        FragmentManager manager = ((Fragment) object).getFragmentManager();
        android.support.v4.app.FragmentTransaction trans = manager.beginTransaction();
        trans.remove((Fragment) object);
        trans.commit();

        super.destroyItem(container, position, object);
    }
}
