package org.pyneo.tabulae.track;

import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.pyneo.tabulae.gui.Track;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/* parse xml like:
* <gpx ...><name>...</name><desc /><trk><trkseg><trkpt lon="..." lat="..."><ele>..</ele><time>2015-08-11T00:00:00Z</time>...
*/
public class TrackGpxParser implements Iterable<TrackGpxParser.TrackPoint>, Constants {
	public static SAXParserFactory factory = SAXParserFactory.newInstance();
	public static final String[] formats = new String[]{
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		"yyyy-MM-dd'T'HH:mm:ssZ",
		"yyyy-MM-dd'T'HH:mmZ",
		"yyyy-MM-dd'T'HH:mm:ss'Z'",
		"yyyy-MM-dd HH:mm:ss.SSSZ",
		"yyyy-MM-dd HH:mmZ",
		"yyyy-MM-dd HH:mm",
		"yyyy-MM-dd",
		};
	private static final String CMT = "cmt";
	private static final String DESC = "desc";
	private static final String ELE = "ele";
	private static final String NAME = "name";
	private static final String TIME = "time";
	private static final String TRK = "trk";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private static final String TRKPT = "trkpt";
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	private List<TrackPoint> track = new ArrayList<TrackPoint>();
	private String trackname;
	private String description;

	public TrackGpxParser(File file) throws Exception {
		SAXParser parser = factory.newSAXParser();
		parser.parse(file, new Handler());
	}

	public Iterator<TrackPoint> iterator() {
		return track.iterator();
	}

	public static Date parseDate(final String str) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(UTC);
		for (String format : formats) {
			sdf.applyPattern(format);
			try {
				return sdf.parse(str);
			}
			catch (ParseException e) {
				// if (DEBUG) Log.e(TAG, e.toString(), e);
				; // ignore parser errors
			}
		}
		return new Date(0);
	}

	private class Handler extends DefaultHandler {
		private StringBuilder cdata = new StringBuilder();
		private TrackPoint trkpt;

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			cdata.append(ch, start, length);
			super.characters(ch, start, length);
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			cdata.delete(0, cdata.length());
			if (localName.equalsIgnoreCase(TRK)) {
				;
			}
			else if (localName.equalsIgnoreCase(TRKPT)) {
				trkpt = new TrackPoint(
					Double.parseDouble(attributes.getValue(LAT)), // latitude
					Double.parseDouble(attributes.getValue(LON)) // longitude
					);
			}
			super.startElement(uri, localName, name, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			if (localName.equalsIgnoreCase(TRK)) {
				; // done reading
			}
			else if (localName.equalsIgnoreCase(NAME)) {
				trackname = cdata.toString().trim(); // trackname
			}
			else if (localName.equalsIgnoreCase(CMT)) {
				cdata.toString().trim(); //
			}
			else if (localName.equalsIgnoreCase(DESC)) {
				description = cdata.toString().trim(); // trackdescription
			}
			else if (localName.equalsIgnoreCase(ELE)) {
				trkpt.setAltitude((int)Double.parseDouble(cdata.toString().trim())); // altitude
			}
			else if (localName.equalsIgnoreCase(TIME)) {
				trkpt.setTimestamp(parseDate(cdata.toString().trim())); // time
			}
			else if (localName.equalsIgnoreCase(TRKPT)) {
				track.add(trkpt);
				trkpt = null;
			}
			super.endElement(uri, localName, name);
		}
	}

	static public class TrackPoint extends GeoPoint {
		private Date timestamp;

		TrackPoint(final double latitude, final double longitude) {
			super(latitude, longitude);
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
	}
}
