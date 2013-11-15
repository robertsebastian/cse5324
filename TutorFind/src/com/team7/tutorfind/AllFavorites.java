package com.team7.tutorfind;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class AllFavorites extends Activity implements DatabaseRequest.Listener {
	
	DatabaseRequest mDatabaseReq = null;
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide(); //TODO: Should be some way to do this in the layout XML file
        setContentView(R.layout.activity_login);
        //TODO: If already logged in, automatically proceed to main activity
    }
	
	// This class will only have one instance that will
	// contain an arraylist with all contacts in it.
	// Singleton

	private static AllFavorites allFavorites;

	// By creating a Context you gain access to the 
	// current state of the complete application.
	// With it you can get information about all the Activitys
	// in the app among other things.

	// By accessing the Context you control every part of
	// the application along with everything that app
	// is allowed to access on the device.

	private Context applicationContext;

	// This ArrayList will hold all the Contacts

	private ArrayList<Favorite> favoriteList;

	private AllFavorites(Context applicationContext){

		this.applicationContext = applicationContext;

		favoriteList = new ArrayList<Favorite>();

		// TODO
		// This will be the section where we query
		// the database and get the users favorite list
		// then we will loop through it like below until
		// all favorites are added, 
		// the rest is already in place
		
	}

	// Checks if an instance of allContacts exists. If it does
	// the one instance is returned. Otherwise the instance is
	// created.

	public static AllFavorites get(Context context){

		if(allFavorites == null){

			// getApplicationContext returns the global Application object
			// This Context is global to every part of the application

			allFavorites = new AllFavorites(context.getApplicationContext());

		}

		return allFavorites;

	}

	public ArrayList<Favorite> getFavoriteList(){

		sendDatabaseRequest();
		return favoriteList;
		

	}
	
	public void sendDatabaseRequest() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(applicationContext);		
		
		JSONObject j = new JSONObject();
		try {						
			j.put("action",  "get_favorites"); 
		    String[] keyValue = pref.getString("session_id", null).split(",");
	        //j.put("session_id", keyValue[1]);
					
		} catch(JSONException e) {
			Log.e("login", "Error handling JSON input");
		}

		mDatabaseReq = new DatabaseRequest(j, this, applicationContext);
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		// TODO Auto-generated method stub
		
		Favorite paulSmith = new Favorite();
		
		for (int i = 0; i < 25; i++)
		{
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
		
		mDatabaseReq = null;
	}

}
