package de.emdete.tabulae.track;

import de.emdete.tabulae.Constants;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import de.emdete.thinstore.StoreObject;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import de.emdete.tabulae.Base;
import de.emdete.tabulae.R;
import de.emdete.tabulae.Tabulae;
import android.database.sqlite.SQLiteDatabase;

public class Track extends Base {
	List<Layer> polylines = new ArrayList<>();

	@Override
	public void onCreate(Bundle bundle) {
		if (de.emdete.tabulae.Constants.DEBUG) Log.d(Constants.TAG, "Track.onCreate");
		super.onCreate(bundle);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Constants.DEBUG) Log.d(Constants.TAG, "Track.onResume");
		showVisibleTracks();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (Constants.DEBUG) Log.d(Constants.TAG, "Track.onPause");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		Layers layers = mapView.getLayerManager().getLayers();
		for (Layer polyline: polylines) {
			((AlternatingLine)polyline).getLatLongs().clear();
			layers.remove(polyline, false);
		}
		polylines.clear();
	}

	void showVisibleTracks() {
		if (Constants.DEBUG) Log.d(Constants.TAG, "Track.showVisibleTracks");
		try {
			MapView mapView = ((Tabulae) getActivity()).getMapView();
			Layers layers = mapView.getLayerManager().getLayers();
			if (Constants.DEBUG) Log.d(Constants.TAG, "showVisibleTracks layers.size=" + layers.size());
			for (Layer polyline: polylines) {
				((AlternatingLine)polyline).getLatLongs().clear();
				if (!layers.remove(polyline, false)) {
					Log.e(Constants.TAG, "showVisibleTracks remove did not remove layer?!?");
				}
			}
			polylines.clear();
			if (Constants.DEBUG) Log.d(Constants.TAG, "showVisibleTracks layers.size=" + layers.size());
			BoundingBox bb = null;
			try (final SQLiteDatabase db = ((Tabulae)getActivity()).getReadableDatabase()) {
				for (StoreObject item : StoreObject.query(db, TrackItem.class).where("visible").equal(true).fetchAll()) {
					// if (DEBUG) Log.d(TAG, "Track.showVisibleTracks item.description=" + ((TrackItem)item).getDescription());
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
						polylines.add(polyline);
					}
				}
			}
			layers.addAll(polylines);
			if (Constants.DEBUG) Log.d(Constants.TAG, "showVisibleTracks layers.size=" + layers.size());
			// mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
			// 	bb.getCenterPoint(),
			// 	LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(),
			// 	bb,
			// 	mapView.getModel().displayModel.getTileSize())));
			// mapView.getModel().mapViewPosition.setCenter(latLongs.get(0));
		}
		catch (Exception e) {
			Log.e(Constants.TAG, "Track.showVisibleTracks e=" + e, e);
		}
	}

	public void importGpx() {
		File[] gpxs = ((Tabulae) getActivity()).getGpxDir().listFiles();
		if (gpxs != null) for (File gpx : gpxs) {
			if (gpx.isFile() && gpx.toString().endsWith(".gpx")) {
				try {
					if (Constants.DEBUG) Log.d(Constants.TAG, "Track.inform import gpx=" + gpx);
					try (final SQLiteDatabase db = ((Tabulae)getActivity()).getWritableDatabase()) {
						TrackGpxParser track = new TrackGpxParser(gpx, db);
						if (StoreObject.query(db, TrackItem.class).where("name").equal(track.trackItem.getName()).count() == 0) {
							track.trackItem.insert(db);
							if (Constants.DEBUG) Log.d(Constants.TAG, "Track.inform stored name=" + track.trackItem.getName());
						}
					}
				}
				catch (Exception e) {
					Log.e(Constants.TAG, "Track.inform", e);
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
