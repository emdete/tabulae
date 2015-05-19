package org.pyneo.maps.poi;

import org.pyneo.maps.R;

public interface Constants extends org.pyneo.maps.Constants {
	public enum CATEGORY { CATEGORYID, NAME, HIDDEN, ICONID, MINZOOM };
	public enum POINT { POINTID, NAME, DESCR, LAT, LON, ALT, HIDDEN, CATEGORYID, ICONID };
}
