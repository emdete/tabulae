// Created by plusminus on 00:47:05 - 02.10.2008
package org.pyneo.maps.utils;


import android.location.Location;

/**
 * Converts some usual types from one to another.
 * @author Nicolas Gramlich
 *
 */
public class TypeConverter {
	public static GeoPoint locationToGeoPoint(final Location aLoc) {
		return new GeoPoint((int)(aLoc.getLatitude() * 1E6), (int)(aLoc.getLongitude() * 1E6));
	}
}
