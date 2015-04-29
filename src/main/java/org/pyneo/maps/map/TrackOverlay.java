package org.pyneo.maps.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;

import org.pyneo.maps.MainActivity;
import org.pyneo.maps.poi.PoiManager;
import org.pyneo.maps.track.Track;
import org.pyneo.maps.utils.SimpleThreadFactory;
import org.pyneo.maps.utils.Ut;

import org.andnav.osm.util.GeoPoint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrackOverlay extends TileViewOverlay {
	protected ExecutorService mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("TrackOverlay"));
	private Paint[] mPaints;
	private int mLastZoom;
	private Path[] mPaths;
	private Track[] mTracks;
	private Point mBaseCoords;
	private GeoPoint mBaseLocation;
	private PoiManager mPoiManager;
	private TrackThread mThread;
	private boolean mThreadRunned = false;
	private TileView mOsmv;
	private Handler mMainMapActivityCallbackHandler;
	private boolean mStopDraw = false;
	private org.pyneo.maps.map.TileView.OpenStreetMapViewProjection mProjection;

	public TrackOverlay(MainActivity mainActivity, PoiManager poiManager, Handler aHandler) {
		mMainMapActivityCallbackHandler = aHandler;
		mTracks = null;
		mPoiManager = poiManager;
		mBaseCoords = new Point();
		mBaseLocation = new GeoPoint(0, 0);
		mLastZoom = -1;
		mThread = new TrackThread();
		mThread.setName("Track thread");
	}

	@Override
	public void Free() {
		if (mPoiManager != null)
			mPoiManager.StopProcessing();
		if (mProjection != null)
			mProjection.StopProcessing();
		mThreadExecutor.shutdown();
		super.Free();
	}

	public void setStopDraw(boolean stopdraw) {
		mStopDraw = stopdraw;
	}

	@Override
	protected void onDraw(Canvas c, TileView osmv) {
		if (mStopDraw) return;

		if (!mThreadRunned && (mTracks == null || mLastZoom != osmv.getZoomLevel())) {
			mPaths = null;
			mLastZoom = osmv.getZoomLevel();
			mOsmv = osmv;
			mProjection = mOsmv.getProjection();
			mThreadRunned = true;
			mThreadExecutor.execute(mThread);
			return;
		}

		if (mPaths == null)
			return;

		final org.pyneo.maps.map.TileView.OpenStreetMapViewProjection pj = osmv.getProjection();
		final Point screenCoords = new Point();

		pj.toPixels(mBaseLocation, screenCoords);

		c.save();
		if (screenCoords.x != mBaseCoords.x && screenCoords.y != mBaseCoords.y) {
			c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y - mBaseCoords.y);
			c.scale((float)osmv.mTouchScale, (float)osmv.mTouchScale, mBaseCoords.x, mBaseCoords.y);
		}
		;
		for (int i = 0; i < mPaths.length; i++)
			if (mPaths[i] != null && mPaints[i] != null)
				c.drawPath(mPaths[i], mPaints[i]);
		c.restore();
	}

	@Override
	protected void onDrawFinished(Canvas c, TileView osmv) {
	}

	public void clearTrack() {
		mTracks = null;
	}

	private class TrackThread extends Thread {

		@Override
		public void run() {
			Ut.d("run TrackThread");

			mPaths = null;

			if (mTracks == null) {
				mTracks = mPoiManager.getTrackChecked(false);
				if (mTracks == null) {
					Ut.d("Track is null. Stopped??");
					mThreadRunned = false;
					mStopDraw = true;
					return;
				}
				Ut.d("Track loaded");
			}

			try {
				mPaths = new Path[mTracks.length];
				mPaints = new Paint[mTracks.length];

				for (int i = 0; i < mTracks.length; i++) {
					if (mTracks[i] != null) {
						try {
							mPaths[i] = mProjection.toPixelsTrackPoints(mPoiManager.getGeoDatabase().getTrackPoints(mTracks[i].getId()), mBaseCoords, mBaseLocation);
							mPaints[i] = new Paint();
							mPaints[i].setAntiAlias(true);
							mPaints[i].setStyle(Paint.Style.STROKE);
							mPaints[i].setStrokeCap(Paint.Cap.ROUND);
							mPaints[i].setColor(mTracks[i].Color);
							mPaints[i].setStrokeWidth(mTracks[i].Width);
							mPaints[i].setAlpha(Color.alpha(mTracks[i].ColorShadow));
							mPaints[i].setShadowLayer((float)mTracks[i].ShadowRadius, 0, 0, mTracks[i].ColorShadow);

							Message.obtain(mMainMapActivityCallbackHandler, Ut.MAPTILEFSLOADER_SUCCESS_ID).sendToTarget();
						}
						catch (Exception e) {
							mPaths[i] = null;
						}
					} else
						mPaths[i] = null;
				}
			}
			catch (Exception e) {
				Ut.e(e.toString(), e);
			}

			mThreadRunned = false;
		}
	}

}
