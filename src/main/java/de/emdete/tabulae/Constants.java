package de.emdete.tabulae;

import java.text.SimpleDateFormat;
import java.util.Locale;
import android.os.Build;

public interface Constants {
	String TAG = "de.emdete.tabulae";
	String USER_AGENT = "Tabulae/" + BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + ")";
	boolean DEBUG = BuildConfig.DEBUG;
	// static { DEBUG = Log.isLoggable("de.emdete.android", Log.DEBUG); }
	float USER_FONT_FACTOR = 1.3f; // TODO: determine sp/dp factor somehow
	String ACTION_CONVERSATIONS_REQUEST = "eu.siacs.conversations.location.request";
	String ACTION_CONVERSATIONS_SHOW = "eu.siacs.conversations.location.show";
	String GEO = "geo";
	String HTTPS = "https";
	String HTTP = "http";
	String LATITUDE = "latitude";
	String LONGITUDE = "longitude";
	String ALTITUDE = "altitude";
	String ACCURACY = "accuracy";
	String JID = "jid";
	String NAME = "name";
	String DESCRIPTION = "description";
	SimpleDateFormat ISODATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z+0000'", Locale.UK); // ISO8601
}
