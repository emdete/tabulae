package org.pyneo.tabulae;

public interface Constants {
	String TAG = "org.pyneo.tabulae";
	boolean DEBUG = true;
	byte MIN_ZOOM = (byte)4;
	byte MAX_ZOOM = (byte)21;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }
	float USER_FONT_FACTOR = 1.3f; // TODO: determine sp/dp factor somehow
	public static final String ACTION_CONVERSATIONS_REQUEST = "eu.siacs.conversations.location.request";
	public static final String ACTION_CONVERSATIONS_SHOW = "eu.siacs.conversations.location.show";
	public static final String GEO = "geo";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ALTITUDE = "altitude";
	public static final String ACCURACY = "accuracy";
	public static final String JID = "jid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
}
