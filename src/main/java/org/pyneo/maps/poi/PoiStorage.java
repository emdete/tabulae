package org.pyneo.maps.poi;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import java.io.File;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.pyneo.maps.poi.Constants;
import org.pyneo.maps.R;
import org.pyneo.maps.R;
import org.pyneo.maps.track.TrackStorage;
import org.pyneo.maps.utils.Storage;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.utils.Ut;

public class PoiStorage extends TrackStorage implements Constants { // TODO extend Storage from util
	public PoiStorage(Context ctx) {
		super(ctx);
	}

	// POI ----------------------------------------------------------------------------

	public long addPoi(final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId, final int aPointSourceId, final int hidden, final int iconid) {
		long newId = -1;
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
			newId = mDatabase.insert(POINTS, null, cv);
		}
		return newId;
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
			mDatabase.update(POINTS, cv, UPDATE_POINTS, args);
		}
	}

	static final String STAT_deletePoi = "DELETE FROM points WHERE pointid = @1";
	public void deletePoi(final int id) {
		if (isDatabaseReady()) {
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(STAT_deletePoi, args);
		}
	}

	static final String STAT_DeleteAllPoi = "DELETE FROM points";
	public void deleteAllPoi() {
		if (isDatabaseReady()) {
			mDatabase.execSQL(STAT_DeleteAllPoi);
		}
	}

	public static final String STAT_getPoi = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM points WHERE pointid = @1";
	public Cursor getPoi(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(id)};
			return mDatabase.rawQuery(STAT_getPoi, args);
		}
		return null;
	}

	public SQLiteCursorLoader getPoiListCursorLoader() {
		return getPoiListCursorLoader(LAT + ',' + LON);
	}

	public SQLiteCursorLoader getPoiListCursorLoader(String sortColNames) {
		File folder = Ut.getAppMainDir(mCtx, DATA);
		folder = new File(folder, GEODATA_FILENAME);
		return new SQLiteCursorLoader(mCtx, new GeoDatabaseHelper(mCtx, folder.getAbsolutePath()), STAT_GET_POI_LIST + sortColNames, null);
	}

	public Cursor getPoiListCursor() {
		return getPoiListCursor(LAT + ',' + LON);
	}

	public static final String STAT_GET_POI_LIST = "SELECT lat, lon, points.name, descr, pointid, pointid _id, pointid ID, category.iconid, category.name as catname FROM points LEFT JOIN category ON category.categoryid = points.categoryid ORDER BY ";
	public Cursor getPoiListCursor(String sortColNames) {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(STAT_GET_POI_LIST + sortColNames, null);
		}
		return null;
	}

	public static final String STAT_PoiListNotHidden = "SELECT poi.lat, poi.lon, poi.name, poi.descr, poi.pointid, poi.pointid _id, poi.pointid ID, poi.categoryid, cat.iconid FROM points poi LEFT JOIN category cat ON cat.categoryid = poi.categoryid WHERE poi.hidden = 0 AND cat.hidden = 0 AND cat.minzoom <= @1 AND poi.lon BETWEEN @2 AND @3 AND poi.lat BETWEEN @4 AND @5 ORDER BY lat, lon";
	public Cursor getPoiListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(zoom + 1), Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
			return mDatabase.rawQuery(STAT_PoiListNotHidden, args);
		}
		return null;
	}

	// POI CATEGORY -------------------------------------------------------------------------------

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
			newId = mDatabase.insert(CATEGORY, null, cv);
		}
		return newId;
	}

	public static final String STAT_PoiCategoryList = "SELECT name, iconid, categoryid _id, hidden FROM category ORDER BY name";
	public Cursor getPoiCategoryListCursor() {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(STAT_PoiCategoryList, null);
		}
		return null;
	}

	public static final String STAT_deletePoiCategory = "DELETE FROM category WHERE categoryid = @1";
	public void deletePoiCategory(final int id) {
		if (isDatabaseReady() && id != ZERO) { // predef category My POI never delete
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(STAT_deletePoiCategory, args);
		}
	}

	public static final String STAT_getPoiCategory = "SELECT name, categoryid, hidden, iconid, minzoom FROM category WHERE categoryid = @1";
	public Cursor getPoiCategory(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(id)};
			return mDatabase.rawQuery(STAT_getPoiCategory, args);
		}
		return null;
	}

	public static final String UPDATE_CATEGORY = "categoryid = @1";
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
			mDatabase.update(CATEGORY, cv, UPDATE_CATEGORY, args);
		}
	}
}
