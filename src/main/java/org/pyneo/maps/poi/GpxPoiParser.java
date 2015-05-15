package org.pyneo.maps.poi;

import android.database.Cursor;

import org.andnav.osm.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

public class GpxPoiParser extends DefaultHandler implements Constants {
	private static final String WPT = "wpt";
	private static final String CMT = "cmt";
	private StringBuilder builder;
	private PoiManager mPoiManager;
	private PoiPoint mPoiPoint;
	private int mCategoryId;
	private HashMap<String, Integer> mCategoryMap;

	public GpxPoiParser(PoiManager poiManager, int CategoryId) {
		super();
		builder = new StringBuilder();
		mPoiManager = poiManager;
		mCategoryId = CategoryId;
		mPoiPoint = new PoiPoint();

		mCategoryMap = new HashMap<String, Integer>();
		Cursor c = mPoiManager.getPoiCategoryListCursor();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					mCategoryMap.put(c.getString(0), c.getInt(2));
				}
				while (c.moveToNext());
			}
			c.close();
		}
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
		if (localName.equalsIgnoreCase(WPT)) {
			mPoiPoint = new PoiPoint();
			mPoiPoint.mCategoryId = mCategoryId;
			mPoiPoint.mGeoPoint = GeoPoint.from2DoubleString(attributes.getValue(LAT), attributes.getValue(LON));
		}
		else if (localName.equalsIgnoreCase("categoryid") && mPoiPoint != null) {
			final String attrName = attributes.getValue(Constants.NAME);
			if (mCategoryMap.containsKey(attrName)) {
				mPoiPoint.mCategoryId = mCategoryMap.get(attrName);
			}
			else {
				mPoiPoint.mCategoryId = (int)mPoiManager.addPoiCategory(attrName, 0, Integer.parseInt(attributes.getValue(Constants.ICONID)));
				mCategoryMap.put(attrName, mPoiPoint.mCategoryId);
			}
		}
		super.startElement(uri, localName, name, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase(WPT)) {
			if (mPoiPoint.mTitle.equalsIgnoreCase(""))
				mPoiPoint.mTitle = "POI";
			mPoiManager.updatePoi(mPoiPoint);
		}
		else if (localName.equalsIgnoreCase(NAME)) {
			if (mPoiPoint != null)
				mPoiPoint.mTitle = builder.toString().trim();
		}
		else if (localName.equalsIgnoreCase(CMT)) {
			if (mPoiPoint != null)
				mPoiPoint.mDescr = builder.toString().trim();
		}
		else if (localName.equalsIgnoreCase(DESC)) {
			if (mPoiPoint != null)
				if (mPoiPoint.mDescr.equals(""))
					mPoiPoint.mDescr = builder.toString().trim();
		}
		super.endElement(uri, localName, name);
	}

}
