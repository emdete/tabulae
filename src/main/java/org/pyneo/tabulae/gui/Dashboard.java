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
	DashboardItem[] dashboardItems;

	static class DashboardItem {
		int event;
		String value_key;
		TextView textView;

		DashboardItem(
			Activity activity,
			ViewGroup viewGroup,
			int event,
			String header,
			String value_key,
			String unit
			)
		{
			View item = (View)LayoutInflater.from(activity).inflate(R.layout.dashboard_item, viewGroup, false);
			((TextView)item.findViewById(R.id.data_header)).setText(header);
			((TextView)item.findViewById(R.id.data_unit)).setText(unit);
			viewGroup.addView(item);
			textView = ((TextView)item.findViewById(R.id.data_value));
			this.value_key = value_key;
			this.event = event;
			textView.setText("---");
		}
		void inform(int event, Bundle extra) {
			if (event == this.event && extra.containsKey(value_key)) {
				String value = extra.get(value_key).toString();
				if (value.length() > 8) {
					value = value.substring(0, 8);
				}
				textView.setText(value);
			}
		}
	}

	public void inform(int event, Bundle extra) {
		if (DEBUG) Log.d(TAG, "Dashboard.inform event=" + event + ", extra=" + extra);
		if (dashboardItems != null) {
			for (DashboardItem d: dashboardItems) {
				d.inform(event, extra);
			}
		}
		switch (event) {
			case R.id.event_dashboard:
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
		dashboardItems = new DashboardItem[]{
			new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_latitude), "latitude", getString(R.string.unit_degree)),
			new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_longitude), "longitude", getString(R.string.unit_degree)),
			//new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_provider), "provider", getString(R.string.unit_empty)),
			new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_accuracy), "accuracy", getString(R.string.unit_m)),
			new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_speed), "speed", getString(R.string.unit_kmh)),
			new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_satellite), "satellites", getString(R.string.unit_empty)),
			new DashboardItem(getActivity(), viewGroup, R.id.event_zoom, getString(R.string.title_zoom), "zoom_level", getString(R.string.unit_zoom)),
			};
	}
}
