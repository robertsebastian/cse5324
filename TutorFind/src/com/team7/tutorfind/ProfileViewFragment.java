package com.team7.tutorfind;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProfileViewFragment extends Fragment implements DatabaseRequest.Listener {

	static final String TAG = "ProfileViewFragment";
	
	// Apply text to a piece of this view or hide the label and text boxes
	private void mapTextData(String fieldText, int labelId, int textId) {
		TextView labelView = (TextView)getView().findViewById(labelId);
		TextView textView = (TextView)getView().findViewById(textId);
		
		if(fieldText == null || fieldText.equals("null")) {
			labelView.setVisibility(View.GONE);
			textView.setVisibility(View.GONE);
		} else {
			labelView.setVisibility(View.VISIBLE);
			textView.setVisibility(View.VISIBLE);
			textView.setText(fieldText);
		}		
	}
	
	// mapTextData using a field from a user object
	private void mapTextData(JSONObject user, String fieldName, int labelId, int textId) {
		mapTextData(user.optString(fieldName, null), labelId, textId);
	}	
	
	private void updateFields(JSONObject user)
	{
		// Variable fields
		String price = null;
		if(user.has("price_per_hour")) {
			String.format(Locale.US, "$%.2f/hr", user.optDouble("price_per_hour", 999.0));
		}

		// Fill in available data
		mapTextData(user, "name", R.id.profileNameText, R.id.profileNameText);
		mapTextData(user, "public_email_address", R.id.viewEmailText, R.id.emailData);
		mapTextData(user, "phone", R.id.viewPhoneText, R.id.phoneData);
		mapTextData(user, "loc_address", R.id.viewMeetingText, R.id.meetingData);
		mapTextData(user, "subject_tags", R.id.viewTagText, R.id.tagData);
		// Add meeting times
		mapTextData(price, R.id.viewPriceText, R.id.priceData);
		mapTextData(user, "about_me", R.id.viewBioText, R.id.bioData);
		
		//starButton = (ToggleButton)view.findViewById(R.id.starbutton);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState)	{	
		View view = inflater.inflate(R.layout.fragment_profile_view, container, false);
		return view;
	}
	
	public void showUser(int userId, Context context)
	{
		JSONObject j = new JSONObject();
		try {
			j.put("action", "get_user");
			j.put("user_id", userId);
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		new DatabaseRequest(j, this, context);
	}
	
	public void onStarButtonClick(View v)
	{
		// Send request to toggle favorite
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		try {
			if(response.getBoolean("success") && response.getString("action").equals("get_user")) {
				updateFields(response);
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		} catch(NullPointerException e) {
			Log.e(TAG, e.toString());
		}
	}
}
