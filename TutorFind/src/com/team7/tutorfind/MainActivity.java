package com.team7.tutorfind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.widget.EditText;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.team7.tutorfind.MESSAGE";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.action_settings:
    		startActivity(new Intent(this, SettingsActivity.class));
    		break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	
    	return true;
    }

    public void sendMessage (View view)
    {
//    	Intent intent = new Intent(this, DisplayMessageActivity.class);
//    	EditText editText = (EditText)findViewById(R.id.login_field);
//    	String message = editText.getText().toString();
  //  	editText = (EditText)findViewById(R.id.password_field);
    //	String message1 = editText.getText().toString();
//    	message = message + "-" + message1;
  //  	intent.putExtra(EXTRA_MESSAGE,  message);
    //	startActivity(sendIntent);
    	//startActivity(sendintent);
    	Intent sendIntent = new Intent();
    	sendIntent.setAction(Intent.ACTION_SEND);
    	sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text");
    	sendIntent.setType("text/plain");
    	startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.enter_login)));
    }
}
