package net.yourhome.app.views.musicplayer;

import net.yourhome.app.R;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;

public class PlaylistSelectorActivity extends FragmentActivity implements ActionBar.TabListener {

	private FragmentTabHost mTabHost;

	// private ListActivity me = this;
	private Activity me = this;
	private String TAG = "PlaylistSelectorActivity";

    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    // private ActionBar actionBar;
    // Tab titles
    private String[] tabs = { "Playlists", "Accounts"};
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spotify_playlistselection_tab);
 
        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        // actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
 
        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 
            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
            	// actionBar.setSelectedNavigationItem(position);
            }
 
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
 
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
	public class TabsPagerAdapter extends FragmentPagerAdapter {
		 
	    public TabsPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }
	 
	    @Override
	    public Fragment getItem(int index) {
	 
	        switch (index) {
	        case 0:
	            return new PlaylistTab();
	        case 1:
	            return new AccountSelectorTab();
	        }
	 
	        return null;
	    }
	 
	    @Override
	    public int getCount() {
	        // get item count - equal to number of tabs
	        return 2;
	    }
	 
	}
}
