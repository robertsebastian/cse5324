package com.team7.tutorfind;

import android.util.Log;

public class Favorite {
	
	private String lastName;
	private String firstName;
	private String phoneNumber;
	private String emailAddress;
	private String userID;
	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		
		Log.e("Favorite", lastName);
		
		this.lastName = lastName;
	}

	public String getfirstName() {
		return firstName;
	}

	public void setfirstName(String firstName) {
		
		Log.e("Favorite", firstName);
		
		this.firstName = firstName;
	}


	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		
		Log.e("Favorite", phoneNumber);
		
		this.phoneNumber = phoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddressy(String emailAddress) {
		
		Log.e("Favorite", emailAddress);
		
		this.emailAddress = emailAddress;
	}

	public String getUserID() {
		return userID;
	}
	
	public void setUserID(String userID) {
		
		Log.e("Favorite", userID);
		
		this.userID = userID;
	}	

}
