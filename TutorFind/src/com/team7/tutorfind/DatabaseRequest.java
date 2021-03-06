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
	protected boolean mUseCache;
	
	public DatabaseRequest(JSONObject request, Listener listener, Context context) {
		this(request, listener, context, true);
	}
	
	public DatabaseRequest(JSONObject request, Listener listener, Context context, boolean showProgress) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);		
		
		mListener = listener;
		mContext = context;
		mShowProgress = showProgress;
		mUseCache = pref.getBoolean("pref_use_cache", true);
		
		// Immediately return cached data if possible
		if(mUseCache) {
			attemptToHandleFromCache(request);
		}
		
		// Read address and port from preferences
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
	
	// Try to call onDatabaseResponse immediately with cached data if possible.
	// onDatabaseResponse will be called again if fresh data is eventually
	// received.
	private void attemptToHandleFromCache(JSONObject request) {
		String action = request.optString("action");
		
		JSONObject result = null;
		if(action.equals("get_user")) {
			result = CacheManager.get(mContext, action, "user_id", request);
		} else if(action.equals("get_picture")) {
			result = CacheManager.get(mContext, action, "user_id", request);
			try {
				// If we found a cached picture, tell the database how old it is
				if(result != null) request.put("timestamp", result.optInt("timestamp"));
			} catch(JSONException e) {}
		} else if(action.equals("get_favorites")) {
			result = CacheManager.get(mContext, action, null, request);
		}
		
		if(result != null) mListener.onDatabaseResponse(result);
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
		StringBuilder result = new StringBuilder();
		try {
			// Add session ID to request if available
			if(mSessionId != null) {
				requests[0].put("session_id", mSessionId);
			}
			
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
			int numRead = 0;
			
			while ((numRead = in.read(bytes)) >= 0) {
			    result.append(new String(bytes, 0, numRead));
			}
			in.close();
			
			return new JSONObject(result.toString());
		} catch(JSONException e) {
			Log.e(TAG, e.toString(), e);
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
		
		// Nothing to do if bad response
		if(response == null) return;
		
		if(mUseCache) {
			// Nothing to do if request has already been satisfied by cache
			if(response.optBoolean("cache_ok")) return;
			
			// Cache requests
			String action = response.optString("action");
			if(action.equals("get_user") && response.has("user_id")) {
				CacheManager.put(mContext, action, "user_id", response);
			} else if(action.equals("get_picture") && response.has("user_id")) {
				CacheManager.put(mContext, action, "user_id", response);
			} else if(action.equals("get_favorites")) {
				CacheManager.put(mContext, action, null, response);
			}
		}
		
		mListener.onDatabaseResponse(response);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		this.cancel(true);
	}
}
