package org.pyneo.tabulae.track;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.mapsforge.core.model.LatLong;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

/* parse xml like:
* <gpx ...><name>...</name><desc /><trk><trkseg><trkpt lon="..." lat="..."><ele>..</ele><time>2015-08-11T00:00:00Z</time>...
* see https://de.wikipedia.org/wiki/GPS_Exchange_Format
*/
class TrackGpxParser implements Iterable<TrackGpxParser.TrackPoint>, Constants {
	static SAXParserFactory factory = SAXParserFactory.newInstance();
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
	static {
		final TimeZone UTC = TimeZone.getTimeZone("UTC");
		for (SimpleDateFormat sdf: simpleDateFormats) {
			sdf.setTimeZone(UTC);
		}
	}
	protected static final String CMT = "cmt";
	protected static final String DESC = "desc";
	protected static final String ELE = "ele";
	protected static final String NAME = "name";
	protected static final String TIME = "time";
	protected static final String TRK = "trk";
	protected static final String LAT = "lat";
	protected static final String LON = "lon";
	protected static final String TRKPT = "trkpt";
	protected SimpleDateFormat successSdf = simpleDateFormats[0];
	protected String trackname;
	protected String description;
	protected String comment;
	protected List<TrackPoint> track = new ArrayList<TrackPoint>();

	public TrackGpxParser(File file) throws Exception {
		SAXParser parser = factory.newSAXParser();
		parser.parse(file, new Handler());
	}

	public Iterator<TrackPoint> iterator() {
		return track.iterator();
	}

	Date parseDate(final String str) {
		try { // try the last successful format to parse
			return successSdf.parse(str);
		}
		catch (ParseException e) {
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

	protected class Handler extends DefaultHandler {
		protected StringBuilder cdata = new StringBuilder();
		protected TrackPoint trkpt;

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			cdata.append(ch, start, length);
			super.characters(ch, start, length);
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			cdata.delete(0, cdata.length());
			if (localName.equalsIgnoreCase(TRK)) {
				//
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
				// done reading
			}
			else if (localName.equalsIgnoreCase(NAME)) {
				trackname = cdata.toString().trim(); // trackname
			}
			else if (localName.equalsIgnoreCase(CMT)) {
				comment = cdata.toString().trim(); //
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

	static public class TrackPoint extends LatLong {
		protected Date timestamp;
		protected int altitude;

		TrackPoint(final double latitude, final double longitude) {
			super(latitude, longitude);
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}

		public void setAltitude(int altitude) {
			this.altitude = altitude;
		}

		public int getAltitude() {
			return altitude;
		}
	}
}
