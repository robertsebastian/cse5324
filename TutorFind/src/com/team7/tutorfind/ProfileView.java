package com.team7.tutorfind;

import org.json.JSONObject;
import java.lang.String;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class ProfileView extends Activity implements DatabaseRequest.Listener {

	//Temporary test data until the database interface is implemented
	private String firstName = "John",
				   lastName = "Doe",
				   emailTest = "something@email.com",
				   phoneTest = "123-456-7890",
				   meetTest = "1234 SomeStreet Here",
				   travelTest = "25 Miles",
				   catTest = "Biology, Math, Chemestry",
				   timesTest = "Monday 2:00PM",
				   priceTest = "$25/hr",
				   BioTest = "Tell me More";
	
	private TextView emText, phoneText, meetingText, travelText,
	 catText, timesText, priceText, bioText;
    private TextView nameData, emData, phoneData, meetingData, travelData,
	 catData, timesData, priceData, bioData;
	
	private boolean updateTextFields()
	{
		nameData.setText(firstName + " " + lastName);
		emData.setText(emailTest);
		phoneData.setText(phoneTest);
		meetingData.setText(meetTest);
		travelData.setText(travelTest);
		catData.setText(catTest);
		timesData.setText(timesTest);
		priceData.setText(priceTest);
		bioData.setText(BioTest);
		
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_view);
		
		//Setup connections for TextView on page
		TextView emText, phoneText, meetingText, travelText,
				 catText, timesText, priceText, bioText;
		TextView nameData, emData, phoneData, meetingData, travelData,
		 		 catData, timesData, priceData, bioData;
		
		//Link information for each text field
		emText = (TextView) findViewById(R.id.viewEmailText);
		phoneText = (TextView) findViewById(R.id.viewPhoneText);
		meetingText = (TextView) findViewById(R.id.viewMeetingText);
		travelText = (TextView) findViewById(R.id.viewTravelText);
		catText = (TextView) findViewById(R.id.viewTagText);
		timesText = (TextView) findViewById(R.id.viewTimesText);
		priceText = (TextView) findViewById(R.id.viewPriceText);
		bioText = (TextView) findViewById(R.id.viewBioText);
		
		nameData = (TextView) findViewById(R.id.profileNameText);
		emData = (TextView) findViewById(R.id.emailData);
		phoneData = (TextView) findViewById(R.id.phoneData);
		meetingData = (TextView) findViewById(R.id.meetingData);
		travelData = (TextView) findViewById(R.id.travelData);
		catData = (TextView) findViewById(R.id.tagData);
		timesData = (TextView) findViewById(R.id.timesData);
		priceData = (TextView) findViewById(R.id.priceData);
		bioData = (TextView) findViewById(R.id.bioData);
		
		updateTextFields();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_view, menu);
		return true;
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		// TODO Auto-generated method stub
		
	}

}
