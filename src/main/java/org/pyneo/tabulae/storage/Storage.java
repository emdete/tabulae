package org.pyneo.tabulae.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.poi.PoiItem;
import org.pyneo.tabulae.track.TrackItem;
import org.pyneo.tabulae.track.TrackPointItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** a storage class to store projos into the database. it contains all
 * information to take care of the database interface, the pojos itself dont
 * have any persistance functionality */
public class Storage extends SQLiteOpenHelper implements Constants {
	private final static int mCurrentVersion = 1;
	private final static String ID = "tabulae";

	/* these are enums that are "misused" - they hold the names of the columns
	 * of a table in the database. sqlite does not control colum types but
	 * colum types per records so you don't neet do specify any when creating
	 * the table. that's why just names are enough. furthermore the first will
	 * be the primay key and must named "id" if android shall autofill it. the
	 * second is meant as a uniq human identifier. */
	public enum activity {id, name, description, };
	public enum category {id, name, description, visible, iconid, minzoom, };
	public enum points {id, name, description, latitude, longitude, altitude, visible, categoryid, iconid, };
	public enum trackpoints {id, sequence, trackid, latitude, longitude, altitude, speed, timestamp, attribute, };
	public enum tracks {id, name, description, timestamp, timezone, visible, pointcount, duration, distance, categoryid, activityid, crop_to, crop_from, };

	/** the storage retrieves the location of the database by calling
	 * getBaseDir from Tabulae */
	public Storage(final Tabulae context) {
		super(context, new File(context.getBaseDir(), ID + ".db").getAbsolutePath(), null, mCurrentVersion);
		if (DEBUG) Log.d(TAG, "db=" + new File(context.getBaseDir(), ID + ".db").getAbsolutePath());
	}

	@Override public void onCreate(final SQLiteDatabase db) {
		db.execSQL(createStatement(activity.class, activity.values()));
		db.execSQL(createStatement(category.class, category.values()));
		db.execSQL(createStatement(points.class, points.values()));
		db.execSQL(createStatement(trackpoints.class, trackpoints.values()));
		db.execSQL(createStatement(tracks.class, tracks.values()));
	}

