package org.pyneo.maps.poi;

import android.database.Cursor;

import org.pyneo.maps.utils.GeoPoint;
import org.pyneo.maps.utils.CursorI;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

public class KmlPoiParser extends DefaultHandler implements Constants {
	private static final String Placemark = "Placemark";
	private static final String Point = "Point";
	private static final String NAME = "name";
	private static final String coordinates = "coordinates";
	private static final String description = "description";
	private StringBuilder builder;
	private PoiManager mPoiManager;
	private PoiPoint mPoiPoint;
	private int mCategoryId;
	private String[] mStrArray;
	private boolean mItIsPoint;
	private HashMap<String, Integer> mCategoryMap;

	public KmlPoiParser(PoiManager poiManager, int CategoryId) {
		super();
		builder = new StringBuilder();
		mPoiManager = poiManager;
		mCategoryId = CategoryId;
		mPoiPoint = new PoiPoint();
		mItIsPoint = false;

		mCategoryMap = new HashMap<String, Integer>();
		for (Cursor c: new CursorI(mPoiManager.getPoiCategories())) {
			mCategoryMap.put(c.getString(category.name.ordinal()), c.getInt(category.categoryid.ordinal()));
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
		if (localName.equalsIgnoreCase(Placemark)) {
			mPoiPoint = new PoiPoint();
			mPoiPoint.mCategoryId = mCategoryId;
			mItIsPoint = false;
		} else if (localName.equalsIgnoreCase("categoryid") && mPoiPoint != null) {
			final String attrName = attributes.getValue(Constants.NAME);
			if (mCategoryMap.containsKey(attrName)) {
				mPoiPoint.mCategoryId = mCategoryMap.get(attrName);
			} else {
				mPoiPoint.mCategoryId = (int)mPoiManager.addPoiCategory(attrName, 0, Integer.parseInt(attributes.getValue(Constants.ICONID)));
				mCategoryMap.put(attrName, mPoiPoint.mCategoryId);
			}
		}
		super.startElement(uri, localName, name, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase(Placemark)) {
			if (mItIsPoint) {
				if (mPoiPoint.mTitle.equalsIgnoreCase("")) mPoiPoint.mTitle = "POI";
				mPoiManager.updatePoi(mPoiPoint);
			}
		} else if (localName.equalsIgnoreCase(NAME)) {
			if (mPoiPoint != null)
				mPoiPoint.mTitle = builder.toString().trim();
		} else if (localName.equalsIgnoreCase(description)) {
			if (mPoiPoint != null)
				mPoiPoint.mDescr = builder.toString().trim();
		} else if (localName.equalsIgnoreCase(coordinates)) {
			mStrArray = builder.toString().split(",");
			if (mPoiPoint != null)
				mPoiPoint.mGeoPoint = GeoPoint.from2DoubleString(mStrArray[1], mStrArray[0]);
		} else if (localName.equalsIgnoreCase(Point)) {
			mItIsPoint = true;
		}
		super.endElement(uri, localName, name);
	}

}
