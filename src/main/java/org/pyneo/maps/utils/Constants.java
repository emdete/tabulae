package org.pyneo.maps.utils;

public interface Constants extends org.pyneo.maps.Constants {
	static public final boolean DEBUGMODE = false;
	static public final float DEG2RAD = (float)(Math.PI / 180.0);
	static public final float PI = (float)Math.PI;
	static public final float PI_2 = PI / 2.0f;
	static public final float PI_4 = PI / 4.0f;
	static public final float RAD2DEG = (float)(180.0 / Math.PI);
	static public final int IO_BUFFER_SIZE = 8 * 1024;
	static public final int MAPTILE_LATITUDE_INDEX = 0;
	static public final int MAPTILE_LONGITUDE_INDEX = 1;
	static public final int NOT_SET = Integer.MIN_VALUE;
	static public final int OpenSpaceUpperBoundArray[] = { 2, 5, 10, 25, 50, 100, 200, 500, 1000, 2000, 4000 };
	static public final int RADIUS_EARTH_METERS = 6378140;
	static public final int TILEPROVIDER_SUCCESS_ID = 1000;
	static public final int TILEPROVIDER_FAIL_ID = TILEPROVIDER_SUCCESS_ID + 1;
	static public final int TILEPROVIDER_INDEXIND_SUCCESS_ID = TILEPROVIDER_SUCCESS_ID + 2;
	static public final int TILEPROVIDER_INDEXIND_FAIL_ID = TILEPROVIDER_SUCCESS_ID + 3;
	static public final int TILEPROVIDER_ERROR_MESSAGE = TILEPROVIDER_SUCCESS_ID + 4;
	static public final int TILEPROVIDER_SEARCH_OK_MESSAGE = TILEPROVIDER_SUCCESS_ID + 5;
	static public final String DEBUGTAG = "org.andnav.osm";
	static public final String[] formats = new String[]{
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		"yyyy-MM-dd'T'HH:mm:ssZ",
		"yyyy-MM-dd'T'HH:mmZ",
		"yyyy-MM-dd'T'HH:mm:ss'Z'",
		"yyyy-MM-dd HH:mm:ss.SSSZ",
		"yyyy-MM-dd HH:mmZ",
		"yyyy-MM-dd HH:mm",
		"yyyy-MM-dd",
	};
}
