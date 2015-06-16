package org.pyneo.maps.poi;

import org.pyneo.maps.R;

public interface Constants extends org.pyneo.maps.Constants {
	String CATEGORYID = "categoryid";
	String POINTID = "pointid";

	public enum category { categoryid, name, hidden, iconid, minzoom };
	public enum points { pointid, name, descr, lat, lon, alt, hidden, categoryid, iconid };
}
