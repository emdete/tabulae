package org.pyneo.tabulae.map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.pyneo.tabulae.gui.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class Map extends Base implements Constants {
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private MapView mapView;
	private int currentMap = R.id.event_map_vector;
	private HashMap<Integer,LayerB> layers = new HashMap<>();
	private boolean follow = true;
	private double latitude = 52.517037;
	private double longitude = 13.38886;
	private byte zoom = 14;

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_zoom_in: {
					MapViewPosition mvp = mapView.getModel().mapViewPosition;
					zoom = mvp.getZoomLevel();
					zoom++;
					mvp.setZoomLevel(zoom);
					zoom = mvp.getZoomLevel();
					extra = new Bundle();
					extra.putInt("zoom_level", zoom);
					((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
				}
			break;
			case R.id.event_zoom_out: {
					MapViewPosition mvp = mapView.getModel().mapViewPosition;
					zoom = mvp.getZoomLevel();
					zoom--;
					mvp.setZoomLevel(zoom);
					zoom = mvp.getZoomLevel();
					extra = new Bundle();
					extra.putInt("zoom_level", zoom);
					((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
				}
			break;
			case R.id.event_map_list: {
				inform(R.id.event_map_outdooractive, null);
			}
			break;
			case R.id.event_map_vector:
			case R.id.event_map_bing_satellite:
			case R.id.event_map_google_satellite:
			case R.id.event_map_mapquest:
			case R.id.event_map_outdooractive: {
				layers.get(currentMap).setVisible(false);
				currentMap = event;
				layers.get(currentMap).setVisible(true);
			}
			break;
		}
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "Map.onAttach"); }
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "Map.onCreate"); }
		super.onCreate(bundle);
		AndroidGraphicFactory.createInstance(getActivity().getApplication());
		mapView = new MapView(getActivity()) {
			@Override public boolean onTouchEvent(MotionEvent motionEvent) {
				Bundle extra = new Bundle();
				extra.putBoolean("autofollow", false);
				((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
				return super.onTouchEvent(motionEvent);
			}
		};
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(false);
		mapView.getModel().mapViewPosition.setZoomLevelMin((byte) 4);
		mapView.getModel().mapViewPosition.setZoomLevelMax((byte) 20);
		mapView.setBuiltInZoomControls(false);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		DisplayModel displayModel = mapView.getModel().displayModel;
		displayModel.setBackgroundColor(0xffbbbbbb);
		displayModel.setUserScaleFactor(1.5f);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		return mapView;
	}

	@Override public void onStart() {
		super.onStart();
		if (DEBUG) { Log.d(TAG, "Map.onStart"); }
		mapView.getModel().mapViewPosition.setCenter(new LatLong(latitude, longitude));
		mapView.getModel().mapViewPosition.setZoomLevel(zoom);
		layers.put(R.id.event_map_vector, new LayerV((Tabulae) getActivity(), mapView));
		layers.put(R.id.event_map_bing_satellite, new LayerBingSat((Tabulae) getActivity(), mapView));
		layers.put(R.id.event_map_google_satellite, new LayerGoogleSat((Tabulae) getActivity(), mapView));
		layers.put(R.id.event_map_mapquest, new LayerMapQuest((Tabulae) getActivity(), mapView));
		layers.put(R.id.event_map_outdooractive, new LayerOutdoorActive((Tabulae) getActivity(), mapView));
		layers.get(currentMap).setVisible(true);
	}

	@Override public void onPause() {
		super.onPause();
		for (LayerB layerB : layers.values()) {
			layerB.onPause();
		}
	}

	@Override public void onResume() {
		super.onResume();
		for (LayerB layerB : layers.values()) {
			layerB.onResume();
		}
	}

	@Override public void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
		for (LayerB layerB : layers.values()) {
			layerB.onDestroy();
		}
	}

	@Override public void onDestroy() {
		super.onDestroy();
		if (DEBUG) { Log.d(TAG, "Map.onDestroy"); }
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();
	}

	public MapView getMapView() {
		return mapView;
	}
}
