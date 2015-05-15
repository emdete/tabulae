// Created by plusminus on 18:00:24 - 25.09.2008
package org.andnav.osm.util;

/**
 *
 * @author Nicolas Gramlich
 *
 */
public interface Constants {
	boolean DEBUGMODE = false;
	int MAPTILE_LATITUDE_INDEX = 0;
	int MAPTILE_LONGITUDE_INDEX = 1;
	int OpenSpaceUpperBoundArray[] = { 2, 5, 10, 25, 50, 100, 200, 500, 1000, 2000, 4000 };
	float DEG2RAD = (float)(Math.PI / 180.0);
	float RAD2DEG = (float)(180.0 / Math.PI);
	float PI = (float)Math.PI;
	float PI_4 = PI / 4.0f;
	float PI_2 = PI / 2.0f;
	int RADIUS_EARTH_METERS = 6378140;
	String DEBUGTAG = "org.andnav.osm";
	int NOT_SET = Integer.MIN_VALUE;
}
