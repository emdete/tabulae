package org.pyneo.tabulae.map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;

import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.layer.TileLayer;
import org.mapsforge.map.model.FrameBufferModel;
import org.mapsforge.map.model.common.Observer;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.MultiMapDataStore;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.pyneo.tabulae.geolocation.Locus;
import org.pyneo.tabulae.gui.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.track.TrackGpxParser;

public class Map extends Base implements Constants {
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private MapView mapView;
	private TileRendererLayer tileLayer;
	private TileCache tileCache;
	private boolean follow = true;
	private double latitude = 52.517037;
	private double longitude = 13.38886;
	private double accuracy = 0;
	private byte zoom = 14;
	FileSystemTileCache fsc;

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
			case R.id.location:
				if (extra != null && follow) {
					latitude = extra.getDouble("latitude", 0);
					longitude = extra.getDouble("longitude", 0);
					// mapView.getModel().mapViewPosition.setCenter(new LatLong(latitude, longitude));
					mapView.getModel().mapViewPosition.animateTo(new LatLong(latitude, longitude));
				}
			break;
			case R.id.event_map_list: {
				mapView.setVisibility(View.INVISIBLE);
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
		mapView = new MapView(getActivity());
		//
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(false);
		mapView.setBuiltInZoomControls(false);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		tileCache = AndroidUtil.createTileCache(getActivity(), "mapcache", mapView.getModel().displayModel.getTileSize(),
			1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
		final MapViewPosition mapViewPosition = mapView.getModel().mapViewPosition;
		mapViewPosition.addObserver(new Observer() {
			LatLong lastLatLong = null;
			byte lastZoom = -1;
			@Override public void onChange() {
				Bundle extra = new Bundle();
				LatLong currentLatLong = mapViewPosition.getCenter();
				if (!currentLatLong.equals(lastLatLong)) {
					extra.putSerializable("latlong", currentLatLong);
					//extra.putBoolean("autofollow", false);
					//((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
					lastLatLong = currentLatLong;
					if (DEBUG) Log.d(TAG, "Map.Observer.onChange " + extra);
				}
				byte currentZoom = mapViewPosition.getZoomLevel();
				if (lastZoom != currentZoom) {
					extra.putByte("zoom", currentZoom);
					//((Tabulae)getActivity()).inform(0, extra);
					lastZoom = currentZoom;
				}
			}
		});
		final FrameBufferModel frameBufferModel = mapView.getModel().frameBufferModel;
		frameBufferModel.addObserver(new Observer() {
			MapPosition lastMapPosition;
			Dimension lastDimension;
			@Override public void onChange() {
				Bundle extra = new Bundle();
				MapPosition currentMapPosition = frameBufferModel.getMapPosition();
				if (currentMapPosition != null && !currentMapPosition.equals(lastMapPosition)) {
					extra.putSerializable("position", currentMapPosition);
					lastMapPosition = currentMapPosition;
					if (DEBUG) Log.d(TAG, "Map.Observer.onChange " + extra);
				}
				Dimension currentDimension = frameBufferModel.getDimension();
				if (currentDimension != null && !currentDimension.equals(lastDimension)) {
					extra.putSerializable("dimension", currentDimension);
					lastDimension = currentDimension;
				}
			}
		});
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
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE);
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore, mapView.getModel().mapViewPosition,
			false, true, AndroidGraphicFactory.INSTANCE);
		File mapsDir = ((Tabulae)getActivity()).getMapsDir();
		File[] maps = mapsDir.listFiles();
		if (maps != null) for (File map: maps) {
			Log.d(TAG, "map=" + map);
			// TODO use boundingBox of MapFile to sort the files?
			multiMapDataStore.addMapDataStore(new MapFile(map), true, true);
		}
		tileLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		mapView.getLayerManager().getLayers().add(tileLayer);
	}

	@Override public void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
		mapView.getLayerManager().getLayers().remove(tileLayer);
		tileLayer.onDestroy();
	}

	@Override public void onDestroy() {
		super.onDestroy();
		if (DEBUG) { Log.d(TAG, "Map.onDestroy"); }
		tileCache.destroy();
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();
	}

	public MapView getMapView() {
		return mapView;
	}
}

