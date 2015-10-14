package org.pyneo.tabulae;

import java.text.SimpleDateFormat;
import java.util.Locale;

public interface Constants {
	String TAG = "org.pyneo.tabulae";
	boolean DEBUG = true;
	byte MIN_ZOOM = (byte)4;
	byte MAX_ZOOM = (byte)21;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }
	float USER_FONT_FACTOR = 1.3f; // TODO: determine sp/dp factor somehow
	String ACTION_CONVERSATIONS_REQUEST = "eu.siacs.conversations.location.request";
	String ACTION_CONVERSATIONS_SHOW = "eu.siacs.conversations.location.show";
	String GEO = "geo";
	String LATITUDE = "latitude";
	String LONGITUDE = "longitude";
	String ALTITUDE = "altitude";
	String ACCURACY = "accuracy";
	String JID = "jid";
	String NAME = "name";
	String DESCRIPTION = "description";
	SimpleDateFormat ISODATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z+0000'", Locale.UK); // ISO8601
}
