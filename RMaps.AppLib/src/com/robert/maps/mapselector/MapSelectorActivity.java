package com.robert.maps.mapselector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;

import com.robert.maps.applib.R;

public class MapSelectorActivity extends Activity {
	private ScrollView mScrollView; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapselector);
		
		mScrollView = (ScrollView) findViewById(R.id.GridInt);
	}
	
}
