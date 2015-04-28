package org.pyneo.maps.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import org.pyneo.maps.R;
import org.pyneo.maps.kml.constants.PoiConstants;
import org.pyneo.maps.utils.Ut;

import java.io.File;

public class GeoData implements PoiConstants {
	private static GeoData mInstance = null;

	private Context mContext;
	private GeoDataDatabaseOpenHelper mSQLiteOpenHelper;

	public GeoData(Context context) {
		mContext = context;

		File folder = Ut.getAppMainDir(context, DATA);
		mSQLiteOpenHelper = new GeoDataDatabaseOpenHelper(context, folder.getAbsolutePath() + GEODATA_FILENAME);
	}

	public static GeoData getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new GeoData(context);
		}

		return mInstance;
	}

	public SQLiteCursorLoader getPoiListCursorLoader() {
		return getPoiListCursorLoader(LATLON);
	}

	public SQLiteCursorLoader getPoiListCursorLoader(String sortColNames) {
		// Not change the order of the fields
		return new SQLiteCursorLoader(mContext, mSQLiteOpenHelper, STAT_GET_POI_LIST + sortColNames, null);
	}

	protected class GeoDataDatabaseOpenHelper extends SQLiteSDOpenHelper {
		private final static int mCurrentVersion = 22;

		public GeoDataDatabaseOpenHelper(final Context context, final String name) {
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

		public void LoadActivityListFromResource(final SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_drop_activity);
			db.execSQL(SQL_CREATE_activity);
			String[] act = mContext.getResources().getStringArray(R.array.track_activity);
			for (int i = 0; i < act.length; i++) {
				db.execSQL(String.format(SQL_CREATE_insert_activity, i, act[i]));
			}
		}

	}

}
