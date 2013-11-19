package com.team7.tutorfind;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ListFragment;
import android.content.Context;

public class FavoritesFragment extends ListFragment implements DatabaseRequest.Listener {
	DatabaseRequest mDatabaseReq = null;
	public static final String TAG = "favorites";

    private ArrayList<Favorite> favoriteList;
    
   	private static AllFavorites allFavorites;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void CreateFavorites(Context context)
	{
		JSONObject j = new JSONObject();
		try {
			j.put("action", "get_favorites");
			//String[] keyValue = pref.getString("session_id", null).split(",");
			// j.put("session_id", keyValue[1]);

		} catch (JSONException e) {
			Log.e("login", "Error handling JSON input");
		}
		new DatabaseRequest(j, this, context);
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

	/*public void sendDatabaseRequest() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);

		JSONObject j = new JSONObject();
		try {
			j.put("action", "get_favorites");
			String[] keyValue = pref.getString("session_id", null).split(",");
			// j.put("session_id", keyValue[1]);

		} catch (JSONException e) {
			Log.e("login", "Error handling JSON input");
		}

		// FavoritesFragment favoritesFragment = new FavoritesFragment(this);
		// DetailFragment fragment = (DetailFragment) getFragmentManager()
		// .findFragmentById(R.id.detailFragment);
		// Context cont;
		// cont=getApplicationContext();
		// ProfileViewFragment().getActivity();

		// AllFavorites.get(getActivity())
		
		//FavoritesFragment favoritesFragment;
		//favoritesFragment = new FavoritesFragment();
		//favoritesFragment.getActivity()
		
		//Context context;
		//ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		//ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		
		mDatabaseReq = new DatabaseRequest(j, this, context);

		// setContentView(R.layout.activity_login
		// ProfileViewFragment profileViewFragment = null;
		// profileViewFragment.getActivity()
		// getActivity()
		// getApplicationContext()
	}*/

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
		// TODO Auto-generated method stub
		
		favoriteList = new ArrayList<Favorite>();
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

		mDatabaseReq = null;
	}
	
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
	  //sendDatabaseRequest();
  }
}
