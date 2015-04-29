package org.pyneo.maps.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceGroup;
import android.view.Menu;
import android.view.MenuItem;

import org.pyneo.maps.MainPreferences;
import org.pyneo.maps.R;
import org.pyneo.maps.preference.PredefMapsPrefActivity;
import org.pyneo.maps.tileprovider.TileSourceBase;
import org.pyneo.maps.utils.CheckBoxPreferenceExt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
* This class catches events from parsing predefmaps xml and applies the
* attributes to different objects. Depending on the ctor used this is either a
* TileSourceBase, a Menu, a PreferenceGroup or a ArrayList.
*
* TODO: This should me done cleaner with different subclasses :/
*/
public class PredefMapsParser extends DefaultHandler {
	private static final String MAP = "map";
	private static final String LAYER = "layer";
	private static final String TIMEDEPENDENT = "timedependent";
	private static final String CACHE = "cache";
	private static final String TRUE = "true";
	private static final String ID = "id";
	private static final String CATEGORY = "cat";
	private static final String NAME = "name";
	private static final String DESCR = "descr";
	private static final String BASEURL = "baseurl";
	private static final String IMAGE_FILENAMEENDING = "image_filenameending";
	private static final String ZOOM_MINLEVEL = "zoom_minlevel";
	private static final String ZOOM_MAXLEVEL = "zoom_maxlevel";
	private static final String MAPTILE_SIZEPX = "maptile_sizepx";
	private static final String URL_BUILDER_TYPE = "url_builder_type";
	private static final String TILE_SOURCE_TYPE = "tile_source_type";
	private static final String PROJECTION = "projection";
	private static final String YANDEX_TRAFFIC_ON = "yandex_traffic_on";
	private static final String GOOGLESCALE = "googlescale";
	private final TileSourceBase mRendererInfo;
	private final String mMapId;
	private Menu mSubmenu;
	private boolean mNeedMaps;
	private boolean mNeedOverlays;
	private int mNeedProjection;
	private PreferenceGroup mPrefMapsgroup;
	private PreferenceGroup mPrefOverlaysgroup;
	private Context mPrefActivity;
	private SharedPreferences mSharedPreferences;
	private ArrayList<String> mID;
	private ArrayList<String> mName;

	public PredefMapsParser(final ArrayList<String> arrayListID, final ArrayList<String> arrayListName, final boolean aGetMaps, final boolean aGetOverlays, final int aProjection) {
		super();

		mID = arrayListID;
		mName = arrayListName;
		mNeedMaps = aGetMaps;
		mNeedOverlays = aGetOverlays;
		mNeedProjection = aProjection;

		mSubmenu = null;
		mRendererInfo = null;
		mMapId = null;
		mPrefMapsgroup = null;
		mPrefOverlaysgroup = null;
		mPrefActivity = null;
	}

	public PredefMapsParser(final PreferenceGroup aPrefMapsgroup, final PreferenceGroup aPrefOverlaysgroup, final Context aPrefActivity) {
		super();
		mSubmenu = null;
		mRendererInfo = null;
		mMapId = null;
		mPrefMapsgroup = aPrefMapsgroup;
		mPrefOverlaysgroup = aPrefOverlaysgroup;
		mPrefActivity = aPrefActivity;
	}

	public PredefMapsParser(final Menu aSubmenu, final SharedPreferences pref, boolean aNeedOverlays, int aProjection) {
		super();
		mSubmenu = aSubmenu;
		mNeedOverlays = aNeedOverlays;
		mNeedProjection = aProjection;
		mSharedPreferences = pref;
		mRendererInfo = null;
		mMapId = null;
		mPrefMapsgroup = null;
		mPrefOverlaysgroup = null;
	}

	public PredefMapsParser(final Menu aSubmenu, final SharedPreferences pref) {
		this(aSubmenu, pref, false, 0);
	}

