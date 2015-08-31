package org.pyneo.tabulae.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.poi.PoiItem;
import org.pyneo.tabulae.track.Track;
import org.pyneo.tabulae.track.TrackItem;
import org.pyneo.tabulae.track.TrackPointItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** a storage class to store projos into the database. it contains all
 * information to take care of the database interface, the pojos itself dont
 * have any persistance functionality */
public class Storage extends SQLiteOpenHelper implements Constants {
	protected final static int mCurrentVersion = 1;
	static final private String ID = "tabulae";

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
	public enum tracks {id, name, description, timestamp, timezone, visible, pointcount, duration, distance, categoryid, activityid, cropto, cropfrom, };

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
	}

	/** constructs a create statement from a enum. */
	static public String createStatement(Class table, Object[] cols) {
		String stmnt = "CREATE TABLE ";
		stmnt += table.getSimpleName();
		stmnt += " (";
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) stmnt += ", ";
			stmnt += cols[i];
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
			for (Cursor cursor : new CursorI(db.query(table, new String[]{points.id.toString()}, points.name.toString() + " = ?", new String[]{name}, null, null, null))) {
				return cursor.getLong(0);
			}
		}
		finally {
			db.close();
		}
		return -1;
	}

	public List<PoiItem> getVisiblePoints() {
		List<PoiItem> ret = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		try {
			for (Cursor cursor : new CursorI(db.query(points.class.getSimpleName(), null, points.visible.toString() + " != 0", null, null, null, null))) {
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
			cv.put(points.name.toString(), poiItem.getName());
			cv.put(points.description.toString(), poiItem.getDescription());
			cv.put(points.latitude.toString(), poiItem.getLatitude());
			cv.put(points.longitude.toString(), poiItem.getLongitude());
			cv.put(points.visible.toString(), poiItem.isVisible()? 1: 0);
			if (id == -1) {
				id = db.insert(points.class.getSimpleName(), null, cv);
				if (id == -1) {
					throw new Exception("insert did not succeed");
				}
			}
			else {
				int count = db.update(points.class.getSimpleName(), cv, points.id.toString() + " = ?", new String[]{Long.toString(id)});
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

	long store(TrackItem trackItem) {
		long id = trackItem.getId();
		if (id == -1) {
			id = exists(tracks.class.getSimpleName(), trackItem.getName());
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(tracks.name.toString(), trackItem.getName());
			if (id == -1) {
				id = db.insert(tracks.class.getSimpleName(), null, cv);
				if (id == -1) {
					throw new Exception("insert did not succeed");
				}
			}
			else {
				int count = db.update(tracks.class.getSimpleName(), cv, tracks.id.toString() + " = ?", new String[]{Long.toString(id)});
				if (count != 1) {
					id = -1;
					throw new Exception("update did not effect only one row but count=" + count + ", id=" + id);
				}
			}
			trackItem.setId(id);
			db.delete(trackpoints.class.getSimpleName(), trackpoints.trackid.toString() + " = ?", new String[]{Long.toString(id)});
			for (TrackPointItem trackPointItem: trackItem.getTrackPoints()) {
				trackPointItem.setTrackid(id);
				_store(db, trackPointItem);
			}
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
	 * private and requires a db obj. */
	private long _store(SQLiteDatabase db, TrackPointItem trackPointItem) {
		long id = -1;
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(trackpoints.sequence.toString(), trackPointItem.getSequence());
			cv.put(trackpoints.trackid.toString(), trackPointItem.getSequence());
			cv.put(trackpoints.latitude.toString(), trackPointItem.getSequence());
			cv.put(trackpoints.longitude.toString(), trackPointItem.getSequence());
			cv.put(trackpoints.altitude.toString(), trackPointItem.getSequence());
			cv.put(trackpoints.speed.toString(), trackPointItem.getSequence());
			cv.put(trackpoints.timestamp.toString(), trackPointItem.getSequence());
			cv.put(trackpoints.attribute.toString(), trackPointItem.getSequence());
			id = db.insert(trackpoints.class.getSimpleName(), null, cv);
			if (id == -1) {
				throw new Exception("insert did not succeed");
			}
			trackPointItem.setId(id);
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

	/*
	public void remove() {
		super.remove(Storage.points.class.getSimpleName());
	}

	public void store() {
		super.store(
			Storage.
			Storage.points.values(),
			new Object[]{
				name,
				description,
				latitude,
				longitude,
				visible,
				});
	}

	public void remove(String table) {
		SQLiteDatabase mDatabase = null;
		mDatabase.delete(table, points.id.toString() + " = ?", new String[]{Long.toString(id)});
	}

	public void store(String table, Object[] names, Object[] fields) {
		if (fields.length != names.length-1) {
			throw new RuntimeException("length of fields does not match length of names");
		}
		SQLiteDatabase mDatabase = null;
		if (id == -1) {
				// TODO commit
				return;
			}
		}
	}
	*/
}
