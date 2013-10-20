package com.team7.tutorfind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide(); //TODO: Should be some way to do this in the layout XML file
        setContentView(R.layout.activity_login);
        
        //TODO: If already logged in, automatically proceed to main activity       
    }
	
	public void onRegisterButtonClicked(View v)
	{
		startActivity(new Intent(this, MainActivity.class));
	}
	
	public void onLoginButtonClicked(View v)
	{
		startActivity(new Intent(this, MainActivity.class));
	}
}
