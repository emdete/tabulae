package de.emdete.tabulae.track;

import android.util.Log;
import java.util.Iterator;
import java.util.List;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Polyline;
import static de.emdete.tabulae.track.Constants.*;

class AlternatingLine extends Polyline {
	final static Paint[] paints = new Paint[4];

	static {
		for (int i = 0; i < paints.length; i++) {
			paints[i] = AndroidGraphicFactory.INSTANCE.createPaint();
			paints[i].setStrokeWidth(24);
			paints[i].setStyle(Style.STROKE);
		}
		paints[0].setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.GREEN));
		paints[1].setColor(AndroidGraphicFactory.INSTANCE.createColor(255, 255, 255, 0));
		paints[2].setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.RED));
		paints[3].setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE));
	}
	boolean imOnScreen = false;
	boolean active = false;

	public AlternatingLine(GraphicFactory graphicFactory) {
		super(null, graphicFactory);
	}

	@Override public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		imOnScreen = false;
		if (!getLatLongs().isEmpty()) {
			int bl = getLatLongs().size() / 3 * 2;
			int index = 0;
			Iterator<LatLong> iterator = getLatLongs().iterator();
			if (iterator.hasNext()) {
				long mapSize = MercatorProjection.getMapSize(zoomLevel, displayModel.getTileSize());
				LatLong from = iterator.next();
				while (iterator.hasNext()) {
					LatLong to = iterator.next();
					if (boundingBox.contains(to) || boundingBox.contains(from)) {
						Paint paint = getPaintStroke(from, to, index<bl?2:1);
						int x1 = (int) (MercatorProjection.longitudeToPixelX(from.longitude, mapSize) - topLeftPoint.x);
						int y1 = (int) (MercatorProjection.latitudeToPixelY(from.latitude, mapSize) - topLeftPoint.y);
						int x2 = (int) (MercatorProjection.longitudeToPixelX(to.longitude, mapSize) - topLeftPoint.x);
						int y2 = (int) (MercatorProjection.latitudeToPixelY(to.latitude, mapSize) - topLeftPoint.y);
						canvas.drawLine(x1, y1, x2, y2, paint);
						index++;
					}
					from = to;
				}
			}
			imOnScreen = index > 0;
		}
		//if (DEBUG) Log.d(TAG, "AlternatingLine.draw count=" + count);
	}

	@Override public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
		active = false;
		if (near(tapLatLong)) {
			if (DEBUG) Log.d(TAG, "onTap tapLatLong=" + tapLatLong + ", layerXY=" + layerXY + ", tapXY=" + tapXY + ", this=" + this);
			active = true;
			return true;
		}
		return false;
	}

	@Override public synchronized Paint getPaintStroke() {
		throw new RuntimeException("getPaintStroke called with no parms"); // i hate to do that, it's just to validate my code
	}

	Paint getPaintStroke(LatLong from, LatLong to, int c) {
		if (active) {
			return paints[3];
		}
		return paints[c]; // TODO
	}

	void setLatLongs(List<LatLong> list) {
		getLatLongs().addAll(list);
	}

	boolean near(LatLong latLong) {
		return imOnScreen;
	}
}
