package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;

public class TutorFindActivity extends Activity implements
	SearchView.OnQueryTextListener,
	DatabaseRequest.Listener
{        
	public static final String TAG = "TutorFindActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
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
	
    // Handle result filtering options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	return true;
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
    
    public void onChangePasswordOption(MenuItem item) {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_change_password, null);
		
		final EditText pw1 = (EditText)layout.findViewById(R.id.password1);
		final EditText pw2 = (EditText)layout.findViewById(R.id.password2);
    	
    	new AlertDialog.Builder(this)
        .setTitle("Change Password")
        .setView(layout)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                	if(!pw1.getText().toString().equals(pw2.getText().toString())) return;
                	
                	JSONObject req = new JSONObject();
                	req.put("password", pw1.getText().toString());
                	req.put("action", "change_password");
                	new DatabaseRequest(req, TutorFindActivity.this, TutorFindActivity.this, false);
                } catch(JSONException e) {
                	Log.e(TAG, e.toString(), e);
                }
            }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
    }
    
    public void onLogoutOption(MenuItem item) {
    	startActivity(new Intent(this, LoginActivity.class));
    }
}
