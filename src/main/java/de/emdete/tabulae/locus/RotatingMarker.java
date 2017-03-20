package de.emdete.tabulae.locus;
/*
 * Copyright © 2015 Himal Rai (initial implementation from https://groups.google.com/forum/#!topic/mapsforge-dev/BS-b3q5XAa0)
 * Copyright © 2015 Emux (valuable hints)
 * Copyright © 2015 M. Dietrich (cleanup, simplification)
 */
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Rectangle;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

class RotatingMarker extends Marker {
	protected float degree = 0.0f;
	protected float px = 0.0f;
	protected float py = 0.0f;

	public RotatingMarker(LatLong latLong, Bitmap bitmap, int horizontalOffset, int verticalOffset) {
		super(latLong, bitmap, horizontalOffset, verticalOffset);
	}

	@Override
	public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		if (getLatLong() == null || getBitmap() == null) {
			return;
		}
		long mapSize = MercatorProjection.getMapSize(zoomLevel, displayModel.getTileSize());
		double pixelX = MercatorProjection.longitudeToPixelX(getLatLong().longitude, mapSize);
		double pixelY = MercatorProjection.latitudeToPixelY(getLatLong().latitude, mapSize);
		int halfBitmapWidth = getBitmap().getWidth() / 2;
		int halfBitmapHeight = getBitmap().getHeight() / 2;
		int left = (int) (pixelX - topLeftPoint.x - halfBitmapWidth + getHorizontalOffset());
		int top = (int) (pixelY - topLeftPoint.y - halfBitmapHeight + getVerticalOffset());
		int right = left + getBitmap().getWidth();
		int bottom = top + getBitmap().getHeight();
		Rectangle bitmapRectangle = new Rectangle(left, top, right, bottom);
		Rectangle canvasRectangle = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
		if (!canvasRectangle.intersects(bitmapRectangle)) {
			return;
		}
		android.graphics.Canvas androidCanvas = AndroidGraphicFactory.getCanvas(canvas);
		androidCanvas.save();
		androidCanvas.rotate(degree, (float) (pixelX - topLeftPoint.x), (float) (pixelY - topLeftPoint.y));
		canvas.drawBitmap(getBitmap(), left, top);
		androidCanvas.restore();
	}

	public RotatingMarker setDegree(float degree) {
		this.degree = degree;
		return this;
	}

	public RotatingMarker setPivotPoint(float px, float py) {
		this.px = px;
		this.py = py;
		return this;
	}
}
