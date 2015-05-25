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
import org.pyneo.maps.utils.TableE;
import org.pyneo.maps.utils.Ut;

public class PoiStorage extends TrackStorage implements Constants { // TODO extend from util.Storage
	public PoiStorage(Context ctx) {
		super(ctx);
	}

	// POI ----------------------------------------------------------------------------
	private static final String POINTS_ = points.class.getSimpleName();
	private static final String POINTS_POINTID = points.pointid.name();
	private static final String POINTS_NAME = points.name.name();
	private static final String POINTS_DESCR = points.descr.name();
	private static final String POINTS_LAT = points.lat.name();
	private static final String POINTS_LON = points.lon.name();
	private static final String POINTS_ALT = points.alt.name();
	private static final String POINTS_HIDDEN = points.hidden.name();
	private static final String POINTS_CATEGORYID = points.categoryid.name();
	private static final String POINTS_ICONID = points.iconid.name();

	public long addPoi(final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId, final int hidden, final int iconid) {
		long newId = -1;
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(POINTS_NAME, aName);
			cv.put(POINTS_DESCR, aDescr);
			cv.put(POINTS_LAT, aLat);
			cv.put(POINTS_LON, aLon);
			cv.put(POINTS_ALT, aAlt);
			cv.put(POINTS_CATEGORYID, aCategoryId);
			cv.put(POINTS_HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(POINTS_ICONID, 0);
			}
			else
				cv.put(POINTS_ICONID, iconid);
			newId = mDatabase.insert(POINTS_, null, cv);
		}
		return newId;
	}

	public void updatePoi(final int id, final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(POINTS_NAME, aName);
			cv.put(POINTS_DESCR, aDescr);
			cv.put(POINTS_LAT, aLat);
			cv.put(POINTS_LON, aLon);
			cv.put(POINTS_ALT, aAlt);
			cv.put(POINTS_CATEGORYID, aCategoryId);
			cv.put(HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(POINTS_ICONID, 0);
			}
			else
				cv.put(POINTS_ICONID, iconid);
			final String[] args = {Integer.toString(id)};
			mDatabase.update(POINTS_, cv, UPDATE_POINTS, args);
		}
	}

	static final String POINTS__DELETE_WHERE_ID = "DELETE FROM points WHERE pointid = @1";
	public void deletePoi(final int id) {
		if (isDatabaseReady()) {
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(POINTS__DELETE_WHERE_ID, args);
		}
	}

	static final String POINTS__DELETE = "DELETE FROM points";
	public void deleteAllPoi() {
		if (isDatabaseReady()) {
			mDatabase.execSQL(POINTS__DELETE);
		}
	}

	public static final String POINTS__SELECT_WHERE_ID = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, iconid FROM points WHERE pointid = @1";
	public Cursor getPoi(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(id)};
			return mDatabase.rawQuery(POINTS__SELECT_WHERE_ID, args);
		}
		return null;
	}

	public SQLiteCursorLoader getPoiListCursorLoader() {
		return getPoiListCursorLoader(LAT + ',' + LON);
	}

	public SQLiteCursorLoader getPoiListCursorLoader(String sortColNames) {
		File folder = Ut.getAppMainDir(mCtx, DATA);
		folder = new File(folder, GEODATA_FILENAME);
		return new SQLiteCursorLoader(mCtx, new GeoDatabaseHelper(mCtx, folder.getAbsolutePath()), POINTS__SELECT_ORDER + sortColNames, null);
	}

	public Cursor getPoiListCursor() {
		return getPoiListCursor(LAT + ',' + LON);
	}

	public static final String POINTS__SELECT_ORDER =
		"SELECT p.lat, p.lon, p.name, p.descr, p.pointid, p.pointid _id, p.pointid ID, c.iconid, c.name as catname " +
		"FROM points p " +
		"LEFT JOIN category c ON c.categoryid = p.categoryid " +
		"ORDER BY ";
	public Cursor getPoiListCursor(String sortColNames) {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(POINTS__SELECT_ORDER + sortColNames, null);
		}
		return null;
	}

	public static final String STAT_PoiListNotHidden =
		"SELECT p.lat, p.lon, p.name, p.descr, p.pointid, p.pointid _id, p.pointid ID, p.categoryid, c.iconid " +
		"FROM points p " +
		"LEFT JOIN category c ON c.categoryid = p.categoryid " +
		"WHERE p.hidden = 0 AND c.hidden = 0 AND c.minzoom <= @1 AND p.lon BETWEEN @2 AND @3 AND p.lat BETWEEN @4 AND @5 " +
		"ORDER BY lat, lon";
	public Cursor getPoiListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(zoom + 1), Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
			return mDatabase.rawQuery(STAT_PoiListNotHidden, args);
		}
		return null;
	}

	// POI CATEGORY -------------------------------------------------------------------------------
	private static final String CATEGORY_ = category.class.getSimpleName();
	private static final String CATEGORY_CATEGORYID = category.categoryid.name();
	private static final String CATEGORY_NAME = category.name.name();
	private static final String CATEGORY_HIDDEN = category.hidden.name();
	private static final String CATEGORY_ICONID = category.iconid.name();
	private static final String CATEGORY_MINZOOM = category.minzoom.name();

	public long addPoiCategory(final String name, final int hidden, final int iconid) {
		long newId = -1;
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(CATEGORY_NAME, name);
			cv.put(CATEGORY_HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(CATEGORY_ICONID, 0);
			}
			else
				cv.put(CATEGORY_ICONID, iconid);
			newId = mDatabase.insert(CATEGORY_, null, cv);
		}
		return newId;
	}

	private static final String CATEGORY__SELECT_ORDER_NAME = TableE.selectStatement(
		category.class, category.values(), null, new Object[]{category.name});
	public Cursor getPoiCategories() {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(CATEGORY__SELECT_ORDER_NAME, null);
		}
		return null;
	}

	private static final String CATEGORY__SELECT_WHERE_ID = TableE.selectStatement(
		category.class, category.values(), new Object[]{category.categoryid}, null);
	public Cursor getPoiCategory(final int id) {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(CATEGORY__SELECT_WHERE_ID, new String[]{Integer.toString(id)});
		}
		return null;
	}

	private static final String CATEGORY__UPDATE_HIDDEN = "UPDATE category SET hidden = 1 - hidden * 1 WHERE categoryid = @1";
	public void togglePoiCategoryHidden(final int id) {
		if (isDatabaseReady()) {
			// TODO use proper setter... blindly toggle is ugly
			mDatabase.execSQL(CATEGORY__UPDATE_HIDDEN, new String[]{Long.toString(id)});
		}
	}

	private static final String CATEGORY__UPDATE_CATEGORY = TableE.equalsList(
		new Object[]{category.categoryid});
	public void updatePoiCategory(final int id, final String name, final int hidden, final int iconid, final int minzoom) {
		if (isDatabaseReady()) {
			Ut.i("updatePoiCategory id=" + id + ", CATEGORY_=" + CATEGORY_ + ", CATEGORY__UPDATE_CATEGORY=" + CATEGORY__UPDATE_CATEGORY);
			final ContentValues cv = new ContentValues();
			cv.put(CATEGORY_NAME, name);
			cv.put(CATEGORY_HIDDEN, hidden);
			if (iconid < 0 || iconid >= POI_ICON_RESOURCE_IDS.length) {
				Ut.e("iconid="+iconid, new Exception());
				cv.put(CATEGORY_ICONID, 0);
			}
			else
				cv.put(CATEGORY_ICONID, iconid);
			cv.put(CATEGORY_MINZOOM, minzoom);
			final String[] args = {Integer.toString(id)};
			mDatabase.update(CATEGORY_, cv, CATEGORY__UPDATE_CATEGORY, args);
		}
	}

	private static final String CATEGORY__DELETE_WHERE_ID = TableE.deleteStatement(
		category.class, new Object[]{category.categoryid});
	public void deletePoiCategory(final int id) {
		if (isDatabaseReady() && id != ZERO) { // TODO: predef category My POI never delete
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(CATEGORY__DELETE_WHERE_ID, args);
		}
	}
}
