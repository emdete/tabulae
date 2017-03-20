package de.emdete.tabulae.fawlty;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import de.emdete.tabulae.Base;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.view.MapView;
import de.emdete.tabulae.R;
import de.emdete.tabulae.Tabulae;
import static de.emdete.tabulae.fawlty.Constants.*;

public class Fawlty extends Base {
	private static final String STATE_ENABLED = "fawlty_enabled";
	protected boolean enabled;
	protected WirelessEnvListener wirelessEnvListener;
	protected String last_ident;
	protected LatLong last_latLong;
	protected LatLong last_latLong_tower;
	protected Circle circle;
	protected Marker marker;
	protected Bitmap bitmap;

	static double distance_in_meter(final LatLong latlong1, final LatLong latlong2) {
		final double R = 6371000f; // Radius of the earth in m
		final double dLat = (latlong1.latitude - latlong2.latitude) * Math.PI / 180f;
		final double dLon = (latlong1.longitude - latlong2.longitude) * Math.PI / 180f;
		final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(latlong1.latitude * Math.PI / 180f) * Math.cos(latlong2.latitude * Math.PI / 180f) *
						Math.sin(dLon / 2) * Math.sin(dLon / 2);
		final double c = 2f * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		//if (DEBUG) Log.d(TAG, "Fawlty.onCreate");
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			enabled = savedInstanceState.getBoolean(STATE_ENABLED);
		}
		last_latLong = new LatLong(0, 0);
		last_latLong_tower = null;
		wirelessEnvListener = new WirelessEnvListener(getActivity()) {
			@Override public void onLocationChanged(Location location, String ident) {
				//if (DEBUG) Log.d(TAG, "got it location=" + location + ", ident=" + ident + ", last_ident=" + Fawlty.this.last_ident);
				last_latLong = new LatLong(location.getLatitude(), location.getLongitude());
				if (location.getExtras().containsKey("latitude_tower")) {
					last_latLong_tower = new LatLong(location.getExtras().getDouble("latitude_tower"), location.getExtras().getDouble("longitude_tower"));
				} else {
					last_latLong_tower = null;
				}
				float accuracy = location.getAccuracy();
				long rcd = location.getExtras().getLong("rcd", 5000);
				if (!ident.equals(Fawlty.this.last_ident) || !last_latLong.equals(circle.getPosition())) {
					circle.setLatLong(last_latLong);
					if (rcd == 2000) {
						circle.setRadius(accuracy);
						if (last_latLong_tower != null) {
							marker.setLatLong(last_latLong_tower);
							marker.setVisible(true);
						} else {
							marker.setVisible(false);
						}
						circle.setVisible(true);
					} else {
						circle.setVisible(false);
						marker.setVisible(false);
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(getActivity(), "Ident: " + last_ident + " not found", Toast.LENGTH_LONG).show();
							}
						});
					}
					Fawlty.this.last_ident = ident;
					//if (DEBUG) Log.d(TAG, "location set");
					notice(R.id.event_notify_cell, "cell_ident", ident);
				}
			}
		};
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.d(TAG, "Fawlty.onSaveInstanceState");
		outState.putBoolean(STATE_ENABLED, enabled);
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Fawlty.onResume");
		if (enabled) {
			enable();
		}
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Fawlty.onPause");
		disable();
	}

	void enable() {
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		if (circle == null) {
			Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
			paint.setColor(0x77ff0000);
			paint.setStrokeWidth(0);
			paint.setStyle(Style.FILL);
			circle = new Circle(last_latLong, 1, paint, null) {
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
			bitmap = AndroidGraphicFactory.convertToBitmap(
					getResources().getDrawable(R.drawable.poi_red, null));
			bitmap.incrementRefCount();
			marker = new Marker(new LatLong(0, 0), bitmap, 0, -bitmap.getHeight() / 2);
			marker.setVisible(false);
		}
		mapView.getLayerManager().getLayers().add(circle);
		mapView.getLayerManager().getLayers().add(marker);
		//paint = (Paint)paint.clone();
		//paint.setColor(0x7700ff00);
		//FixedPixelCircle tappableCircle = new FixedPixelCircle(new LatLong(51.24,6.79), 50, paint, null);
		//mapView.getLayerManager().getLayers().add(tappableCircle);
		wirelessEnvListener.enable();
	}

	void disable() {
		wirelessEnvListener.disable();
		if (circle != null) {
			MapView mapView = ((Tabulae) getActivity()).getMapView();
			mapView.getLayerManager().getLayers().remove(circle);
			//circle.onDestroy();
			//circle = null;
			//bitmap.decrementRefCount();
			mapView.getLayerManager().getLayers().remove(marker);
			//marker.onDestroy();
			//marker = null;
		}
	}

	public void inform(int event, Bundle extra) {
		switch (event) {
			case R.id.event_request_fawlty: {
				notice(R.id.event_notify_fawlty, "enabled", enabled);
			}
			break;
			case R.id.event_do_fawlty: {
				if (enabled) {
					disable();
					enabled = false;
					Toast.makeText(getActivity(), "Serving cell disabled", Toast.LENGTH_SHORT).show();
				} else {
					enable();
					enabled = true;
					Toast.makeText(getActivity(), "Serving cell enabled", Toast.LENGTH_SHORT).show();
				}
				notice(R.id.event_notify_fawlty, "enabled", enabled);
			}
			break;
		}
	}
}
