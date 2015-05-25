package org.pyneo.maps.poi;

public class PoiCategory implements Constants {
	private final int mId;
	String mName;
	boolean mHidden;
	int mIconId;
	int mMinZoom;

	public PoiCategory(int id, String name, boolean hidden, int iconid, int minzoom) {
		super();
		mId = id;
		mName = name;
		mHidden = hidden;
		mIconId = iconid;
		mMinZoom = minzoom;
	}

	public PoiCategory() {
		this(Constants.EMPTY_ID, "", false, 0, 14);
	}

	public PoiCategory(String name) {
		this(Constants.EMPTY_ID, name, false, 0, 14);
	}

	public int getId() {
		return mId;
	}
}
