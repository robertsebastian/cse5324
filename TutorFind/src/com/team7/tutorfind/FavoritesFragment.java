package com.team7.tutorfind;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritesFragment extends ListFragment implements DatabaseRequest.Listener {
	public static final String TAG = "favorites";

    private ArrayList<JSONObject> favoriteList;
   	private FavoriteAdapter favoriteAdapter;
   	
   	// Initialize data fields
   	@Override
   	public void onCreate(Bundle savedInstanceState) {
   		super.onCreate(savedInstanceState);
   		
   		favoriteList = new ArrayList<JSONObject>();
		favoriteAdapter = new FavoriteAdapter(favoriteList);
		setListAdapter(favoriteAdapter);   		
   	}
    
   	// Use a custom view that includes a "no favorites" message when empty
   	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_favorites, null);
	}
	
   	// Kick of a database request when we start back up. This relies on the
   	// cache to quickly populate our view
	@Override
	public void onStart() {
		super.onStart();
		JSONObject j = new JSONObject();
		try {
			j.put("action", "get_favorites");
		} catch (JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
		new DatabaseRequest(j, this, getActivity(), false);				
	}
	
	private class FavoriteAdapter extends ArrayAdapter<JSONObject> {

		public FavoriteAdapter(ArrayList<JSONObject> contacts) {
	    	
	    		// An Adapter acts as a bridge between an AdapterView and the 
				// data for that view. The Adapter also makes a View for each 
				// item in the data set. (Each list item in our ListView)
			
				// The constructor gets a Context so it can use the 
				// resource being the simple_list_item and the ArrayList
				// android.R.layout.simple_list_item_1 is a predefined 
				// layout provided by Android that stands in as a default
	    	
	            super(getActivity(), R.layout.favorites_row, contacts);
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
						.inflate(R.layout.favorites_row, null);
				
			}
			
			// Find the right data to put in the list item
			
			JSONObject theFavorite = getItem(position);
			
			// Put the right data into the right components
			
			TextView lastNameTextView =
	                (TextView)convertView.findViewById(R.id.name);
			
			lastNameTextView.setText(theFavorite.optString("name", ""));
	        
	        TextView phoneNumberView =
	                (TextView)convertView.findViewById(R.id.phone);
	        
	        phoneNumberView.setText(theFavorite.optString("phone", ""));
	        
	        TextView emailAddressView =
	                (TextView)convertView.findViewById(R.id.email);
	        
	        emailAddressView.setText(theFavorite.optString("public_email_address", ""));
			
			// Return the finished list item for display
			
	        return convertView;
			
		}
		
	}

	// Update our list on a database response
	public void onDatabaseResponse(JSONObject response) {
		// Nothing to do if failed request or not a favorites response
		if(response == null || !response.optBoolean("success")) return;
		if(!response.optString("action").equals("get_favorites")) return;
		
		try {
			JSONArray resultArray = response.getJSONArray("favorites");
			
			// Build a list of results and update adapter
			favoriteList.clear();
			for(int i = 0; i < resultArray.length(); i++) {
				favoriteList.add(resultArray.getJSONObject(i));
			}
			favoriteAdapter.notifyDataSetChanged();
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	// Show the profile on list item click
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		JSONObject favorite = favoriteAdapter.getItem(position);
		Intent i = new Intent(getActivity(), ProfileViewActivity.class);
		i.putExtra("user_id", favorite.optInt("user_id"));
		startActivity(i);
	}
}