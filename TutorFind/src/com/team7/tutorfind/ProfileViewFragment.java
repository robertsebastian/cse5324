package com.team7.tutorfind;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	private Bitmap mPicture = null;
	
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
	
	// Add an onClick event to a random view. Used to handle email, phone,
	// SMS, and address clicks with the correct application
	private void addAction(View v, final Intent intent) {
		v.setClickable(true);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(intent);
			}
		});
	}
	
	// Find the value field of a profile view item with a given tag
	private View findValue(String tag) {
		return getView().findViewWithTag(tag).findViewById(R.id.profile_view_value);
	}
	
	// Find a profile item view with a given tag and set its text to the mUser
	// field of the same name
	private void setText(String tag) {
		setText(tag, mUser.optString(tag));
	}
	
	// Find a profile item view with a given tag and set its text
	private void setText(String tag, String text) {
		ViewGroup item = (ViewGroup)getView().findViewWithTag(tag);
		
		boolean isHidden = mUser.isNull(tag) || text.equals(""); 
		item.setVisibility(isHidden ? View.GONE : View.VISIBLE);
		
		TextView textView = (TextView)item.findViewById(R.id.profile_view_value);
		textView.setText(text);		
	}
	
	// Set up a ratings profile view item with the content of mUser
	private void setRating(String tag) {
		ViewGroup item = (ViewGroup)getView().findViewWithTag(tag);
		item.setVisibility(View.VISIBLE);
		
		int numRatings = mUser.optInt("num_reviews");
		float rating = (float)mUser.optDouble("score");
		
		String numRatingsStr = String.format(Locale.US, "(%d)", numRatings);
		String ratingStr = String.format(Locale.US, "%.1f", rating);
		
		// Show no rating if unrated
		if(numRatings == 0) {
			ratingStr = "-.-";
			rating = 0.0f;
		}
		
		((RatingBar)item.findViewById(R.id.profile_view_item_rating_stars)).setRating(rating);
		((TextView)item.findViewById(R.id.profile_view_item_num_ratings)).setText(numRatingsStr);
		((TextView)item.findViewById(R.id.profile_view_item_rating)).setText(ratingStr);
	}
	
	private void onUserUpdated() {
		if(mUser == null || getView() == null) return;
		
		// Update favorite start on options menu
		getActivity().invalidateOptionsMenu();
		
		// Put user's name in the title bar
		getActivity().getActionBar().setTitle(mUser.optString("name"));		
		
		// Hide placeholder picture if user doesn't have one to load
		if(!mUser.optBoolean("has_picture")) {
			getView().findViewById(R.id.profilePicture).setVisibility(View.GONE);
		}
		
		// Default to all views turned off
		ViewGroup list = (ViewGroup)getView().findViewById(R.id.profile_content_list);
		for(int i = 0; i < list.getChildCount(); i++) {
			list.getChildAt(i).setVisibility(View.GONE);
		}
		
		// Fill in view items
		setText("public_email_address");
		setText("phone");
		if(mUser.optBoolean("tutor_flag")) {
			setText("subject_tags");
			setText("loc_address");
			setText("price_per_hour", String.format(Locale.US, "$%.2f/hr", mUser.optDouble("price_per_hour", 999.0)));
			setText("availability", Util.availabilityToString(mUser.optInt("availability", 0xFFFFFFFF), getActivity()));
			setText("about_me");
			setRating("rating");
		}
		
		// Add view actions
		JSONArray mapUsers = new JSONArray();
        mapUsers.put(mUser);        
        
		addAction(findValue("public_email_address"),
				new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + mUser.optString("public_email_address"))));
		addAction(findValue("phone"), 
				new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + mUser.optString("phone"))));
		addAction(getView().findViewById(R.id.profile_view_item_message_button),
				new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + mUser.optString("phone"))));
		addAction(findValue("loc_address"),
				new Intent(getActivity(), MapActivity.class).putExtra("users", mapUsers.toString()));
		addAction(getView().findViewById(R.id.profile_view_item_rating_layout),
				new Intent(getActivity(), ReviewActivity.class).putExtra("user_id", mUser.optInt("user_id")));
	}
	
	private void onPictureUpdated() {
		ImageView img = (ImageView)getView().findViewById(R.id.profilePicture);
		img.setImageBitmap(mPicture);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
		
		mUserId = getArguments().getInt("user_id");
		fetchUser();
	}
	
	// Handle edit item click
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_profile_edit && mUser != null) {
			Intent i = new Intent(getActivity(), ProfileEditActivity.class);
			i.putExtra("user", mUser.toString());
			if(mPicture != null) i.putExtra("picture", Util.encodePicture(mPicture));
			startActivityForResult(i, 0);
            return true;
		}
    	return super.onOptionsItemSelected(item);
	}
	
	// Send database request for userId
	public void fetchUser()
	{
		try {
			JSONObject userReq = new JSONObject();
			userReq.put("action", "get_user");
			userReq.put("user_id", mUserId);
			new DatabaseRequest(userReq, this, getActivity(), false);
			
			JSONObject picReq = new JSONObject();
			picReq.put("action", "get_picture");
			picReq.put("user_id", mUserId);
			new DatabaseRequest(picReq, this, getActivity(), false);
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}

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

		// Nothing to do if failed request
		if(!response.optBoolean("success")) return;
		
		// Handle responses
		String action = response.optString("action");
		if(action.equals("get_user")) {
			mUser = response;
			onUserUpdated();
			
		} else if(action.equals("get_picture") && !response.isNull("picture")) {
			mPicture = Util.decodePicture(response.optString("picture"));
			onPictureUpdated();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK) return;
		
		if(!data.getExtras().containsKey("user")) return;
			
		try {
			mUser = new JSONObject(data.getExtras().getString("user"));
			onUserUpdated();
			
			if(mUser.has("picture")) {
				mPicture = Util.decodePicture(mUser.optString("picture"));
				mUser.remove("picture");
				onPictureUpdated();
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
	}
}
