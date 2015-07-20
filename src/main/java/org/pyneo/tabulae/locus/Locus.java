package org.pyneo.tabulae.locus;

import android.os.Bundle;
import android.util.Log;
import android.location.Location;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.Base;

public class Locus extends Base implements Constants {
	private ThreeStateLocationOverlay myLocationOverlay;

	@Override public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (DEBUG) Log.d(TAG, "Locus.onCreate");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		myLocationOverlay = new ThreeStateLocationOverlay(getActivity(), mapView.getModel().mapViewPosition) {
			@Override public void onLocationChanged(Location location) {
				super.onLocationChanged(location);
				((Tabulae)getActivity()).inform(R.id.location, ThreeStateLocationOverlay.toBundle(location));
			}
		};
		myLocationOverlay.setSnapToLocationEnabled(false);
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Locus.onResume");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().add(myLocationOverlay);
		myLocationOverlay.enable(false);
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Locus.onPause");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		myLocationOverlay.disable();
		mapView.getLayerManager().getLayers().remove(myLocationOverlay);
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Locus.inform event=" + event + ", extra=" + extra);
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
