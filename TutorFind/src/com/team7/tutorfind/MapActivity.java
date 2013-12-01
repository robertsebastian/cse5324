package com.team7.tutorfind;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity {
	private static final String TAG = "maps_activity";
	
	private GoogleMap map;
	
	ArrayList<JSONObject> mUsers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.the_map)).getMap();

		// Extract users list from intent parameters
		try {
			JSONArray userArr = new JSONArray(getIntent().getStringExtra("users"));
			
			mUsers = new ArrayList<JSONObject>();
			for(int i = 0; i < userArr.length(); i++) {
				mUsers.add((JSONObject)userArr.get(i));
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}

		// Load markers for everybody in the user list with a position
		LatLng defaultPos = null;
		for(JSONObject user : mUsers) {
			if(user.isNull("loc_lat") || user.isNull("loc_lon")) return;
			
			LatLng pos = new LatLng(user.optDouble("loc_lat"), user.optDouble("loc_lon"));
			
			if(defaultPos == null) defaultPos = pos; // First valid user is default position
			
			map.addMarker(new MarkerOptions()
				.position(pos)
				.title(user.optString("name")));
		}
		
		// Go to the first valid user in the list
		if(defaultPos != null) {
			CameraPosition p = new CameraPosition(defaultPos, 13.0f, 0.0f, 0.0f);
			map.moveCamera(CameraUpdateFactory.newCameraPosition(p));
		}
	}


}
