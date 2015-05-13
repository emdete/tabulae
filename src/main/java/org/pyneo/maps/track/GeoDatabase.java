package org.pyneo.maps.track;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import android.database.sqlite.SQLiteOpenHelper;

import org.pyneo.maps.R;
import org.pyneo.maps.poi.Constants;
import org.pyneo.maps.utils.Ut;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeoDatabase implements Constants {
	protected final Context mCtx;
	@SuppressLint("SimpleDateFormat")
	protected final SimpleDateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private SQLiteDatabase mDatabase;

	public GeoDatabase(Context ctx) {
		super();
		mCtx = ctx;
		mDatabase = getDatabase();
	}

	public void addPoi(final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId, final int aPointSourceId, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, aName);
			cv.put(DESCR, aDescr);
			cv.put(LAT, aLat);
			cv.put(LON, aLon);
			cv.put(ALT, aAlt);
			cv.put(CATEGORYID, aCategoryId);
			cv.put(POINTSOURCEID, aPointSourceId);
			cv.put(HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(ICONID, 0);
			}
			else
				cv.put(ICONID, iconid);
			this.mDatabase.insert(POINTS, null, cv);
		}
	}

	public void updatePoi(final int id, final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId, final int aPointSourceId, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, aName);
			cv.put(DESCR, aDescr);
			cv.put(LAT, aLat);
			cv.put(LON, aLon);
			cv.put(ALT, aAlt);
			cv.put(CATEGORYID, aCategoryId);
			cv.put(POINTSOURCEID, aPointSourceId);
			cv.put(HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(ICONID, 0);
			}
			else
				cv.put(ICONID, iconid);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(POINTS, cv, UPDATE_POINTS, args);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (mDatabase != null) {
			if (mDatabase.isOpen()) {
				mDatabase.close();
				mDatabase = null;
			}
		}
		super.finalize();
	}

	public Cursor getPoiListCursor() {
		return getPoiListCursor("lat, lon");
	}

	public Cursor getPoiListCursor(String sortColNames) {
		if (isDatabaseReady()) {
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_GET_POI_LIST + sortColNames, null);
		}

		return null;
	}

	public Cursor getPoiListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(zoom + 1), Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_PoiListNotHidden, args);
		}

		return null;
	}

	public Cursor getPoiCategoryListCursor() {
		if (isDatabaseReady()) {
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_PoiCategoryList, null);
		}

		return null;
	}

	public Cursor getActivityListCursor() {
		if (isDatabaseReady()) {
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_ActivityList, null);
		}

		return null;
	}

	public Cursor getPoi(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(id)};
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_getPoi, args);
		}

		return null;
	}

	public void deletePoi(final int id) {
		if (isDatabaseReady()) {
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(STAT_deletePoi, args);
		}
	}

	public void deletePoiCategory(final int id) {
		if (isDatabaseReady() && id != ZERO) { // predef category My POI never delete
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(STAT_deletePoiCategory, args);
		}
	}

	private boolean isDatabaseReady() {
		boolean ret = true;

		if (mDatabase == null)
			mDatabase = getDatabase();

		if (mDatabase == null)
			ret = false;
		else if (!mDatabase.isOpen())
			mDatabase = getDatabase();

		if (!ret)
			try {
				Toast.makeText(mCtx, mCtx.getText(R.string.message_geodata_notavailable), Toast.LENGTH_LONG).show();
			}
			catch (Exception e) {
				Ut.e(e.toString(), e);
			}

		return ret;
	}

	public void FreeDatabases() {
		if (mDatabase != null) {
			if (mDatabase.isOpen()) {
				mDatabase.close();
			}
			mDatabase = null;
		}
	}

	protected SQLiteDatabase getDatabase() {
		File folder = Ut.getAppMainDir(mCtx, DATA);
		if (folder.exists()) {
			folder = new File(folder, GEODATA_FILENAME);
			try {
				Ut.i("getDatabase folder=" + folder.getAbsolutePath());
				return new GeoDatabaseHelper(mCtx, folder.getAbsolutePath()).getWritableDatabase();
			}
			catch (Exception e) {
				Ut.e(e.toString(), e);
			}
		}
		return null;
	}

	public Cursor getPoiCategory(final int id) {
		if (isDatabaseReady()) {
			// Not change the order of the fields
			final String[] args = {Integer.toString(id)};
			return mDatabase.rawQuery(STAT_getPoiCategory, args);
		}
		return null;
	}

	public void LoadActivityListFromResource(final SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_drop_activity);
		db.execSQL(SQL_CREATE_activity);
		String[] act = mCtx.getResources().getStringArray(R.array.track_activity);
		for (int i = 0; i < act.length; i++) {
			db.execSQL(String.format(SQL_CREATE_insert_activity, i, act[i]));
		}
	}

	public long addPoiCategory(final String title, final int hidden, final int iconid) {
		long newId = -1;

		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, title);
			cv.put(HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(ICONID, 0);
			}
			else
				cv.put(ICONID, iconid);
			newId = this.mDatabase.insert(CATEGORY, null, cv);
		}

		return newId;
	}

	public void updatePoiCategory(final int id, final String title, final int hidden, final int iconid, final int minzoom) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, title);
			cv.put(HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(ICONID, 0);
			}
			else
				cv.put(ICONID, iconid);
			cv.put(MINZOOM, minzoom);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(CATEGORY, cv, UPDATE_CATEGORY, args);
		}
	}

	public void DeleteAllPoi() {
		if (isDatabaseReady()) {
			mDatabase.execSQL(org.andnav.osm.util.Constants.STAT_DeleteAllPoi);
		}
	}

	public void beginTransaction() {
		mDatabase.beginTransaction();
	}

	public void rollbackTransaction() {
		mDatabase.endTransaction();
	}

	public void commitTransaction() {
		mDatabase.setTransactionSuccessful();
		mDatabase.endTransaction();
	}

	public Cursor getTrackListCursor(final String units) {
		return getTrackListCursor(units, "trackid DESC");
	}

	public Cursor getTrackListCursor(final String units, final String sortColNames) {
		if (isDatabaseReady()) {
			// Not change the order of the fields
			return mDatabase.rawQuery(String.format(STAT_getTrackList + sortColNames, units), null);
		}

		return null;
	}

	public long addTrack(final String name, final String descr, final int show, final int cnt, final double distance,
						 final double duration, final int category, final int activity, final Date date, final String style) {
		long newId = -1;

		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(DESCR, descr);
			cv.put(SHOW, show);
			cv.put(CNT, cnt);
			cv.put(DISTANCE, distance);
			cv.put(DURATION, duration);
			cv.put(CATEGORYID, category);
			cv.put(ACTIVITY, activity);
			cv.put(DATE, date.getTime() / 1000);
			cv.put(STYLE, style);
			newId = this.mDatabase.insert(TRACKS, null, cv);
		}

		return newId;
	}

	public void updateTrack(final int id, final String name, final String descr, final int show, final int cnt, final double distance, final double duration, final int category, final int activity, final Date date, final String style) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(DESCR, descr);
			cv.put(SHOW, show);
			cv.put(CNT, cnt);
			cv.put(DISTANCE, distance);
			cv.put(DURATION, duration);
			cv.put(CATEGORYID, category);
			cv.put(ACTIVITY, activity);
			cv.put(DATE, date.getTime() / 1000);
			cv.put(STYLE, style);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args);
		}
	}

	public void addTrackPoint(final long trackid, final double lat,
							  final double lon, final double alt, final double speed,
							  final Date date) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(TRACKID, trackid);
			cv.put(LAT, lat);
			cv.put(LON, lon);
			cv.put(ALT, alt);
			cv.put(SPEED, speed);
			cv.put(DATE, date.getTime() / 1000);
			this.mDatabase.insert(TRACKPOINTS, null, cv);
		}
	}

	public Cursor getTrackChecked() {
		if (isDatabaseReady()) {
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_getTrackChecked, null);
		}

		return null;
	}

	public void setTrackChecked(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_setTrackChecked_1, args);
			//mDatabase.execSQL(STAT_setTrackChecked_2, args);
		}
	}

	public Cursor getTrack(final long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_getTrack, args);
		}

		return null;
	}

	public Cursor getTrackPoints(final long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_getTrackPoints, args);
		}

		return null;
	}

	public void setCategoryHidden(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_setCategoryHidden, args);
		}
	}

	public void deleteTrack(final int id) {
		if (isDatabaseReady()) {
			beginTransaction();
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_deleteTrack_1, args);
			mDatabase.execSQL(STAT_deleteTrack_2, args);
			commitTransaction();
		}
	}

	public long JoinTracks() {
		final Cursor ctc = getTrackChecked();
		if (ctc == null) return -1;
		if (ctc.getCount() < 2) {
			ctc.close();
			return -1;
		}
		ctc.close();

		final ContentValues cv = new ContentValues();
		cv.put(NAME, mCtx.getString(R.string.track));
		cv.put(SHOW, 0);
		cv.put(ACTIVITY, 0);
		cv.put(CATEGORYID, 0);
		final long newId = mDatabase.insert(TRACKS, null, cv);
		cv.put(NAME, mCtx.getString(R.string.track) + ONE_SPACE + newId);

		mDatabase.execSQL(String.format("INSERT INTO 'trackpoints' (trackid, lat, lon, alt, speed, date) SELECT %d, lat, lon, alt, speed, date FROM 'trackpoints' WHERE trackid IN (SELECT trackid FROM 'tracks' WHERE show = 1) ORDER BY date", newId));
		final String[] args = {Long.toString(newId)};
		final Cursor c = mDatabase.rawQuery("SELECT MIN(date) FROM 'trackpoints' WHERE trackid = @1", args);
		if (c != null) {
			if (c.moveToFirst()) {
				cv.put(DATE, c.getDouble(0));
			}
			c.close();
		}
		final String[] args2 = {Long.toString(newId)};
		mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args2);

		return newId;
	}

	public int saveTrackFromWriter(final SQLiteDatabase db) {
		int res = 0;
		if (isDatabaseReady()) {
			final Cursor c = db.rawQuery(STAT_saveTrackFromWriter, null);
			if (c != null) {
				if (c.getCount() > 1) {
					beginTransaction();

					res = c.getCount();
					long newId = -1;

					final ContentValues cv = new ContentValues();
					cv.put(NAME, mCtx.getString(R.string.track));
					cv.put(SHOW, 0);
					cv.put(ACTIVITY, 0);
					cv.put(CATEGORYID, 0);
					newId = mDatabase.insert(TRACKS, null, cv);
					res = (int)newId;

					cv.put(NAME, mCtx.getString(R.string.track) + ONE_SPACE + newId);
					if (c.moveToFirst()) {
						cv.put(DATE, c.getInt(4));
					}
					final String[] args = {Long.toString(newId)};
					mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args);

					if (c.moveToFirst()) {
						do {
							cv.clear();
							cv.put(TRACKID, newId);
							cv.put(LAT, c.getDouble(0));
							cv.put(LON, c.getDouble(1));
							cv.put(ALT, c.getDouble(2));
							cv.put(SPEED, c.getDouble(3));
							cv.put(DATE, c.getInt(4));
							mDatabase.insert(TRACKPOINTS, null, cv);
						} while (c.moveToNext());
					}

					commitTransaction();
				}
				c.close();

				db.execSQL(STAT_CLEAR_TRACKPOINTS);
			}

		}

		return res;
	}

	public Cursor getMixedMaps() {
		if (isDatabaseReady())
			return mDatabase.rawQuery(STAT_get_maps, null);
		return null;
	}

	public long addMap(int type, String params) {
		long newId = -1;

		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, "New map");
			cv.put(TYPE, type);
			cv.put(PARAMS, params);
			newId = this.mDatabase.insert(MAPS, null, cv);
		}

		return newId;
	}

	public Cursor getMap(long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_get_map, args);
		}
		return null;
	}

	public void updateMap(long id, String name, int type, String params) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(TYPE, type);
			cv.put(PARAMS, params);
			final String[] args = {Long.toString(id)};
			this.mDatabase.update(MAPS, cv, UPDATE_MAPS, args);
		}
	}

	public void deleteMap(long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			mDatabase.delete(MAPS, UPDATE_MAPS, args);
		}
	}

	protected class GeoDatabaseHelper extends SQLiteOpenHelper {
		private final static int mCurrentVersion = 22;

		public GeoDatabaseHelper(final Context context, final String name) {
			super(context, name, null, mCurrentVersion);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_points);
			db.execSQL(SQL_CREATE_pointsource);
			db.execSQL(SQL_CREATE_category);
			db.execSQL(SQL_ADD_category);
			db.execSQL(SQL_CREATE_tracks);
			db.execSQL(SQL_CREATE_trackpoints);
			db.execSQL(SQL_CREATE_maps);
			db.execSQL(SQL_CREATE_routes);
			LoadActivityListFromResource(db);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
//			Ut.dd("Upgrade data.db from ver." + oldVersion + " to ver."
//					+ newVersion);

			if (oldVersion < 2) {
				db.execSQL(SQL_UPDATE_1_1);
				db.execSQL(SQL_UPDATE_1_2);
				db.execSQL(SQL_UPDATE_1_3);
				db.execSQL(SQL_CREATE_points);
				db.execSQL(SQL_UPDATE_1_5);
				db.execSQL(SQL_UPDATE_1_6);
				db.execSQL(SQL_UPDATE_1_7);
				db.execSQL(SQL_UPDATE_1_8);
				db.execSQL(SQL_UPDATE_1_9);
				db.execSQL(SQL_CREATE_category);
				db.execSQL(SQL_ADD_category);
				//db.execSQL(SQL_UPDATE_1_11);
				//db.execSQL(SQL_UPDATE_1_12);
			}
			if (oldVersion < 3) {
				db.execSQL(SQL_UPDATE_2_7);
				db.execSQL(SQL_UPDATE_2_8);
				db.execSQL(SQL_UPDATE_2_9);
				db.execSQL(SQL_CREATE_category);
				db.execSQL(SQL_UPDATE_2_11);
				db.execSQL(SQL_UPDATE_2_12);
			}
			if (oldVersion < 5) {
				db.execSQL(SQL_CREATE_tracks);
				db.execSQL(SQL_CREATE_trackpoints);
			}
			if (oldVersion < 18) {
				db.execSQL(SQL_UPDATE_6_1);
				db.execSQL(SQL_UPDATE_6_2);
				db.execSQL(SQL_UPDATE_6_3);
				db.execSQL(SQL_CREATE_tracks);
				db.execSQL(SQL_UPDATE_6_4);
				db.execSQL(SQL_UPDATE_6_5);
				LoadActivityListFromResource(db);
			}
			if (oldVersion < 20) {
				db.execSQL(SQL_UPDATE_6_1);
				db.execSQL(SQL_UPDATE_6_2);
				db.execSQL(SQL_UPDATE_6_3);
				db.execSQL(SQL_CREATE_tracks);
				db.execSQL(SQL_UPDATE_20_1);
				db.execSQL(SQL_UPDATE_6_5);
			}
			if (oldVersion < 21) {
				db.execSQL(SQL_CREATE_maps);
			}
			if (oldVersion < 22) {
				db.execSQL(SQL_CREATE_routes);
			}
		}

	}

}
