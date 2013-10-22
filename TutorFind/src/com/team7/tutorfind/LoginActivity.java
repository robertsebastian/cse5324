package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends Activity implements DatabaseRequest.Listener {
	DatabaseRequest mDatabaseReq = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide(); //TODO: Should be some way to do this in the layout XML file
        setContentView(R.layout.activity_login);
        //TODO: If already logged in, automatically proceed to main activity
    }
	
	// Handle response to either login or register messages
	public void onDatabaseResponse(JSONObject response) {
		try {
			TextView errorText = (TextView)findViewById(R.id.login_error_text);
			
			if(response == null) {
				// Some sort of connection issue, most likely
				errorText.setText(R.string.error_connecting_to_server); 
			} else if(response.getBoolean("success") == false) {
				// Login/registration failed -- display appropriate error text
				String error = response.getString("error");

				if(error.equals("duplicate_user")) {
					errorText.setText(getString(R.string.error_duplicate_user));
				} else if(error.equals("invalid_email")) {
					errorText.setText(getString(R.string.error_invalid_login));
				} else if(error.equals("invalid_password")) {
					errorText.setText(getString(R.string.error_invalid_password));
				} else if(error.equals("login_failed")) {
					errorText.setText(getString(R.string.error_login_failed));
				} else {
					// Unknown error, just display text
					errorText.setText(error);
				}
			} else {
				// Success -- Save session ID
				String sessionId = response.getString("session_id");
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor edit = pref.edit();
				edit.putString("session_id", sessionId);
				edit.commit();	
				
				// Go to main activity
				startActivity(new Intent(this, MainActivity.class));
			}
		} catch(JSONException e) {
			Log.e("login", e.toString());
		}
		mDatabaseReq = null;
	}
	
	// Send the appropriate request type containing email/password fields
	private void sendDatabaseRequest(String action) {
		// Clear out error text on new request
		TextView errorText = (TextView)findViewById(R.id.login_error_text);
		errorText.setText("");
		
		// Send request
		JSONObject j = new JSONObject();
		try {
			TextView login = (TextView)findViewById(R.id.login_field);
			TextView password = (TextView)findViewById(R.id.password_field);
			
			j.put("action",   action);
			j.put("email",    login.getText());
			j.put("password", password.getText());
		} catch(JSONException e) {
			Log.e("login", "Error handling JSON input");
		}
		
		mDatabaseReq = new DatabaseRequest(j, this, this);	
	}
	
	public void onRegisterButtonClicked(View v)
	{
		sendDatabaseRequest("register");
	}
	
	public void onLoginButtonClicked(View v)
	{
		sendDatabaseRequest("login");
	}
}