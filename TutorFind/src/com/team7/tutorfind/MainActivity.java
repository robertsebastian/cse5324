package com.team7.tutorfind;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

public class MainActivity extends TutorFindActivity implements OnNavigationListener {
	public static final String TAG = "main";
	
	ProfileViewFragment mProfileFragment;
	FavoritesFragment   mFavoritesFragment;
	Fragment mSelectedFragment;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        // setup action bar for tabs
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        
        // Create dropdown navigation list
        ArrayList<String> itemList = new ArrayList<String>();
        itemList.add("My Profile");
        itemList.add("Favorites");
        ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(actionBar.getThemedContext(), android.R.layout.simple_list_item_1, android.R.id.text1, itemList);
        actionBar.setListNavigationCallbacks(aAdpt, this);
        
        // See if fragments are already present (this happens after a rotation)
        FragmentManager fm = getFragmentManager();
        mProfileFragment = (ProfileViewFragment)fm.findFragmentByTag(ProfileViewFragment.TAG);
        mFavoritesFragment = (FavoritesFragment)fm.findFragmentByTag(FavoritesFragment.TAG);
        
        // Add fragments to container
        FragmentTransaction ft = fm.beginTransaction();
        if(mProfileFragment == null) {
            int userId = PreferenceManager.getDefaultSharedPreferences(this).getInt("user_id", -1);
        	mProfileFragment   = ProfileViewFragment.create(userId, null);    
            ft.add(android.R.id.content, mProfileFragment, ProfileViewFragment.TAG);
            ft.detach(mProfileFragment);      
        }
        if(mFavoritesFragment == null) {
	        mFavoritesFragment = new FavoritesFragment();
	        ft.add(android.R.id.content, mFavoritesFragment, FavoritesFragment.TAG);
	        ft.detach(mFavoritesFragment);
        }
        ft.commit();
        
        // Restore selected position if restored
        if(savedInstanceState != null) {
        	Log.d(TAG, "Restoring to item " + savedInstanceState.getInt("nav_position"));
    		getActionBar().setSelectedNavigationItem(savedInstanceState.getInt("nav_position"));       	
        }
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("nav_position", getActionBar().getSelectedNavigationIndex());
		Log.d(TAG, "Saved instance state");
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// Only two fragments, so just select the opposite one when we get this notification
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if(mSelectedFragment != null) ft.detach(mSelectedFragment);
		if(itemId == 0) {
			mSelectedFragment = mProfileFragment;
		} else {
			mSelectedFragment = mFavoritesFragment;
		}
		ft.attach(mSelectedFragment);
		ft.commit();
		return false;
	}
}
