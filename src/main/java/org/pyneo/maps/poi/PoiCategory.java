package org.pyneo.maps.poi;

import org.pyneo.maps.R;
import org.pyneo.maps.utils.Ut;

public class PoiCategory implements Constants {
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
		this(Constants.EMPTY_ID, "", false, 0, 14);
	}

	public PoiCategory(String title) {
		this(Constants.EMPTY_ID, title, false, 0, 14);
	}

	public int getId() {
		return Id;
	}

}
