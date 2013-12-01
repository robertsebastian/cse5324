package com.team7.tutorfind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements InfoWindowAdapter, OnInfoWindowClickListener {
	private static final String TAG = "maps_activity";
	
	private GoogleMap map;
	
	ArrayList<JSONObject> mUsers;
	HashMap<String, JSONObject> mMarkerMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.the_map)).getMap();
		mMarkerMap = new HashMap<String, JSONObject>();
		mUsers = new ArrayList<JSONObject>();
		
		// Use this activity to draw the info pop-ups and handle clicks on them
		map.setInfoWindowAdapter(this);
		map.setOnInfoWindowClickListener(this);

		// Extract users list from intent parameters
		try {
			JSONArray userArr = new JSONArray(getIntent().getStringExtra("users"));
			
			for(int i = 0; i < userArr.length(); i++) {
				mUsers.add((JSONObject)userArr.get(i));
			}
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		}

		// Load markers for everybody in the user list with a position
		Marker defaultMarker = null;
		for(JSONObject user : mUsers) {
			if(user.isNull("loc_lat") || user.isNull("loc_lon")) return;	
			
			Marker mark = map.addMarker(new MarkerOptions()
				.position(new LatLng(user.optDouble("loc_lat"), user.optDouble("loc_lon")))
				.title(user.optString("name")));
			mMarkerMap.put(mark.getId(), user);
			
			if(defaultMarker == null) defaultMarker = mark;
		}
		
		// Go to the first valid user in the list
		if(defaultMarker != null) {
			CameraPosition p = new CameraPosition(defaultMarker.getPosition(), 13.0f, 0.0f, 0.0f);
			map.moveCamera(CameraUpdateFactory.newCameraPosition(p));
			defaultMarker.showInfoWindow();			
		}
	}

	// Show more detailed information for user pop-up
	// TODO: Just copy-pasted this shit from search activity. Should make common somehow.	
	@Override
	public View getInfoContents(Marker m) {
		JSONObject user = mMarkerMap.get(m.getId());
		
		if(user == null) return null;		
		
		// Show something useful in the title bar
		getActionBar().setTitle(user.optString("name"));

		// Inflate the summary layout and fill it in
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View info = inflater.inflate(R.layout.user_summary_row, null);
		
		float rating = (float)user.optDouble("score", 0.0);
		int num_reviews = user.optInt("num_reviews", 0);
		
		// Unrated is represented by no stars -- make sure low rated tutors show a half star
		if(num_reviews > 0 && rating < 0.5) rating = 0.5f;
		
		String name    = user.optString("name", "???");
		String price   = String.format(Locale.US, "$%.2f/hr", user.optDouble("price_per_hour", 999.0));
		String dist    = String.format(Locale.US, "%.1f mi", user.optDouble("distance", 999.0));
		String reviews = String.format(Locale.US, "(%d)", user.optInt("num_reviews", 0));
		
		((TextView)info.findViewById(R.id.user_summary_name)).setText(name);
		((TextView)info.findViewById(R.id.user_summary_price)).setText(price);
		
		// Don't show distance if we don't have it
		if(!user.isNull("distance")) {
			((TextView)info.findViewById(R.id.user_summary_distance)).setText(dist);
		} else {
			info.findViewById(R.id.user_summary_distance).setVisibility(View.GONE);
		}
		
		((TextView)info.findViewById(R.id.user_summary_num_reviews)).setText(reviews);
		((RatingBar)info.findViewById(R.id.user_summary_score)).setRating(rating);
		info.setBackgroundResource(0);
		
		// No advertising here
		info.findViewById(R.id.advertisementText).setVisibility(View.GONE);
		
		return info;
	}

	// Use standard window border
	@Override
	public View getInfoWindow(Marker m) {
		return null;
	}

	// Kick off a profile view intent when a marker is clicked
	@Override
	public void onInfoWindowClick(Marker m) {
		JSONObject user = mMarkerMap.get(m.getId());
		
		if(user == null) return;
		
		Intent i = new Intent(this, ProfileViewActivity.class);
		i.putExtra("user_id", user.optInt("user_id", -1));
		i.putExtra("user", user.toString());
		startActivity(i);
	}
}