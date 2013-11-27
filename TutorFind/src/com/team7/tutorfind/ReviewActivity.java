package com.team7.tutorfind;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReviewActivity extends TutorFindActivity
{
	public static final String TAG = "review_activity";
	
	private float mMyScore = 0.0f;
	private String mMyComment = "";
	
	// Array adapter to manage the search results ListView. Populates user
	// summary views and handles sorting of the data set.
	private static class ReviewArrayAdapter extends ArrayAdapter<JSONObject> {
		private final Context mContext;
		private final List<JSONObject> mReviews;
		
		public ReviewArrayAdapter(Context context, List<JSONObject> reviews) {
			super(context, R.layout.review_row, reviews);
			mContext = context;
			mReviews = reviews;
		}
		
		// Fill a user summary view with data from a user object
		public void fillReview(JSONObject review, View v) {
			float rating = (float)review.optDouble("score", 0.0);
			String ratingStr = String.format(Locale.US, "%.1f", rating);
			String comment = review.optString("text", "");
			
			((TextView)v.findViewById(R.id.name)).setText(review.optString("submitter_name", ""));
			((TextView)v.findViewById(R.id.rating)).setText(ratingStr);
			((RatingBar)v.findViewById(R.id.rating_stars)).setRating(rating);
			((TextView)v.findViewById(R.id.comment)).setText(comment);
		}
		
		// Build the view for a given row position in the search results list
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.review_row, parent, false);
			
			fillReview(mReviews.get(position), rowView);
			
			return rowView;
		}
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		getIntent().getExtras().getString("user");
		getReviews();
	}
	
	protected void getReviews() {
		try {
			JSONObject j = new JSONObject();
			j.put("action",     "get_reviews");
			j.put("subject_id", getIntent().getExtras().getInt("user_id"));
			
			new DatabaseRequest(j, this, this, false);
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
	}
	
	// Send database request to review this user
	public void review(float score, String text)
	{
		JSONObject j = new JSONObject();
		try {
			mMyScore = score;
			mMyComment = text;
			
			j.put("action",     "submit_review");
			j.put("subject_id", getIntent().getExtras().getInt("user_id"));
			j.put("score",      mMyScore);
			j.put("text",       mMyComment);
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
		new DatabaseRequest(j, this, this, false);		
	}
	
	// Update displayed reviews from a database response
	public void updateReviews(JSONObject response) {
		try {
			JSONArray reviewsArr = response.getJSONArray("reviews");
			
			int ourUser = PreferenceManager.getDefaultSharedPreferences(this).getInt("user_id", -1);
	
			// Build a list of results
			ArrayList<JSONObject> reviews = new ArrayList<JSONObject>();
			for(int i = 0; i < reviewsArr.length(); i++) {
				JSONObject review = reviewsArr.getJSONObject(i);
				if(review.optInt("submitter_id", -1) == ourUser) {
					// Save off our review for later editing if it's present
					mMyScore = (float)review.optDouble("score", 0.0);
					mMyComment = review.optString("text", "");
					
					// Make sure our review shows up first
					reviews.add(0, reviewsArr.getJSONObject(i));
				} else {
					reviews.add(reviewsArr.getJSONObject(i));
				}
			}
			
			// Create new adapter using the list as its data source
			ListView list = (ListView)findViewById(R.id.reviews_list);
			list.setAdapter(new ReviewArrayAdapter(this, reviews));
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		if(response == null) return;

		String action = response.optString("action");
		if(action.equals("get_reviews")) {
			// New reviews to post
			updateReviews(response);
		} else if(action.equals("submit_review")) {
			// Re-request views if we just successfully submitted on
			getReviews();
		}
		
		super.onDatabaseResponse(response);		
	}

	private void showAddReviewDialog() {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_add_review, null);
		
		final TextView text = (TextView)layout.findViewById(R.id.comment);
		final RatingBar rating = (RatingBar)layout.findViewById(R.id.rating);
		
		text.setText(mMyComment);		
		rating.setRating(mMyScore);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);		
		builder.setTitle(R.string.review_title)
			.setView(layout)
		    .setCancelable(true)
		    .setNegativeButton(R.string.review_cancel, null)
		    .setPositiveButton(R.string.review_ok, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int which) {
		    		review(rating.getRating(), text.getText().toString());
		    	}
		    });
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	int ourUser = PreferenceManager.getDefaultSharedPreferences(this).getInt("user_id", -1);
    	int subjectUser = getIntent().getExtras().getInt("user_id");
    	
    	if(ourUser != subjectUser) {
    		getMenuInflater().inflate(R.menu.reviews, menu);
    	}
        return true;
    }
    
    // Handle result filtering options and map view action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.action_add_review:
    		showAddReviewDialog();
    		break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	
    	return true;
    }
}