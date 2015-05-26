package org.pyneo.maps.track;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
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
import org.pyneo.maps.utils.Storage;
import org.pyneo.maps.utils.CursorI;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackStorage extends Storage implements Constants {
	public TrackStorage(Context ctx) {
		super(ctx);
	}

	public void addTrackPoint(final long trackid, final double lat, final double lon, final double alt, final double speed, final Date date) {
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

	public Cursor getActivityListCursor() {
		if (isDatabaseReady()) {
			// Not change the order of the fields
			return mDatabase.rawQuery(STAT_ActivityList, null);
		}

		return null;
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

	public static final String STAT_get_maps = "SELECT mapid, name, type, params FROM 'maps';";
	public CursorI getMixedMaps() {
		Cursor ret = null;
		if (isDatabaseReady())
			ret = mDatabase.rawQuery(STAT_get_maps, null);
		return new CursorI(ret);
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
}
