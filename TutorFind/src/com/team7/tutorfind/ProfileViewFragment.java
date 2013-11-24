package com.team7.tutorfind;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ProfileViewFragment extends Fragment implements
	DatabaseRequest.Listener, View.OnClickListener {

	static final String TAG = "ProfileViewFragment";
	
	private JSONObject mUser = null;
	private int mUserId = -1;
	
	// Create a fragment with user_id and an optional user data argument
	static ProfileViewFragment create(int userId, String user) {		
		Bundle args = new Bundle();
		args.putInt("user_id", userId);
		if(user != null) args.putString("user", user);
		
		ProfileViewFragment frag = new ProfileViewFragment();
		frag.setArguments(args);
		
		return frag;
	}
	
	// Append a profile text field to the content list from a user object
	private View addTextField(ViewGroup root, String title, JSONObject user, String field) {
		if(user.isNull(field)) return null;
		return addTextField(root, title, user.optString(field));
	}
	
	// Append a profile text field to the content list
	private View addTextField(ViewGroup root, String title, String content) {
		if(content == null) return null;
		
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
		View field = inflater.inflate(R.layout.profile_view_item, null);
		TextView titleView = (TextView)field.findViewById(R.id.profile_view_item_title);		
		TextView contentView = (TextView)field.findViewById(R.id.profile_view_item_content);		
		titleView.setText(title);
		contentView.setText(content);

		root.addView(field);
		
		return contentView;
	}
	
	private void uriAction(View v, final String uri) {
		if(v == null) return;
		
		v.setClickable(true);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				Log.d("TEST", uri);
				i.setData(Uri.parse(uri));
				startActivity(i);
			}
		});
	}
	
	private void addRatingField(ViewGroup root, String title, float rating, int numRatings) {
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		String numRatingsStr = String.format(Locale.US, "(%d)", numRatings);
		String ratingStr = String.format(Locale.US, "%.1f", rating);
		
		View field = inflater.inflate(R.layout.profile_view_item_rating, null);	
		((TextView)field.findViewById(R.id.profile_view_item_title)).setText(title);
		((RatingBar)field.findViewById(R.id.profile_view_item_rating_stars)).setRating(rating);
		((TextView)field.findViewById(R.id.profile_view_item_num_ratings)).setText(numRatingsStr);
		((TextView)field.findViewById(R.id.profile_view_item_rating)).setText(ratingStr);

		root.addView(field);		
	}
	
	// Initialize views with values from a user object
	private void onUserUpdated()
	{
		if(mUser == null) return;
		
		Log.d(TAG, mUser.toString());
		// Variable fields
		String price = null;
		if(!mUser.isNull("price_per_hour")) {
			String.format(Locale.US, "$%.2f/hr", mUser.optDouble("price_per_hour", 999.0));
		}
		
		((TextView)getView().findViewById(R.id.profileNameText)).setText(mUser.optString("name"));
		
		((ToggleButton)getView().findViewById(R.id.starbutton)).setChecked(mUser.optBoolean("favorited"));
		
		ViewGroup root = (ViewGroup)getView().findViewById(R.id.profile_content_list);
		root.removeAllViews();
		
		View mailView     = addTextField(root, "EMAIL",            mUser, "public_email_address");
		View phoneView    = addTextField(root, "PHONE",            mUser, "phone");
		View locationView = addTextField(root, "MEETING LOCATION", mUser, "loc_address");
		
		addTextField(root, "SUBJECTS", mUser, "subject_tags");
		addTextField(root, "RATE", price);
		addTextField(root, "ABOUT ME", mUser, "about_me");
		addRatingField(root, "RATING", (float)mUser.optDouble("score"), mUser.optInt("num_reviews"));
		
		uriAction(mailView, "mailto:" + mUser.optString("public_email_address"));
		uriAction(phoneView, "tel:" + mUser.optString("phone"));
		uriAction(locationView, "geo:" + mUser.optDouble("loc_lat") + ":" + mUser.optDouble("loc_lon"));
		
		// Show/hide elements as appropriate for view us vs another user
		int ourUser = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("user_id", -1);		
		if(getArguments().getInt("user_id") == ourUser) {
			getView().findViewById(R.id.editProfileButton).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.reviewButton).setVisibility(View.GONE);
			getView().findViewById(R.id.starbutton).setVisibility(View.GONE);
		} else {
			getView().findViewById(R.id.editProfileButton).setVisibility(View.GONE);
			getView().findViewById(R.id.reviewButton).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.starbutton).setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)	{	
		View view = inflater.inflate(R.layout.fragment_profile_view, container, false);
		
		view.findViewById(R.id.editProfileButton).setOnClickListener(this);
		view.findViewById(R.id.reviewButton).setOnClickListener(this);
		view.findViewById(R.id.showReviewsButton).setOnClickListener(this);
		view.findViewById(R.id.starbutton).setOnClickListener(this);
		
		return view;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.editProfileButton:
			startActivity(new Intent(getActivity(), ProfileEditActivity.class));
			break;
		case R.id.reviewButton:
			showAddReviewDialog();
			// TODO: Actually launch an activity
			break;
		case R.id.showReviewsButton:
			// TODO: Actually launch an activity
			break;
		case R.id.starbutton:
			setFavorite(((ToggleButton)v).isChecked());
			break;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
		try {
			String user = getArguments().getString("user");
			
			if(user != null) {
				// Use pre-fetched user data from search results if available
				mUser = new JSONObject(user);
				mUserId = mUser.optInt("user_id", -1);
				onUserUpdated();
			} else {
				// Otherwise kick of database request to retrieve user
				mUserId = getArguments().getInt("user_id");
				fetchUser();
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
	}
	
	// Send database request for userId
	public void fetchUser()
	{
		JSONObject j = new JSONObject();
		try {
			j.put("action", "get_user");
			j.put("user_id", mUserId);
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
		new DatabaseRequest(j, this, getActivity());
	}
	
	// Send database request to set favorite status for the viewed user
	public void setFavorite(boolean favorited)
	{
		JSONObject j = new JSONObject();
		try {
			mUser.put("favorited", favorited);
			
			j.put("action",     "set_favorite");
			j.put("subject_id", mUserId);
			j.put("favorited",  favorited);
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
		new DatabaseRequest(j, this, getActivity(), false);		
	}
	
	// Send database request to review this user
	public void review(float score, String text)
	{
		JSONObject j = new JSONObject();
		try {
			mUser.put("my_score", score);
			mUser.put("my_comment", text);
			
			j.put("action",     "submit_review");
			j.put("subject_id", mUserId);
			j.put("score",      score);
			j.put("text",       text);
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
		new DatabaseRequest(j, this, getActivity(), false);		
	}

	// Update display with requested user data
	@Override
	public void onDatabaseResponse(JSONObject response) {
		try {
			if(response.getBoolean("success") && response.getString("action").equals("get_user")) {
				mUser = response;
				onUserUpdated();
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		} catch(NullPointerException e) {
			Log.e(TAG, e.toString(), e);
		}
	}
	
	private void showAddReviewDialog() {
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_add_review, null);
		
		final TextView text = (TextView)layout.findViewById(R.id.comment);
		final RatingBar rating = (RatingBar)layout.findViewById(R.id.rating);
		
		if(!mUser.isNull("my_comment")) text.setText(mUser.optString("my_comment"));		
		if(!mUser.isNull("my_score")) rating.setRating((float)mUser.optDouble("my_score"));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());		
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
}
