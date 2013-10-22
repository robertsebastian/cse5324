package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
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
        
        /*
        FragmentManager fm = getFragmentManager();
        Fragment defaultFrag = fm.findFragmentById(R.id.fragment_edit_profile);
        
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_fragment, defaultFrag);
        ft.commit();*/
        
    }
	
	@Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    	//String items[] = getResources().getStringArray(R.array.action_list);
        		
        return true;
    }

	@Override
	public boolean onQueryTextChange(String newText) {
		return true;
	}
	
	@Override
	public boolean onQueryTextSubmit(String newText) {
		// Kick off search request
		JSONObject j = new JSONObject();
		try {
			j.put("action", "search");
			j.put("query",  newText);
		} catch(JSONException e) {
			Log.e("main", e.toString());
		}
		mDatabaseReq = new DatabaseRequest(j, this, this);
		return true;
	}
	
	protected void handleSearchResults(JSONObject results) throws JSONException {
		Log.d("main", "Results received: " + results.toString());
        
		
        FragmentManager fm = getFragmentManager();
        //Fragment searchFrag = fm.findFragmentById(R.id.fragment_search_results);
        
        FragmentTransaction ft = fm.beginTransaction();
        //ft.replace(R.id.fragment_container, new SearchResultsFragment());
        SearchResultsFragment search = new SearchResultsFragment();
        ft.replace(R.id.fragment_container, search);
        ft.commit();
        fm.executePendingTransactions();
        search.setText(results.toString());
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
			} else if(response.getString("action").equals("search")) {
				handleSearchResults(response);
			}
		} catch(JSONException e) {
			Log.e("main", e.toString());
		}
		mDatabaseReq = null;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
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
