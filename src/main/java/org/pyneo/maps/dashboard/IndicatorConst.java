package org.pyneo.maps.dashboard;

public interface IndicatorConst {
	String GPS = "gps";
	String OFF = "off";
	String EMPTY = "";

	String JNAME = "name";
	String JINDICATORS = "indicators";
	String JMAIN = "main";
	String JMAINLANDSCAPE = "main_landscape";
	String JTAG = "tag";
	String JINDEX = "index";
	String DASHBOARD_DIR = "data/dashboards";
	String DASHBOARD_FILE = "%s/%s.json";

	String GPSSPEED = "gpsspeed";
	String GPSLAT = "gpslat";
	String GPSLON = "gpslon";
	String GPSBEARING = "gpsbearing";
	String GPSELEV = "gpselev";
	String GPSACCURACY = "gpsaccuracy";
	String GPSTIME = "gpstime"; // UTC time of this fix, in milliseconds since January 1, 1970
	String GPSPROVIDER = "gpsprovider";

	String MAPNAME = "mapname";
	String MAPZOOM = "mapzoom";
	String MAPCENTERLAT = "mapcenterlat";
	String MAPCENTERLON = "mapcenterlon";

	String TRCNT = "trcnt";
	String TRDIST = "trdist";
	String TRDURATION = "trduration";
	String TRMAXSPEED = "trmaxspeed";
	String TRAVGSPEED = "travgspeed";
	String TRMOVETIME = "trmovetime";
	String TRAVGMOVESPEED = "travgmovespeed";

	String TARGETDISTANCE = "targetdistance";
	String TARGETBEARING = "targetbearing";
}
