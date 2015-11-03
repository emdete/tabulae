package org.pyneo.tabulae.gui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;

public class Dashboard extends Base implements Constants {
	protected DashboardItem[] dashboardItems;
	protected boolean enabled = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.d(TAG, "Dashboard.onCreateView");
		return inflater.inflate(R.layout.dashboard, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.d(TAG, "Dashboard.onActivityCreated");
		ViewGroup viewGroup = (ViewGroup) getActivity().findViewById(R.id.dashboard).findViewById(R.id.dashboard_list);
		viewGroup.removeAllViews();
		dashboardItems = new DashboardItem[]{
				new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_provider), "provider", getString(R.string.unit_empty)),
				new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_satellite), "satellites", getString(R.string.unit_empty)),
				new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_bearing), "bearing", getString(R.string.unit_degree)),
				new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_accuracy), "accuracy", getString(R.string.unit_m)),
				new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_speed), "speed", getString(R.string.unit_kmh)),
				new DashboardItem(getActivity(), viewGroup, R.id.event_zoom, getString(R.string.title_zoom), "zoom_level", getString(R.string.unit_zoom)),
				new DashboardItem(getActivity(), viewGroup, R.id.cell_ident, "Cell Identifier", "cell_ident", ""),
				new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_latitude), "latitude", getString(R.string.unit_degree)),
				new DashboardItem(getActivity(), viewGroup, R.id.location, getString(R.string.title_longitude), "longitude", getString(R.string.unit_degree)),
				new DashboardItem(getActivity(), viewGroup, R.id.event_current_map, getString(R.string.title_current_map), "current_map", getString(R.string.unit_empty)),
				new DashboardItem(getActivity(), viewGroup, R.id.event_current_time, getString(R.string.title_current_time), "current_time", getString(R.string.unit_empty)),
		};
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Dashboard.inform event=" + event + ", extra=" + extra);
		if (dashboardItems != null) {
			for (DashboardItem d : dashboardItems) {
				d.inform(event, extra);
			}
		}
		switch (event) {
			case R.id.event_dashboard:
				getActivity().findViewById(R.id.dashboard_list).setVisibility(enabled ? View.GONE : View.VISIBLE);
				enabled = !enabled;
				break;
		}
	}

	static class DashboardItem {
		int event;
		String value_key;
		TextView textView;
		Activity activity;

		DashboardItem(Activity activity, ViewGroup viewGroup, int event, String header, String value_key, String unit) {
			View item = LayoutInflater.from(activity).inflate(R.layout.dashboard_item, viewGroup, false);
			((TextView) item.findViewById(R.id.data_header)).setText(header);
			((TextView) item.findViewById(R.id.data_unit)).setText(unit);
			viewGroup.addView(item);
			textView = ((TextView) item.findViewById(R.id.data_value));
			this.value_key = value_key;
			this.event = event;
			this.activity = activity;
			textView.setText("---");
		}

		void inform(int event, Bundle extra) {
			if (event == this.event && extra != null && extra.containsKey(value_key)) {
				Object temp = extra.get(value_key);
				if (temp == null) {
					temp = "---";
				}
				String value = temp.toString();
				if (value.length() > 8) {
					value = value.substring(0, 8);
				}
				final String v = value;
				activity.runOnUiThread(new Runnable() {
					public void run() {
						textView.setText(v);
					}
				});
			}
		}
	}
}
