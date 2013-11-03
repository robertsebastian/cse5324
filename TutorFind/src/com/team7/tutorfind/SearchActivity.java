package com.team7.tutorfind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchActivity extends Activity implements
SearchView.OnQueryTextListener,
DatabaseRequest.Listener
{
	public static final String TAG = "search_activity";
	public static final String SEARCH_QUERY = "com.team7.tutorfind.SEARCH_QUERY";
	
	private UserSummaryArrayAdapter mAdapter;
	
	// Array adapter to manage the search results ListView. Populates user
	// summary views and handles sorting of the data set.
	private static class UserSummaryArrayAdapter extends ArrayAdapter<JSONObject> {
		private final Context mContext;
		private final List<JSONObject> mUsers;
		
		public UserSummaryArrayAdapter(Context context, List<JSONObject> users) {
			super(context, R.layout.user_summary_row, users);
			mContext = context;
			mUsers = users;
		}
		
		// Fill a user summary view with data from a user object
		public void fillUserSummary(JSONObject user, View v) {
			float rating = (float)user.optDouble("score", 0.0);
			int num_reviews = user.optInt("num_reviews", 0);
			
			// Unrated is represented by no stars -- make sure low rated tutors show a half star
			if(num_reviews > 0 && rating < 0.5) rating = 0.5f;
			
			String name    = user.optString("name", "???");
			String price   = String.format(Locale.US, "$%.2f/hr", user.optDouble("price_per_hour", 999.0));
			String dist    = String.format(Locale.US, "%.1f mi", user.optDouble("distance", 999.0));
			String reviews = String.format(Locale.US, "(%d)", user.optInt("num_reviews", 0));
			
			((TextView)v.findViewById(R.id.user_summary_name)).setText(name);
			((TextView)v.findViewById(R.id.user_summary_price)).setText(price);
			((TextView)v.findViewById(R.id.user_summary_distance)).setText(dist);
			((TextView)v.findViewById(R.id.user_summary_num_reviews)).setText(reviews);
			((RatingBar)v.findViewById(R.id.user_summary_score)).setRating(rating);
		}
		
		// Build the view for a given row position in the search results list
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.user_summary_row, parent, false);
			
			fillUserSummary(mUsers.get(position), rowView);
			
			return rowView;
		}
	}
	
	// Comparison functions for sorting search results
	private class DistanceComparator implements Comparator<JSONObject> {
		@Override
		public int compare(JSONObject a, JSONObject b) {
			return Double.compare(a.optDouble("distance"), b.optDouble("distance"));
		}
	}
	
	private class PriceComparator implements Comparator<JSONObject> {
		@Override
		public int compare(JSONObject a, JSONObject b) {
			return Double.compare(a.optDouble("price_per_hour"), b.optDouble("price_per_hour"));
		}
	}
	
	private class ScoreComparator implements Comparator<JSONObject> {
		@Override
		public int compare(JSONObject a, JSONObject b) {
			return Double.compare(b.optDouble("score"), a.optDouble("score"));
		}
	}
	
	// Get Simple method to grab the last known location. Warning: Can return null
	protected Location getLastLocation()
	{
	    LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    String lp = lm.getBestProvider(new Criteria(), false);
	    return lm.getLastKnownLocation(lp);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        doSearch(getIntent().getStringExtra(SEARCH_QUERY));
    }
	
	protected void doSearch(String query) {
		JSONObject j = new JSONObject();
		Location l = getLastLocation();
		if(l != null) Log.d(TAG, l.toString());
		try {
			Location loc = getLastLocation();	
			
			j.put("action", "search");
			j.put("query", query);
			j.put("lat", loc != null ? loc.getLatitude() : 32.715278);
			j.put("lon", loc != null ? loc.getLongitude() : -97.016944);
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		new DatabaseRequest(j, this, this);
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		try {
			updateResults(response.getJSONArray("results"));
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		} catch(NullPointerException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	public void updateResults(JSONArray resultArray) {
		try {
			ListView list = (ListView)findViewById(R.id.search_results_list);
			
			// Build a list of results
			ArrayList<JSONObject> results = new ArrayList<JSONObject>();
			for(int i = 0; i < resultArray.length(); i++) {
				results.add(resultArray.getJSONObject(i));
			}
			
			// Create new adapter using the list as its data source
			Collections.sort(results, new DistanceComparator());
			mAdapter = new UserSummaryArrayAdapter(this, results);
			
			list.setAdapter(mAdapter);
			
		} catch(JSONException e) {
			Log.e("search", e.toString());
		}		
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		return true;
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		doSearch(query);
		return true;
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.search_results, menu);
        
        // Listen to search entries
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        
        return true;
    }
    
    // Handle result filtering options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            break;
    	case R.id.action_filter_by_distance:
    		mAdapter.sort(new DistanceComparator());
    		break;
    	case R.id.action_filter_by_price:
    		mAdapter.sort(new PriceComparator());
    		break;
    	case R.id.action_filter_by_rating:
    		mAdapter.sort(new ScoreComparator());
    		break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	
    	return true;
    }
}