package com.team7.tutorfind;

import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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
	
	// Convert a base64 encoded picture string and convert it to a Bitmap
	public static Bitmap decodePicture(String picture) {
		byte[] pictureBytes = Base64.decode(picture.getBytes(), Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length);
	}
}
