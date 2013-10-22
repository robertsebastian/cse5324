package com.team7.tutorfind;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class DatabaseRequest extends AsyncTask<JSONObject, Void, JSONObject> {
	public interface Listener {
		public void onDatabaseResponse(JSONObject response);
	}
	
	protected Listener mListener;
	protected String mAddress;
	protected String mSessionId;
	protected int mPort;
	
	public DatabaseRequest(JSONObject request, Listener listener, Context context) {
		mListener = listener;
		
		// Read address and port from preferences
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		mAddress = pref.getString("pref_db_address", "10.0.2.2");
		mSessionId = pref.getString("session_id", null);
		try {
			mPort = Integer.parseInt(pref.getString("pref_db_port", "8000"));
		} catch(NumberFormatException e) {
			mPort = 8000;
		}
		
		// Kick off request
		execute(request);
	}
	
	// Make HTTP request to database
	protected JSONObject doInBackground(JSONObject... requests) {
		try {
			// Add session ID to request if available
			requests[0].put("session_id", mSessionId);
			
			// Open POST request
			URL url = new URL("http", mAddress, mPort, "tutor_find_db"); // TODO: Fix hard coded stuff
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			
			// Write JSON output
			OutputStream out = conn.getOutputStream();
			out.write(requests[0].toString().getBytes());
			out.close();
			
			// Read JSON response
			InputStream in = conn.getInputStream();
			
			byte[] bytes = new byte[1000];
			StringBuilder result = new StringBuilder();
			int numRead = 0;
			
			while ((numRead = in.read(bytes)) >= 0) {
			    result.append(new String(bytes, 0, numRead));
			}
			in.close();
			
			return new JSONObject(result.toString());
		} catch(JSONException e) {
			Log.e("db", e.toString());
		} catch(MalformedURLException e) {
			Log.e("db", e.toString());
		} catch(IOException e) {
			Log.e("db", e.toString());
		}
		return null;
	}
	
	// Notify listener of response
	protected void onPostExecute(JSONObject response) {
		mListener.onDatabaseResponse(response);
	}
}
