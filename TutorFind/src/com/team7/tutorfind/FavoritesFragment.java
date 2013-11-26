package com.team7.tutorfind;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritesFragment extends ListFragment implements DatabaseRequest.Listener {
	DatabaseRequest mDatabaseReq = null;
	public static final String TAG = "favorites";

    private ArrayList<Favorite> favoriteList;
    
   	private static AllFavorites allFavorites;
   	
   	private FavoriteAdapter favoriteAdapter;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		CreateFavorites(getActivity());			
	}
	
	public void CreateFavorites(Context context)
	{
		JSONObject j = new JSONObject();
		try {
			j.put("action", "get_favorites");

		} catch (JSONException e) {
			Log.e("login", "Error handling JSON input");
		}
		new DatabaseRequest(j, this, context, false);
	}
	
	private class FavoriteAdapter extends ArrayAdapter<Favorite> {

		public FavoriteAdapter(ArrayList<Favorite> contacts) {
	    	
	    		// An Adapter acts as a bridge between an AdapterView and the 
				// data for that view. The Adapter also makes a View for each 
				// item in the data set. (Each list item in our ListView)
			
				// The constructor gets a Context so it can use the 
				// resource being the simple_list_item and the ArrayList
				// android.R.layout.simple_list_item_1 is a predefined 
				// layout provided by Android that stands in as a default
	    	
	            super(getActivity(), android.R.layout.simple_list_item_1, contacts);
	    }
		
		// getView is called each time it needs to display a new list item
		// on the screen because of scrolling for example.
		// The Adapter is asked for the new list row and getView provides
		// it.
		// position represents the position in the Array from which we will 
		// be pulling data.
		// convertView is a pre-created list item that will be reconfigured 
		// in the code that follows.
		// ViewGroup is our ListView
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			// Check if this is a recycled list item and if not we inflate it
			
			if(convertView == null){
				
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.fragment_favorites, null);
				
			}
			
			// Find the right data to put in the list item
			
			Favorite theFavorite = getItem(position);
			
			// Put the right data into the right components
			
			TextView lastNameTextView =
	                (TextView)convertView.findViewById(R.id.lastname_textbox);
			
			lastNameTextView.setText(theFavorite.getLastName());
			
	        TextView firstNameView =
	                (TextView)convertView.findViewById(R.id.firstname_textbox);
	        
	        firstNameView.setText(theFavorite.getfirstName());
	        
	        TextView phoneNumberView =
	                (TextView)convertView.findViewById(R.id.phonenumber_textbox);
	        
	        phoneNumberView.setText(theFavorite.getPhoneNumber());
	        
	        TextView emailAddressView =
	                (TextView)convertView.findViewById(R.id.email_textbox);
	        
	        emailAddressView.setText(theFavorite.getEmailAddress());
			
			// Return the finished list item for display
			
	        return convertView;
			
		}
		
	}

	public static AllFavorites get(Context context) {

		if (allFavorites == null) {

			// getApplicationContext returns the global Application object
			// This Context is global to every part of the application

			allFavorites = new AllFavorites(context.getApplicationContext());

		}

		return allFavorites;

	}
	public ArrayList<Favorite> getFavoriteList() {
		
		return favoriteList;
	}

	public void onDatabaseResponse(JSONObject response) {
		
		try {
			if(response.getBoolean("success") && response.getString("action").equals("get_favorites")) {
				updateResults(response.getJSONArray("favorites"));
			}
			else if (response.getBoolean("success") && response.getString("action").equals("get_user"))
			{
				int userID = response.optInt("user_id", -1);
				Intent i = new Intent(getActivity(), ProfileViewActivity.class);
				i.putExtra("user_id", userID);
				i.putExtra("user", response.toString());
				startActivity(i);
			}			
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		} catch(NullPointerException e) {
			Log.e(TAG, e.toString());
		}
		
		/*favoriteList = new ArrayList<Favorite>();
		Favorite paulSmith = new Favorite();

		for (int i = 0; i < 5; i++) {
			paulSmith.setLastName("Smith");
			paulSmith.setfirstName("Paul");
			paulSmith.setPhoneNumber("555-555-5555");
			paulSmith.setEmailAddressy("paulsmith@example.com");
			favoriteList.add(paulSmith);
		}

		Favorite dude9 = new Favorite();
		dude9.setLastName("The");
		dude9.setfirstName("End");
		dude9.setPhoneNumber("555-555-5555");
		dude9.setEmailAddressy("paulsmith1234567890@example.com");
		favoriteList.add(dude9);
		
		FavoriteAdapter contactAdapter = new FavoriteAdapter(favoriteList);
		
		setListAdapter(contactAdapter);	

		mDatabaseReq = null;*/
	}
	
	public void updateResults(JSONArray resultArray) {
		try {
			favoriteList = new ArrayList<Favorite>();
						
			// Build a list of results
			int k = resultArray.length();
			JSONObject results = new JSONObject();
			for(int i = 0; i < resultArray.length(); i++) {
				Favorite addFavorite = new Favorite();
				results = resultArray.getJSONObject(i);
				String userID = (String)results.getString("user_id");
				addFavorite.setUserID(userID);
				String Name = (String)results.get("name");
				addFavorite.setLastName(Name);
				if(results.getString("phone") != "null")
				{
					String phoneNumber = (String)results.getString("phone");
					addFavorite.setPhoneNumber(phoneNumber);
				}
				String emailAddress = (String)results.get("public_email_address");
				addFavorite.setEmailAddressy(emailAddress);
				favoriteList.add(addFavorite);
			}
			
			favoriteAdapter = new FavoriteAdapter(favoriteList);
			
			setListAdapter(favoriteAdapter);	

			mDatabaseReq = null;
			
			// Fill in available data
			/*mapTextData(user, "name", R.id.profileNameText, R.id.profileNameText);
			mapTextData(user, "public_email_address", R.id.viewEmailText, R.id.emailData);
			mapTextData(user, "phone", R.id.viewPhoneText, R.id.phoneData);
			mapTextData(user, "loc_address", R.id.viewMeetingText, R.id.meetingData);
			mapTextData(null, R.id.viewTimesText, R.id.timesData);
			mapTextData(user, "subject_tags", R.id.viewTagText, R.id.tagData);*/
			
			// Create new adapter using the list as its data source
			/*Collections.sort(results, new DistanceComparator());
			mAdapter = new UserSummaryArrayAdapter(this, results);
			
			list.setAdapter(mAdapter);
			list.setOnItemClickListener(this);*/
			
		} catch(JSONException e) {
			Log.e("search", e.toString());
		}			
	}
	
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
	  
	    JSONObject j = new JSONObject();
	    Favorite favorite = favoriteAdapter.getItem(position);
		String dude = favorite.getUserID();
		int Num = Integer.parseInt(dude);
	    try {
			j.put("action", "get_user");
			j.put("user_id", Num);
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		new DatabaseRequest(j, this, getActivity());
  }
}