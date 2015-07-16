package org.pyneo.tabulae.track;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.layer.TileLayer;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.MultiMapDataStore;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.pyneo.tabulae.gui.Base;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.R;

public class Track extends Base implements Constants {
	private static final String GPXFILE = "/sdcard/tabulae/export/track50.gpx";
	AlternatingLine polyline;

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_track_list: {
				try {
					List<LatLong> latLongs = polyline.getLatLongs();
					if (latLongs.isEmpty()) {
						double minLatitude = 222;
						double minLongitude = 222;
						double maxLatitude = -222;
						double maxLongitude = -222;
						for (TrackGpxParser.TrackPoint trackPoint : new TrackGpxParser(new File(GPXFILE))) {
							//if (DEBUG) Log.d(TAG, "Track.inform trackPoint=" + trackPoint);
							if (trackPoint.latitude > maxLatitude) maxLatitude = trackPoint.latitude;
							if (trackPoint.latitude < minLatitude) minLatitude = trackPoint.latitude;
							if (trackPoint.longitude > maxLongitude) maxLongitude = trackPoint.longitude;
							if (trackPoint.longitude < minLongitude) minLongitude = trackPoint.longitude;
							latLongs.add(trackPoint);
						}
						MapView mapView = ((Tabulae)getActivity()).getMapView();
						BoundingBox bb = new BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
						//if (DEBUG) Log.d(TAG, "Track.inform bb=" + bb);
						extra = new Bundle();
						extra.putBoolean("autofollow", false);
						((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
						mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
							bb.getCenterPoint(),
							LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb,
							mapView.getModel().displayModel.getTileSize())));
					}
					else {
						latLongs.clear();
					}
				}
				catch (Exception e) {
					Log.e(TAG, "Map.inform", e);
				}
			}
			break;
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Track.onCreate");
		super.onCreate(bundle);
		polyline = new AlternatingLine(AndroidGraphicFactory.INSTANCE);
	}

	@Override public void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "Track.onStart");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().add(polyline);
	}

	@Override public void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().remove(polyline);
	}
}
