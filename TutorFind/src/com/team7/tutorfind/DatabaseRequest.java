package com.team7.tutorfind;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class DatabaseRequest extends AsyncTask<JSONObject, Void, JSONObject> implements OnDismissListener {
	public static final String TAG = "database_request";
	
	public interface Listener {
		public void onDatabaseResponse(JSONObject response);
	}
	
	protected Listener mListener;
	protected String mAddress;
	protected String mSessionId;
	protected int mPort;
	protected Context mContext;
	protected ProgressDialog mProgress;
	protected boolean mShowProgress;
	
	public DatabaseRequest(JSONObject request, Listener listener, Context context) {
		this(request, listener, context, true);
	}
	
	public DatabaseRequest(JSONObject request, Listener listener, Context context, boolean showProgress) {
		mListener = listener;
		mContext = context;
		mShowProgress = showProgress;
		
		// Read address and port from preferences
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		mAddress = pref.getString("pref_db_address", "team7.dyndns.org");
		mSessionId = pref.getString("session_id", null);
		try {
			mPort = Integer.parseInt(pref.getString("pref_db_port", "80"));
		} catch(NumberFormatException e) {
			mPort = 80;
		}
		
		// Kick off request
		execute(request);
	}
	
	@Override
	protected void onPreExecute() {
		// Show progress dialog while request is in progress
		if(mShowProgress) {
			mProgress = new ProgressDialog(mContext);
			mProgress.setTitle("Processing...");
			mProgress.setMessage("Please wait.");
			mProgress.setCancelable(true);
			mProgress.setIndeterminate(true);
			mProgress.setOnDismissListener(this);		
			mProgress.show();
		} else {
			mProgress = null;
		}
	}
	
	// Make HTTP request to database
	protected JSONObject doInBackground(JSONObject... requests) {
		try {
			// Add session ID to request if available
			requests[0].put("session_id", mSessionId);
			
			Log.d("DAT", "Connecting to " + mAddress + " " + mPort);
			
			// Open POST request
			URL url = new URL("http", mAddress, mPort, "tutor_find_db.py");
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
			Log.e(TAG, e.toString());
		} catch(MalformedURLException e) {
			Log.e(TAG, e.toString());
		} catch(IOException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}
	
	// Notify listener of response
	protected void onPostExecute(JSONObject response) {
		if(mProgress != null) {
			mProgress.dismiss();
		}
		mListener.onDatabaseResponse(response);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		this.cancel(true);
	}
}
