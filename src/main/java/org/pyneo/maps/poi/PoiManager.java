package org.pyneo.maps.poi;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import org.pyneo.maps.track.Track;
import org.pyneo.maps.track.Track.TrackPoint;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.utils.CursorI;

import org.pyneo.maps.utils.GeoPoint;

import java.util.Date;

public class PoiManager implements Constants {
	protected final Context mCtx;
	private PoiStorage mPoiStorage;
	private boolean mStopProcessing;

	public PoiManager(Context ctx) {
		super();
		mCtx = ctx;
		mPoiStorage = new PoiStorage(ctx);
	}

	public void deleteMap(long id) {
		mPoiStorage.deleteMap(id);
	}

	public void updateMap(long id, String name, int type, String params) {
		mPoiStorage.updateMap(id, name, type, params);
	}

	public Cursor getTrackListCursor(final String units, final String sortColNames) {
		return mPoiStorage.getTrackListCursor(units, sortColNames);
	}

	public long JoinTracks() {
		return mPoiStorage.JoinTracks();
	}

	public int saveTrackFromWriter(final SQLiteDatabase db) {
		return mPoiStorage.saveTrackFromWriter(db);
	}

	public Cursor getActivityListCursor() {
		return mPoiStorage.getActivityListCursor();
	}

	public long addPoiCategory(final String title, final int hidden, final int iconid) {
		return mPoiStorage.addPoiCategory(title, hidden, iconid);
	}

	public void setCategoryHidden(final int id) {
		mPoiStorage.setCategoryHidden(id);
	}

	public CursorI getMixedMaps() {
		return mPoiStorage.getMixedMaps();
	}

	public Cursor getMap(long id) {
		return mPoiStorage.getMap(id);
	}

	public Cursor getTrackPoints(final long id) {
		return mPoiStorage.getTrackPoints(id);
	}

	public Cursor getPoiCategoryListCursor() {
		return mPoiStorage.getPoiCategoryListCursor();
	}

	public Cursor getPoiListCursor(String sortColNames) {
		return mPoiStorage.getPoiListCursor(sortColNames);
	}

	public void FreeDatabases() {
		mPoiStorage.FreeDatabases();
	}

	public void StopProcessing() {
		mStopProcessing = true;
	}

	private boolean Stop() {
		if (mStopProcessing) {
			mStopProcessing = false;
			return true;
		}
		return false;
	}

	public void addPoi(final String title, final String descr, GeoPoint point) {
		mPoiStorage.addPoi(title, descr, point.getLatitude(), point.getLongitude(), ZERO, ZERO, ZERO, ZERO, 0);
	}

	public void updatePoi(final PoiPoint point) {
		if (point.getId() < 0)
			mPoiStorage.addPoi(point.mTitle, point.mDescr, point.mGeoPoint.getLatitude(), point.mGeoPoint.getLongitude(), point.mAlt, point.mCategoryId, point.mPointSourceId, point.mHidden? ONE: ZERO, point.mIconId);
		else
			mPoiStorage.updatePoi(point.getId(), point.mTitle, point.mDescr, point.mGeoPoint.getLatitude(), point.mGeoPoint.getLongitude(), point.mAlt, point.mCategoryId, point.mPointSourceId, point.mHidden? ONE: ZERO, point.mIconId);
	}

