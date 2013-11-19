package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class ProfileEditActivity extends Activity implements DatabaseRequest.Listener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_edit);
		
		//TODO: Need to get the user data from the login to add to the database
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_view, menu);
		return true; 
	}
	
	public void sendDatabaseData ()
	{
		//create new JSONObject to send to that database
		JSONObject j = new JSONObject();
		
		try
		{
			//Create links to get data from the view
			TextView firstField = (TextView)findViewById(R.id.firstEdit);
			TextView lastField = (TextView)findViewById(R.id.lastEdit);
			TextView emailField = (TextView)findViewById(R.id.emailEdit);
			TextView phoneField = (TextView)findViewById(R.id.phoneEdit);
			TextView meetingField = (TextView)findViewById(R.id.meetingEdit);
			TextView travelField = (TextView)findViewById(R.id.travelEdit);
			TextView tagField = (TextView)findViewById(R.id.tagEdit);
			TextView timesField = (TextView)findViewById(R.id.timesEdit);
			TextView priceField = (TextView)findViewById(R.id.priceEdit);
			TextView bioField = (TextView)findViewById(R.id.bioEdit);
			
			//concat the first and last name into one string
			String name = firstField.getText().toString()+lastField.getText().toString();
			
			//put information within a JSONObject to send to the database
			j.put("name", name);
			j.put("email", emailField.getText());
			j.put("phone", phoneField.getText());
			j.put("meeting", meetingField.getText());
			j.put("travel", travelField.getText());
			j.put("tag", tagField.getText());
			j.put("times", timesField.getText());
			j.put("price", priceField.getText());
			j.put("bio", bioField.getText());
			//TODO: Need to check the field that are in the database.
		} catch(JSONException e) {
			Log.e("profileEdit", "Error handling JSON input from view");
		}
		
		new DatabaseRequest(j, this, this);
	}
	
	
	public void onSubmitButtonClicked(View v)
	{
		sendDatabaseData();
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		// TODO Auto-generated method stub
		
	}

}
