package org.pyneo.maps.utils;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Storage implements Constants {
	protected final static int mCurrentVersion = 22;
	protected final Context mCtx;
	@SuppressLint("SimpleDateFormat")
	protected final SimpleDateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	protected SQLiteDatabase mDatabase;

	protected Storage(Context ctx) {
		super();
		mCtx = ctx;
		mDatabase = getDatabase();
	}

	protected boolean isDatabaseReady() {
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
				Ut.d("getDatabase folder=" + folder.getAbsolutePath());
				return new GeoDatabaseHelper(mCtx, folder.getAbsolutePath()).getWritableDatabase();
			}
			catch (Exception e) {
				Ut.e(e.toString(), e);
			}
		}
		return null;
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

	protected class GeoDatabaseHelper extends SQLiteOpenHelper {
		public GeoDatabaseHelper(final Context context, final String name) {
			super(context, name, null, mCurrentVersion);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_points);
			db.execSQL(SQL_CREATE_category);
			db.execSQL(SQL_ADD_category);
			db.execSQL(SQL_CREATE_tracks);
			db.execSQL(SQL_CREATE_trackpoints);
			db.execSQL(SQL_CREATE_maps);
			db.execSQL(SQL_CREATE_routes);
			loadActivityListFromResource(db);
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
				loadActivityListFromResource(db);
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

		public void loadActivityListFromResource(final SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_drop_activity);
			db.execSQL(SQL_CREATE_activity);
			String[] act = mCtx.getResources().getStringArray(R.array.track_activity);
			for (int i = 0; i < act.length; i++) {
				db.execSQL(String.format(SQL_CREATE_insert_activity, i, act[i]));
			}
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
}
