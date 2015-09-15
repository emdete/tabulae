package org.pyneo.tabulae.poi;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.view.MapView;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import java.util.ArrayList;
import java.util.List;
import co.uk.rushorm.core.RushSearch;

public class Poi extends Base implements Constants {
	class PointAd {
		protected PoiItem poiItem;
		protected Marker marker;
		protected Bitmap bitmap;

		PointAd(final PoiItem poiItem) {
			this.poiItem = poiItem;
			MapView mapView = ((Tabulae)getActivity()).getMapView();
			if (marker != null) {
				bitmap.decrementRefCount();
				mapView.getLayerManager().getLayers().remove(marker);
				marker.onDestroy();
				marker = null;
			}
			LatLong latLong = new LatLong(poiItem.getLatitude(), poiItem.getLongitude());
			bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.poi_black, null));
			bitmap.incrementRefCount();
			marker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2) {
				@Override public boolean onTap(LatLong geoPoint, Point viewPosition, Point tapPoint) {
					if (contains(viewPosition, tapPoint)) {
						Toast.makeText(getActivity(), String.format("%s: '%s'", poiItem.getName(), poiItem.getDescription()), Toast.LENGTH_SHORT).show();
						return true;
					}
					return false;
				}
			};
			mapView.getLayerManager().getLayers().add(marker);
		}

		void onDestroy() {
			MapView mapView = ((Tabulae)getActivity()).getMapView();
			if (marker != null) {
				bitmap.decrementRefCount();
				mapView.getLayerManager().getLayers().remove(marker);
				marker.onDestroy();
				marker = null;
			}
		}
	}
	protected List<PointAd> pointsAd = new ArrayList<PointAd>();

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Poi.onCreate");
		super.onCreate(bundle);
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Poi.onResume");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		for (PoiItem poiItem: new RushSearch().whereEqual("visible", true).find(PoiItem.class)) {
			Log.d(TAG, "Poi.onResume poiItem=" + poiItem);
			pointsAd.add(new PointAd(poiItem));
		}
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Poi.onPause");
		for (PointAd pointAd: pointsAd) {
			pointAd.onDestroy();
		}
		pointsAd.clear();
	}

	static public String storePointPosition(Tabulae activity, String name, String description, double latitude, double longitude, boolean visible) {
		PoiItem item = null;
		List<PoiItem> items = new RushSearch().whereEqual("name", name).find(PoiItem.class);
		switch (items.size()) {
			case 0:
				if (DEBUG) Log.d(TAG, "Poi.storePointPosition new item name=" + name);
				item = new PoiItem(name, description, latitude, longitude, visible);
			break;
			case 1:
				if (DEBUG) Log.d(TAG, "Poi.storePointPosition update item name=" + name);
				item = items.get(0);
				item.setDescription(description);
				item.setLatitude(latitude);
				item.setLongitude(longitude);
				item.setVisible(visible);
			break;
			default:
				Log.e(TAG, "Poi.storePointPosition poiItem not unique! name=" + name);
				throw new RuntimeException("Not unique by name");
		}
		item.save();
		return item.getId();
	}

	void center() {
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		Bundle extra = new Bundle();
		extra.putBoolean("autofollow", false);
		((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
		LatLong latLong = mapView.getModel().mapViewPosition.getMapPosition().latLong;
		BoundingBox bb = new BoundingBox(latLong.latitude, latLong.longitude, latLong.latitude, latLong.longitude);
		for (PointAd pointAd: pointsAd) {
			PoiItem poiItem = pointAd.poiItem;
			latLong = new LatLong(poiItem.getLatitude(), poiItem.getLongitude());
			bb = bb.extend(new BoundingBox(latLong.latitude, latLong.longitude, latLong.latitude, latLong.longitude)); // TODO use extend with LatLong
		}
		try {
			byte zoom = LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb, mapView.getModel().displayModel.getTileSize());
			if (zoom > MAX_ZOOM) {
				zoom = MAX_ZOOM;
			}
			mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(bb.getCenterPoint(), zoom));
		}
		catch (Exception e) {
			mapView.getModel().mapViewPosition.setCenter(latLong);
		}
	}

	public void inform(int event, Bundle extra) {
		switch (event) {
			case R.id.event_poi_list: {
				if (DEBUG) Log.d(TAG, "Poi.inform event=" + event + ", extra=" + extra);
				center();
			}
			break;
		}
	}
}
