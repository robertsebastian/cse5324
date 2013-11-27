package com.team7.tutorfind;

import android.os.Bundle;

public class ProfileViewActivity extends TutorFindActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_view);
		
        if (savedInstanceState == null) {
        	// TODO: Also handle user content to avoid another database request
            ProfileViewFragment f = ProfileViewFragment.create(
            		getIntent().getIntExtra("user_id", -1),
            		getIntent().getStringExtra("user"));
            getFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
        }
	}
}