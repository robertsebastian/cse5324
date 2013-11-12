package com.team7.tutorfind;

import org.json.JSONObject;
import java.lang.String;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ProfileViewFragment extends Fragment implements DatabaseRequest.Listener {

	static final String TAG = "ProfileView";
	
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
    private ToggleButton starButton; 
	
	private boolean updateTextFields()
	{
		String concatName = firstName + " " + lastName;
		nameData.setText(concatName);
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
	
	public void initFields(View view)
	{
		emText = (TextView)view.findViewById(R.id.viewEmailText);
		phoneText = (TextView)view.findViewById(R.id.viewPhoneText);
		meetingText = (TextView)view.findViewById(R.id.viewMeetingText);
		travelText = (TextView)view.findViewById(R.id.viewTravelText);
		catText = (TextView)view.findViewById(R.id.viewTagText);
		timesText = (TextView)view.findViewById(R.id.viewTimesText);
		priceText = (TextView)view.findViewById(R.id.viewPriceText);
		bioText = (TextView)view.findViewById(R.id.viewBioText);
		
		nameData = (TextView)view.findViewById(R.id.profileNameText);
		emData = (TextView)view.findViewById(R.id.emailData);
		phoneData = (TextView)view.findViewById(R.id.phoneData);
		meetingData = (TextView)view.findViewById(R.id.meetingData);
		travelData = (TextView)view.findViewById(R.id.travelData);
		catData = (TextView)view.findViewById(R.id.tagData);
		timesData = (TextView)view.findViewById(R.id.timesData);
		priceData = (TextView)view.findViewById(R.id.priceData);
		bioData = (TextView)view.findViewById(R.id.bioData);
		
		starButton = (ToggleButton)view.findViewById(R.id.starbutton);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState)	{	
		View view = inflater.inflate(R.layout.fragment_profile_view, container, false);
		//Setup connections for TextView on page
		
		//Link information for each text field
		initFields(view);
		updateTextFields();
		
		//add the click listener
		starButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO: add button code here!!!
			}
		});
		return view;
	}
	
	public void onStarButtonClick(View v)
	{
	}

	@Override
	public void onDatabaseResponse(JSONObject response) {
		// TODO Auto-generated method stub
		
	}
}
