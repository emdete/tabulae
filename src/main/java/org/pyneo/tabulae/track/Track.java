package org.pyneo.tabulae.track;

import org.pyneo.thinstore.StoreObject;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.util.List;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import android.database.sqlite.SQLiteDatabase;
import static org.pyneo.tabulae.track.Constants.*;

public class Track extends Base {
	AlternatingLine polyline;

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Track.onCreate");
		super.onCreate(bundle);
		polyline = new AlternatingLine(AndroidGraphicFactory.INSTANCE);
		// showVisibleTracks();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Track.onResume");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		mapView.getLayerManager().getLayers().add(polyline);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Track.onPause");
		polyline.getLatLongs().clear();
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		mapView.getLayerManager().getLayers().remove(polyline);
	}

	void showVisibleTracks() {
		if (DEBUG) Log.d(TAG, "Track.showVisibleTracks");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		SQLiteDatabase db = ((Tabulae)getActivity()).getReadableDatabase();
		try {
			for (StoreObject item : StoreObject.query(db, TrackItem.class).where("visible").equal(true).fetchAll()) {
				if (DEBUG) Log.d(TAG, "Track.showVisibleTracks item=" + item);
				TrackItem trackItem = (TrackItem)item;
				if (DEBUG) Log.d(TAG, "Track.showVisibleTracks size=" + trackItem.getTrackPointItems(db).size());
				List<LatLong> latLongs = trackItem.getTrackLatLongs(db);
				if (DEBUG) Log.d(TAG, "Track.showVisibleTracks size=" + latLongs.size());
				if (latLongs.size() > 0) {
					polyline.setLatLongs(latLongs);
					//BoundingBox bb = new BoundingBox(latLongs);
					//mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(bb.getCenterPoint(), LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb, mapView.getModel().displayModel.getTileSize())));
					mapView.getModel().mapViewPosition.setCenter(latLongs.get(0));
					Bundle extra = new Bundle();
					extra.putBoolean("autofollow", false);
					((Tabulae) getActivity()).inform(R.id.event_do_autofollow, extra);
					if (DEBUG) return;
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Track.showVisibleTracks e=" + e, e);
		}
	}

	public void importGpx() {
		File[] gpxs = ((Tabulae) getActivity()).getGpxDir().listFiles();
		if (gpxs != null) for (File gpx : gpxs) {
			if (gpx.isFile() && gpx.toString().endsWith(".gpx")) {
				try {
					if (DEBUG) Log.d(TAG, "Track.inform import gpx=" + gpx);
					SQLiteDatabase db = ((Tabulae)getActivity()).getWritableDatabase();
					TrackGpxParser track = new TrackGpxParser(gpx, db);
					if (StoreObject.query(db, TrackItem.class).where("name").equal(track.trackItem.getName()).count() == 0) {
						track.trackItem.insert(db);
						if (DEBUG) Log.d(TAG, "Track.inform stored name=" + track.trackItem.getName());
					}
				}
				catch (Exception e) {
					Log.e(TAG, "Track.inform", e);
				}
			}
		}
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_do_track_list: {
				showVisibleTracks();
			}
			break;
		}
	}
}
