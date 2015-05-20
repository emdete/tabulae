package org.pyneo.maps.poi;

import org.pyneo.maps.R;
import org.pyneo.maps.utils.Ut;

public class PoiCategory implements Constants {
	private final int mId;
	String mTitle;
	boolean mHidden;
	int mIconId;
	int mMinZoom;

	public PoiCategory(int id, String title, boolean hidden, int iconid, int minzoom) {
		super();
		mId = id;
		mTitle = title;
		mHidden = hidden;
		mIconId = iconid;
		mMinZoom = minzoom;
	}

	public PoiCategory() {
		this(Constants.EMPTY_ID, "", false, 0, 14);
	}

	public PoiCategory(String title) {
		this(Constants.EMPTY_ID, title, false, 0, 14);
	}

	public int getId() {
		return mId;
	}

}
