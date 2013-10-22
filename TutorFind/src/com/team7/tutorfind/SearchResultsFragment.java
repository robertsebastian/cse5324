package com.team7.tutorfind;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SearchResultsFragment extends Fragment {
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search_results, container, false);
	}
	
	public void setText(String text) {
		TextView textView = (TextView)getView().findViewById(R.id.test_search_results);
		textView.setText(text);
	}
}
