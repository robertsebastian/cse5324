package com.team7.tutorfind;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends TutorFindActivity implements OnItemClickListener, OnItemSelectedListener
{
	public static final String TAG = "search_activity";
	public static final String SEARCH_QUERY = "com.team7.tutorfind.SEARCH_QUERY";
	
	private UserSummaryArrayAdapter mAdapter;
	private JSONArray mResults;
	private ArrayList<JSONObject> mDisplayedResults;
	private Comparator<JSONObject> mComparator;
	
	// Array adapter to manage the search results ListView. Populates user
	// summary views and handles sorting of the data set.
	private static class UserSummaryArrayAdapter extends ArrayAdapter<JSONObject> {
		private final List<JSONObject> mUsers;
		
		public UserSummaryArrayAdapter(Context context, List<JSONObject> users) {
			super(context, R.layout.user_summary_row, users);
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
			
			if(user.optBoolean("preferred_flag")) {
				v.setBackgroundResource(R.color.search_result_preferred_background);
				v.findViewById(R.id.advertisementText).setVisibility(View.VISIBLE);
			} else {
				v.setBackgroundResource(0);
				v.findViewById(R.id.advertisementText).setVisibility(View.GONE);
			}
		}
		
		// Build the view for a given row position in the search results list
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.user_summary_row, parent, false);
			
			fillUserSummary(mUsers.get(position), rowView);
			
			return rowView;
		}
	}
	
	// User comparison function -- always sort preferred users to the top
	static private int compareUser(String key, JSONObject a, JSONObject b, boolean ascending) {
		boolean aPreferred = a.optBoolean("preferred_flag");
		boolean bPreferred = b.optBoolean("preferred_flag");
		

		
		if(aPreferred && !bPreferred) return -1;
		if(!aPreferred && bPreferred) return 1;
		return ascending ? Double.compare(a.optDouble(key), b.optDouble(key)) :
						   Double.compare(b.optDouble(key), a.optDouble(key));
	}
	
	// Comparison functions for sorting search results
	private class DistanceComparator implements Comparator<JSONObject> {
		@Override
		public int compare(JSONObject a, JSONObject b) {
			return compareUser("distance", a, b, true);
		}
	}
	
	private class PriceComparator implements Comparator<JSONObject> {
		@Override
		public int compare(JSONObject a, JSONObject b) {
			return compareUser("price_per_hour", a, b, true);
		}
	}
	
	private class ScoreComparator implements Comparator<JSONObject> {
		@Override
		public int compare(JSONObject a, JSONObject b) {
			return compareUser("score", a, b, false);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		JSONObject user = mAdapter.getItem(position);
		Intent i = new Intent(this, ProfileViewActivity.class);
		i.putExtra("user_id", user.optInt("user_id", -1));
		i.putExtra("user", user.toString());
		startActivity(i);
	}
	
	// Get Simple method to grab the last known location. Warning: Can return null
	protected Location getLastLocation()
	{
	    LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    String lp = lm.getBestProvider(new Criteria(), false);
	    return lm.getLastKnownLocation(lp);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		doSearch(getIntent().getStringExtra(SEARCH_QUERY));
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
 		mDisplayedResults = new ArrayList<JSONObject>();    
 		mComparator = new DistanceComparator();
		mAdapter = new UserSummaryArrayAdapter(this, mDisplayedResults);
		
		ListView list = (ListView)findViewById(R.id.search_results_list);		
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);
		list.setEmptyView(findViewById(R.id.empty_list_no_results));
		
		((Spinner)findViewById(R.id.day_filter)).setOnItemSelectedListener(this);
		((Spinner)findViewById(R.id.time_filter)).setOnItemSelectedListener(this);
    }
	
	@Override
	public void onStart() {
		super.onStart();
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
			Log.e(TAG, e.toString(), e);
		}
		new DatabaseRequest(j, this, this, false);
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		super.onDatabaseResponse(response);
		try {
			mResults = response.getJSONArray("results");
			onNewResults();
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		} catch(NullPointerException e) {
			Log.e(TAG, e.toString(), e);
		}
	}
	
	public void onNewResults() {
		if(mResults == null) return;
		
		try {
			// Build our availability filter mask
			int day = ((Spinner)findViewById(R.id.day_filter)).getSelectedItemPosition();
			int time = ((Spinner)findViewById(R.id.time_filter)).getSelectedItemPosition();
			int dayMask[] = getResources().getIntArray(R.array.availability_days_mask);
			int timeMask[] = getResources().getIntArray(R.array.availability_times_mask);
			int filter = dayMask[day] & timeMask[time];
			
			// Build a list of results
			mDisplayedResults.clear();
			for(int i = 0; i < mResults.length(); i++) {
				JSONObject user = mResults.getJSONObject(i);
				if((user.optInt("availability", 0xFFFFFFFF) & filter) != 0) {
					mDisplayedResults.add(user);
				}
			}
			mAdapter.sort(mComparator);
			mAdapter.notifyDataSetChanged();
		} catch(JSONException e) {
			Log.e("search", e.toString(), e);
		}		
	}
	
	private void onFilterChanged() {
		if(mAdapter != null && mComparator != null) mAdapter.sort(mComparator);
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		doSearch(query);
		return true;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_results, menu);
        return true;
    }
    
    // Handle result filtering options and map view action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.action_filter_by_distance:
    		mComparator = new DistanceComparator();
    		onFilterChanged();
    		break;
    	case R.id.action_filter_by_price:
    		mComparator = new PriceComparator();
    		onFilterChanged();
    		break;
    	case R.id.action_filter_by_rating:
    		mComparator = new ScoreComparator();
    		onFilterChanged();
    		break;
    	case R.id.action_map:
    		if(mAdapter != null) {
    			// Kick of maps activity with results that are actually displayed in the listview
				JSONArray results = new JSONArray();
				for(int i = 0; i < mAdapter.getCount(); i++) {
					results.put((JSONObject)mAdapter.getItem(i));
				}
    			startActivity(new Intent(this, MapActivity.class).putExtra("users", results.toString()));
    		}
    		break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	
    	return true;
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        onNewResults();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parentView) {
        onNewResults();
    }
}