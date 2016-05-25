package org.pyneo.tabulae.track;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.pyneo.thinstore.StoreObject;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
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
	List<AlternatingLine> polylines = new ArrayList<>();

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Track.onCreate");
		super.onCreate(bundle);
		polylines.add(new AlternatingLine(AndroidGraphicFactory.INSTANCE));
		showVisibleTracks();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Track.onResume");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		for (AlternatingLine polyline: polylines) {
			mapView.getLayerManager().getLayers().add(polyline);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Track.onPause");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		for (AlternatingLine polyline: polylines) {
			polyline.getLatLongs().clear();
			mapView.getLayerManager().getLayers().remove(polyline);
		}
	}

	void showVisibleTracks() {
		if (DEBUG) Log.d(TAG, "Track.showVisibleTracks");
		try {
			MapView mapView = ((Tabulae) getActivity()).getMapView();
			for (AlternatingLine polyline: polylines) {
				mapView.getLayerManager().getLayers().remove(polyline);
			}
			polylines.clear();
			BoundingBox bb = null;
			try (final SQLiteDatabase db = ((Tabulae)getActivity()).getReadableDatabase()) {
				for (StoreObject item : StoreObject.query(db, TrackItem.class).where("visible").equal(true).fetchAll()) {
					if (DEBUG) Log.d(TAG, "Track.showVisibleTracks item.description=" + ((TrackItem)item).getDescription());
					List<LatLong> latLongs = ((TrackItem)item).getTrackLatLongs(db);
					// if (DEBUG) Log.d(TAG, "Track.showVisibleTracks size=" + latLongs.size());
					if (latLongs.size() > 0) {
						if (bb == null) {
							bb = new BoundingBox(latLongs);
						}
						else {
							bb = bb.extendBoundingBox(new BoundingBox(latLongs));
						}
						AlternatingLine polyline = new AlternatingLine(AndroidGraphicFactory.INSTANCE);
						polyline.setLatLongs(latLongs);
						Bundle extra = new Bundle();
						extra.putBoolean("autofollow", false);
						((Tabulae) getActivity()).inform(R.id.event_do_autofollow, extra);
						mapView.getLayerManager().getLayers().add(polyline);
					}
				}
			}
			// mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
			// 	bb.getCenterPoint(),
			// 	LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(),
			// 	bb,
			// 	mapView.getModel().displayModel.getTileSize())));
			// mapView.getModel().mapViewPosition.setCenter(latLongs.get(0));
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
					try (final SQLiteDatabase db = ((Tabulae)getActivity()).getWritableDatabase()) {
						TrackGpxParser track = new TrackGpxParser(gpx, db);
						if (StoreObject.query(db, TrackItem.class).where("name").equal(track.trackItem.getName()).count() == 0) {
							track.trackItem.insert(db);
							if (DEBUG) Log.d(TAG, "Track.inform stored name=" + track.trackItem.getName());
						}
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
