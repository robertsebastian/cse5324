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
	public static final String TAG = "favorites";

    private ArrayList<JSONObject> favoriteList;
    
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
			Log.e(TAG, e.toString(), e);
		}
		new DatabaseRequest(j, this, context, false);
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
	    	
	            super(getActivity(), R.layout.fragment_favorites, contacts);
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
			
			JSONObject theFavorite = getItem(position);
			
			// Put the right data into the right components
			
			TextView lastNameTextView =
	                (TextView)convertView.findViewById(R.id.lastname_textbox);
			
			lastNameTextView.setText(theFavorite.optString("name", ""));
	        
	        TextView phoneNumberView =
	                (TextView)convertView.findViewById(R.id.phonenumber_textbox);
	        
	        phoneNumberView.setText(theFavorite.optString("phone", ""));
	        
	        TextView emailAddressView =
	                (TextView)convertView.findViewById(R.id.email_textbox);
	        
	        emailAddressView.setText(theFavorite.optString("public_email_address", ""));
			
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
	public ArrayList<JSONObject> getFavoriteList() {
		
		return favoriteList;
	}

	public void onDatabaseResponse(JSONObject response) {
		if(response == null || !response.optBoolean("success")) return;
		
		try {
			if(response.getString("action").equals("get_favorites")) {
				updateResults(response.getJSONArray("favorites"));
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		} catch(NullPointerException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	public void updateResults(JSONArray resultArray) {
		try {
			favoriteList = new ArrayList<JSONObject>();
						
			// Build a list of results
			for(int i = 0; i < resultArray.length(); i++) {
				favoriteList.add(resultArray.getJSONObject(i));
			}
			
			favoriteAdapter = new FavoriteAdapter(favoriteList);
			
			setListAdapter(favoriteAdapter);	
			
		} catch(JSONException e) {
			Log.e("search", e.toString());
		}			
	}
	
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
	    JSONObject favorite = favoriteAdapter.getItem(position);
		Intent i = new Intent(getActivity(), ProfileViewActivity.class);
		i.putExtra("user_id", favorite.optInt("user_id"));
		startActivity(i);		
  }
}