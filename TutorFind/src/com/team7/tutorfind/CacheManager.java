package com.team7.tutorfind;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class CacheManager {
	public static final String TAG = "CacheManager";
	
	static void put(Context context, String type, String keyField, JSONObject obj) {
		if(obj == null || !obj.has(keyField)) return; // Make sure user is valid
		
		SharedPreferences pref = context.getSharedPreferences("cache", 0);
		SharedPreferences.Editor edit = pref.edit();
		String file = String.format(Locale.US, "%s:%s", type, obj.optString(keyField));
		edit.putString(file, obj.toString());
		edit.commit();
	}
	
	static JSONObject get(Context context, String type, String key, JSONObject obj) {
		if(obj.has(key)) {
			return get(context, type, obj.optInt(key));
		}
		return null;
	}
	
	static JSONObject get(Context context, String type, int key) {
		SharedPreferences pref = context.getSharedPreferences("cache", 0);
		String objStr = pref.getString(String.format(Locale.US, "%s:%d", type, key), null);
		
		if(objStr == null) return null;
		
		try {
			return new JSONObject(objStr);
		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	static void del(Context context, String type, String keyField, JSONObject request) {
		if(request.has(keyField)) {
			del(context, type, request.optInt(keyField));
		}
	}
	
	static void del(Context context, String type, int key) {
		SharedPreferences pref = context.getSharedPreferences("cache", 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.remove(String.format(Locale.US, "%s:%d", type, key));
		edit.commit();		
	}
}
