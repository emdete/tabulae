package org.pyneo.maps.applib.tileprovider;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Message;

import org.pyneo.maps.applib.R;
import org.pyneo.maps.applib.utils.RException;
import org.pyneo.maps.applib.utils.SQLiteMapDatabase;
import org.pyneo.maps.applib.utils.SimpleThreadFactory;
import org.pyneo.maps.applib.utils.Ut;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TileProviderSQLITEDB extends TileProviderFileBase {
	private ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new SimpleThreadFactory("TileProviderSQLITEDB"));
	private SQLiteMapDatabase mUserMapDatabase;
	private String mMapID;
	private ProgressDialog mProgressDialog;
	private HashMap<String, XYZ> mPending2 = new HashMap<String, XYZ>();

	public TileProviderSQLITEDB(Context ctx, final String filename, final String mapid, MapTileMemCache aTileCache) throws SQLiteException, RException {
		super(ctx);
		mTileURLGenerator = new TileURLGeneratorBase(filename);
		mTileCache = aTileCache == null? new MapTileMemCache(): aTileCache;
		mUserMapDatabase = new SQLiteMapDatabase();
		mUserMapDatabase.setFile(filename);
		mMapID = mapid;

		final File file = new File(filename);
		Ut.d("TileProviderSQLITEDB: mapid = " + mapid);
		Ut.d("TileProviderSQLITEDB: filename = " + filename);
		Ut.d("TileProviderSQLITEDB: file.exists = " + file.exists());
		Ut.d("TileProviderSQLITEDB: getAppMapsDir = " + Ut.getAppMapsDir(ctx));
		if (needIndex(mapid, file.length(), file.lastModified(), false)) {
			mProgressDialog = Ut.ShowWaitDialog(ctx, R.string.message_updateminmax);
			new IndexTask().execute(file.length(), file.lastModified());
		}

		this.mThreadPool.execute(new Runnable() {
			public void run() {
				XYZ xyz = null;
				Collection<XYZ> col = null;
				Iterator<XYZ> it = null;
				byte[] data = null;
				Bitmap bmp = null;

				while (!mThreadPool.isShutdown()) {
					synchronized (mPending2) {
						col = mPending2.values();
						it = col.iterator();
						if (it.hasNext())
							xyz = it.next();
						else
							xyz = null;
					}

					if (xyz == null) {
						synchronized (mPending2) {
							try {
								SendMessageSuccess();
								mPending2.wait();
							}
							catch (InterruptedException e) {
							}
						}
					} else {
						data = mUserMapDatabase.getTile(xyz.X, xyz.Y, xyz.Z);

						if (data != null) {
							try {
								bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
								mTileCache.putTile(xyz.TILEURL, bmp);
							}
							catch (Throwable e) {
								e.printStackTrace();
							}
						}

						synchronized (mPending2) {
							mPending2.remove(xyz.TILEURL);
						}
					}

				}
			}
		});
	}

	public void updateMapParams(TileSource tileSource) {
		tileSource.ZOOM_MINLEVEL = ZoomMinInCashFile(mMapID);
		tileSource.ZOOM_MAXLEVEL = ZoomMaxInCashFile(mMapID);
	}

	@Override
	public void Free() {
		mThreadPool.shutdown();
		synchronized (mPending2) {
			mPending2.notify();
		}
		mUserMapDatabase.Free();
		super.Free();
	}

	public Bitmap getTile(final int x, final int y, final int z) {
		final String tileurl = mTileURLGenerator.Get(x, y, z);

		final Bitmap bmp = mTileCache.getMapTile(tileurl);
		if (bmp != null)
			return bmp;

		synchronized (mPending2) {
			if (this.mPending2.containsKey(tileurl))
				return mLoadingMapTile;
		}

		synchronized (mPending2) {
			mPending2.put(tileurl, new XYZ(tileurl, x, y, z));
			mPending2.notify();
		}

		return mLoadingMapTile;
	}

	public int[] findTheMap(int zoomLevel) {
		return mUserMapDatabase.findTheMap(zoomLevel);
	}

	private class IndexTask extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			try {
				final long fileLength = params[0];
				Ut.d("IndexTask: fileLength = " + fileLength);
				final long fileModified = params[1];
				Ut.d("IndexTask: fileModified = " + fileModified);
				mUserMapDatabase.updateMinMaxZoom();
				Ut.d("IndexTask: mUserMapDatabase.updateMinMaxZoom = OK");
				final int minzoom = mUserMapDatabase.getMinZoom();
				Ut.d("IndexTask: minzoom = " + minzoom);
				final int maxzoom = mUserMapDatabase.getMaxZoom();
				Ut.d("IndexTask: maxzoom = " + maxzoom);

				CommitIndex(mMapID, fileLength, fileModified, minzoom, maxzoom);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result && mCallbackHandler != null)
				Message.obtain(mCallbackHandler, MessageHandlerConstants.MAPTILEFSLOADER_INDEXIND_SUCCESS_ID).sendToTarget();
			try {
				if (mProgressDialog != null)
					mProgressDialog.dismiss();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class XYZ {
		public String TILEURL;
		public int X;
		public int Y;
		public int Z;

		public XYZ(final String tileurl, final int x, final int y, final int z) {
			TILEURL = tileurl;
			X = x;
			Y = y;
			Z = z;
		}
	}

}
