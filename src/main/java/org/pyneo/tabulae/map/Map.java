package org.pyneo.tabulae.map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class Map extends Base implements Constants {
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private MapView mapView;
	private int currentMap = -1;
	private HashMap<Integer,LayerBase> layers = new HashMap<>();

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
		// TODO: consider mapView.setGestureDetector();?
		mapView.getMapScaleBar().setVisible(false);
		mapView.getModel().mapViewPosition.setZoomLevelMin((byte) 4);
		mapView.getModel().mapViewPosition.setZoomLevelMax((byte) 20);
		mapView.setBuiltInZoomControls(false);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		DisplayModel displayModel = mapView.getModel().displayModel;
		displayModel.setBackgroundColor(0xffbbbbbb);
		displayModel.setUserScaleFactor(1.5f);
		//layers.put(R.id.event_map_vector, new LayerMapsForge((Tabulae) getActivity(), mapView));
		layers.put(R.id.event_map_openandromaps, new LayerOpenAndroMaps((Tabulae) getActivity(), mapView));
		//layers.put(R.id.event_map_bing_satellite, new LayerBingSat((Tabulae) getActivity(), mapView));
		//layers.put(R.id.event_map_google_satellite, new LayerGoogleSat((Tabulae) getActivity(), mapView));
		//layers.put(R.id.event_map_mapquest, new LayerMapQuest((Tabulae) getActivity(), mapView));
		//layers.put(R.id.event_map_outdoor_active, new LayerOutdoorActive((Tabulae) getActivity(), mapView));
		currentMap = R.id.event_map_openandromaps;
		layers.get(currentMap).setVisible(true);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		return mapView;
	}

	@Override public void onResume() {
		super.onResume();
		for (LayerBase layerB : layers.values()) {
			layerB.onResume();
		}
	}

	@Override public void onPause() {
		super.onPause();
		for (LayerBase layerB : layers.values()) {
			layerB.onPause();
			layerB.onDestroy();
		}
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
	}

	public MapView getMapView() {
		return mapView;
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_zoom_in: {
				MapViewPosition mvp = mapView.getModel().mapViewPosition;
				byte zoom = mvp.getZoomLevel();
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
				byte zoom = mvp.getZoomLevel();
				zoom--;
				mvp.setZoomLevel(zoom);
				zoom = mvp.getZoomLevel();
				extra = new Bundle();
				extra.putInt("zoom_level", zoom);
				((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
			}
			break;
			case R.id.event_map_vector:
			case R.id.event_map_openandromaps:
			case R.id.event_map_bing_satellite:
			case R.id.event_map_google_satellite:
			case R.id.event_map_mapquest:
			case R.id.event_map_outdoor_active: {
				layers.get(currentMap).setVisible(false);
				currentMap = event;
				layers.get(currentMap).setVisible(true);
			}
			break;
		}
	}
}
