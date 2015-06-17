package org.pyneo.maps.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import org.pyneo.maps.R;
import org.pyneo.maps.utils.DistanceFormatter;
import org.pyneo.maps.map.TileView.OpenStreetMapViewProjection;
import org.pyneo.maps.utils.Ut;

import org.pyneo.maps.utils.GeoPoint;

import java.util.Locale;

/**
* Class to show everything around my own location. Will be a needle showing the
* direction of movement, centered circles to show distances (if enabled).
*/
public class MyLocationOverlay extends TileViewOverlay {
	private final static int CROSS_SIZE = 32; // TODO make dpi depending
	private final static int METER_IN_PIXEL = 156412;
	private final static int SCALE[][] = {{
		25000000, 15000000, 8000000, 4000000, 2000000, 1000000, 500000, 250000, 100000, 50000, 25000, 15000, 8000, 4000, 2000, 1000, 500, 250, 100, 50, 25, 10, 5, }, {
		15000, 8000, 4000, 2000, 1000, 500, 250, 100, 50, 25, 15, 8, 21120, 10560, 5280, 3000, 1500, 500, 250, 100, 50, 25, 10,
		}};
	private final Paint mPaint = new Paint();
	private final Paint mPaintCross = new Paint();
	private Bitmap mNolocationIcon = null;
	private Bitmap mLocationIcon = null;
	private Bitmap mTargetIcon = null;
	private GeoPoint mLastGeoPoint;
	private GeoPoint mTargetLocation;
	private Bitmap mArrow = null;
	private Context mCtx;
	private float mAccuracy;
	private int mPrefAccuracy;
	private float mBearing;
	private boolean mIsMoving;
	private boolean mIsCurrentLocation;
	private Paint mPaintAccuracyFill;
	private Paint mPaintAccuracyBorder;
	private Paint mPaintLineToGPS;
	private boolean mNeedCrosshair;
	private boolean mNeedCircleDistance;
	private Location mLoc;
	private DistanceFormatter mDf;
	private boolean mLineToGPS;
	private int mUnits;
	private TextView mLabelVw;
	private Drawable mCenterCross;
	private int mZoomLevel;
	private int mScaleCorretion;
	private double mTouchScale;
	private int mWidth;

