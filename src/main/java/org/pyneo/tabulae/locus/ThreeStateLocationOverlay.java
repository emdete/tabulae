/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2014 Ludwig M Brinckmann
 * Copyright © 2014 devemux86
 * Copyright © 2015 M. Dietrich
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pyneo.tabulae.locus;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.model.MapViewPosition;
import org.pyneo.tabulae.R;

/**
 * A thread-safe {@link Layer} implementation to display the current location. NOTE: This code really does not reflect
 * Android best practice and used in production leads to bad user experience (e.g. long time to first fix, excessive
 * battery use, non-compliance with the Android lifecycle...). Best use the new location services provided by Google
 * Play Services. Also note that ThreeStateLocationOverlay needs to be added to a view before requesting location updates
 * (otherwise no DisplayModel is set).
 */
class ThreeStateLocationOverlay extends Layer implements LocationListener, Constants {
	protected static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
	protected float minDistance = 0.0f;
	protected long minTime = 0;
	protected boolean showAccuracy;
	protected final Circle circle;
	protected Location lastLocation;
	protected final LocationManager locationManager;
	protected final MapViewPosition mapViewPosition;
	protected Marker marker;
	protected final Marker map_needle_pinned;
	protected final Marker map_needle_off;
	protected final RotatingMarker map_needle;
	protected boolean myLocationEnabled;
	protected boolean snapToLocationEnabled;

	/**
	* Constructs a new {@code ThreeStateLocationOverlay} with the default circle paints.
	*
	* @param context a reference to the application context.
	* @param mapViewPosition the {@code MapViewPosition} whose location will be updated.
	*/
	public ThreeStateLocationOverlay(Context context, MapViewPosition mapViewPosition) {
		this(context, mapViewPosition, getDefaultCircleFill(), getDefaultCircleStroke());
	}

	/**
	* Constructs a new {@code ThreeStateLocationOverlay} with the given circle paints.
	*
	* @param context a reference to the application context.
	* @param mapViewPosition the {@code MapViewPosition} whose location will be updated.
	* @param circleFill the {@code Paint} used to fill the circle that represents the accuracy of the current location (might be null).
	* @param circleStroke the {@code Paint} used to stroke the circle that represents the accuracy of the current location (might be null).
	*/
	public ThreeStateLocationOverlay(Context context, MapViewPosition mapViewPosition, Paint circleFill, Paint circleStroke) {
		super();
		this.mapViewPosition = mapViewPosition;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		map_needle_pinned = new Marker(null, AndroidGraphicFactory.convertToBitmap(
			context.getResources().getDrawable(R.drawable.map_needle_pinned, null)), 0, 0);
		map_needle = new RotatingMarker(null, AndroidGraphicFactory.convertToBitmap(
			context.getResources().getDrawable(R.drawable.map_needle, null)), 0, 0);
		map_needle_off = new Marker(null, AndroidGraphicFactory.convertToBitmap(
			context.getResources().getDrawable(R.drawable.map_needle_off, null)), 0, 0);
		marker = map_needle_off;
		showAccuracy = true;
		circle = new Circle(null, 0, circleFill, circleStroke);
	}

	/**
	* Stops the receiving of location updates. Has no effect if location updates are already disabled.
	*/
	public synchronized void disable() {
		if (myLocationEnabled) {
			myLocationEnabled = false;
			locationManager.removeUpdates(this);
			// TODO trigger redraw?
		}
	}

