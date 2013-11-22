package com.team7.tutorfind;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends TutorFindActivity
{
	public static final String TAG = "main";

	// Class to manage selecting different fragments for the tabbed interface
    private class TabSelector implements ActionBar.TabListener {
    	Fragment mFragment;
    	boolean mIsAdded = false;
    	
    	TabSelector(Fragment frag) {
    		mFragment = frag;
    	}
    	
        // Select fragment associated with the tab
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	if(mIsAdded) {
        		ft.show(mFragment);
        	} else {
        		ft.add(android.R.id.content, mFragment);
        		mIsAdded = true;
        	}
        }

        // Nothing to do on tab unselected
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        	ft.hide(mFragment);
        }

        // Nothing to do if the tag is already selected
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        } 	
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        // setup action bar for tabs
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        int userId = PreferenceManager.getDefaultSharedPreferences(this).getInt("user_id", -1);

        actionBar.addTab(actionBar.newTab()
        		.setText("Profile")
        		.setTabListener(new TabSelector(ProfileViewFragment.create(userId, null))));
        actionBar.addTab(actionBar.newTab()
        		.setText("Favorites")
        		.setTabListener(new TabSelector(new FavoritesFragment())));
    }
}
