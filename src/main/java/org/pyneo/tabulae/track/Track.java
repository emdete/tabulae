package org.pyneo.tabulae.track;

import android.location.Location;
import android.support.annotation.NonNull;
import android.widget.Toast;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.pyneo.tabulae.traffic.Traffic;
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

public class Track extends Base implements Constants {
	AlternatingLine polyline;
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(@NonNull Runnable r) {
			return new Thread(r, "inform");
		}
	});

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
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		SQLiteDatabase db = ((Tabulae)getActivity()).getDatabase();
		try {
			for (StoreObject item : StoreObject.query(((Tabulae)getActivity()).getDatabase(), TrackItem.class).where("visible").equal(true).fetchAll()) {
				TrackItem trackItem = (TrackItem)item;
				List<LatLong> latLongs = trackItem.getTrackLatLongs(db);
				polyline.setLatLongs(latLongs);
				//BoundingBox bb = new BoundingBox(latLongs);
				//mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(bb.getCenterPoint(), LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb, mapView.getModel().displayModel.getTileSize())));
				mapView.getModel().mapViewPosition.setCenter(latLongs.get(0));
				Bundle extra = new Bundle();
				extra.putBoolean("autofollow", false);
				((Tabulae) getActivity()).inform(R.id.event_set_autofollow, extra);
				if (DEBUG) return;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Track.showVisibleTracks e=" + e, e);
		}
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_do_track_list: {
				File[] gpxs = ((Tabulae) getActivity()).getGpxDir().listFiles();
				if (gpxs != null) for (File gpx : gpxs) {
					if (gpx.isFile() && gpx.toString().endsWith(".gpx")) {
						try {
							if (DEBUG) Log.d(TAG, "Track.inform import gpx=" + gpx);
							SQLiteDatabase db = ((Tabulae)getActivity()).getDatabase();
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
				showVisibleTracks();
			}
			break;
			case R.id.event_do_traffic:
				try {
					final File cache_dir = new File(((Tabulae) getActivity()).getBaseDir(), "cache");
					//noinspection ResultOfMethodCallIgnored
					cache_dir.mkdirs();
					mThreadPool.execute(new Runnable() {
						public void run() {
							try {
								Traffic.Incidents incidents = Traffic.go(cache_dir, null);
								for (Traffic.Incident incident: incidents) {
									TrackItem trackItem = new TrackItem(incident.getName(), incident.getDescription());
									Log.d(TAG, "incident=" + incident);
									for (Location position: incident.getPosition()) {
										trackItem.add(null, new TrackPointItem(position.getLatitude(), position.getLongitude()));
									}
									trackItem.insert(null);
								}
							}
							catch (Exception e) {
								Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
								Log.d(TAG, "traffic load e=" + e, e);
							}
						}
					});
				}
				catch (Exception e) {
					Log.d(TAG, "traffic load e=" + e);
				}
			break;
		}
	}
}
