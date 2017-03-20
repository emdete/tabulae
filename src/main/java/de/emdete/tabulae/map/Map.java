package de.emdete.tabulae.map;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import de.emdete.tabulae.Constants;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.util.AndroidPreferences;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;
import de.emdete.tabulae.Base;
import de.emdete.tabulae.R;
import de.emdete.tabulae.Tabulae;

public class Map extends Base {
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	protected MapView mapView;
	protected int currentMap = -1;
	protected LayerBase layer;
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
			notifyMap();
			if (id != -1) {
				Editor editor = getPreferences().edit();
				editor.putInt("currentMap", currentMap);
				editor.commit();
			}
		}
		if (layer != null) {
			layer.setVisible(true);
			layer.onResume();
		}
	}

	@Override public void onCreate(Bundle bundle) {
		if (de.emdete.tabulae.Constants.DEBUG) Log.d(Constants.TAG, "Map.onCreate bundle=" + bundle);
		super.onCreate(bundle);
		// AndroidGraphicFactory.createInstance(getActivity().getApplication());
		mapView = new MapView(getActivity()) {
			@Override public boolean onTouchEvent(MotionEvent motionEvent) {
				if (snapToLocationEnabled) {
					notifyAutofollow(false);
				}
				return super.onTouchEvent(motionEvent);
			}
		};
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
		if (Constants.DEBUG) Log.d(Constants.TAG, "Map.onCreateView");
		return mapView;
	}

	static public void onCreateOptionsMenu(Tabulae activity) {
		activity.setVisible(R.id.event_do_map_vector, LayerMapsForge.isAvaiable(activity));
		activity.setVisible(R.id.event_do_map_openandromaps, LayerOpenAndroMaps.isAvaiable(activity));
		activity.setVisible(R.id.event_do_map_bing_satellite, LayerBingSat.isAvaiable(activity));
		activity.setVisible(R.id.event_do_map_google_satellite, LayerGoogleSat.isAvaiable(activity));
		activity.setVisible(R.id.event_do_map_mapquest, LayerMapQuest.isAvaiable(activity));
		activity.setVisible(R.id.event_do_map_outdoor_active, LayerOutdoorActive.isAvaiable(activity));
		activity.setVisible(R.id.event_do_map_wanderreitkarte, LayerWanderreitKarte.isAvaiable(activity));
	}

	@Override public void onResume() {
		super.onResume();
		currentMap = getPreferences().getInt("currentMap", 0);
		preferencesFacade = new AndroidPreferences(getPreferences());
		mapView.getModel().init(preferencesFacade);
		notifyZoom();
		notifyAutofollow(getPreferences().getBoolean("autoFollow", false));
		if (snapToLocationEnabled) { // if snapToLocationEnabled was on map-center is the last location
			lastLocation = mapView.getModel().mapViewPosition.getCenter();
		}
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

	void notifyMap() {
		if (layer != null) {
			notice(R.id.event_notify_map, "current_map", layer.getId());
		}
	}

	void notifyZoom() {
		notice(R.id.event_notify_zoom, "zoom_level", mapView.getModel().mapViewPosition.getZoomLevel());
	}

	void notifyAutofollow(boolean newValue) {
		if (newValue != snapToLocationEnabled) {
			snapToLocationEnabled = newValue;
			notice(R.id.event_notify_autofollow, "autofollow", snapToLocationEnabled);
			Editor editor = getPreferences().edit();
			editor.putBoolean("autoFollow", snapToLocationEnabled);
			editor.commit();
			if (snapToLocationEnabled) {
				centerIfFollow();
			}
		}
	}

	void centerIfFollow() {
		if (lastLocation != null && snapToLocationEnabled) {
			//if (DEBUG) Log.d(TAG, "Map.inform lastLocation=" + lastLocation);
			mapView.getModel().mapViewPosition.setCenter(lastLocation);
		}
	}

	private LatLong toLatLong(Bundle extra) {
		return new LatLong(extra.getDouble("latitude"), extra.getDouble("longitude"));
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_do_autofollow: {
				//if (DEBUG) Log.d(TAG, "Map.inform event=event_autofollow, extra=" + extra);
				notifyAutofollow(!snapToLocationEnabled);
			}
			break;
			case R.id.event_request_autofollow: {
				notice(R.id.event_notify_autofollow, "autofollow", snapToLocationEnabled);
			}
			break;
			case R.id.event_notify_location: {
				//if (DEBUG) Log.d(TAG, "Map.inform event=location, extra=" + extra);
				lastLocation = toLatLong(extra);
				//if (DEBUG) Log.d(TAG, "Map.inform lastLocation=" + lastLocation);
				centerIfFollow();
			}
			break;
			case R.id.event_do_zoom_in: {
				MapViewPosition mvp = mapView.getModel().mapViewPosition;
				mvp.setZoomLevel((byte) (mvp.getZoomLevel() + 1));
				notifyZoom();
			}
			break;
			case R.id.event_do_zoom_out: {
				MapViewPosition mvp = mapView.getModel().mapViewPosition;
				if (mvp.getZoomLevel() > 0) {
					mvp.setZoomLevel((byte) (mvp.getZoomLevel() - 1));
				}
				notifyZoom();
			}
			break;
			case R.id.event_request_zoom: {
				notifyZoom();
			}
			break;
			case R.id.event_request_map: {
				notifyMap();
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
}
