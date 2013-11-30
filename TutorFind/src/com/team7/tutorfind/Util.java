package com.team7.tutorfind;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;

public class Util {
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	/**
	 * Generate a value suitable for use in {@link #setId(int)}.
	 * This value will not collide with ID values generated at build time by aapt for R.id.
	 *
	 * @return a generated ID value
	 */
	public static int generateViewId() {
	    for (;;) {
	        final int result = sNextGeneratedId.get();
	        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	        int newValue = result + 1;
	        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	        if (sNextGeneratedId.compareAndSet(result, newValue)) {
	            return result;
	        }
	    }
	}
	
	public static void resetViewId() {
		sNextGeneratedId.set(1);
	}
	
	// Convert a base64 encoded picture string and convert it to a Bitmap
	public static Bitmap decodePicture(String picture) {
		byte[] pictureBytes = Base64.decode(picture.getBytes(), Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length);
	}
	
	public static void setGroupEnabled(ViewGroup group, boolean enabled){
	    for(int i = 0; i < group.getChildCount(); i++){
	       View child = group.getChildAt(i);
	       child.setEnabled(enabled);
	       if(child instanceof ViewGroup){ 
	          setGroupEnabled((ViewGroup)child, enabled);
	       }
	    }
	}
	
	public static int[][] parseAvailability(String availability) {
		String items[] = TextUtils.split(availability, ":");
		
		int result[][] = new int[items.length][2];
		for(int i = 0; i < items.length; i++) {
			String[] pair = TextUtils.split(items[i], ",");
			result[i][0] = Integer.decode(pair[0]);
			result[i][1] = Integer.decode(pair[1]);
		}

		return result;
	}
	
	public static String availabilityToString(int a, Context context) {
		String dayStr[]  = context.getResources().getStringArray(R.array.availability_days);
		String timeStr[] = context.getResources().getStringArray(R.array.availability_times);
		int dayMask[]    = context.getResources().getIntArray(R.array.availability_days_mask);
		int timeMask[]   = context.getResources().getIntArray(R.array.availability_times_mask);
		
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> outTimes = new ArrayList<String>();
		
		for(int d = 0; d < dayMask.length; d++) {
			for(int t = 0; t < timeMask.length; t++) {
				if((a & dayMask[d] & timeMask[t]) != (dayMask[d] & timeMask[t])) continue;

				a &= ~(dayMask[d] & timeMask[t]);
				outTimes.add(timeStr[t]);
			}
			
			if(outTimes.size() > 0) {
				result.add(String.format(Locale.US, "%s: %s", dayStr[d], TextUtils.join(", ", outTimes)));
				outTimes.clear();
			}
		}
		
		return TextUtils.join("\n", result);
	}
}
