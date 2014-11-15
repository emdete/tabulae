package org.pyneo.maps.applib.kml;

import org.pyneo.maps.applib.R;
import org.pyneo.maps.applib.kml.constants.PoiConstants;

import org.andnav.osm.util.GeoPoint;

public class PoiPoint implements PoiConstants {

	private final int Id;
	public String Title;
	public String Descr;
	public GeoPoint GeoPoint;
	public int IconId;
	public double Alt;
	public int CategoryId;
	public int PointSourceId;
	public boolean Hidden;

	public PoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint,
					int iconid, int categoryid, double alt, int sourseid, int hidden) {
		this.Id = id;
		this.Title = mTitle;
		this.Descr = mDescr;
		this.GeoPoint = mGeoPoint;
		this.IconId = iconid;
		this.Alt = alt;
		this.CategoryId = categoryid;
		this.PointSourceId = sourseid;
		this.Hidden = hidden == 1? true: false;
	}

	public PoiPoint() {
		this(EMPTY_ID, "", "", null, R.drawable.poi, 0, 0, 0, 0);
	}

	public PoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint, int categoryid, int iconid) {
		this(id, mTitle, mDescr, mGeoPoint, iconid, categoryid, 0, 0, 0);
	}

	public PoiPoint(String mTitle, String mDescr, GeoPoint mGeoPoint, int iconid) {
		this(EMPTY_ID, mTitle, mDescr, mGeoPoint, iconid, 0, 0, 0, 0);
	}

	public static int EMPTY_ID() {
		return EMPTY_ID;
	}

	public int getId() {
		return Id;
	}

}
