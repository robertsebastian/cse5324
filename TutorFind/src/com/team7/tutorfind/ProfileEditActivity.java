package com.team7.tutorfind;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ProfileEditActivity extends Activity implements
	DatabaseRequest.Listener,
	OnCheckedChangeListener 
{
	public static final String TAG = "profile_edit";
	
	private JSONObject mUser;
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
		TextView mTitle;
		
		Item(ViewGroup group) {
			mTitle = (TextView)group.findViewById(R.id.profile_item_title);
			mText  = (TextView)group.findViewById(R.id.profile_edit_value);
			mDbKey = (String)mText.getTag();
			
			mText.setText(mUser.isNull(mDbKey) ? "" : mUser.optString(mDbKey));
			mText.setId(Util.generateViewId()); // ID must be unique for rotation to work right
		}
		
		// Enable or disable the edit field
		public void setEnabled(boolean enabled) {
			int color = getResources().getColor(
					enabled ? R.color.profile_item_title_normal : R.color.profile_item_title_disabled);
			mTitle.setTextColor(color);
			mText.setEnabled(enabled);
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
	
	// Save text from all items in user object
	public void saveAllItems() {
		for(Item i : mCommonItems) i.save();
		for(Item i : mTutorItems) i.save();		
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
			items.add(new Item((ViewGroup)list.getChildAt(i)));
		}
		return items;
	}
	
	public void onUserUpdated() {
		if(mUser == null) return;
		
		// Map to the profile view items in the layout
		mCommonItems = addItemsFromLayout((ViewGroup)findViewById(R.id.common_list));
		mTutorItems = addItemsFromLayout((ViewGroup)findViewById(R.id.tutor_list));
		
		// Enable/disable tutor-only fields based on whether we are a tutor or not
		CheckBox isTutor = (CheckBox)findViewById(R.id.is_tutor_checkbox);
		isTutor.setOnCheckedChangeListener(this);
		isTutor.setChecked(mUser.optBoolean("tutor_flag"));
		for(Item i : mTutorItems) i.setEnabled(mUser.optBoolean("tutor_flag"));
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		try {
			mUser = new JSONObject(getIntent().getExtras().getString("user"));
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		onUserUpdated();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		try {
			mUser.put("tutor_flag", isChecked);
		} catch(JSONException e) {
			Log.e(TAG, e.toString());
		}
		
		// Enable/disable tutor-only fields based on whether we are a tutor or not
		for(Item i : mTutorItems) i.setEnabled(isChecked);
	}
	
	// Handle actionbar menu actions
	public void onSaveButton(MenuItem item) {
		try {
			// Save all items to our user object and send it to the database
			saveAllItems();
			mUser.put("action", "update_user");
			new DatabaseRequest(mUser, this, this, true);
		} catch(JSONException e) {
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
			Intent i = new Intent().putExtra("user", response.toString());
			setResult(Activity.RESULT_OK, i);
			finish();
		}
	}
}