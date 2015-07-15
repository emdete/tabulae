package org.pyneo.tabulae.geolocation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.gui.Base;
import org.pyneo.tabulae.map.Map;

public class Locus extends Base implements Constants {
	private ThreeStateLocationOverlay myLocationOverlay;

	@Override public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	@Override public void onStart() {
		super.onStart();
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		myLocationOverlay = new ThreeStateLocationOverlay(getActivity(), mapView.getModel().mapViewPosition);
		myLocationOverlay.setSnapToLocationEnabled(true);
		mapView.getLayerManager().getLayers().add(myLocationOverlay);
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
		myLocationOverlay.disable();
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume");
		myLocationOverlay.enable(true);
	}

	public void inform(int event, Bundle extra) {
		if (DEBUG) Log.d(TAG, "Locus.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_autofollow: {
				if (extra != null && extra.containsKey("autofollow")) {
					myLocationOverlay.setSnapToLocationEnabled(extra.getBoolean("autofollow"));
				}
				else { // just keypress, toggle:
					extra = new Bundle();
					extra.putBoolean("autofollow", !myLocationOverlay.isSnapToLocationEnabled());
					((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
				}
			}
			break;
		}
	}
}
