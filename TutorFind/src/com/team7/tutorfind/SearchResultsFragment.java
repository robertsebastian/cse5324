package com.team7.tutorfind;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class SearchResultsFragment extends Fragment {
	public static final String TAG = "search_results_fragment";
	
	static SearchResultsFragment newInstance(JSONArray results) {
		SearchResultsFragment f = new SearchResultsFragment();
		
		// Add results argument
		Bundle args = new Bundle();
		args.putString("results", results.toString());
		f.setArguments(args);
		
		return f;
	}
	
	private static class UserSummaryArrayAdapter extends ArrayAdapter<JSONObject> {
		private final Context mContext;
		private final List<JSONObject> mUsers;
		
		public UserSummaryArrayAdapter(Context context, List<JSONObject> users) {
			super(context, R.layout.user_summary_row, users);
			mContext = context;
			mUsers = users;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.user_summary_row, parent, false);
			
			TextView name     = (TextView)rowView.findViewById(R.id.user_summary_name);
			TextView cost     = (TextView)rowView.findViewById(R.id.user_summary_price);
			TextView distance = (TextView)rowView.findViewById(R.id.user_summary_distance);
			RatingBar rating  = (RatingBar)rowView.findViewById(R.id.user_summary_score);
			
			JSONObject user = mUsers.get(position);			
			name.setText(user.optString("name"));
			cost.setText(String.format("$%.2f/hr", user.optDouble("price_per_hour", 999.0)));
			distance.setText(String.format("%.1f mi", user.optDouble("distance", 99.0)));
			rating.setRating((float)user.optDouble("score", 0.0));
			
			return rowView;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search_results, container, false);

	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		try {
			//TextView textView = (TextView)getView().findViewById(R.id.test_search_results);
			//textView.setText(getArguments().getString("results"));
			ListView list = (ListView)getView().findViewById(R.id.search_results_list);
			
			JSONArray resultArray = new JSONArray(getArguments().getString("results"));
			ArrayList<JSONObject> results = new ArrayList<JSONObject>();
			for(int i = 0; i < resultArray.length(); i++) {
				results.add(resultArray.getJSONObject(i));
			}
			
			SearchResultsFragment.UserSummaryArrayAdapter arrayAdapter = new SearchResultsFragment.UserSummaryArrayAdapter(getActivity(), results);
			//ArrayAdapter<JSONObject> arrayAdapter = new ArrayAdapter<JSONObject>(getActivity(),
			//           android.R.layout.simple_expandable_list_item_1, results);
			list.setAdapter(arrayAdapter);
		} catch(JSONException e) {
			Log.e("search", e.toString());
		}
	}
	
	public void onDestroy() {
		Log.d("search", "Search fragment destroyed");
		super.onDestroy();
	}
}
