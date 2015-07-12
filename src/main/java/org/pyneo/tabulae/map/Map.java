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

import org.mapsforge.map.android.view.MapView;
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

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_zoom: {
				//mapView.getController().setZoom(zoom);
			}
			break;
			case R.id.event_zoom_in:
				if (true) {
					extra = new Bundle();
					extra.putInt("zoom_level", ++zoom);
					((Tabulae)getActivity()).inform(R.id.event_zoom, extra);
				}
				break;
			case R.id.event_zoom_out:
				if (true) {
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
				}
				break;
			case R.id.event_map_list: {
				mapView.setVisibility(View.INVISIBLE);
			}
			case R.id.event_autofollow: {
				follow = extra == null || extra.getBoolean("autofollow");
				if (follow) {
					; // mapView.getController().setCenter(new GeoPoint(latitude, longitude));
				}
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
	}

	public MapView getMapView() {
		return mapView;
	}
}