	@Override
	public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		if (!myLocationEnabled) {
			return;
		}
		if (showAccuracy) {
			circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
		}
		marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
	}

	/**
	* Enables the receiving of location updates from the most accurate {@link LocationProvider} available.
	*
	* @param snapToLocationEnabled wether the maps should snap to the current location
	* @return true
	*/
	public synchronized boolean enable(boolean snapToLocationEnabled) {
		this.snapToLocationEnabled = snapToLocationEnabled;
		circle.setDisplayModel(displayModel);
		map_needle_pinned.setDisplayModel(displayModel);
		map_needle.setDisplayModel(displayModel);
		map_needle_off.setDisplayModel(displayModel);
		enableLocationProvider();
		return true;
	}

	/**
	* @return the most-recently received location fix (might be null).
	*/
	public synchronized Location getLastLocation() {
		return lastLocation;
	}

	/**
	* @return true if the receiving of location updates is currently enabled, false otherwise.
	*/
	public synchronized boolean isEnabled() {
		return myLocationEnabled;
	}

	/**
	* @return true if the snap-to-location mode is enabled, false otherwise.
	*/
	public synchronized boolean isSnapToLocationEnabled() {
		return snapToLocationEnabled;
	}

	@Override public void onDestroy() {
		map_needle_pinned.onDestroy();
		map_needle.onDestroy();
		map_needle_off.onDestroy();
	}

	@Override public void onLocationChanged(Location location) {
		synchronized (this) {
			long age = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1000000000;
			if (age > 3 || !location.hasAccuracy() || location.getAccuracy() == 0) {
				//if (DEBUG) Log.d(TAG, "ThreeStateLocationOverlay.onLocationChanged off: age=" + age);
				marker = map_needle_off;
				circle.setRadius(0);
			}
			else {
				float accuracy = location.getAccuracy();
				//if (DEBUG) { Log.d(TAG, "ThreeStateLocationOverlay.onLocationChanged circle: accuracy=" + accuracy); }
				circle.setRadius(accuracy);
				if (!location.hasSpeed() || !location.hasBearing()) {
					marker = map_needle_pinned;
					//if (DEBUG) { Log.d(TAG, "ThreeStateLocationOverlay.onLocationChanged pinned: no speed or bearing"); }
				}
				else {
					float speed = location.getSpeed();
					if (speed < 2.0) {
						//if (DEBUG) { Log.d(TAG, "ThreeStateLocationOverlay.onLocationChanged pinned: speed=" + speed); }
						marker = map_needle_pinned;
					}
					else {
						float bearing = location.getBearing();
						map_needle.setDegree(bearing);
						marker = map_needle;
					}
				}
			}
			LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude(), true);
			marker.setLatLong(latLong);
			circle.setLatLong(latLong);
			if (snapToLocationEnabled) {
				mapViewPosition.setCenter(latLong);
			}
			requestRedraw();
			lastLocation = location;
		}
	}

	@Override public void onProviderDisabled(String provider) {
		enableLocationProvider();
	}

	@Override public void onProviderEnabled(String provider) {
		enableLocationProvider();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// do nothing
	}

	/**
	* Minimum distance between location updates, in meters.
	* You should call this before calling {@link ThreeStateLocationOverlay#enable(boolean)}.
	*/
	public void setMinDistance(float minDistance) {
		this.minDistance = minDistance;
	}

	/**
	* Minimum time interval between location updates, in milliseconds.
	* You should call this before calling {@link ThreeStateLocationOverlay#enable(boolean)}.
	*/
	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}

	/**
	* @param snapToLocationEnabled whether the map should be centered at each received location.
	*/
	public synchronized void setSnapToLocationEnabled(boolean snapToLocationEnabled) {
		this.snapToLocationEnabled = snapToLocationEnabled;
		if (snapToLocationEnabled && lastLocation != null) {
			onLocationChanged(lastLocation);
		}
	}

	protected synchronized boolean enableLocationProvider() {
		disable();
		for (String provider : locationManager.getProviders(true)) {
			Location pl = locationManager.getLastKnownLocation(provider);
			lastLocation = betterLocation(lastLocation, pl);
		}
		boolean result = false;
		for (String provider : locationManager.getProviders(true)) {
			if (LocationManager.GPS_PROVIDER.equals(provider) || LocationManager.NETWORK_PROVIDER.equals(provider)) {
				result = true;
				locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
			}
		}
		myLocationEnabled = result;
		if (lastLocation != null) {
			onLocationChanged(lastLocation);
		}
		return result;
	}

	protected static Paint getDefaultCircleFill() {
		return getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL);
	}

	protected static Paint getDefaultCircleStroke() {
		return getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.STROKE);
	}

	protected static Paint getPaint(int color, int strokeWidth, Style style) {
		Paint paint = GRAPHIC_FACTORY.createPaint();
		paint.setColor(color);
		paint.setStrokeWidth(strokeWidth);
		paint.setStyle(style);
		return paint;
	}

	public static Location toLocation(Bundle location) {
		Location ret = null;
		if (location != null) {
			ret = new Location(location.getString("provider"));
			if (location.containsKey("accuracy")) {
				ret.setAccuracy((float)location.getDouble("accuracy"));
			}
			if (location.containsKey("altitude")) ret.setAltitude(location.getDouble("altitude"));
			if (location.containsKey("bearing")) {
				ret.setBearing((float)location.getDouble("bearing"));
			}
			ret.setElapsedRealtimeNanos(location.getLong("elapsed"));
			ret.setLatitude(location.getDouble("latitude"));
			ret.setLongitude(location.getDouble("longitude"));
			if (location.containsKey("speed")) ret.setSpeed((float)location.getDouble("speed"));
			ret.setTime(location.getLong("time"));
		}
		return ret;
	}

	public static Bundle toBundle(Location location) {
		Bundle ret = null;
		if (location != null) {
			ret = new Bundle(location.getExtras());
			ret.putString("provider", location.getProvider());
			if (location.hasAccuracy() && location.getAccuracy() != 0) {
				ret.putDouble("accuracy", location.getAccuracy());
			}
			if (location.hasAltitude()) ret.putDouble("altitude", location.getAltitude());
			if (location.hasBearing()) ret.putDouble("bearing", location.getBearing());
			ret.putLong("elapsed", location.getElapsedRealtimeNanos());
			ret.putDouble("latitude", location.getLatitude());
			ret.putDouble("longitude", location.getLongitude());
			if (location.hasSpeed()) ret.putDouble("speed", location.getSpeed());
			ret.putLong("time", location.getTime());
		}
		return ret;
	}

	private Location betterLocation(Location l1, Location l2) {
		if (l1 == null) return l2;
		if (l2 == null) return null;
		if (l2.isFromMockProvider())
			if (l1.isFromMockProvider())
				return null;
			else
				return l1;
		if (Math.abs(l1.getElapsedRealtimeNanos() - l2.getElapsedRealtimeNanos()) < 3E9 && l1.hasAccuracy() && l2.hasAccuracy()) {
			return l1.getAccuracy() < l2.getAccuracy()? l1: l2;
		}
		if (l1.getElapsedRealtimeNanos() < l2.getElapsedRealtimeNanos()) return l2;
		return l1;
	}
}
