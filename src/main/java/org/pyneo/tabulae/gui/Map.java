package org.pyneo.tabulae.gui;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class Map extends Base implements Constants {
	private boolean visible = true;

	public void inform(int event, Bundle extra) {
		if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "Map.onAttach"); }
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "Map.onCreate"); }
		super.onCreate(bundle);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		return inflater.inflate(R.layout.map, container, false);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) { Log.d(TAG, "Map.onActivityCreated"); }
	}
}
