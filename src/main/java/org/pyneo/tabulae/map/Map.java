package org.pyneo.tabulae.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.util.AndroidPreferences;
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
	protected LayerBase layer;
	protected SharedPreferences preferences;
	protected PreferencesFacade preferencesFacade;
	protected boolean snapToLocationEnabled;
	protected LatLong lastLocation;

	void activateLayer(int id) {
		if (layer == null || id != currentMap) {
			if (layer != null) {
				layer.onPause();
				layer.onDestroy();
				layer = null;
			}
			currentMap = id;
			switch (id) {
				case 0:
					layer = new LayerMapsForge((Tabulae) getActivity(), mapView);
					break;
				case 1:
					layer = new LayerOpenAndroMaps((Tabulae) getActivity(), mapView);
					break;
				case 2:
					layer = new LayerBingSat((Tabulae) getActivity(), mapView);
					break;
				case 3:
					layer = new LayerGoogleSat((Tabulae) getActivity(), mapView);
					break;
				case 4:
					layer = new LayerMapQuest((Tabulae) getActivity(), mapView);
					break;
				case 5:
					layer = new LayerOutdoorActive((Tabulae) getActivity(), mapView);
					break;
				case 6:
					layer = new LayerWanderreitKarte((Tabulae) getActivity(), mapView);
					break;
			}
			Bundle extra = new Bundle();
			if (layer != null) {
				extra.putString("current_map", layer.getId());
			}
			((Tabulae) getActivity()).inform(R.id.event_notify_map, extra);
			if (id != -1) {
				Editor editor = preferences.edit();
				editor.putInt("currentMap", currentMap); // TODO do not put a resource id into preferences
				editor.commit();
			}
		}
		if (layer != null) {
			layer.setVisible(true);
			layer.onResume();
		}
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Map.onCreate bundle=" + bundle);
		super.onCreate(bundle);
		// AndroidGraphicFactory.createInstance(getActivity().getApplication());
		mapView = new MapView(getActivity()) {
			@Override public boolean onTouchEvent(MotionEvent motionEvent) {
				if (snapToLocationEnabled) {
					Bundle extra = new Bundle();
					extra.putBoolean("autofollow", false);
					((Tabulae) getActivity()).inform(R.id.event_notify_autofollow, extra);
				}
				return super.onTouchEvent(motionEvent);
			}
		};
		preferences = getActivity().getSharedPreferences("map", Context.MODE_PRIVATE);
		mapView.setClickable(true);
		// TODO: consider mapView.setGestureDetector();?
		mapView.getMapScaleBar().setVisible(false);
		mapView.getModel().mapViewPosition.setZoomLevelMin((byte) 4);
		mapView.getModel().mapViewPosition.setZoomLevelMax((byte) 20);
		mapView.getModel();
		mapView.setBuiltInZoomControls(false);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		DisplayModel displayModel = mapView.getModel().displayModel;
		displayModel.setBackgroundColor(0xffbbbbbb);
		displayModel.setUserScaleFactor(1.5f);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.d(TAG, "Map.onCreateView");
		return mapView;
	}

	@Override public void onResume() {
		super.onResume();
		currentMap = preferences.getInt("currentMap", 0);
		preferencesFacade = new AndroidPreferences(preferences);
		mapView.getModel().init(preferencesFacade);
		announceZoom();
		snapToLocationEnabled = preferences.getBoolean("autoFollow", false);
		if (snapToLocationEnabled) { // if snapToLocationEnabled was on map-center is the last location
			lastLocation = mapView.getModel().mapViewPosition.getCenter();
		}
		Bundle extra = new Bundle();
		extra.putBoolean("autofollow", snapToLocationEnabled);
		((Tabulae) getActivity()).inform(R.id.event_notify_autofollow, extra);
		activateLayer(currentMap);
	}

	@Override public void onPause() {
		super.onPause();
		mapView.getModel().save(preferencesFacade);
		preferencesFacade.save();
		activateLayer(-1);
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
	}

	public MapView getMapView() {
		return mapView;
	}

	void announceZoom() {
		Bundle extra = new Bundle();
		extra.putInt("zoom_level", mapView.getModel().mapViewPosition.getZoomLevel());
		((Tabulae) getActivity()).inform(R.id.event_notify_zoom, extra);
	}

	void centerIfFollow() {
		if (lastLocation != null && snapToLocationEnabled) {
			//if (DEBUG) Log.d(TAG, "Map.inform lastLocation=" + lastLocation);
			mapView.getModel().mapViewPosition.setCenter(lastLocation);
		}
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_set_autofollow: {
				//if (DEBUG) Log.d(TAG, "Map.inform event=event_autofollow, extra=" + extra);
				extra = new Bundle();
				extra.putBoolean("autofollow", !snapToLocationEnabled);
				((Tabulae) getActivity()).inform(R.id.event_notify_autofollow, extra);
			}
			break;
			case R.id.event_notify_autofollow: {
				//if (DEBUG) Log.d(TAG, "Map.inform event=autofollow, extra=" + extra);
				boolean newValue = extra.getBoolean("autofollow");
				if (newValue != snapToLocationEnabled) {
					snapToLocationEnabled = newValue;
					Editor editor = preferences.edit();
					editor.putBoolean("autoFollow", snapToLocationEnabled);
					editor.commit();
					centerIfFollow();
				}
			}
			break;
			case R.id.event_notify_location: {
				//if (DEBUG) Log.d(TAG, "Map.inform event=location, extra=" + extra);
				lastLocation = toLatLong(extra);
				//if (DEBUG) Log.d(TAG, "Map.inform lastLocation=" + lastLocation);
				centerIfFollow();
			}
			break;
			case R.id.event_zoom_in: {
				MapViewPosition mvp = mapView.getModel().mapViewPosition;
				mvp.setZoomLevel((byte) (mvp.getZoomLevel() + 1));
				announceZoom();
			}
			break;
			case R.id.event_zoom_out: {
				MapViewPosition mvp = mapView.getModel().mapViewPosition;
				if (mvp.getZoomLevel() > 0) {
					mvp.setZoomLevel((byte) (mvp.getZoomLevel() - 1));
				}
				announceZoom();
			}
			break;
			case R.id.event_do_send_location: {
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
			break;
			case R.id.event_do_view_location: {
				final MapViewPosition mvp = mapView.getModel().mapViewPosition;
				final String label = "";
				//final byte zoom = mvp.getZoomLevel();
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
			break;
			case R.id.event_do_map_vector:
				activateLayer(0);
				break;
			case R.id.event_do_map_openandromaps:
				activateLayer(1);
				break;
			case R.id.event_do_map_bing_satellite:
				activateLayer(2);
				break;
			case R.id.event_do_map_google_satellite:
				activateLayer(3);
				break;
			case R.id.event_do_map_mapquest:
				activateLayer(4);
				break;
			case R.id.event_do_map_outdoor_active:
				activateLayer(5);
				break;
			case R.id.event_do_map_wanderreitkarte:
				activateLayer(6);
				break;
		}
	}

	private LatLong toLatLong(Bundle extra) {
		return new LatLong(extra.getDouble("latitude"), extra.getDouble("longitude"));
	}
}
