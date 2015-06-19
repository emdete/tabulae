package org.pyneo.tabulae.map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.ResourceProxy;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.drawing.OsmPath;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.geolocation.Locus;
import org.pyneo.tabulae.gui.Base;
import org.pyneo.tabulae.track.TrackGpxParser;

import java.io.File;

public class Map extends Base implements Constants {
	private MapView mapView;
	private boolean follow = true;
	private double latitude = 52;
	private double longitude = 7;
	private double accuracy = 0;
	private int zoom = 14;
	private Provider p = new Provider(); // TODO helps loading
	private IMyLocationConsumer myLocationConsumer;
	private IMyLocationProvider iMyLocationProvider = new IMyLocationProvider() {
		@Override
		public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
			if (DEBUG) Log.d(TAG, "Map..startLocationProvider");
			Map.this.myLocationConsumer = myLocationConsumer;
			return true;
		}

		@Override
		public void stopLocationProvider() {
			if (DEBUG) Log.d(TAG, "Map..stopLocationProvider");
			Map.this.myLocationConsumer = null;
		}

		@Override
		public Location getLastKnownLocation() {
			if (DEBUG) Log.d(TAG, "Map..getLastKnownLocation");
			Location last = new Location("gps");
			last.setLatitude(latitude);
			last.setLongitude(longitude);
			last.setAccuracy(1);
			return last;
		}
	};

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_zoom: {
				mapView.getController().setZoom(zoom);
			}
			break;
			case R.id.event_zoom_in:
				if (mapView.canZoomIn()) {
					extra = new Bundle();
					extra.putInt("zoom_level", ++zoom);
					((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
				}
				break;
			case R.id.event_zoom_out:
				if (mapView.canZoomOut()) {
					extra = new Bundle();
					extra.putInt("zoom_level", --zoom);
					((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
				}
				break;
			case R.id.location:
				if (follow) {
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
				break;
			case R.id.event_map_list: {
				mapView.setVisibility(View.INVISIBLE);
			}
			case R.id.event_autofollow: {
				follow = extra == null || extra.getBoolean("autofollow");
				if (follow) mapView.getController().setCenter(new GeoPoint(latitude, longitude));
			}
			break;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		if (DEBUG) {
			Log.d(TAG, "Map.onAttach");
		}
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) {
			Log.d(TAG, "Map.onCreate");
		}
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) {
			Log.d(TAG, "Map.onCreateView");
		}
		return inflater.inflate(R.layout.map, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.d(TAG, "Map.onActivityCreated");
		final float density = getActivity().getResources().getDisplayMetrics().density;
		mapView = (MapView)getActivity().findViewById(R.id.mapview);
		mapView.setMinZoomLevel(2);
		mapView.setBuiltInZoomControls(false);
		mapView.setMultiTouchControls(true);
		mapView.getController().setZoom(zoom);
		//zoom = mapView.getController().getZoom();
		mapView.getController().setCenter(new GeoPoint(latitude, longitude));
		mapView.setMapListener(new MapListener() {
			@Override
			public boolean onScroll(ScrollEvent event) {
				if (follow && event.getX() + event.getY() > 50 * density) {
					Bundle extra = new Bundle();
					extra.putBoolean("autofollow", false);
					//((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
				}
				return true;
			}

			@Override
			public boolean onZoom(ZoomEvent event) {
				Bundle extra = new Bundle();
				extra.putInt("zoom_level", event.getZoomLevel());
				((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
				return true;
			}
		});
		String ts = "BING: Satellite";
		if (TileSourceFactory.containsTileSource(ts)) {
			mapView.setTileSource(TileSourceFactory.getTileSource(ts));
		}
		if (true) { // dpi / sp scaling:
			//mapView.setTilesScaledToDpi(true);
			final float user_def = 1.3f; // TODO: where to get sp/dp?
			TileSystem.setTileSize((int)(mapView.getTileProvider().getTileSource().getTileSizePixels() * density * user_def));
		}
		if (false) { // rotation
			RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(getActivity(), mapView);
			mRotationGestureOverlay.setEnabled(false);
			mapView.getOverlays().add(mRotationGestureOverlay);
		}
		if (true) { // mylocation
			ResourceProxyImpl rp = new ResourceProxyImpl(getActivity());
			MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(iMyLocationProvider, mapView, rp);
			Bitmap bm = rp.getBitmap(ResourceProxy.bitmap.person);
			mLocationOverlay.setPersonHotspot(bm.getWidth() / 2, bm.getHeight() / 2);
			mLocationOverlay.setDrawAccuracyEnabled(true);
			mLocationOverlay.disableFollowLocation();
			mapView.getOverlays().add(mLocationOverlay);
			mLocationOverlay.enableMyLocation();
		}
		try {
			TrackGpxParser t = new TrackGpxParser(new File("/sdcard/tabulae/export/track46.gpx"));
			for (TrackGpxParser.TrackPoint p: t) {
				Log.d(TAG, "trkpt p=" + p);
			}
		}
		catch (Exception e) {
			Log.e(TAG, "e=" + e, e);
		}
		if (false) { // add a track overlay
			Overlay mPathOverlay;
			mPathOverlay = new Overlay(getActivity()) {
				private int mLastZoomLevel;
				private OsmPath mPath;
				protected Paint mPaint = new Paint();
				@Override protected void draw(Canvas c, MapView osmv, boolean shadow) {
					if (shadow) return;
					final Projection proj = osmv.getProjection();
					if (mPath == null || mLastZoomLevel != proj.getZoomLevel()) {
						mPath = createPath();
						mLastZoomLevel = proj.getZoomLevel();
					}
					mPath.onDrawCycle(proj); // adapt panning
					c.drawPath(mPath, mPaint);
				}
				private OsmPath createPath() {
					final OsmPath path = new OsmPath();
					final Projection proj = mapView.getProjection();
					Point p = null;
					p = proj.toPixels(new GeoPoint(52.0, 7.0), p);
					path.moveTo(p.x, p.y);
					path.lineTo(p.x, p.y);
					path.close();
					return path;
				}
			};
			mapView.getOverlayManager().add(mPathOverlay);
		}
	}
}

