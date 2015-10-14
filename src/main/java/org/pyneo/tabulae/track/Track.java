package org.pyneo.tabulae.track;

import android.os.Bundle;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

import java.io.File;
import java.util.List;

import co.uk.rushorm.core.RushSearch;

public class Track extends Base implements Constants {
	AlternatingLine polyline;

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Track.onCreate");
		super.onCreate(bundle);
		polyline = new AlternatingLine(AndroidGraphicFactory.INSTANCE);
		// showVisibleTracks();
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Track.onResume");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().add(polyline);
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Track.onPause");
		polyline.getLatLongs().clear();
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().remove(polyline);
	}

	void showVisibleTracks() {
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		for (TrackItem trackItem: new RushSearch().whereEqual("visible", true).find(TrackItem.class)) {
			List<LatLong> latLongs = trackItem.getTrackLatLongs();
			polyline.setLatLongs(latLongs);
			//BoundingBox bb = new BoundingBox(latLongs);
			//mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(bb.getCenterPoint(), LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb, mapView.getModel().displayModel.getTileSize())));
			mapView.getModel().mapViewPosition.setCenter(latLongs.get(0));
			Bundle extra = new Bundle();
			extra.putBoolean("autofollow", false);
			((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
			return;
		}
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_track_list: {
				File[] gpxs = ((Tabulae)getActivity()).getGpxDir().listFiles();
				if (gpxs != null) for (File gpx: gpxs) {
					if (gpx.isFile() && gpx.toString().endsWith(".gpx")) {
						try {
							if (DEBUG) Log.d(TAG, "Track.inform import gpx=" + gpx);
							TrackGpxParser track = new TrackGpxParser(gpx);
							if (new RushSearch().whereEqual("name", track.trackItem.getName()).count(TrackItem.class) == 0) {
								track.trackItem.save();
								if (DEBUG) Log.d(TAG, "Track.inform stored name=" + track.trackItem.getName());
							}
						}
						catch (Exception e) {
							Log.e(TAG, "Track.inform", e);
						}
					}
				}
				showVisibleTracks();
			}
			break;
		}
	}
}
