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
import java.util.List;

class AlternatingLine extends Polyline implements Constants {
	Paint[] paints = new Paint[1];

	public AlternatingLine(GraphicFactory graphicFactory) {
		super(null, graphicFactory);
		for (int i=0;i<paints.length;i++) {
			paints[i] = AndroidGraphicFactory.INSTANCE.createPaint();
			paints[i].setStrokeWidth(16);
			paints[i].setStyle(Style.STROKE);
			paints[i].setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE)); // TODO: different colors
		}
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
		return paints[0]; // TODO
	}

	void setLatLongs(List<LatLong> list) {
		getLatLongs().addAll(list);
	}
}
