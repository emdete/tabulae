package org.pyneo.maps.utils;

public interface Constants extends org.pyneo.maps.Constants {
	static public final int TILEPROVIDER_SUCCESS_ID = 1000;
	static public final int TILEPROVIDER_FAIL_ID = TILEPROVIDER_SUCCESS_ID + 1;
	static public final int TILEPROVIDER_INDEXIND_SUCCESS_ID = TILEPROVIDER_SUCCESS_ID + 2;
	static public final int TILEPROVIDER_INDEXIND_FAIL_ID = TILEPROVIDER_SUCCESS_ID + 3;
	static public final int TILEPROVIDER_ERROR_MESSAGE = TILEPROVIDER_SUCCESS_ID + 4;
	static public final int TILEPROVIDER_SEARCH_OK_MESSAGE = TILEPROVIDER_SUCCESS_ID + 5;
	static public final int IO_BUFFER_SIZE = 8 * 1024;
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
