package org.pyneo.tabulae.track;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.List;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.R;

public class Track extends Base implements Constants {
	@SuppressLint("SdCardPath")
	AlternatingLine polyline;

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_track_list: {
				try {
					List<LatLong> latLongs = polyline.getLatLongs();
					if (latLongs.isEmpty()) {
						double minLatitude = 222;
						double minLongitude = 222;
						double maxLatitude = -222;
						double maxLongitude = -222;
						for (TrackGpxParser.TrackPoint trackPoint : new TrackGpxParser(
							new File(((Tabulae)getActivity()).getGpxDir(), "sample.gpx"))) {
							//if (DEBUG) Log.d(TAG, "Track.inform trackPoint=" + trackPoint);
							if (trackPoint.latitude > maxLatitude) maxLatitude = trackPoint.latitude;
							if (trackPoint.latitude < minLatitude) minLatitude = trackPoint.latitude;
							if (trackPoint.longitude > maxLongitude) maxLongitude = trackPoint.longitude;
							if (trackPoint.longitude < minLongitude) minLongitude = trackPoint.longitude;
							latLongs.add(trackPoint);
						}
						MapView mapView = ((Tabulae)getActivity()).getMapView();
						BoundingBox bb = new BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
						//if (DEBUG) Log.d(TAG, "Track.inform bb=" + bb);
						extra = new Bundle();
						extra.putBoolean("autofollow", false);
						((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
						mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
							bb.getCenterPoint(),
							LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb,
							mapView.getModel().displayModel.getTileSize())));
					}
					else {
						latLongs.clear();
					}
				}
				catch (Exception e) {
					Log.e(TAG, "Map.inform", e);
				}
			}
			break;
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Track.onCreate");
		super.onCreate(bundle);
		polyline = new AlternatingLine(AndroidGraphicFactory.INSTANCE);
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Track.onResume");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().add(polyline);
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
		polyline.getLatLongs().clear();
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().remove(polyline);
	}
}
