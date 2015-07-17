package org.pyneo.tabulae.track;

import android.util.Log;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Polyline;
import java.util.HashMap;
import java.util.Iterator;

class AlternatingLine extends Polyline implements Constants {
	public AlternatingLine(GraphicFactory graphicFactory) {
		super(null, graphicFactory);
	}

	@Override public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		int count = 0;
		if (!getLatLongs().isEmpty()) {
			Iterator<LatLong> iterator = getLatLongs().iterator();
			if (iterator.hasNext()) {
				long mapSize = MercatorProjection.getMapSize(zoomLevel, displayModel.getTileSize());
				LatLong from = iterator.next();
				while (iterator.hasNext()) {
					LatLong to = iterator.next();
					if (boundingBox.contains(to) || boundingBox.contains(from)) {
						Paint paint = getPaintStroke(from, to);
						int x1 = (int) (MercatorProjection.longitudeToPixelX(from.longitude, mapSize) - topLeftPoint.x);
						int y1 = (int) (MercatorProjection.latitudeToPixelY(from.latitude, mapSize) - topLeftPoint.y);
						int x2 = (int) (MercatorProjection.longitudeToPixelX(to.longitude, mapSize) - topLeftPoint.x);
						int y2 = (int) (MercatorProjection.latitudeToPixelY(to.latitude, mapSize) - topLeftPoint.y);
						canvas.drawLine(x1, y1, x2, y2, paint);
						count++;
					}
					from = to;
				}
			}
		}
		//if (DEBUG) Log.d(TAG, "AlternatingLine.draw count=" + count);
	}

	public synchronized Paint getPaintStroke() {
		throw new RuntimeException("getPaintStroke called with no parms"); // i hate to do that, it's just to validate my code
	}

	synchronized Paint getPaintStroke(LatLong from, LatLong to) {
		Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
		paint.setStrokeWidth(16);
		paint.setStyle(Style.STROKE);
		paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE));
		return paint;
	}

	static class BB extends BoundingBox {
		public BB(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
			super(minLatitude, minLongitude, maxLatitude, maxLongitude);
		}

		public BoundingBox extend(LatLong latLong) {
			return new BB(
				Math.min(minLatitude, latLong.latitude),
				Math.min(minLongitude, latLong.longitude),
				Math.max(maxLatitude, latLong.latitude),
				Math.max(maxLongitude, latLong.longitude));
		}
	}
}
