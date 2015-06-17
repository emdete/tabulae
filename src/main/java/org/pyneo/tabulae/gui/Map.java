package org.pyneo.tabulae.gui;

import android.graphics.Color;
import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;

import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class Map extends Base implements Constants {
	private int zoom = 14;
	MapView mapView;
	boolean follow = true;
	double latitude = 52;
	double longitude = 7;
	double accuracy = 0;

	public void inform(int event, Bundle extra) {
		if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
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
		if (DEBUG) { Log.d(TAG, "Map.onActivityCreated"); }
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
		// Add tiles layer with custom tile source
		//final ITileSource tileSource = new ParedTileSource("FietsRegionaal", null, 3, 18, 256, new String[]{"http://overlay.openstreetmap.nl/openfietskaart-rcn/"});
		//mapView.setTileSource(tileSource);
		// scale to sp:
		if (false) {
			mapView.setTilesScaledToDpi(true);
		}
		else {
			final float density = getActivity().getResources().getDisplayMetrics().density;
			final float user_def = 1.3f; // TODO: where to get sp/dp?
			TileSystem.setTileSize((int) (mapView.getTileProvider().getTileSource().getTileSizePixels() * density * user_def));
		}
	}
}
