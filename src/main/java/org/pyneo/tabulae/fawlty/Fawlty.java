package org.pyneo.tabulae.fawlty;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.map.android.util.MapViewerTemplate;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.view.MapView;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class Fawlty extends Base implements Constants {
	WirelessEnvListener wirelessEnvListener;
	String last_ident;
	Circle circle;

	@Override public void onCreate(Bundle bundle) {
		//if (DEBUG) Log.d(TAG, "Fawlty.onCreate");
		super.onCreate(bundle);
		wirelessEnvListener = new WirelessEnvListener(getActivity()) {
			@Override public void onLocationChanged(Location location, String ident) {
				//if (DEBUG) Log.d(TAG, "got it location=" + location + ", ident=" + ident + ", last_ident=" + Fawlty.this.last_ident);
				LatLong ll = new LatLong(location.getLatitude(), location.getLongitude());
				float accuracy = location.getAccuracy();
				if (!ident.equals(Fawlty.this.last_ident) || !ll.equals(circle.getPosition())) {
					circle.setLatLong(ll);
					circle.setRadius(accuracy);
					Fawlty.this.last_ident = ident;
					//if (DEBUG) Log.d(TAG, "location set");
					Bundle extra = new Bundle();
					extra.putString("cell_ident", ident);
					((Tabulae)getActivity()).inform(R.id.cell_ident, extra);
				}
			}
		};
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Fawlty.onResume");
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
		paint.setColor(0x77ff0000);
		paint.setStrokeWidth(0);
		paint.setStyle(Style.FILL);
		circle = new Circle(new LatLong(0,0), 1, paint, null) {
			@Override public boolean onTap(LatLong geoPoint, Point viewPosition, Point tapPoint) {
				if (contains(geoPoint)) {
					Toast.makeText(getActivity(), "Ident: " + last_ident, Toast.LENGTH_LONG).show();
					return true;
				}
				return false;
			}
			boolean contains(LatLong geoPoint) {
				double d = distance_in_meter(getPosition(), geoPoint);
				if (DEBUG) Log.d(TAG, "contains d=" + d + ", radius=" + getRadius());
				return d < getRadius();
			}
		};
		mapView.getLayerManager().getLayers().add(circle);
		//paint = (Paint)paint.clone();
		//paint.setColor(0x7700ff00);
		//FixedPixelCircle tappableCircle = new FixedPixelCircle(new LatLong(51.24,6.79), 50, paint, null);
		//mapView.getLayerManager().getLayers().add(tappableCircle);
		wirelessEnvListener.enable();
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Fawlty.onPause");
		wirelessEnvListener.disable();
		MapView mapView = ((Tabulae)getActivity()).getMapView();
		mapView.getLayerManager().getLayers().remove(circle);
	}

	public void inform(int event, Bundle extra) {
	}

	static double distance_in_meter(final LatLong latlong1, final LatLong latlong2) {
		final double R = 6371000f; // Radius of the earth in m
		final double dLat = (latlong1.latitude - latlong2.latitude) * Math.PI / 180f;
		final double dLon = (latlong1.longitude - latlong2.longitude) * Math.PI / 180f;
		final double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
			Math.cos(latlong1.latitude * Math.PI / 180f) * Math.cos(latlong2.latitude * Math.PI / 180f) *
				Math.sin(dLon/2) * Math.sin(dLon/2);
		final double c = 2f * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		final double d = R * c;
		return d;
	}
}
