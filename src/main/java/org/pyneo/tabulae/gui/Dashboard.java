package org.pyneo.tabulae.gui;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class Dashboard extends Base implements Constants {
	private boolean visible = true;

	public void inform(int event, Bundle extra) {
		if (DEBUG) Log.d(TAG, "Dashboard.inform event=" + event);
		switch (event) {
			case R.id.event_autofollow:
				getActivity().findViewById(R.id.dashboard_list).setVisibility(visible? View.GONE: View.VISIBLE);
				visible = !visible;
			break;
		}
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) Log.d(TAG, "Dashboard.onAttach");
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Dashboard.onCreate");
		super.onCreate(bundle);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.d(TAG, "Dashboard.onCreateView");
		return inflater.inflate(R.layout.dashboard, container, false);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.d(TAG, "Dashboard.onActivityCreated");
		ViewGroup viewGroup = (ViewGroup)getActivity().findViewById(R.id.dashboard).findViewById(R.id.dashboard_list);
		viewGroup.removeAllViews();
		for (int i=0;i<3;i++) {
			View item = (View)LayoutInflater.from(getActivity()).inflate(R.layout.dashboard_item, viewGroup, false);
			((TextView)item.findViewById(R.id.data_header)).setText("Speed");
			((TextView)item.findViewById(R.id.data_value)).setText("52." + i);
			((TextView)item.findViewById(R.id.data_unit)).setText("km/h");
			viewGroup.addView(item);
		}
	}
}
