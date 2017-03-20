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
package de.emdete.tabulae.locus;

import android.content.Context;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Build;
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

/**
 * A thread-safe {@link Layer} implementation to display the current location.
 */
class ThreeStateLocationOverlay extends Layer {
	protected static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
	protected final Circle circle;
	protected final Marker map_needle_pinned;
	protected final Marker map_needle_off;
	protected final RotatingMarker map_needle;
	protected float minDistance = 0.0f;
	protected long minTime = 0;
	protected boolean showAccuracy = true;
	protected Marker marker;
	protected Context context;

	/**
	 * Constructs a new {@code ThreeStateLocationOverlay} with the default circle paints.
	 *
	 * @param context a reference to the application context.
	 */
	public ThreeStateLocationOverlay(Context context) {
		super();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			map_needle_pinned = new Marker(null, AndroidGraphicFactory.convertToBitmap(
				context.getResources().getDrawable(de.emdete.tabulae.R.drawable.map_needle_pinned, null)), 0, 0);
			map_needle = new RotatingMarker(null, AndroidGraphicFactory.convertToBitmap(
				context.getResources().getDrawable(de.emdete.tabulae.R.drawable.map_needle, null)), 0, 0);
			map_needle_off = new Marker(null, AndroidGraphicFactory.convertToBitmap(
				context.getResources().getDrawable(de.emdete.tabulae.R.drawable.map_needle_off, null)), 0, 0);
		}
		else {
			map_needle_pinned = new Marker(null, AndroidGraphicFactory.convertToBitmap(
				context.getResources().getDrawable(de.emdete.tabulae.R.drawable.map_needle_pinned, null)), 0, 0);
			map_needle = new RotatingMarker(null, AndroidGraphicFactory.convertToBitmap(
				context.getResources().getDrawable(de.emdete.tabulae.R.drawable.map_needle, null)), 0, 0);
			map_needle_off = new Marker(null, AndroidGraphicFactory.convertToBitmap(
				context.getResources().getDrawable(de.emdete.tabulae.R.drawable.map_needle_off, null)), 0, 0);
		}
		marker = map_needle_off;
		Paint circleFill = getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL);
		Paint circleStroke = getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.STROKE);
		circle = new Circle(null, 0, circleFill, circleStroke);
	}

	protected static Paint getPaint(int color, int strokeWidth, Style style) {
		Paint paint = GRAPHIC_FACTORY.createPaint();
		paint.setColor(color);
		paint.setStrokeWidth(strokeWidth);
		paint.setStyle(style);
		return paint;
	}

	@Override
	public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		if (showAccuracy) {
			circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
		}
		marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
	}

	/**
	 * Enables the receiving of location updates from the most accurate {@link LocationProvider} available.
	 *
	 * @return true
	 */
	public synchronized boolean enable() {
		circle.setDisplayModel(displayModel);
		map_needle_pinned.setDisplayModel(displayModel);
		map_needle.setDisplayModel(displayModel);
		map_needle_off.setDisplayModel(displayModel);
		return true;
	}

	@Override
	public void onDestroy() {
		map_needle_pinned.onDestroy();
		map_needle.onDestroy();
		map_needle_off.onDestroy();
	}

	public void onLocationChanged(Location location) {
		//if (DEBUG) Log.d(TAG, "ThreeStateLocationOverlay.onLocationChanged location=" + location);
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
			LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());
			marker.setLatLong(latLong);
			circle.setLatLong(latLong);
			requestRedraw();
		}
	}
}
