package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;

public class MainActivity extends Activity implements
	ActionBar.OnNavigationListener,
	SearchView.OnQueryTextListener,
	DatabaseRequest.Listener
{
	public static final String TAG = "main";
	
	protected DatabaseRequest mDatabaseReq = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);     
        
        // Load navigation list for action bar
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setTitle("");
        
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.action_list,
                android.R.layout.simple_spinner_dropdown_item);
        bar.setListNavigationCallbacks(spinnerAdapter, this);
    }
	
	@Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO: Pretty ugly way to determine which item is selected -- probably a better way
    	String item = getResources().getStringArray(R.array.action_list)[itemPosition];
    	String my_profile_str = getResources().getString(R.string.action_list_my_profile);
    	String favorites_str = getResources().getString(R.string.action_list_favorites);
    	
    	// Transition fragement_container to the selected fragment
		FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(item.equals(my_profile_str)) {
        	ft.replace(R.id.fragment_container, new ProfileEditFragment());
        } else if(item.equals(favorites_str)) {
        	ft.replace(R.id.fragment_container, new FavoritesFragment());
        }
        ft.commit();
        
        return true;
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
			j.put("lat", 32.715278);
			j.put("lon", -97.016944
					);
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		mDatabaseReq = new DatabaseRequest(j, this, this);
		return true;
	}
	
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
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				Fragment searchFragment = SearchResultsFragment.newInstance(response.getJSONArray("results"));
				ft.replace(R.id.fragment_container, searchFragment);
				ft.addToBackStack(SearchResultsFragment.TAG);
				ft.commit();     				
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		mDatabaseReq = null;
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
