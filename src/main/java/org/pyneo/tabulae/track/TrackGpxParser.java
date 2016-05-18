package org.pyneo.tabulae.track;

import android.util.Log;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.database.sqlite.SQLiteDatabase;
import static org.pyneo.tabulae.track.Constants.*;

class TrackGpxParser {
	/* parse xml like:
	* <gpx ...><name>...</name><desc /><trk><trkseg><trkpt lon="..." lat="..."><ele>..</ele><time>2015-08-11T00:00:00Z</time>...
	* see https://de.wikipedia.org/wiki/GPS_Exchange_Format
	* see http://www.topografix.com/GPX/1/1/
	*/
	static final SimpleDateFormat[] simpleDateFormats = new SimpleDateFormat[]{
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd HH:mmZ", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd", Locale.US),
	};
	private static final String CMT = "cmt";
	private static final String DESC = "desc";
	private static final String ELE = "ele";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private static final String NAME = "name";
	private static final String TIME = "time";
	private static final String TRKPT = "trkpt";
	private static final String TRK = "trk";
	private static final String WPT = "wpt";
	static SAXParserFactory factory = SAXParserFactory.newInstance();

	static {
		final TimeZone UTC = TimeZone.getTimeZone("UTC");
		for (SimpleDateFormat sdf : simpleDateFormats) {
			sdf.setTimeZone(UTC);
		}
	}

	SQLiteDatabase db;
	SimpleDateFormat successSdf = simpleDateFormats[0];
	TrackItem trackItem;

	public TrackGpxParser(File file, SQLiteDatabase db) throws Exception {
		SAXParser parser = factory.newSAXParser();
		parser.parse(file, new Handler());
	}

	Date parseDate(final String str) {
		try { // try the last successful format to parse
			return successSdf.parse(str);
		}
		catch (ParseException ignore) {
		}
		for (SimpleDateFormat sdf : simpleDateFormats) {
			try {
				successSdf = sdf;
				return sdf.parse(str);
			}
			catch (ParseException e) {
				// if (DEBUG) Log.e(TAG, e.toString(), e);
				// ignore parser errors
			}
		}
		return new Date(0);
	}

	class Handler extends DefaultHandler {
		StringBuilder cdata = new StringBuilder();
		TrackPointItem trackPointItem;

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			cdata.append(ch, start, length);
			super.characters(ch, start, length);
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			cdata.delete(0, cdata.length());
			if (localName.equalsIgnoreCase(TRKPT)) {
				if (trackPointItem != null) Log.e(TAG, "trkpt in trkpt");
				trackPointItem = new TrackPointItem(
						Double.parseDouble(attributes.getValue(LAT)), // latitude
						Double.parseDouble(attributes.getValue(LON)) // longitude
				);
			} else if (localName.equalsIgnoreCase("gpx")) {
				if (DEBUG)
					Log.d(TAG, "start gpx version=" + attributes.getValue("version") + ", creator=" + attributes.getValue("creator"));
				trackItem = new TrackItem();
			} else if (localName.equalsIgnoreCase("rte")) {
				if (DEBUG) Log.d(TAG, "This is probably a route file");
			} else if (localName.equalsIgnoreCase(WPT)) {
				if (DEBUG) Log.d(TAG, "This is probably a way point file");
			}
			super.startElement(uri, localName, name, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			if (trackItem != null) { // we need a TRK tag around
				if (localName.equalsIgnoreCase(NAME)) {
					trackItem.setName(cdata.toString().trim());
				} else if (localName.equalsIgnoreCase(CMT)) {
					trackItem.setComment(cdata.toString().trim());
				} else if (localName.equalsIgnoreCase(DESC)) {
					trackItem.setDescription(cdata.toString().trim());
				}
			}
			if (trackPointItem != null) { // we need a TRKPT tag around
				if (localName.equalsIgnoreCase(ELE)) {
					trackPointItem.setAltitude((int) Double.parseDouble(cdata.toString().trim()));
				} else if (localName.equalsIgnoreCase(TIME)) {
					trackPointItem.setTimestamp(parseDate(cdata.toString().trim()));
				} else if (localName.equalsIgnoreCase("sym")) {
					trackPointItem.setAttribute(Integer.parseInt(cdata.toString().trim()));
				} else if (localName.equalsIgnoreCase(TRKPT)) {
					try {
						trackItem.add(db, trackPointItem);
					}
					catch (Exception e) {
						throw new SAXException(e.toString(), e);
					}
					trackPointItem = null;
				}
			}
			super.endElement(uri, localName, name);
		}
	}
}
