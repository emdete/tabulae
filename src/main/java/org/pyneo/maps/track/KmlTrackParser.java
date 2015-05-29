package org.pyneo.maps.track;

import org.pyneo.maps.poi.PoiManager;
import org.pyneo.maps.utils.Ut;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KmlTrackParser extends DefaultHandler {
	private static final String Placemark = "Placemark";
	private static final String LineString = "LineString";
	private static final String NAME = "name";
	private static final String coordinates = "coordinates";
	private static final String description = "description";
	private StringBuilder builder;
	private PoiManager mPoiManager;
	private Track mTrack;
	private String[] mStrArray;
	private String[] mStrArray2;
	private boolean mItIsTrack;

	public KmlTrackParser(PoiManager poiManager) {
		super();
		builder = new StringBuilder();
		mPoiManager = poiManager;
		mTrack = new Track();
		mItIsTrack = false;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		builder.append(ch, start, length);
		super.characters(ch, start, length);
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes)
		throws SAXException {
		builder.delete(0, builder.length());
		if (localName.equalsIgnoreCase(Placemark)) {
			mTrack = new Track();
			mItIsTrack = false;
		}
		super.startElement(uri, localName, name, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase(Placemark)) {
			if (mItIsTrack) {
				if (mTrack.Name.equalsIgnoreCase(""))
					mTrack.Name = "Track";
				mTrack.CalculateStat();
				mPoiManager.updateTrack(mTrack);
			}
		} else if (localName.equalsIgnoreCase(NAME)) {
			if (mTrack != null)
				mTrack.Name = builder.toString().trim();
		} else if (localName.equalsIgnoreCase(description)) {
			if (mTrack != null)
				mTrack.Descr = builder.toString().trim();
		} else if (localName.equalsIgnoreCase(coordinates)) {
			mStrArray = builder.toString().trim().split("\n");
			if (mStrArray.length < 2)
				mStrArray = builder.toString().trim().split(" ");
			for (int i = 0; i < mStrArray.length; i++) {
				if (!mStrArray[i].trim().equals("")) {
					mStrArray2 = mStrArray[i].trim().split(",");
					if (mTrack != null) {
						mTrack.AddTrackPoint();
						mTrack.LastTrackPoint.setLat(Double.parseDouble(mStrArray2[1]));
						mTrack.LastTrackPoint.setLon(Double.parseDouble(mStrArray2[0]));
						if (mStrArray2.length > 2)
							try {
								mTrack.LastTrackPoint.setAlt(Double.parseDouble(mStrArray2[2]));
							}
							catch (NumberFormatException e) {
								try {
									mTrack.LastTrackPoint.setAlt((double)Integer.parseInt(mStrArray2[2]));
								}
								catch (NumberFormatException e1) {
									Ut.e(e.toString(), e1);
								}
							}
					}
				}
			}
		} else if (localName.equalsIgnoreCase(LineString)) {
			mItIsTrack = true;
		}

		super.endElement(uri, localName, name);
	}

}
