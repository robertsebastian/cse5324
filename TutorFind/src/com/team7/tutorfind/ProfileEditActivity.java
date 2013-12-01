package com.team7.tutorfind;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileEditActivity extends Activity implements
	DatabaseRequest.Listener,
	OnCheckedChangeListener 
{
	public static final String TAG = "profile_edit";
	
	private JSONObject mUser;
	private Bitmap mPicture;
	private List<Item> mCommonItems;
	private List<Item> mTutorItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        
		setContentView(R.layout.activity_profile_edit);
	}
	
	// Container for handling an edittext for a profile view item
	private class Item {
		String mDbKey;
		TextView mText;
		
		Item(ViewGroup group) {
			mText  = (TextView)group.findViewWithTag("content");
			mDbKey = (String)group.getTag();
			
			mText.setText(mUser.isNull(mDbKey) ? "" : mUser.optString(mDbKey));
		}
		
		// Store text items in user object
		public void save() {
			try {
				mUser.put(mDbKey, mText.getText());
			} catch(JSONException e) {
				Log.e(TAG, e.toString());
			}
		}
	}
	
	public void addAvailabilityItem(int day, int time) {
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.availability_row, null);
		
		((ViewGroup)findViewById(R.id.availability_list)).addView(row);		
		((Spinner)row.findViewWithTag("day")).setSelection(day);
		((Spinner)row.findViewWithTag("time")).setSelection(time);
	}
	
	public void loadAvailability() {
		if(mUser.isNull("availability_string")) return;
		
		((ViewGroup)findViewById(R.id.availability_list)).removeAllViews();
		for(int[] pair : Util.parseAvailability(mUser.optString("availability_string"))) {
			addAvailabilityItem(pair[0], pair[1]);
		}
	}
	
	public void saveAvailability() {
		ViewGroup availGroup = (ViewGroup)findViewById(R.id.availability_list);
		ArrayList<String> availList = new ArrayList<String>();
		
		int availability = 0;
		int dayMask[] = getResources().getIntArray(R.array.availability_days_mask);
		int timeMask[] = getResources().getIntArray(R.array.availability_times_mask);
		
		for(int i = 0; i < availGroup.getChildCount(); i++) {
			int day = ((Spinner)availGroup.getChildAt(i).findViewWithTag("day")).getSelectedItemPosition();
			int time = ((Spinner)availGroup.getChildAt(i).findViewWithTag("time")).getSelectedItemPosition();
			
			availList.add(String.format(Locale.US, "%d,%d", day, time));
			availability |= dayMask[day] & timeMask[time];
		}
		
		try {
			mUser.put("availability", availability);
			mUser.put("availability_string", TextUtils.join(":", availList));
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	// Save text from all items in user object
	public void saveAllItems() {
		for(Item i : mCommonItems) i.save();
		for(Item i : mTutorItems) i.save();
		saveAvailability();
	}
	
	// Save off text form all items in user object
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		saveAllItems();
		getIntent().putExtra("user", mUser.toString());
	}
	
	// Search a LinearLayout for profile items and map them to our user object.
	// This relies on a specific layout format: LinearLayout of views that
	// contain title and value resource views
	private List<Item> addItemsFromLayout(ViewGroup list) {
		ArrayList<Item> items = new ArrayList<Item>();
		for(int i = 0; i < list.getChildCount(); i++) {
			ViewGroup group = (ViewGroup)list.getChildAt(i);
			if(group.findViewWithTag("content") != null) {
				items.add(new Item(group));
			}
		}
		return items;
	}
	
	public void onUserUpdated() {
		if(mUser == null) return;
		
		// Map to the profile view items in the layout
		mCommonItems = addItemsFromLayout((ViewGroup)findViewById(R.id.common_list));
		mTutorItems = addItemsFromLayout((ViewGroup)findViewById(R.id.tutor_list));
		
		loadAvailability();
		
		// Enable/disable tutor-only fields based on whether we are a tutor or not
		CheckBox isTutor = (CheckBox)findViewById(R.id.is_tutor_checkbox);
		isTutor.setOnCheckedChangeListener(this);
		isTutor.setChecked(mUser.optBoolean("tutor_flag"));
		Util.setGroupEnabled((ViewGroup)findViewById(R.id.tutor_list), mUser.optBoolean("tutor_flag"));
	}
	
	public void onPictureUpdated() {
		if(mPicture == null) return;
		ImageButton img = (ImageButton)findViewById(R.id.profile_picture);
		img.setImageBitmap(mPicture);
		img.invalidate();
		img.requestLayout();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();

		if(mUser == null) {
			try {
				mUser = new JSONObject(getIntent().getExtras().getString("user"));
			} catch(JSONException e) {
				Log.e(TAG, e.toString());
			}
		}
		
		if(!mUser.isNull("picture")) {
			mPicture = Util.decodePicture(mUser.optString("picture"));
		} else if(getIntent().getExtras().containsKey("picture")) {
			mPicture = Util.decodePicture(getIntent().getExtras().getString("picture"));
		}
		
		onUserUpdated();
		onPictureUpdated();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		try {
			mUser.put("tutor_flag", isChecked);
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		
		// Enable/disable tutor-only fields based on whether we are a tutor or not
		Util.setGroupEnabled((ViewGroup)findViewById(R.id.tutor_list), mUser.optBoolean("tutor_flag"));
	}
	
	// Handle actionbar menu actions
	public void onSaveButton(MenuItem item) {
		try {
			// Look up the address field and add the lat/lon
			if(!mUser.isNull("loc_address") && !mUser.optString("loc_address").isEmpty()) {
				Geocoder g = new Geocoder(this, Locale.US);
				List<Address> result = g.getFromLocationName(mUser.optString("loc_address"), 1);
				
				if(!result.isEmpty()) {
					mUser.put("loc_lat", result.get(0).getLatitude());
					mUser.put("loc_lon", result.get(0).getLongitude());
					
					Log.d(TAG, "Put lat: " + result.get(0).getLatitude());
					Log.d(TAG, "Put lon: " + result.get(0).getLongitude());
				}
			}
			
			// Save all items to our user object and send it to the database
			saveAllItems();
			mUser.put("action", "update_user");
			new DatabaseRequest(mUser, this, this, true);
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
		} catch(IOException e) {
			Log.e(TAG, e.toString(), e);
		}
	}
	
	public void removeAvailabilityLine(View v) {
		((ViewGroup)findViewById(R.id.availability_list)).removeView((View)v.getParent());
	}
	
	public void addAvailabilityLine(View v) {
		addAvailabilityItem(0, 0);
	}
		
	public void onPictureClicked(View v) {
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        Uri fileUri = Uri.fromFile(new File(getExternalFilesDir(null), "temp.jpg"));
	    i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	    
		startActivityForResult(i, 0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK) return;
		
		try {
			// Load picture from camera and scale/crop it to at most 640x400
			Uri imgFile = Uri.fromFile(new File(getExternalFilesDir(null), "temp.jpg"));
			Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), imgFile);
			
			int newHeight = (int)((float)img.getHeight() / (float)img.getWidth() * 640.0f);
			img = Bitmap.createScaledBitmap(img, 640, newHeight, true);
			
			if(img.getHeight() > 400) {
				img = Bitmap.createBitmap(img, 0, (img.getHeight() - 400) / 2, img.getWidth(), 400);
			}
			
			System.gc();
			
			Log.d(TAG, "Got picture");
			
			// Store picture with user
			if(mUser != null) {
				try {
					mUser.put("picture", Util.encodePicture(img));
					Log.d(TAG, mUser.toString());
				} catch(JSONException e) {
					Log.e(TAG, e.toString());
				}
			}
			
			// Show picture
			Log.d(TAG, "Showing picture");
			mPicture = img;
			onPictureUpdated();
		} catch(IOException e) {
			Log.e(TAG, e.toString());
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_edit, menu);
        return true;
    }

	@Override
	public void onDatabaseResponse(JSONObject response) {
		if(!response.optBoolean("success")) return;
		
		String action = response.optString("action");
		
		// If user successfully updated, return to calling activity
		if(action.equals("update_user")) {
			// Append picture if we have one
			if(mPicture != null) {
				try {
					response.put("picture", Util.encodePicture(mPicture));
				} catch(JSONException e) {
					Log.e(TAG, e.toString(), e);
				}
			}
			
			// Send result to caller
			Intent i = new Intent().putExtra("user", response.toString());
			setResult(Activity.RESULT_OK, i);
			finish();
		}
	}
}