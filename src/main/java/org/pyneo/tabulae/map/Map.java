package org.pyneo.tabulae.map;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.events.MapListener;
import org.osmdroid.ResourceProxy;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.geolocation.Locus;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.map.Provider;
import org.pyneo.tabulae.gui.Base;

public class Map extends Base implements Constants {
	private int zoom = 14;
	MapView mapView;
	boolean follow = true;
	double latitude = 52;
	double longitude = 7;
	double accuracy = 0;
	Provider p = new Provider(); // TODO helps loading
	IMyLocationProvider iMyLocationProvider = new IMyLocationProvider() {
		@Override public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
			if (DEBUG) Log.d(TAG, "Map..startLocationProvider");
			Map.this.myLocationConsumer = myLocationConsumer;
			return true;
		}
		@Override public void stopLocationProvider() {
			if (DEBUG) Log.d(TAG, "Map..stopLocationProvider");
			Map.this.myLocationConsumer = null;
		}
		@Override public Location getLastKnownLocation() {
			if (DEBUG) Log.d(TAG, "Map..getLastKnownLocation");
			Location last = new Location("gps");
			last.setLatitude(latitude);
			last.setLongitude(longitude);
			last.setAccuracy(1);
			return last;
		}
	};
	IMyLocationConsumer myLocationConsumer;

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_zoom_in: {
				zoom++;
			}
			break;
			case R.id.event_zoom_out: {
				zoom--;
			}
			break;
			case R.id.location: if (follow) {
				latitude = extra.getDouble("latitude", 0);
				longitude = extra.getDouble("longitude", 0);
				accuracy = extra.getDouble("accuracy", 0);
				mapView.getController().setCenter(new GeoPoint(latitude, longitude));
			}
			if (myLocationConsumer != null) {
				Location current = Locus.toLocation(extra);
				if (DEBUG) Log.d(TAG, "Map.inform current=" + current);
				myLocationConsumer.onLocationChanged(current, iMyLocationProvider);
			}
			return;
			case R.id.event_autofollow:
				follow = !follow;
				if (follow) {
					mapView.getController().setCenter(new GeoPoint(latitude, longitude));
				}
			return;
			default: return;
			// mapView.scrollTo();
		}
		mapView.getController().setZoom(zoom);
		zoom = mapView.getController().setZoom(zoom);
		extra = new Bundle();
		extra.putInt("zoom_level", zoom);
		((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "Map.onAttach"); }
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "Map.onCreate"); }
		super.onCreate(bundle);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		return inflater.inflate(R.layout.map, container, false);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.d(TAG, "Map.onActivityCreated");
		mapView = (MapView)getActivity().findViewById(R.id.mapview);
		mapView.setMinZoomLevel(2);
		mapView.setBuiltInZoomControls(false);
		mapView.setMultiTouchControls(true);
		mapView.getController().setZoom(zoom);
		mapView.getController().setCenter(new GeoPoint(latitude, longitude));
		mapView.setMapListener(new MapListener() {
			@Override
			public boolean onScroll(ScrollEvent event) {
				return true;
			}
			@Override
			public boolean onZoom(ZoomEvent event) {
				Bundle extra = new Bundle();
				extra.putInt("zoom_level", event.getZoomLevel());
				((Tabulae) getActivity()).inform(R.id.event_zoom, extra);
				return true;
			}
		});
		String ts = "BING: Satellite";
		if (TileSourceFactory.containsTileSource(ts)) {
			mapView.setTileSource(TileSourceFactory.getTileSource(ts));
		}
		if (false) {
			mapView.setTilesScaledToDpi(true);
		}
		else {
			final float density = getActivity().getResources().getDisplayMetrics().density;
			final float user_def = 1.3f; // TODO: where to get sp/dp?
			TileSystem.setTileSize((int) (mapView.getTileProvider().getTileSource().getTileSizePixels() * density * user_def));
		}
		if (false) { // rotation
			RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(getActivity(), mapView);
			mRotationGestureOverlay.setEnabled(false);
			mapView.getOverlays().add(mRotationGestureOverlay);
		}
		if (true) { // location
			MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(getActivity(), iMyLocationProvider, mapView);
			mapView.getOverlays().add(mLocationOverlay);
			mLocationOverlay.enableMyLocation();
		}
	}
}
