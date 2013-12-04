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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
		LatLngBounds.Builder points = new LatLngBounds.Builder();
		for(JSONObject user : mUsers) {
			if(user.isNull("loc_lat") || user.isNull("loc_lon")) return;	
			
			LatLng loc = new LatLng(user.optDouble("loc_lat"), user.optDouble("loc_lon"));
			Marker mark = map.addMarker(new MarkerOptions().position(loc).title(user.optString("name")));
			mMarkerMap.put(mark.getId(), user);
			
			points.include(loc);
		}
		
		// Show all points as soon as the view is created
		final LatLngBounds box = points.build();
		final View mapView = findViewById(R.id.the_map);
		mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
		    @SuppressWarnings("deprecation")
			@Override 
		    public void onGlobalLayout() { 
		        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
				try {
					map.moveCamera(CameraUpdateFactory.newLatLngBounds(box, 100));
				} catch(IllegalStateException e) {}
		    } 
		});
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
		String reviews = String.format(Locale.US, "(%d)", user.optInt("num_reviews", 0));
		
		((TextView)info.findViewById(R.id.name)).setText(name);
		((TextView)info.findViewById(R.id.price)).setText(price);
		((TextView)info.findViewById(R.id.num_reviews)).setText(reviews);
		((RatingBar)info.findViewById(R.id.score)).setRating(rating);
		info.setBackgroundResource(0);
		
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