package org.pyneo.tabulae.conversations;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
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

public class Conversations extends Base implements Constants {
	protected boolean visible = true;
	protected Marker marker;
	protected Bitmap bitmap;

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "Conversations.onCreate");
		super.onCreate(bundle);
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Conversations.onResume");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Conversations.onPause");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		if (marker != null) {
			bitmap.decrementRefCount();
			mapView.getLayerManager().getLayers().remove(marker);
			marker.onDestroy();
			marker = null;
		}
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Conversations.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.conversations_request: {
				MapView mapView = ((Tabulae)getActivity()).getMapView();
				LatLong location = mapView.getModel().mapViewPosition.getCenter(); // TODO defere location determination?
				Intent result = new Intent();
				result.putExtra(LATITUDE, location.latitude);
				result.putExtra(LONGITUDE, location.longitude);
				//result.putExtra(ALTITUDE, .getAltitude());
				//result.putExtra(ACCURACY, .getAccuracy());
				getActivity().setResult(Activity.RESULT_OK, result);
				getActivity().finish();
			}
			break;
			case R.id.conversations_show: {
				MapView mapView = ((Tabulae)getActivity()).getMapView();
				if (marker != null) { // remove other if avail
					bitmap.decrementRefCount();
					mapView.getLayerManager().getLayers().remove(marker);
					marker.onDestroy();
					marker = null;
				}
				if (extra.containsKey(LONGITUDE) && extra.containsKey(LATITUDE)) {
					final String jid = extra.getString(JID);
					final String name = extra.getString(NAME);
					/*
					if (name == null || name.length() == 0) {
						if (jid != null && jid.length() > 0) {
							name = jid.split("@")[0]; // TODO avoid regex
						}
						else {
							name = "Jabber";
							jid = "@xmpp";
						}
					} */
					double latitude = extra.getDouble(LATITUDE, 0);
					double longitude = extra.getDouble(LONGITUDE, 0);
					double altitude = 0;
					double accuracy = 0;
					Log.i(TAG, "onCreate location received longitude=" + longitude + ", latitude=" + latitude + ", jid=" + jid + ", name=" + name);
					LatLong latLong = new LatLong(latitude, longitude);
					bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.poi_blue, null));
					bitmap.incrementRefCount();
					marker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2) {
						@Override public boolean onTap(LatLong geoPoint, Point viewPosition, Point tapPoint) {
							if (contains(viewPosition, tapPoint)) {
								Toast.makeText(getActivity(), "Jid: %s\nName: %s".format(jid, name), Toast.LENGTH_LONG).show();
								return true;
							}
							return false;
						}
					};
					mapView.getLayerManager().getLayers().add(marker);
					extra = new Bundle();
					extra.putBoolean("autofollow", false);
					((Tabulae)getActivity()).inform(R.id.event_autofollow, extra);
					try {
						LatLong location = mapView.getModel().mapViewPosition.getMapPosition().latLong; // TODO: sort
						BoundingBox bb = new BoundingBox(latLong.latitude, latLong.longitude, location.latitude, location.longitude);
						mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
							bb.getCenterPoint(),
							LatLongUtils.zoomForBounds(mapView.getModel().mapViewDimension.getDimension(), bb,
									mapView.getModel().displayModel.getTileSize())));
					}
					catch (Exception e) {
						mapView.getModel().mapViewPosition.setCenter(latLong);
					}
				}
				else
					Log.w(TAG, "onCreate conversations intent recceived with no latitude/longitude");
			}
			break;
		}
	}
}
