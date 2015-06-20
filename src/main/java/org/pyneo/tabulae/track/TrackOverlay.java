package org.pyneo.tabulae.track;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.drawing.OsmPath;
import org.osmdroid.views.overlay.Overlay;

class TrackOverlay extends Overlay implements Constants {
	private final static int lineWidth = 2;
	private final TrackGpxParser parsedTrack;
	private final float density;
	private int mLastZoomLevel;
	private OsmPath mPath;
	protected final Paint mPaint = new Paint();

	public TrackOverlay(Activity activity, TrackGpxParser parsedTrack) {
		super(activity);
		this.parsedTrack = parsedTrack;
		this.density = activity.getResources().getDisplayMetrics().density * USER_FONT_FACTOR;
	}

	@Override
	protected void draw(Canvas c, MapView osmv, boolean shadow) {
		if (shadow) return;
		final Projection proj = osmv.getProjection();
		if (mPath == null || mLastZoomLevel != proj.getZoomLevel()) {
			mPath = new OsmPath();
			Point p = new Point();
			int lx = 0;
			int ly = 0;
			for (TrackGpxParser.TrackPoint trackPoint : parsedTrack) {
				//Log.d(TAG, "trkpt trackPoint=" + trackPoint);
				p = proj.toPixels(trackPoint, p);
				Log.d(org.pyneo.tabulae.Constants.TAG, "p x=" + p.x + ", y=" + p.y);
				if (mPath.isEmpty()) {
					mPath.moveTo(p.x, p.y);
					lx = p.x;
					ly = p.y;
				}
				else if (Math.abs(lx - p.x) + Math.abs(ly - p.y) > lineWidth * density) {
					mPath.lineTo(p.x, p.y);
					lx = p.x;
					ly = p.y;
				}
			}
			//mPath.close();
			mLastZoomLevel = proj.getZoomLevel();
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setColor(Color.RED);
			mPaint.setStrokeWidth(lineWidth * density);
		}
		mPath.onDrawCycle(proj); // adapt panning
		c.drawPath(mPath, mPaint);
	}
}
