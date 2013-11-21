package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class TutorFindActivity extends Activity implements
	SearchView.OnQueryTextListener,
	DatabaseRequest.Listener
{
	@Override
	public boolean onQueryTextChange(String newText) {
		return true;
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		Intent i = new Intent(this, SearchActivity.class);
		i.putExtra(SearchActivity.SEARCH_QUERY, query);
		startActivity(i);
		return true;
	}
	
	@Override
	public void onDatabaseResponse(JSONObject response) {
		try {
			if(response == null) {
				// Some sort of connection issue, most likely
				// TODO: Error dialog
			} else if(response.getBoolean("success") == false) {
				// TODO: Error message
				// TODO: If invalid session, bump back to login screen
			}
		} catch(JSONException e) {
			// TODO: Error message
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        // Listen to search entries
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        
        return true;
    }
    
    public void onSettingsOption(MenuItem item) {
    	startActivity(new Intent(this, SettingsActivity.class));
    }
    
    public void onLogoutOption(MenuItem item) {
    	startActivity(new Intent(this, LoginActivity.class));
    }
}
