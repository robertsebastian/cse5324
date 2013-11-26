package com.team7.tutorfind;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
	
	// Return true if this fragment is displaying our user -- Don't call this
	// until onViewCreated or getActivity() will return null
	public boolean isOurUser() {
		int ourUser = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("user_id", -1);
		return mUserId == ourUser;
	}
	
	// Attach an ACTION_VIEW intent with a uri parameter. Used to handle phone,
	// text, and e-mail address fields with the correct application
	private void addAction(View v, final Intent intent) {
		if(intent == null) return;
		
		v.setVisibility(View.VISIBLE);
		v.setClickable(true);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(intent);
			}
		});
	}
	
	// Append a profile text field to the content list. This contains a title,
	// a text content area, and a hidden SMS button. An optional intent for
	// content area and SMS buttons may be provided. The SMS button is shown
	// only if an intent is provided.
	private void addTextField(ViewGroup root, String title, String content) {
		addTextField(root, title, content, null, null);
	}
	
	private void addTextField(ViewGroup root, String title, String content, Intent action, Intent smsAction) {
		if(content == null || content.equals("null")) return;
		
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		View field = inflater.inflate(R.layout.profile_view_item, null);
		
		TextView titleView   = (TextView)field.findViewById(R.id.profile_view_item_title);		
		TextView contentView = (TextView)field.findViewById(R.id.profile_view_item_content);
		ImageView smsView    = (ImageView)field.findViewById(R.id.profile_view_item_message_button);
		
		titleView.setText(title);
		contentView.setText(content);
		addAction(contentView, action);
		addAction(smsView, smsAction);

		root.addView(field);
	}
	
	// Initialize rating bar activity with an optional intent to launch on click
	private void addRatingField(ViewGroup root, String title, float rating, int numRatings, Intent action) {
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		String numRatingsStr = String.format(Locale.US, "(%d)", numRatings);
		String ratingStr = String.format(Locale.US, "%.1f", rating);
		
		// Show no rating if unrated
		if(numRatings == 0) {
			ratingStr = "-.-";
			rating = 0.0f;
		}
		
		View field = inflater.inflate(R.layout.profile_view_item_rating, null);	
		
		((TextView)field.findViewById(R.id.profile_view_item_title)).setText(title);
		((RatingBar)field.findViewById(R.id.profile_view_item_rating_stars)).setRating(rating);
		((TextView)field.findViewById(R.id.profile_view_item_num_ratings)).setText(numRatingsStr);
		((TextView)field.findViewById(R.id.profile_view_item_rating)).setText(ratingStr);
		
		addAction(field.findViewById(R.id.profile_view_item_rating_layout), action);

		root.addView(field);
	}
	
	// Initialize views with values from a user object
	private void onUserUpdated()
	{
		if(mUser == null) return;
		
		// Update favorite start on options menu
		getActivity().invalidateOptionsMenu();		
		
		// Build picture
		if(!mUser.isNull("picture")) {
			byte[] pictureBytes = Base64.decode(mUser.optString("picture").getBytes(), Base64.DEFAULT);
			Bitmap picture = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length);
			
			ImageView img = (ImageView)getView().findViewById(R.id.profilePicture);
			img.setImageBitmap(picture);
		}
		
		// Build price string
		String price = null;
		if(!mUser.isNull("price_per_hour")) {
			String.format(Locale.US, "$%.2f/hr", mUser.optDouble("price_per_hour", 999.0));
		}
		
		// Build list of users to show on map when address is clicked
		JSONArray mapUsers = new JSONArray();
		mapUsers.put(mUser);
		
		// Add all variable user fields to the view
		ViewGroup root = (ViewGroup)getView().findViewById(R.id.profile_content_list);
		root.removeAllViews(); // Delete existing fields for a refresh
		
		addTextField(root, "EMAIL",
				mUser.optString("public_email_address", null), 
				new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + mUser.optString("public_email_address"))),
				null);
		addTextField(root, "PHONE",
				mUser.optString("phone", null),
				new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + mUser.optString("phone"))),
				new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + mUser.optString("phone"))));
		addTextField(root, "MEETING LOCATION",
				mUser.optString("loc_address", null),
				new Intent(getActivity(), MapActivity.class).putExtra("users", mapUsers.toString()),
				null);
		addTextField(root, "SUBJECTS", mUser.optString("subject_tags", null));
		addTextField(root, "RATE", price);
		addTextField(root, "ABOUT ME", mUser.optString("about_me", null));
		addRatingField(root, "RATING", (float)mUser.optDouble("score"), mUser.optInt("num_reviews"),
				new Intent(getActivity(), ReviewActivity.class).putExtra("user_id", mUser.optInt("user_id")));
		
		// Put user's name in the title bar
		getActivity().getActionBar().setTitle(mUser.optString("name"));		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)	{	
		View view = inflater.inflate(R.layout.fragment_profile_view, container, false);
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {	
		super.onCreateOptionsMenu(menu, inflater);
		
		if(isOurUser()) {
			inflater.inflate(R.menu.profile_view_self, menu);
		} else {
			inflater.inflate(R.menu.profile_view_other, menu);
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		// Update favorite status for currently shown user
		if(!isOurUser() && mUser != null) {
			ToggleButton b = (ToggleButton)menu.findItem(R.id.action_toggle_favorite).getActionView();
			b.setOnClickListener(this);
			b.setChecked(mUser.optBoolean("favorited"));
		}
	}
	
	// Handle favorite button clicks
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.starbutton) {
			setFavorite(((ToggleButton)v).isChecked());
		}
	}

	// Fill in user data if available, otherwise kick off request
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
				fetchUser();
			} else {
				// Otherwise kick of database request to retrieve user
				mUserId = getArguments().getInt("user_id");
				fetchUser();
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
	}
	
	// Handle edit item click
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_profile_edit) {
            startActivity(new Intent(getActivity(), ProfileEditActivity.class));
            return true;
		}
    	return super.onOptionsItemSelected(item);
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

	// Update display with requested user data
	@Override
	public void onDatabaseResponse(JSONObject response) {
		if(getActivity() == null) return; // Make sure we're still active
		
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
}
