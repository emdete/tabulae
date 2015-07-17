package org.pyneo.tabulae.poi;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.view.MapView;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.Base;

public class Poi extends Base implements Constants {
	protected boolean visible = true;
	protected Marker marker;

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Poi.onCreate");
		super.onCreate(bundle);
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Poi.onResume");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Poi.onPause");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		if (marker != null) {
			mapView.getLayerManager().getLayers().remove(marker);
			marker.onDestroy();
			marker = null;
		}
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Poi.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_poi_list: {
				MapView mapView = ((Tabulae)getActivity()).getMapView();
				if (marker != null) {
					mapView.getLayerManager().getLayers().remove(marker);
					marker.onDestroy();
					marker = null;
				}
				else {
					LatLong latLong = new LatLong(51.18199624, 6.20537151);
					Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.poi_black, null));
					marker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2) {
						@Override public boolean onTap(LatLong geoPoint, Point viewPosition, Point tapPoint) {
							if (contains(viewPosition, tapPoint)) {
								Toast.makeText(getActivity(), "262.02.1.2", Toast.LENGTH_SHORT).show();
								return true;
							}
							return false;
						}
					};
					mapView.getLayerManager().getLayers().add(marker);
					extra = new Bundle();
					extra.putBoolean("autofollow", false);
					((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
					mapView.getModel().mapViewPosition.animateTo(latLong);
				}
			}
			break;
		}
	}
}
