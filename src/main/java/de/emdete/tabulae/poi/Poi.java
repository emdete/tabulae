package de.emdete.tabulae.poi;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import de.emdete.thinstore.StoreObject;
import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.view.MapView;
import de.emdete.tabulae.Base;
import de.emdete.tabulae.R;
import de.emdete.tabulae.Tabulae;
import static de.emdete.tabulae.poi.Constants.*;

public class Poi extends Base {
	protected List<PointAd> pointsAd = new ArrayList<>();

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Poi.onCreate");
		super.onCreate(bundle);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Poi.onResume");
		//MapView mapView = ((Tabulae)getActivity()).getMapView();
		try {
			try (final SQLiteDatabase db = ((Tabulae)getActivity()).getReadableDatabase()) {
				for (StoreObject item: StoreObject.query(db, PoiItem.class).where("visible").equal(true).fetchAll()) {
					PoiItem poiItem = (PoiItem)item;
					Log.d(TAG, "Poi.onResume poiItem=" + poiItem);
					pointsAd.add(new PointAd(poiItem));
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Poi.onResume e=" + e, e);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Poi.onPause");
		for (PointAd pointAd : pointsAd) {
			pointAd.onDestroy();
		}
		pointsAd.clear();
	}

	class PointAd {
		protected PoiItem poiItem;
		protected Marker marker;
		protected Bitmap bitmap;

		PointAd(final PoiItem poiItem) {
			this.poiItem = poiItem;
			MapView mapView = ((Tabulae) getActivity()).getMapView();
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
				@Override
				public boolean onTap(LatLong geoPoint, Point viewPosition, Point tapPoint) {
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
			if (DEBUG) Log.d(TAG, "Poi.PointAd.onDestroy");
			MapView mapView = ((Tabulae) getActivity()).getMapView();
			if (marker != null) {
				bitmap.decrementRefCount();
				mapView.getLayerManager().getLayers().remove(marker);
				marker.onDestroy();
				marker = null;
			}
			poiItem.setVisible(false);
			try {
				try (final SQLiteDatabase db = ((Tabulae)getActivity()).getWritableDatabase()) {
					poiItem.update(db);
				}
			}
			catch (Exception e) {
				Log.e(TAG, "Poi.PointAd.onDestroy e=" + e, e);
			}
		}
	}

	static public long storePointPosition(Tabulae activity, String name, String description, double latitude, double longitude, boolean visible) {
		//noinspection UnusedAssignment
		PoiItem poiItem = null;
		try {
			List<StoreObject> items = null;
			try (final SQLiteDatabase db = activity.getReadableDatabase()) {
				items = StoreObject.query(db, PoiItem.class).where("name").equal(name).fetchAll();
			}
			switch (items.size()) {
				case 0:
					if (DEBUG) Log.d(TAG, "Poi.storePointPosition new poiItem name=" + name);
					poiItem = new PoiItem(name, description, latitude, longitude, visible);
					break;
				case 1:
					if (DEBUG) Log.d(TAG, "Poi.storePointPosition update poiItem name=" + name);
					poiItem = (PoiItem)items.get(0);
					poiItem.setDescription(description);
					poiItem.setLatitude(latitude);
					poiItem.setLongitude(longitude);
					poiItem.setVisible(visible);
					break;
				default:
					Log.e(TAG, "Poi.storePointPosition poiItem not unique! name=" + name);
					throw new RuntimeException("Not unique by name");
			}
			try (final SQLiteDatabase db = activity.getWritableDatabase()) {
				return poiItem.insert(db).getId();
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Poi.storePointPosition e=" + e, e);
		}
		return -1L;
	}

	void hideAll() {
		for (PointAd pointAd : pointsAd) {
			pointAd.onDestroy();
		}
		pointsAd.clear();
	}

	void center() {
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		Bundle extra = new Bundle();
		extra.putBoolean("autofollow", false);
		((Tabulae) getActivity()).inform(R.id.event_do_autofollow, extra);
		LatLong latLong = mapView.getModel().mapViewPosition.getMapPosition().latLong;
		BoundingBox bb = new BoundingBox(latLong.latitude, latLong.longitude, latLong.latitude, latLong.longitude);
		for (PointAd pointAd : pointsAd) {
			PoiItem poiItem = pointAd.poiItem;
			latLong = new LatLong(poiItem.getLatitude(), poiItem.getLongitude());
			bb = bb.extendCoordinates(latLong);
		}
		try {
			byte zoom = LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb, mapView.getModel().displayModel.getTileSize());
			// TODO adjust min/max zoom
			mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(bb.getCenterPoint(), zoom));
		}
		catch (Exception e) {
			mapView.getModel().mapViewPosition.setCenter(latLong);
		}
	}

	public void inform(int event, Bundle extra) {
		switch (event) {
			case R.id.event_do_poi_new: {
				if (DEBUG) Log.d(TAG, "Poi.inform event=event_poi_new, extra=" + extra);
				storePointPosition((Tabulae) getActivity(),
					extra.getString("name"),
					extra.getString("description"),
					extra.getDouble(LATITUDE),
					extra.getDouble(LONGITUDE),
					true);
				center();
			}
			break;
			case R.id.event_do_poi_list: {
				if (DEBUG) Log.d(TAG, "Poi.inform event=event_poi_list, extra=" + extra);
				hideAll();
			}
			break;
		}
	}
}
