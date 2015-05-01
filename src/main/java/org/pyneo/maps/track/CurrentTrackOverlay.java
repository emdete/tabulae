package org.pyneo.maps.track;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import org.pyneo.maps.MainActivity;
import org.pyneo.maps.map.TileView;
import org.pyneo.maps.map.TileViewOverlay;
import org.pyneo.maps.track.DatabaseHelper;
import org.pyneo.maps.poi.PoiManager;
import org.pyneo.maps.track.Track;
import org.pyneo.maps.track.TrackWriterService;
import org.pyneo.maps.track.IRemoteService;
import org.pyneo.maps.track.ITrackWriterCallback;
import org.pyneo.maps.utils.SimpleThreadFactory;
import org.pyneo.maps.utils.Ut;

import org.andnav.osm.util.GeoPoint;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* Overlay to show the current track during writing. When onDraw is called it
* will fire a thread to initially load the already written track. It will
* connect to the trackwriter service to receive newly written positions.
*/
public class CurrentTrackOverlay extends TileViewOverlay {
	private ExecutorService mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("CurrentTrack"));
	private IRemoteService mService = null;
	private Paint mPaint = new Paint();
	private int mLastZoom = -1;
	private Path mPath = null;
	private Track mTrack = null;
	private Point mBaseCoords = new Point();
	private GeoPoint mBaseLocation = new GeoPoint(0, 0);
	private TileView.OpenStreetMapViewProjection mBasePj = null;
	private boolean mInitialLoading = false;
	private TileView mOsmv = null;
	private Context mContext = null;
	private boolean mIsBound = false;
	private ServiceConnection mConnection = null;
	private Thread mInitialLoader = new Thread() {
		// setName("Track InitialLoader Thread");
		@Override
		public void run() {
			mPath = null;
			if (mTrack == null) {
				mTrack = new Track();
			}
			else {
				mTrack.getPoints().clear();
			}
			final File folder = Ut.getAppMainDir(mContext, "data");
			if (folder.canRead()) {
				SQLiteDatabase db = null;
				try {
					db = new DatabaseHelper(mContext, folder.getAbsolutePath() + "/writedtrack.db").getReadableDatabase();
				}
				catch (Exception e) {
					db = null;
					Ut.e(e.toString(), e);
				}
				if (db != null) {
					final Cursor c = db.rawQuery("SELECT lat, lon FROM trackpoints ORDER BY id", null);
					mPath = mBasePj.toPixelsTrackPoints(c, mBaseCoords, mBaseLocation);
					if (mPath != null && mPath.isEmpty())
						mPath = null;
					db.close();
				}
			}
			try {
				Message.obtain(mOsmv.getHandler(), Ut.MAPTILEFSLOADER_SUCCESS_ID).sendToTarget();
			}
			catch (Exception e) {
				Ut.e(e.toString(), e);
			}
			mInitialLoading = false;
		}
	};
	private ITrackWriterCallback mCallback = new ITrackWriterCallback.Stub() {
		public void newPointWritten(double lat, double lon) {
			Ut.d("newPointWritten lat=" + lat + ", lon=" + lon + ", mPath=" + mPath);
			if (mInitialLoading)
				return;
			if (mPath == null) {
				mPath = new Path();
				mBaseLocation = new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
				mBasePj = mOsmv.getProjection();
				mBaseCoords = mBasePj.toPixels2(mBaseLocation);
				mPath.setLastPoint(mBaseCoords.x, mBaseCoords.y);
			}
			else {
				final GeoPoint geopoint = new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
				final Point point = mBasePj.toPixels2(geopoint);
				mPath.lineTo(point.x, point.y);
			}
		}

		@Override
		public void onTrackStatUpdate(int Cnt, double Distance, long Duration,
			double MaxSpeed, double AvgSpeed, long MoveTime, double AvgMoveSpeed)
			throws RemoteException {
		}

	};

	public CurrentTrackOverlay(MainActivity mainMapActivity, PoiManager poiManager) {
		mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				Ut.i("onServiceConnected: registerCallback");
				mService = IRemoteService.Stub.asInterface(service);
				try {
					mService.registerCallback(mCallback);
				}
				catch (RemoteException e) {
					Ut.e(e.toString(), e);
				}
			}

			public void onServiceDisconnected(ComponentName className) {
				mService = null;
			}
		};
		mTrack = new Track(PreferenceManager.getDefaultSharedPreferences(mainMapActivity).getString("pref_track_style_current", ""));
		mContext = mainMapActivity;
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setColor(mTrack.Color);
		mPaint.setStrokeWidth(mTrack.Width);
		mPaint.setAlpha(Color.alpha(mTrack.ColorShadow));
		mPaint.setShadowLayer((float)mTrack.ShadowRadius, 0, 0, mTrack.ColorShadow);
	}

	@Override
	public void Free() {
		if (mBasePj != null) {
			mBasePj.StopProcessing();
		}
		mThreadExecutor.shutdown();
		super.Free();
	}

	@Override
	protected void onDraw(Canvas c, TileView osmv) {
		if (mInitialLoading) // still loading path?
			return;
		if (mTrack == null || mLastZoom != osmv.getZoomLevel()) { // already written path not loaded?
			mInitialLoading = true;
			mOsmv = osmv;
			mLastZoom = osmv.getZoomLevel();
			mBasePj = mOsmv.getProjection();
			mThreadExecutor.execute(mInitialLoader);
			return;
		}
		if (mPath == null) // path empty?
			return;
		final TileView.OpenStreetMapViewProjection pj = osmv.getProjection();
		final Point screenCoords = new Point();
		pj.toPixels(mBaseLocation, screenCoords);
		c.save();
		if (screenCoords.x != mBaseCoords.x && screenCoords.y != mBaseCoords.y) {
			c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y - mBaseCoords.y);
			c.scale((float)osmv.mTouchScale, (float)osmv.mTouchScale, mBaseCoords.x, mBaseCoords.y);
		}
		c.drawPath(mPath, mPaint);
		c.restore();
	}

	@Override
	protected void onDrawFinished(Canvas c, TileView osmv) {
	}

	public void onResume() {
		mTrack = null;
		if (!(mIsBound = mContext.bindService(new Intent(mContext, TrackWriterService.class), mConnection, 0 /*Context.BIND_AUTO_CREATE*/))) {
			Ut.e("bindService failed class=" + TrackWriterService.class);
		}
	}

	public void onPause() {
		if (mIsBound) {
			mIsBound = false;
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					mService.unregisterCallback(mCallback);
				}
				catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
					Ut.e(e.toString(), e);
				}
			}
			// Detach our existing connection.
			mContext.unbindService(mConnection);
		}
	}
}
