package com.team7.tutorfind;

import org.json.JSONArray;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SearchResultsFragment extends Fragment {
	static SearchResultsFragment newInstance(JSONArray results) {
		SearchResultsFragment f = new SearchResultsFragment();
		
		// Add results argument
		Bundle args = new Bundle();
		args.putString("results", results.toString());
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search_results, container, false);
	}
	
	@Override
	public void onViewCreated (View view, Bundle savedInstanceState) {
		TextView textView = (TextView)getView().findViewById(R.id.test_search_results);
		textView.setText(getArguments().getString("results"));
	}
	
	public void onDestroy() {
		Log.d("search", "Search fragment destroyed");
		super.onDestroy();
	}
}