	private SparseArray<PoiPoint> doCreatePoiListFromCursor(Cursor c) {
		final SparseArray<PoiPoint> items = new SparseArray<PoiPoint>();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					Ut.d("doCreatePoiListFromCursor c=" + c);
					// poi.lat, poi.lon, poi.name, poi.descr, poi.pointid, poi.pointid _id, poi.pointid ID, poi.categoryid, cat.iconid
					items.put(
						c.getInt(4), new PoiPoint(
							c.getInt(4),
							c.getString(2),
							c.getString(3),
							new GeoPoint((int)(1E6 * c.getDouble(0)), (int)(1E6 * c.getDouble(1))),
							c.getInt(7),
							c.getInt(8))
						);
				} while (c.moveToNext());
			}
			c.close();
		}
		return items;
	}

	public SparseArray<PoiPoint> getPoiList() {
		return doCreatePoiListFromCursor(mPoiStorage.getPoiListCursor());
	}

	public SparseArray<PoiPoint> getPoiListNotHidden(int zoom, GeoPoint center, double deltaX, double deltaY) {
		Ut.d("getPoiListNotHidden:");
		return doCreatePoiListFromCursor(
			mPoiStorage.getPoiListNotHiddenCursor(
				zoom,
				center.getLongitude() - deltaX, center.getLongitude() + deltaX,
				center.getLatitude() + deltaY, center.getLatitude() - deltaY
			));
	}

	public void addPoiStartActivity(Context ctx, GeoPoint touchDownPoint) {
		ctx.startActivity((new Intent(ctx, PoiActivity.class))
			.putExtra(LAT, touchDownPoint.getLatitude())
			.putExtra(LON, touchDownPoint.getLongitude()));
	}

	public PoiPoint getPoiPoint(int id) {
		PoiPoint point = null;
		final Cursor c = mPoiStorage.getPoi(id);
		if (c != null) {
			if (c.moveToFirst())
				point = new PoiPoint(
					c.getInt(4),
					c.getString(2),
					c.getString(3),
					new GeoPoint((int)(1E6 * c.getDouble(0)), (int)(1E6 * c.getDouble(1))),
					c.getInt(9),
					c.getInt(7),
					c.getDouble(5),
					c.getInt(8),
					c.getInt(6));
			c.close();
		}

		return point;
	}

	public void deletePoi(final int id) {
		mPoiStorage.deletePoi(id);
	}

	public void deletePoiCategory(final int id) {
		mPoiStorage.deletePoiCategory(id);
	}

	public PoiCategory getPoiCategory(int id) {
		PoiCategory category = null;
		final Cursor c = mPoiStorage.getPoiCategory(id);
		if (c != null) {
			if (c.moveToFirst())
				category = new PoiCategory(id, c.getString(0), c.getInt(2) == ONE, c.getInt(3), c.getInt(4));
			c.close();
		}

		return category;
	}

	public void updatePoiCategory(PoiCategory poiCategory) {
		if (poiCategory.getId() < ZERO)
			mPoiStorage.addPoiCategory(poiCategory.Title, poiCategory.Hidden? ONE: ZERO, poiCategory.IconId);
		else
			mPoiStorage.updatePoiCategory(poiCategory.getId(), poiCategory.Title, poiCategory.Hidden? ONE: ZERO, poiCategory.IconId, poiCategory.MinZoom);
	}

	public void deleteAllPoi() {
		mPoiStorage.deleteAllPoi();
	}

	public void beginTransaction() {
		mPoiStorage.beginTransaction();
	}

	public void rollbackTransaction() {
		mPoiStorage.rollbackTransaction();
	}

	public void commitTransaction() {
		mPoiStorage.commitTransaction();
	}

	public void updateTrack(Track track) {
		if (track.getId() < 0) {
			long newId = mPoiStorage.addTrack(track.Name, track.Descr, track.Show? ONE: ZERO, track.Cnt, track.Distance, track.Duration, track.Category, track.Activity, track.Date, track.Style);

			for (TrackPoint trackpoint : track.getPoints()) {
				mPoiStorage.addTrackPoint(newId, trackpoint.lat, trackpoint.lon, trackpoint.alt, trackpoint.speed, trackpoint.date);
			}
		} else
			mPoiStorage.updateTrack(track.getId(), track.Name, track.Descr, track.Show? ONE: ZERO, track.Cnt, track.Distance, track.Duration, track.Category, track.Activity, track.Date, track.Style);
	}

	public boolean haveTrackChecked() {
		boolean ret = false;
		Cursor c = mPoiStorage.getTrackChecked();
		if (c != null) {
			if (c.moveToFirst())
				ret = true;
			c.close();
		}

		return ret;
	}

	public Track[] getTrackChecked() {
		return getTrackChecked(true);
	}

	public void setTrackChecked(int id) {
		mPoiStorage.setTrackChecked(id);
	}

	public Track[] getTrackChecked(final boolean aNeedPoints) {
		mStopProcessing = false;
		Track tracks[] = null;
		Cursor c = mPoiStorage.getTrackChecked();
		if (c != null) {
			tracks = new Track[c.getCount()];
			final String defStyle = PreferenceManager.getDefaultSharedPreferences(mCtx).getString("pref_track_style", "");
			if (c.moveToFirst())
				do {
					final int pos = c.getPosition();
					String style = c.getString(10);
					if (style == null || style.equalsIgnoreCase(""))
						style = ""; // TODO ?!?
					tracks[pos] = new Track(c.getInt(3), c.getString(0), c.getString(1), c.getInt(2) == ONE, c.getInt(4), c.getDouble(5), c.getDouble(6), c.getInt(7), c.getInt(8), new Date(c.getLong(9) * 1000), style, defStyle);
					if (aNeedPoints) {
						Cursor cpoints = mPoiStorage.getTrackPoints(tracks[pos].getId());
						if (cpoints != null) {
							if (cpoints.moveToFirst()) {
								do {
									if (Stop()) {
										tracks[pos] = null;
										break;
									}
									tracks[pos].AddTrackPoint(); //track.trackpoints.size()
									tracks[pos].LastTrackPoint.lat = cpoints.getDouble(0);
									tracks[pos].LastTrackPoint.lon = cpoints.getDouble(1);
									tracks[pos].LastTrackPoint.alt = cpoints.getDouble(2);
									tracks[pos].LastTrackPoint.speed = cpoints.getDouble(3);
									tracks[pos].LastTrackPoint.date.setTime(cpoints.getLong(4) * 1000); // System.currentTimeMillis()
								} while (cpoints.moveToNext());
							}
							cpoints.close();
						}
					}
				} while (c.moveToNext());
			else {
				c.close();
				return null;
			}
			c.close();

		}
		return tracks;
	}

	public Track getTrack(int id) {
		Track track = null;
		Cursor c = mPoiStorage.getTrack(id);
		if (c != null) {
			if (c.moveToFirst()) {
				final String defStyle = PreferenceManager.getDefaultSharedPreferences(mCtx).getString("pref_track_style", "");
				String style = c.getString(9);
				if (style == null || style.equalsIgnoreCase(""))
					style = "";

				track = new Track(id, c.getString(0), c.getString(1), c.getInt(2) == ONE, c.getInt(3), c.getDouble(4), c.getDouble(5), c.getInt(6), c.getInt(7), new Date(c.getLong(8) * 1000), style, defStyle);
			}
			c.close();
			c = null;

			c = mPoiStorage.getTrackPoints(id);
			if (c != null) {
				if (c.moveToFirst()) {
					do {
						track.AddTrackPoint();
						track.LastTrackPoint.lat = c.getDouble(0);
						track.LastTrackPoint.lon = c.getDouble(1);
						track.LastTrackPoint.alt = c.getDouble(2);
						track.LastTrackPoint.speed = c.getDouble(3);
						track.LastTrackPoint.date.setTime(c.getLong(4) * 1000); // System.currentTimeMillis()
					} while (c.moveToNext());
				}
				c.close();
			}

		}

		return track;
	}

	public void deleteTrack(int id) {
		mPoiStorage.deleteTrack(id);

	}

	public long addMap(int type, String params) {
		return mPoiStorage.addMap(type, params);
	}

}
