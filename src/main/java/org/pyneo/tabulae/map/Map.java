package org.pyneo.tabulae.map;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class Map extends Base implements Constants {
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	protected MapView mapView;
	protected int currentMap = -1;
	protected HashMap<Integer,LayerBase> layers = new HashMap<>();
	protected PreferencesFacade preferencesFacade;

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
		preferencesFacade = new AndroidPreferences(getActivity().getSharedPreferences("map", Context.MODE_PRIVATE));
		mapView.getModel().init(preferencesFacade);
		announceZoom();
		announceLocation();
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
		mapView.getModel().save(preferencesFacade);
		preferencesFacade.save();
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

	void announceLocation() {
		LatLong mvp = mapView.getModel().mapViewPosition.getCenter();
		Bundle extra = new Bundle();
		extra.putDouble("latitude", mvp.latitude);
		extra.putDouble("longitude", mvp.longitude);
		((Tabulae)getActivity()).inform(R.id.location, extra);
	}

	void announceZoom() {
		Bundle extra = new Bundle();
		extra.putInt("zoom_level", mapView.getModel().mapViewPosition.getZoomLevel());
		((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_zoom_in: {
				MapViewPosition mvp = mapView.getModel().mapViewPosition;
				mvp.setZoomLevel((byte)(mvp.getZoomLevel() + 1));
				announceZoom();
			}
			break;
			case R.id.event_zoom_out: {
				MapViewPosition mvp = mapView.getModel().mapViewPosition;
				mvp.setZoomLevel((byte)(mvp.getZoomLevel() - 1));
				announceZoom();
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
			case R.id.event_send_location: {
				final MapViewPosition mvp = mapView.getModel().mapViewPosition;
				final String label = "";
				final byte zoom = mvp.getZoomLevel();
				final LatLong latLong = mvp.getCenter();
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, label + '\n'
					+ "http://www.openstreetmap.org/?mlat=" + latLong.latitude
					+ "&mlon=" + latLong.longitude
					+ "#map=" + zoom
					+ '/' + latLong.latitude
					+ '/' + latLong.longitude
					+ "&layers=T"
					);
				startActivity(intent);
			}
			case R.id.event_view_location: {
				final MapViewPosition mvp = mapView.getModel().mapViewPosition;
				final String label = "";
				final byte zoom = mvp.getZoomLevel();
				final LatLong latLong = mvp.getCenter();
				final Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("geo:"
					+ latLong.latitude + ','
					+ latLong.longitude + "?q="
					+ latLong.latitude + ','
					+ latLong.longitude + '('
					+ label + ')'));
				startActivity(intent);
			}
		}
	}
}
