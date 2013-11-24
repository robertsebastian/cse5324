package com.team7.tutorfind;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MapActivity extends Activity {
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.the_map)).getMap();
		
		Geocoder geoCoder = new Geocoder(this);
		
		try
	    {
			//Need to add input for the address here right here
	    	List<Address> addressList = geoCoder.getFromLocationName("701 S Nedderman Dr, Arlington, TX 76019", 1);
	    	Address address = addressList.get(0);
	    	double lat = address.getLatitude();
	    	double lon = address.getLongitude();
	    	LatLng MyHouse = new LatLng(lat, lon);
	    	addressList = geoCoder.getFromLocation(lat, lon, 1);
	    	String addressname = addressList.get(0).getAddressLine(0);
	    	String cityname = addressList.get(0).getAddressLine(1);
	    	String countryname = addressList.get(0).getAddressLine(2);
	    	
	    	Marker myhouse = map.addMarker(new MarkerOptions().position(MyHouse).title(addressname+" "+cityname+" "+countryname));
		    map.moveCamera(CameraUpdateFactory.newLatLngZoom(MyHouse, 17));
	    }
	    catch (IOException e)
	    {
	    	Log.i("Location fail", "Unable to resolve location");
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

}
