package com.team7.tutorfind;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class CacheManager {
	public static final String TAG = "CacheManager";
	
	// Add an entry to the cache
	static void put(Context context, String type, String keyField, JSONObject obj) {
		if(obj == null || (keyField != null && !obj.has(keyField))) return; // Make sure user is valid
		
		SharedPreferences pref = context.getSharedPreferences("cache", 0);
		SharedPreferences.Editor edit = pref.edit();
		String file = keyField == null ? type :
				String.format(Locale.US, "%s:%s", type, obj.optString(keyField));
		edit.putString(file, obj.toString());
		edit.commit();
	}
	
	// Get an entry from the cache
	static JSONObject get(Context context, String type, String key, JSONObject obj) {
		if(key == null) {
			return get(context, type, -1);
		} else if(obj.has(key)) {
			return get(context, type, obj.optInt(key));
		}
		return null;
	}
	
	// Get an entry from the cache
	static JSONObject get(Context context, String type, int key) {
		SharedPreferences pref = context.getSharedPreferences("cache", 0);
		String objStr = key == -1 ? pref.getString(type, null) :
				pref.getString(String.format(Locale.US, "%s:%d", type, key), null);
		
		if(objStr == null) return null;
		
		try {
			return new JSONObject(objStr);
		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	// Delete a stale cache entry
	static void del(Context context, String type, String keyField, JSONObject request) {
		if(keyField == null) {
			del(context, type, -1);
		} else if(request.has(keyField)) {
			del(context, type, request.optInt(keyField));
		}
	}
	
	// Delete a stale cache entry
	static void del(Context context, String type, int key) {
		SharedPreferences pref = context.getSharedPreferences("cache", 0);
		SharedPreferences.Editor edit = pref.edit();
		String file = key == -1 ? type : String.format(Locale.US, "%s:%d", type, key);
		edit.remove(file);
		edit.commit();		
	}
}
