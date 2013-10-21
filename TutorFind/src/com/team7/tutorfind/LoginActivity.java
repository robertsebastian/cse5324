package com.team7.tutorfind;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class LoginActivity extends Activity implements DatabaseRequest.Listener {
	DatabaseRequest mDatabaseReq = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide(); //TODO: Should be some way to do this in the layout XML file
        setContentView(R.layout.activity_login);
        
        //TODO: If already logged in, automatically proceed to main activity       
    }
	
	public void onDatabaseResponse(JSONObject response)
	{
		Log.d("Login", "Got response" + response);
		mDatabaseReq = null;
	}
	
	public void onRegisterButtonClicked(View v)
	{
		JSONObject j = new JSONObject();
		try {
			j.put("action", "register");
			j.put("email", "alice@example.net");
			j.put("password", "password");
		} catch(JSONException e) {
			Log.e("login", "Error handling JSON input");
		}
		
		mDatabaseReq = new DatabaseRequest(j, this, this);
		//startActivity(new Intent(this, MainActivity.class));
	}
	
	public void onLoginButtonClicked(View v)
	{
		startActivity(new Intent(this, MainActivity.class));
	}
}
