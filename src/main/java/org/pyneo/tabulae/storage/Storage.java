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

	public enum tracks {_id, name, description, timestamp, visible, pointcount, duration, distance, categoryid, activityid, };
	public enum trackpoints {_id, trackid, latitude, longitude, altitude, speed, timestamp, attribute, };
	public enum points {_id, name, description, latitude, longitude, altitude, visible, categoryid, iconid, };
	public enum category {_id, name, description, visible, iconid, minzoom, };
	public enum activity {_id, name, description, };

	public Storage(final Tabulae context) {
		super(context, new File(context.getBaseDir(), ID + ".db").getAbsolutePath(), null, mCurrentVersion);
	}

	@Override public void onCreate(final SQLiteDatabase db) {
		db.execSQL(createStatement(tracks.class, tracks.values()));
	}

	@Override public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		db.execSQL(createStatement(points.class, points.values()));
	}

	public static String fieldList(final Object[] cols, boolean doId) {
		String stmnt = "";
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) stmnt += ", ";
			stmnt += cols[i];
			switch (i) {
				case 0: stmnt += "PRIMARY KEY"; break; // db ident
				case 1: stmnt += "UNIQUE"; break; // human name
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

	public List<PoiItem> getVisiblePoints() {
		List<PoiItem> ret = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		try {
			Cursor cursor = db.query(points.class.getSimpleName(), null, "visible = true", null, null, null, null);
			ret.add(new PoiItem(
					cursor.getInt(points._id.ordinal()),
					cursor.getString(points.name.ordinal()),
					cursor.getString(points.description.ordinal()),
					cursor.getDouble(points.latitude.ordinal()),
					cursor.getDouble(points.longitude.ordinal()),
					cursor.getInt(points.visible.ordinal()) != 0
			));
		}
		finally {
			db.close();
		}
		return ret;
	}

	public void store(PoiItem poiItem) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(points.name.toString(), poiItem.getName());
			cv.put(points.description.toString(), poiItem.getDescription());
			cv.put(points.latitude.toString(), poiItem.getLatitude());
			cv.put(points.longitude.toString(), poiItem.getLongitude());
			cv.put(points.visible.toString(), poiItem.isVisible()? 1: 0);
			long _id = db.insert(points.class.getSimpleName(), null, cv);
			if (_id != -1) {
				poiItem.setId(_id);
				db.setTransactionSuccessful();
			}
		}
		finally {
			db.endTransaction();
			db.close();
		}
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
		mDatabase.delete(table, "_id = ?", new String[]{Long.toString(_id)});
	}

	public void store(String table, Object[] names, Object[] fields) {
		if (fields.length != names.length-1) {
			throw new RuntimeException("length of fields does not match length of names");
		}
		SQLiteDatabase mDatabase = null;
		if (_id == -1) {
				// TODO commit
				return;
			}
		}
	}

	 */
}