	public PredefMapsParser(final TileSourceBase aRendererInfo, final String aMapId) {
		super();
		mSubmenu = null;
		mRendererInfo = aRendererInfo;
		mMapId = aMapId;
		mPrefMapsgroup = null;
		mPrefOverlaysgroup = null;
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if (localName.equalsIgnoreCase(MAP)) {
			if (mRendererInfo != null) {
				// search for a ID in the xml to properly fill a mRendererInfo
				if (attributes.getValue(ID).equalsIgnoreCase(mMapId)) {
					mRendererInfo.ID = attributes.getValue(ID);
					mRendererInfo.MAPID = attributes.getValue(ID);
					mRendererInfo.CATEGORY = attributes.getValue(CATEGORY);
					mRendererInfo.NAME = attributes.getValue(NAME);
					mRendererInfo.BASEURL = attributes.getValue(BASEURL);
					mRendererInfo.ZOOM_MINLEVEL = Integer.parseInt(attributes.getValue(ZOOM_MINLEVEL));
					mRendererInfo.ZOOM_MAXLEVEL = Integer.parseInt(attributes.getValue(ZOOM_MAXLEVEL));
					mRendererInfo.IMAGE_FILENAMEENDING = attributes.getValue(IMAGE_FILENAMEENDING);
					mRendererInfo.MAPTILE_SIZEPX = Integer.parseInt(attributes.getValue(MAPTILE_SIZEPX));
					mRendererInfo.URL_BUILDER_TYPE = Integer.parseInt(attributes.getValue(URL_BUILDER_TYPE));
					mRendererInfo.TILE_SOURCE_TYPE = Integer.parseInt(attributes.getValue(TILE_SOURCE_TYPE));
					mRendererInfo.PROJECTION = Integer.parseInt(attributes.getValue(PROJECTION));
					mRendererInfo.YANDEX_TRAFFIC_ON = Integer.parseInt(attributes.getValue(YANDEX_TRAFFIC_ON));
					mRendererInfo.TIMEDEPENDENT = false;
					if (attributes.getIndex(TIMEDEPENDENT) > -1)
						mRendererInfo.TIMEDEPENDENT = Boolean.parseBoolean(attributes.getValue(TIMEDEPENDENT));
					mRendererInfo.LAYER = false;
					if (attributes.getIndex(LAYER) > -1)
						mRendererInfo.LAYER = Boolean.parseBoolean(attributes.getValue(LAYER));
					mRendererInfo.CACHE = "";
					if (attributes.getIndex(CACHE) > -1)
						mRendererInfo.CACHE = attributes.getValue(CACHE);
					mRendererInfo.GOOGLESCALE = false;
					if (attributes.getIndex(GOOGLESCALE) > -1)
						mRendererInfo.GOOGLESCALE = Boolean.parseBoolean(attributes.getValue(GOOGLESCALE));
					// TODO prevent double fill (in case two providers are configured with the same id)
				}
			} else if (mSubmenu != null) {
				// fill a menu with all possible tile providers
				final int i = attributes.getIndex(LAYER);
				boolean timeDependent = false;
				final int j = attributes.getIndex(TIMEDEPENDENT);
				if (j != -1)
					timeDependent = Boolean.parseBoolean(attributes.getValue(TIMEDEPENDENT));
				if (mSharedPreferences.getBoolean(MainPreferences.PREF_PREDEFMAPS_ + attributes.getValue(ID), true)) {
					final boolean isLayer = !(i == -1 || !attributes.getValue(LAYER).equalsIgnoreCase(TRUE));
					if (mNeedOverlays && isLayer && !timeDependent
						//&& (mNeedProjection == 0 || mNeedProjection == Integer.parseInt(attributes.getValue(PROJECTION)))
						|| !mNeedOverlays && !isLayer) {
						final MenuItem item = mSubmenu.add(R.id.isoverlay, Menu.NONE, Menu.NONE,
							attributes.getValue(CATEGORY) + ": " + attributes.getValue(NAME));
						item.setTitleCondensed(attributes.getValue(ID));
					}
				}
			} else if (mPrefMapsgroup != null && mPrefOverlaysgroup != null) {
				// fill a PreferenceGroup with all possible tile providers
				final int i = attributes.getIndex(LAYER);
				final PreferenceGroup prefGroup = (i == -1 || !attributes.getValue(LAYER).equalsIgnoreCase(TRUE))? mPrefMapsgroup: mPrefOverlaysgroup;
				final CheckBoxPreferenceExt pref = new CheckBoxPreferenceExt(mPrefActivity, MainPreferences.PREF_PREDEFMAPS_ + attributes.getValue(ID));
				pref.setKey(MainPreferences.PREF_PREDEFMAPS_ + attributes.getValue(ID) + "_screen");
				final Intent intent = new Intent(mPrefActivity, PredefMapsPrefActivity.class)
					.putExtra("Key", MainPreferences.PREF_PREDEFMAPS_ + attributes.getValue(ID))
					.putExtra(ID, attributes.getValue(ID))
					.putExtra(NAME, attributes.getValue(NAME))
					.putExtra(PROJECTION, Integer.parseInt(attributes.getValue(PROJECTION)))
					.putExtra(MAPTILE_SIZEPX, Integer.parseInt(attributes.getValue(MAPTILE_SIZEPX)));
				final int j = attributes.getIndex(GOOGLESCALE);
				if (j > -1 && attributes.getValue(GOOGLESCALE).equalsIgnoreCase(TRUE))
					intent.putExtra(GOOGLESCALE, true);
				pref.setIntent(intent);
				pref.setTitle(attributes.getValue(CATEGORY) + ": " + attributes.getValue(NAME));
				pref.setSummary(attributes.getValue(DESCR));
				prefGroup.addPreference(pref);
			} else if (mID != null) {
				// fill two lists with IDs and NAMEs
				final int i = attributes.getIndex(LAYER);
				boolean timeDependent = false;
				final int j = attributes.getIndex(TIMEDEPENDENT);
				if (j != -1)
					timeDependent = Boolean.parseBoolean(attributes.getValue(TIMEDEPENDENT));
				final boolean isLayer = !(i == -1 || !attributes.getValue(LAYER).equalsIgnoreCase(TRUE));
				final int proj = Integer.parseInt(attributes.getValue(PROJECTION));
				if (mNeedMaps && !isLayer || mNeedOverlays && isLayer && !timeDependent && (mNeedProjection == 0 || mNeedProjection == proj)) {
					mID.add(attributes.getValue(ID));
					mName.add(attributes.getValue(NAME));
				}
			} else
				; // TODO report fail!
		}
		super.startElement(uri, localName, name, attributes);
	}

}
