package com.team7.tutorfind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
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
	
	private ListView mListView;
	private UserSummaryArrayAdapter mAdapter;
	private JSONArray mResults;
	private ArrayList<JSONObject> mDisplayedResults;
	private Comparator<JSONObject> mComparator;
	
	int mExpandedUser;
	View mExpandedView;
	
	// Array adapter to manage the search results ListView. Populates user
	// summary views and handles sorting of the data set.
	private class UserSummaryArrayAdapter extends ArrayAdapter<JSONObject> {
		private final List<JSONObject> mUsers;
		
		public UserSummaryArrayAdapter(Context context, List<JSONObject> users) {
			super(context, R.layout.search_result_row, users);
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
			
			String subjectTags = user.isNull("subject_tags") ? "" : user.optString("subject_tags");
			String subjectArr[] = TextUtils.split(subjectTags.toLowerCase(Locale.US), "\\s*,\\s*"); 
			Arrays.sort(subjectArr, String.CASE_INSENSITIVE_ORDER);
			String subjects = " - " + TextUtils.join("\n - ", subjectArr);
			
			((TextView)v.findViewById(R.id.name)).setText(name);
			((TextView)v.findViewById(R.id.price)).setText(price);
			((TextView)v.findViewById(R.id.distance)).setText(dist);
			((TextView)v.findViewById(R.id.subjects)).setText(subjects);
			((TextView)v.findViewById(R.id.num_reviews)).setText(reviews);
			((RatingBar)v.findViewById(R.id.score)).setRating(rating);
			
			if(user.optBoolean("preferred_flag")) {
				v.findViewById(R.id.advertisementText).setVisibility(View.VISIBLE);
			} else {
				v.setBackgroundResource(0);
				v.findViewById(R.id.advertisementText).setVisibility(View.GONE);
			}
			
			View expandView = v.findViewById(R.id.expander);
			if(user.optInt("user_id") == mExpandedUser) {	
				expandView.setVisibility(View.VISIBLE);
				mExpandedView = expandView;
			} else {
				expandView.setVisibility(View.GONE);
			}
		}
		
		// Build the view for a given row position in the search results list
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if(view == null) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.search_result_row, null, false);
			}
			fillUserSummary(mUsers.get(position), view);

			return view;
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
		View v = view.findViewById(R.id.expander);
		JSONObject user = mAdapter.getItem(position);
		
		if(mExpandedView != null && mExpandedView != v) {
			collapse(mExpandedView);
			mExpandedUser = -1;
			mExpandedView = null;
		}

		if(v.getVisibility() == View.GONE) {
			expand(position, v);
			mExpandedUser = user.optInt("user_id");
			mExpandedView = v;
		} else {
			Intent i = new Intent(this, ProfileViewActivity.class);
			i.putExtra("user_id", user.optInt("user_id", -1));
			i.putExtra("user", user.toString());
			startActivity(i);			
		}
	}

	private void expand(final int pos, View v) {
		//set Visible
		v.setVisibility(View.VISIBLE);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		v.measure(widthSpec, heightSpec);

		ValueAnimator anim = slideAnimator(v, 0, v.getMeasuredHeight());
		anim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationEnd(Animator animator) {
				mListView.smoothScrollToPosition(pos);
			}

			@Override
			public void onAnimationCancel(Animator animation) {}
			@Override
			public void onAnimationRepeat(Animator animation) {}
			@Override
			public void onAnimationStart(Animator animation) {}
		});	     
		anim.start();
	}

	private void collapse(final View v) {
		int finalHeight = v.getHeight();

		ValueAnimator anim = slideAnimator(v, finalHeight, 0);
		anim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationEnd(Animator animator) {
				v.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {}
			@Override
			public void onAnimationRepeat(Animator animation) {}
			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.start();
	}

	private ValueAnimator slideAnimator(final View v, int start, int end) {

		ValueAnimator animator = ValueAnimator.ofInt(start, end);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				//Update Height
				int value = (Integer) valueAnimator.getAnimatedValue();
				ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
				layoutParams.height = value;
				v.setLayoutParams(layoutParams);
			}
		});
		return animator;
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
		
		mListView = (ListView)findViewById(R.id.search_results_list);		
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setEmptyView(findViewById(R.id.empty_list_no_results));
		
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
		
		try {
			Location loc = getLastLocation();
					
			j.put("action", "search");
			j.put("query", query);
			j.put("lat", loc != null ? loc.getLatitude() : 32.715278);
			j.put("lon", loc != null ? loc.getLongitude() : -97.016944);
			
			// If the search query can be converted into a location, add that
			// in as a fall back search if subject/name fails
			Geocoder g = new Geocoder(this, Locale.US);
			List<Address> address = g.getFromLocationName(query, 1);			
			if(address.size() > 0) {
				j.put("query_lat", address.get(0).getLatitude());
				j.put("query_lon", address.get(0).getLongitude());
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		} catch(IOException e) {
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
    		if(mAdapter != null && !mAdapter.isEmpty()) {
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