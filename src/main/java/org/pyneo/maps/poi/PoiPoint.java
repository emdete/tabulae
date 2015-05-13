package org.pyneo.maps.poi;

import org.pyneo.maps.R;

import org.andnav.osm.util.GeoPoint;

public class PoiPoint implements Constants {
	private final int mId;
	public String mTitle;
	public String mDescr;
	public GeoPoint mGeoPoint;
	public int mIconId;
	public double mAlt;
	public int mCategoryId;
	public int mPointSourceId;
	public boolean mHidden;

	public PoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint, int iconid, int categoryid, double alt, int sourseid, int hidden) {
		this.mId = id;
		this.mTitle = mTitle;
		this.mDescr = mDescr;
		this.mGeoPoint = mGeoPoint;
		this.mIconId = iconid;
		this.mAlt = alt;
		this.mCategoryId = categoryid;
		this.mPointSourceId = sourseid;
		this.mHidden = hidden == 1;
	}

	public PoiPoint() {
		this(EMPTY_ID, "", "", null, 0, 0, 0, 0, 0);
	}

	public PoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint, int categoryid, int iconid) {
		this(id, mTitle, mDescr, mGeoPoint, iconid, categoryid, 0, 0, 0);
	}

	public PoiPoint(String mTitle, String mDescr, GeoPoint mGeoPoint, int iconid) {
		this(EMPTY_ID, mTitle, mDescr, mGeoPoint, iconid, 0, 0, 0, 0);
	}

	public int getId() {
		return mId;
	}

}
