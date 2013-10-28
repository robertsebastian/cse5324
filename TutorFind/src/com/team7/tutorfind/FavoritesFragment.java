package com.team7.tutorfind;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ListFragment;

public class FavoritesFragment extends ListFragment {

    private ArrayList<Favorite> favoriteList;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the ArrayList from AllFavorites
		
		favoriteList = AllFavorites.get(getActivity()).getFavoriteList();
		
		FavoriteAdapter contactAdapter = new FavoriteAdapter(favoriteList);
		
		// Provides the data for the ListView by setting the Adapter 
		
		setListAdapter(contactAdapter);		
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

    /*String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
        "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
        "Linux", "OS/2" }; 
    Number values[] = new Number[10];
    for (int i = 0; i < 10; i++)
    {
    	values[i] = i;
    }
    ArrayAdapter<Number> adapter = new ArrayAdapter<Number>(getActivity(),
        android.R.layout.simple_list_item_1, values);
    setListAdapter(adapter); */

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
	    
  }
}
