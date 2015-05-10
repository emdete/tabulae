package org.pyneo.maps.poi;

import org.pyneo.maps.R;

public class PoiCategory implements PoiConstants {
	private final int Id;
	public String Title;
	public boolean Hidden;
	public int IconId;
	public int MinZoom;

	public PoiCategory(int id, String title, boolean hidden, int iconid, int minzoom) {
		super();
		Id = id;
		Title = title;
		Hidden = hidden;
		IconId = iconid;
		MinZoom = minzoom;
	}

	public PoiCategory() {
		this(PoiConstants.EMPTY_ID, "", false, R.drawable.poi_red, 14);
	}

	public PoiCategory(String title) {
		this(PoiConstants.EMPTY_ID, title, false, R.drawable.poi_red, 14);
	}

	public int getId() {
		return Id;
	}

}
