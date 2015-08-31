package org.pyneo.tabulae.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.poi.PoiItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Storage extends SQLiteOpenHelper implements Constants {
	protected final static int mCurrentVersion = 1;
	static final private String ID = "tabulae";

	public enum tracks {id, name, description, timestamp, visible, pointcount, duration, distance, categoryid, activityid, };
	public enum trackpoints {id, trackid, latitude, longitude, altitude, speed, timestamp, attribute, };
	public enum points {id, name, description, latitude, longitude, altitude, visible, categoryid, iconid, };
	public enum category {id, name, description, visible, iconid, minzoom, };
	public enum activity {id, name, description, };

	public Storage(final Tabulae context) {
		super(context, new File(context.getBaseDir(), ID + ".db").getAbsolutePath(), null, mCurrentVersion);
		if (DEBUG) Log.d(TAG, "db=" + new File(context.getBaseDir(), ID + ".db").getAbsolutePath());
	}

	@Override public void onCreate(final SQLiteDatabase db) {
		db.execSQL(createStatement(tracks.class, tracks.values()));
		db.execSQL(createStatement(points.class, points.values()));
	}

	@Override public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
	}

	public static String fieldList(final Object[] cols, boolean doId) {
		String stmnt = "";
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) stmnt += ", ";
			stmnt += cols[i];
			switch (i) {
				case 0: stmnt += " PRIMARY KEY"; break; // db ident
				case 1: stmnt += " NOT NULL UNIQUE"; break; // human readable name
			}
		}
		return stmnt;
		//return TextUtils.join(", ", cols);
	}

	static public String createStatement(Class table, Object[] cols) {
		String stmnt = "CREATE TABLE " + table.getSimpleName() + " (" + fieldList(cols, false) + ")";
		// TODO: add DEFAULT / NOT NULL PRIMARY KEY UNIQUE
		if (DEBUG) Log.d(TAG, "stmnt=" + stmnt);
		return stmnt;
	}

	public SQLiteDatabase getDatabase(final Tabulae activity) {
		return new Storage(activity).getWritableDatabase();
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
				poiItem.setId(id);
				db.setTransactionSuccessful();
			}
			else {
				int count = db.update(points.class.getSimpleName(), cv, points.id.toString() + " = ?", new String[]{Long.toString(id)});
				if (count != 1) {
					id = -1;
					throw new Exception("update did not effect only one row but count=" + count + ", id=" + id);
				}
				poiItem.setId(id);
				db.setTransactionSuccessful();
			}
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