	public MyLocationOverlay(final Context ctx) {
		mCtx = ctx.getApplicationContext();
		mPaintAccuracyFill = new Paint();
		mPaintAccuracyFill.setAntiAlias(true);
		mPaintAccuracyFill.setStrokeWidth(2);
		mPaintAccuracyFill.setStyle(Paint.Style.FILL);
		mPaintAccuracyFill.setColor(0x4490B8D8);
		mPaintAccuracyBorder = new Paint(mPaintAccuracyFill);
		mPaintAccuracyBorder.setStyle(Paint.Style.STROKE);
		mPaintAccuracyBorder.setColor(0xFF90B8D8);
		mPaintLineToGPS = new Paint(mPaintAccuracyFill);
		mPaintLineToGPS.setColor(ctx.getResources().getColor(R.color.line_to_gps));
		mPaintCross.setAntiAlias(true);
		mPaintCross.setStyle(Paint.Style.STROKE);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		mPrefAccuracy = Integer.parseInt(pref.getString("pref_accuracy", "1").replace("\"", ""));
		mNeedCrosshair = pref.getBoolean("pref_crosshair", true);
		mNeedCircleDistance = pref.getBoolean("pref_circle_distance", true);
		mLineToGPS = pref.getBoolean("pref_line_gps", false);
		mUnits = Integer.parseInt(pref.getString("pref_units", "0"));
		mDf = new DistanceFormatter(ctx);
		mScaleCorretion = 0;
		mCenterCross = mCtx.getResources().getDrawable(R.drawable.map_center_cross);
		if (mLineToGPS) {
			mLabelVw = (TextView)LayoutInflater.from(ctx).inflate(R.layout.label_map, null);
			mLabelVw.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}

	public GeoPoint getLastGeoPoint() {
		return mLastGeoPoint;
	}

	public Location getLastLocation() {
		return mLoc;
	}

	public void setLocation(final Location loc) {
		Ut.d("setLocation loc=" + loc);
		mLastGeoPoint = new GeoPoint(loc);
		mAccuracy = loc.getAccuracy();
		mBearing = loc.getBearing();
		mIsMoving = loc.getSpeed() > 0.278; // not moving
		mIsCurrentLocation = ((System.currentTimeMillis() - loc.getTime()) / 1000L) < 5; // old location?
		// TODO: from API17 up use: (android.os.SystemClock.elapsedRealtimeNanos() - loc.getElapsedRealtimeNanos()) / 1000000000L;
		mLoc = loc;
	}

	public void setLocation(final GeoPoint geopoint) {
		mLastGeoPoint = geopoint;
		mAccuracy = 0;
		mBearing = 0;
		mIsMoving = false;
		mIsCurrentLocation = false;
	}

	public GeoPoint getTargetLocation() {
		return mTargetLocation;
	}

	public void setTargetLocation(final GeoPoint geopoint) {
		mTargetLocation = geopoint;
	}

	public void setScale(double sizeFactor, double sizeFactorGoogle) {
		mScaleCorretion = Math.max(0, ((int)sizeFactor) - 1) + Math.max(0, ((int)sizeFactorGoogle) - 1);
		if (mScaleCorretion < 0)
			mScaleCorretion = 0;
		Ut.d("setScale mScaleCorretion=" + mScaleCorretion);
	}

	@Override
	public void onDraw(final Canvas c, final TileView tileView) {
		Ut.d("onDraw");
		if (mLastGeoPoint != null) {
			final OpenStreetMapViewProjection pj = tileView.getProjection();
			final Point screenCoords = new Point();
			pj.toPixels(mLastGeoPoint, screenCoords);
			if (mNeedCircleDistance) {
				mZoomLevel = tileView.getZoomLevel();
				mTouchScale = tileView.getTouchScale();
				int dist = SCALE[mUnits][Math.max(0, Math.min(19, mZoomLevel + 1 + (int)(mTouchScale > 1? Math.round(mTouchScale) - 1: -Math.round(1 / mTouchScale) + 1)) + mScaleCorretion)];
				final GeoPoint center = tileView.getMapCenter();
				if (mUnits == 1) {
					if (mZoomLevel < 11) {
						dist = (int)(dist * 1609.344);
					}
					else {
						dist = (int)(dist * 0.305);
					}
				}
				final GeoPoint c2 = center.calculateEndingGlobalCoordinates(center, 90, dist);
				final Point p = new Point();
				pj.toPixels(c2, p);
				mWidth = p.x - tileView.getWidth() / 2;
				c.drawCircle(screenCoords.x, screenCoords.y, mWidth, mPaintCross);
				c.drawCircle(screenCoords.x, screenCoords.y, mWidth * 2, mPaintCross);
				c.drawCircle(screenCoords.x, screenCoords.y, mWidth * 3, mPaintCross);
				c.drawCircle(screenCoords.x, screenCoords.y, mWidth * 4, mPaintCross);
			}
			if (mPrefAccuracy != 0 // not off
				&& ((mAccuracy > 0 && mPrefAccuracy == 1) // always on or
				|| (mPrefAccuracy > 1 && mAccuracy >= mPrefAccuracy)) // larger than
			) {
				int pixelRadius = (int)(tileView.getTouchScale() * mAccuracy / ((float)METER_IN_PIXEL / (1 << tileView.getZoomLevel())));
				c.drawCircle(screenCoords.x, screenCoords.y, pixelRadius, mPaintAccuracyFill);
				c.drawCircle(screenCoords.x, screenCoords.y, pixelRadius, mPaintAccuracyBorder);
			}
			if (mLineToGPS) {
				c.drawLine(screenCoords.x, screenCoords.y, tileView.getWidth() / 2, tileView.getHeight() / 2, mPaintLineToGPS);
				final GeoPoint geo = pj.fromPixels(tileView.getWidth() / 2, tileView.getHeight() / 2);
				final float dist = mLastGeoPoint.distanceTo(geo);
				final String lbl = String.format(Locale.UK, "%s %.1f°", mDf.formatDistance(dist), mLastGeoPoint.bearingTo360(geo));
				mLabelVw.setText(lbl);
				mLabelVw.measure(0, 0);
				mLabelVw.layout(0, 0, mLabelVw.getMeasuredWidth(), mLabelVw.getMeasuredHeight());
				c.save();
				c.translate(tileView.getWidth() / 2 - (screenCoords.x < tileView.getWidth() / 2? 0: mLabelVw.getMeasuredWidth()), tileView.getHeight() / 2);
				mLabelVw.draw(c);
				c.restore();
			}
			if (mTargetLocation != null) {
				final Point screenCoordsTarg = new Point();
				pj.toPixels(mTargetLocation, screenCoordsTarg);
				c.drawLine(screenCoords.x, screenCoords.y, screenCoordsTarg.x, screenCoordsTarg.y, mPaintLineToGPS);
			}
			c.save();
			if (!mIsCurrentLocation) {
				c.rotate(tileView.getBearing(), screenCoords.x, screenCoords.y);
				if (mNolocationIcon == null)
					mNolocationIcon = IconManager.getInstance(mCtx).getNolocationIcon();
				c.drawBitmap(mNolocationIcon, screenCoords.x - mNolocationIcon.getWidth() / 2, screenCoords.y - mNolocationIcon.getHeight() / 2, mPaint);
			}
			else if (!mIsMoving) {
				c.rotate(tileView.getBearing(), screenCoords.x, screenCoords.y);
				if (mLocationIcon == null)
					mLocationIcon = IconManager.getInstance(mCtx).getLocationIcon();
				c.drawBitmap(mLocationIcon, screenCoords.x - mLocationIcon.getWidth() / 2, screenCoords.y - mLocationIcon.getHeight() / 2, mPaint);
			}
			else {
				c.rotate(mBearing, screenCoords.x, screenCoords.y);
				if (mArrow == null)
					mArrow = IconManager.getInstance(mCtx).getArrowIcon();
				c.drawBitmap(mArrow, screenCoords.x - mArrow.getWidth() / 2, screenCoords.y - mArrow.getHeight() / 2, mPaint);
			}
			c.restore();
		}
		if (mTargetLocation != null) {
			final OpenStreetMapViewProjection pj = tileView.getProjection();
			final Point screenCoordsTarg = new Point();
			pj.toPixels(mTargetLocation, screenCoordsTarg);
			if (mTargetIcon == null)
				mTargetIcon = IconManager.getInstance(mCtx).getTargetIcon();
			c.drawBitmap(mTargetIcon, screenCoordsTarg.x - mTargetIcon.getWidth() / 2, screenCoordsTarg.y - mTargetIcon.getHeight() / 2, mPaint);
		}
		final int x = tileView.getWidth() / 2;
		final int y = tileView.getHeight() / 2;
		if (mNeedCrosshair) {
			if (false) { // line cross?
				c.drawLine(x - CROSS_SIZE, y, x + CROSS_SIZE, y, mPaintCross);
				c.drawLine(x, y - CROSS_SIZE, x, y + CROSS_SIZE, mPaintCross);
			}
			else {
				final int dx = mCenterCross.getIntrinsicWidth();
				final int dy = mCenterCross.getIntrinsicHeight();
				final int left = x - dx/2;
				final int right = left + dx;
				final int top = y - dy/2;
				final int bottom = top + dy;
				mCenterCross.setBounds(left, top, right, bottom);
				mCenterCross.draw(c);
			}
		}
	}
}