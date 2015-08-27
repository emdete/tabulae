package org.pyneo.tabulae.fawlty;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.CellIdentityCdma;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.Runnable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class WirelessEnvListener extends PhoneStateListener implements Constants, Runnable {
	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
	long intervalSecs = 5000;
	TelephonyManager telephonyManager;
	private TheDictionary meta_map = new TheDictionary();
	String last_ident;
	Location last_location;

	WirelessEnvListener(Context context) {
		this.telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		try { Meta.fill(meta_map, telephonyManager); } catch (Exception e) { Log.e(TAG, e.getMessage(), e); }
	}

	void setInterval(long intervalSecs) {
		this.intervalSecs = intervalSecs;
	}

	void enable() {
		if (DEBUG) Log.d(TAG, "WirelessEnvListener.enable:");
		executor.scheduleAtFixedRate(this, intervalSecs / 2, intervalSecs, TimeUnit.MILLISECONDS);
	}

	void disable() {
		if (DEBUG) Log.d(TAG, "WirelessEnvListener.disable:");
		executor.shutdownNow();
		executor = new ScheduledThreadPoolExecutor(1);
		last_ident = null;
		last_location = null;
	}

	@Override public void onCellInfoChanged(List<CellInfo> cellInfos) {
		//if (DEBUG) Log.d(TAG, "WirelessEnvListener.onCellInfoChanged: cellInfos=" + cellInfos);
		try {
			Iterable<TheDictionary> cell_ids = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				if (cellInfos == null) {
					cellInfos = telephonyManager.getAllCellInfo();
					//if (DEBUG) Log.e(TAG, "WirelessEnvListener.onCellInfoChanged: from parameter cellInfos=" + cellInfos);
				}
				cell_ids = new CellId(cellInfos);
			}
			if (cellInfos == null) {
				//if (DEBUG) Log.e(TAG, "WirelessEnvListener.onCellInfoChanged: fallback to Pre17, cellInfos=" + cellInfos);
				cell_ids = new CellIdPre17API(telephonyManager, telephonyManager.getCellLocation(), telephonyManager.getNeighboringCellInfo());
			}
			//if (DEBUG) Log.d(TAG, "WirelessEnvListener.onCellInfoChanged: cell_ids=" + cell_ids);
			final TheList cellapi2_request = new TheList();
			boolean changed = false;
			if (cell_ids != null) {
				for (TheDictionary item: cell_ids) {
					String ident = item.getIdent();
					if (ident != null) { // check for complete id information
						cellapi2_request.add(item);
						if (item.getBoolean("registered") && !ident.equals(last_ident)) {
							changed = true;
							last_ident = ident;
						}
					}
				}
			}
			telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
			if (changed) {
				executor.execute(new Runnable() {
					public void run() {
						try {
							if (DEBUG) Log.d(TAG, "WirelessEnvListener.onCellInfoChanged: request=" + cellapi2_request);
							Location location = constructLocation(CellAPI2.retrieveLocation(meta_map, cellapi2_request, "single"));
							if (DEBUG) Log.d(TAG, "WirelessEnvListener.onCellInfoChanged: response=" + location);
							if (location != null) {
								onLocationChanged(location, last_ident);
							}
							last_location = location;
						}
						catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
						}
					}
				});
			}
			else {
				//if (DEBUG) Log.e(TAG, "WirelessEnvListener.onCellInfoChanged: no change in environment, no request done last_location=" + last_location);
				if (last_location != null) {
					onLocationChanged(last_location, last_ident);
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "e=" + e);
		}
	}

	public void onLocationChanged(Location location, String ident) {
		if (DEBUG) Log.e(TAG, "onLocationChanged: location=" + location);
	}

	static public Location constructLocation(TheList cellapi2_response) {
		Location location = null;
		for (TheDictionary ident_location: cellapi2_response) {
			if (!ident_location.getString("type").equals("m")) {
				long rcd = ident_location.getLong("rcd");
				if (rcd / 1000 == 2) {
					double latitude = ident_location.getDouble("latitude");
					double longitude = ident_location.getDouble("longitude");
					double accuracy = 5000.0;
					if (rcd != 2010 && ident_location.containsKey("radius")) { // radius given
						accuracy = ident_location.getDouble("radius");
					}
					location = new Location("cellapi2");
					location.setLatitude(latitude);
					location.setLongitude(longitude);
					location.setAccuracy((float)accuracy);
					location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
					location.setTime(System.currentTimeMillis());
					Bundle extra = new Bundle();
					extra.putDouble("latitude_tower", ident_location.getDouble("latitude_tower"));
					extra.putDouble("longitude_tower", ident_location.getDouble("longitude_tower"));
					extra.putLong("rcd", rcd);
					location.setExtras(extra);
					break;
				}
			}
		}
		return location;
	}

	public void run() {
		//if (DEBUG) Log.d(TAG, "run:");
		try {
			onCellInfoChanged(null);
			telephonyManager.listen(this, PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SERVICE_STATE);
			CellLocation.requestLocationUpdate();
		}
		finally {
		}
	}
}
