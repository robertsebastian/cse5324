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

public class AllFavorites extends Activity {
	DatabaseRequest mDatabaseReq = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		// TODO: If already logged in, automatically proceed to main activity
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

	AllFavorites(Context applicationContext) {

		this.applicationContext = applicationContext;

		favoriteList = new ArrayList<Favorite>();

		// TODO
		// This will be the section where we query
		// the database and get the users favorite list
		// then we will loop through it like below until
		// all favorites are added,
		// the rest is already in place

	}

	// Checks if an instance of allFavorites exists. If it does
	// the one instance is returned. Otherwise the instance is
	// created.

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

}