	@Override public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		if (newVersion != mCurrentVersion) {
			Log.e(TAG, "onUpgrade oldVersion=" + oldVersion + ", newVersion=" + newVersion + ", mCurrentVersion=" + mCurrentVersion);
		}
		switch (oldVersion) {
			case 1:
			break;
		}
	}

	/** android needs a list of string names of the columns for queries. this
	 * method copies enums to strings for that purpose */
	static String[] toStrings(final Enum[] cols) {
		int i = 0;
		String[] ret = new String[cols.length];
		for (Enum col : cols) {
			ret[i++] = col.name();
		}
		return ret;
	}

	/** constructs a create statement from a enum. */
	static public String createStatement(Class table, Enum[] cols) {
		String stmnt = "CREATE TABLE ";
		stmnt += table.getSimpleName();
		stmnt += " (";
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) stmnt += ", ";
			stmnt += cols[i].name();
			switch (i) {
				case 0: stmnt += " PRIMARY KEY"; break; // db ident
				case 1: stmnt += " NOT NULL UNIQUE"; break; // human readable name
			}
		}
		if (table == trackpoints.class) {
			stmnt += ", FOREIGN KEY(trackid) REFERENCES tracks(id)";
		}
		stmnt += ")";
		if (DEBUG) Log.d(TAG, "stmnt=" + stmnt);
		return stmnt;
	}

	long exists(String table, String name) {
		SQLiteDatabase db = getReadableDatabase();
		try {
			for (Cursor cursor : new CursorI(db.query(table, new String[]{points.id.name()}, points.name.name() + " = ?", new String[]{name}, null, null, null))) {
				return cursor.getLong(0);
			}
		}
		finally {
			db.close();
		}
		return -1;
	}

	// points:
	public List<PoiItem> getVisiblePoints() {
		List<PoiItem> ret = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		try {
			for (Cursor cursor : new CursorI(db.query(points.class.getSimpleName(), toStrings(points.values()), points.visible.name() + " != 0", null, null, null, null))) {
				ret.add(new PoiItem(
					cursor.getInt(points.id.ordinal()),
					cursor.getString(points.name.ordinal()),
					cursor.getString(points.description.ordinal()),
					cursor.getDouble(points.latitude.ordinal()),
					cursor.getDouble(points.longitude.ordinal()),
					cursor.getInt(points.visible.ordinal()) != 0
					));
			}
		}
		finally {
			db.close();
		}
		return ret;
	}

	public long store(PoiItem poiItem) {
		long id = poiItem.getId();
		if (id == -1) {
			id = exists(points.class.getSimpleName(), poiItem.getName());
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(points.name.name(), poiItem.getName());
			cv.put(points.description.name(), poiItem.getDescription());
			cv.put(points.latitude.name(), poiItem.getLatitude());
			cv.put(points.longitude.name(), poiItem.getLongitude());
			cv.put(points.visible.name(), poiItem.isVisible()? 1: 0);
			if (id == -1) {
				id = db.insert(points.class.getSimpleName(), null, cv);
				if (id == -1) {
					throw new Exception("insert did not succeed");
				}
			}
			else {
				int count = db.update(points.class.getSimpleName(), cv, points.id.name() + " = ?", new String[]{Long.toString(id)});
				if (count != 1) {
					id = -1;
					throw new Exception("update did not effect only one row but count=" + count + ", id=" + id);
				}
			}
			poiItem.setId(id);
			db.setTransactionSuccessful();
		}
		catch (Exception e) {
			Log.e(TAG, "Storage.store: error", e);
		}
		finally {
			db.endTransaction();
			db.close();
		}
		return id;
	}

	// tracks:
	public List<TrackItem> getVisibleTracks() {
		List<TrackItem> ret = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		try {
			for (Cursor cursor : new CursorI(db.query(tracks.class.getSimpleName(), toStrings(tracks.values()), tracks.visible.name() + " != 0", null, null, null, null))) {
				TrackItem trackItem = new TrackItem(
					cursor.getInt(tracks.id.ordinal()),
					cursor.getString(tracks.name.ordinal()),
					cursor.getString(tracks.description.ordinal())
					//cursor.getInt(tracks.visible.ordinal()) != 0
					);
				getTrackItems(trackItem.getTrackPoints(), trackItem.getId());
				ret.add(trackItem);
			}
		}
		finally {
			db.close();
		}
		return ret;
	}

	public List<TrackPointItem> getTrackItems(List<TrackPointItem> ret, long trackid) {
		if (ret == null) {
			ret = new ArrayList<>();
		}
		return ret;
	}

	long store(TrackItem trackItem) {
		long id = trackItem.getId();
		if (id == -1) {
			id = exists(tracks.class.getSimpleName(), trackItem.getName());
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(tracks.name.name(), trackItem.getName());
			if (id == -1) {
				id = db.insert(tracks.class.getSimpleName(), null, cv);
				if (id == -1) {
					throw new Exception("insert did not succeed");
				}
			}
			else {
				int count = db.update(tracks.class.getSimpleName(), cv, tracks.id.name() + " = ?", new String[]{Long.toString(id)});
				if (count != 1) {
					id = -1;
					throw new Exception("update did not effect only one row but count=" + count + ", id=" + id);
				}
			}
			trackItem.setId(id);
			db.delete(trackpoints.class.getSimpleName(), trackpoints.trackid.name() + " = ?", new String[]{Long.toString(id)});
			int sequence = 0;
			for (TrackPointItem trackPointItem: trackItem.getTrackPoints()) {
				trackPointItem.setTrackid(id);
				trackPointItem.setSequence(sequence++);
				_store(db, trackPointItem);
			}
			cv.put(tracks.pointcount.name(), sequence);
			cv.put(tracks.crop_to.name(), 0);
			cv.put(tracks.crop_from.name(), sequence);
			db.update(tracks.class.getSimpleName(), cv, tracks.id.name() + " = ?", new String[]{Long.toString(id)});
			db.setTransactionSuccessful();
		}
		catch (Exception e) {
			Log.e(TAG, "Storage.store: error", e);
		}
		finally {
			db.endTransaction();
			db.close();
		}
		return id;
	}

	/** this method may be used inside another store method. that's why its
	 * private and requires a db obj. tx is not handled. */
	private long _store(SQLiteDatabase db, TrackPointItem trackPointItem) {
		long id = -1;
		try {
			ContentValues cv = new ContentValues();
			cv.put(trackpoints.sequence.name(), trackPointItem.getSequence());
			cv.put(trackpoints.trackid.name(), trackPointItem.getTrackid());
			cv.put(trackpoints.latitude.name(), trackPointItem.getLatitude());
			cv.put(trackpoints.longitude.name(), trackPointItem.getLongitude());
			cv.put(trackpoints.altitude.name(), trackPointItem.getAltitude());
			cv.put(trackpoints.speed.name(), trackPointItem.getSpeed());
			cv.put(trackpoints.timestamp.name(), trackPointItem.getTimestamp().getTime());
			cv.put(trackpoints.attribute.name(), trackPointItem.getAltitude());
			id = db.insert(trackpoints.class.getSimpleName(), null, cv);
			if (id == -1) {
				throw new Exception("insert did not succeed");
			}
			trackPointItem.setId(id);
		}
		catch (Exception e) {
			Log.e(TAG, "Storage.store: error", e);
		}
		return id;
	}
}
