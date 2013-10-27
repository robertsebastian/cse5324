package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class MainActivity extends Activity implements
	SearchView.OnQueryTextListener,
	DatabaseRequest.Listener,
	ActionBar.TabListener
{
	public static final String TAG = "main";
	
	private ProfileEditFragment   mProfileEditFragment;
	private FavoritesFragment     mFavoritesFragment;
	private SearchResultsFragment mSearchResultsFragment;
	
	// Set content area to selected fragment
	public void selectFragment(String tag) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		selectFragment(tag, ft);
		ft.commit();
	}
	
	public void selectFragment(String tag, FragmentTransaction ft) {
		// TODO: Probably a better way to do this
		if(tag == ProfileEditFragment.TAG) {
			if(mProfileEditFragment == null) mProfileEditFragment = new ProfileEditFragment();
			
			ft.replace(android.R.id.content, mProfileEditFragment, tag);
		} else if(tag == FavoritesFragment.TAG) {
			if(mFavoritesFragment == null) mFavoritesFragment = new FavoritesFragment();
			
			ft.replace(android.R.id.content, mFavoritesFragment, tag);
		} else if(tag == SearchResultsFragment.TAG) {
			if(mSearchResultsFragment == null) mSearchResultsFragment = new SearchResultsFragment();
			
			ft.replace(android.R.id.content, mSearchResultsFragment, tag);
			if(getFragmentManager().findFragmentByTag(tag) == null) {
				ft.addToBackStack(tag);
			}
		}
	}
	
    // Select fragment associated with the tab
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	selectFragment((String)tab.getTag(), ft);
    }

    // Nothing to do on tab unselected
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    // Nothing to do if the tag is already selected
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);   
        
        // setup action bar for tabs
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);

        Tab profileTab = actionBar.newTab().setText("Profile").setTag(ProfileEditFragment.TAG).setTabListener(this);
        Tab favoritesTab = actionBar.newTab().setText("Favorites").setTag(FavoritesFragment.TAG).setTabListener(this);
        actionBar.addTab(profileTab);
        actionBar.addTab(favoritesTab);
    }

	@Override
	public boolean onQueryTextChange(String newText) {
		return true;
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		// Kick off search request
		JSONObject j = new JSONObject();
		try {
			j.put("action", "search");
			j.put("query",  query);
			j.put("lat",    32.715278);
			j.put("lon",    -97.016944);
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		
		new DatabaseRequest(j, this, this);
		selectFragment(SearchResultsFragment.TAG);
		return true;
	}
	
	SearchResultsFragment mSearchFragment;
	@Override
	public void onDatabaseResponse(JSONObject response) {
		try {
			if(response == null) {
				// Some sort of connection issue, most likely
				// TODO: Error dialog
			} else if(response.getBoolean("success") == false) {
				Log.e(TAG, response.toString());
				// TODO: Error message
				// TODO: If invalid session, bump back to login screen
			} else if(response.getString("action").equals("search")) {
				// Replace currently loaded fragment with search results fragment
				mSearchResultsFragment.updateResults(response.getJSONArray("results"));
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        // Listen to search entries
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.action_settings:
    		startActivity(new Intent(this, SettingsActivity.class));
    		break;
    	case R.id.action_logout:
    		// TODO: Log out and return to login screen
    		break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	
    	return true;
    }
}
