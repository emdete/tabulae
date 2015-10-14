package org.pyneo.tabulae.fawlty;

import android.content.Context;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

public class CellIdPre17API implements Constants, Iterator<TheDictionary>, Iterable<TheDictionary> {
	private int mcc = NeighboringCellInfo.UNKNOWN_CID; // NeighboringCellInfo.UNKNOWN_CID == -1
	private int mnc = NeighboringCellInfo.UNKNOWN_CID;
	private int type = NeighboringCellInfo.UNKNOWN_CID;
	private CellLocation cellLocation;
	private List<NeighboringCellInfo> neighboringCellInfoList;
	private int i;
	public static boolean fallback_pre17api = false;

	public CellIdPre17API(TelephonyManager telephonyManager, CellLocation cellLocation, List<NeighboringCellInfo> neighboringCellInfoList) {
		if (DEBUG) Log.d(TAG, "CellIdPre17API:");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			fallback_pre17api = true;
		}
		this.cellLocation = cellLocation;
		this.neighboringCellInfoList = neighboringCellInfoList;
		if (cellLocation != null) {
			this.i = -1;
		}
		else {
			this.i = 0;
		}
		String mccmnc = telephonyManager.getNetworkOperator();
		if (mccmnc != null && mccmnc.length() >= 5 && mccmnc.length() <= 6) {
			mcc = Integer.parseInt(mccmnc.substring(0, 3));
			mnc = Integer.parseInt(mccmnc.substring(3));
		}
		else {
			Log.e(TAG, "CellIdPre17API: wrong legnth (5-6) for mccmnc=" + mccmnc);
		}
		type = telephonyManager.getNetworkType();
		if (DEBUG) Log.d(TAG, "CellIdPre17API: mcc=" + mcc + ", mnc=" + mnc + ", type=" + type + " cellLocation=" + cellLocation + ", neighboringCellInfoList=" + neighboringCellInfoList);
	}

	void determine_type(TheDictionary map) {
		switch (type) {
			case TelephonyManager.NETWORK_TYPE_CDMA: {
				map.put("type", "1");
			}
			break;
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE: {
				map.put("type", "2");
			}
			break;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case TelephonyManager.NETWORK_TYPE_HSUPA: {
				map.put("type", "3");
				Integer i = (Integer)map.get("cid");
				if (i != null) {
					map.put("cid", i % 0x10000);
					map.put("rncid", i / 0x10000);
				}
			}
			break;
			case TelephonyManager.NETWORK_TYPE_LTE: {
				map.put("type", "4");
				Integer i = (Integer)map.pop("lac");
				if (i != null) {
					map.put("tac", i);
				}
				i = (Integer)map.pop("cid");
				if (i != null) {
					map.put("ci", i);
				}
			}
			break;
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_IDEN:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			default: {
				map.put("type", "0");
			}
		}
	}

	///////////////////////// enumerator stuff
	@Override
	public Iterator<TheDictionary> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if (DEBUG) Log.d(TAG, "hasNext:");
		return i < neighboringCellInfoList.size();
	}

	@Override
	public TheDictionary next() {
		if (DEBUG) Log.d(TAG, "next:");
		TheDictionary map = new TheDictionary();
		try {
			if (i < 0) {
				if (cellLocation instanceof GsmCellLocation) {
					fill(map, ((GsmCellLocation)cellLocation));
				}
				else if (cellLocation instanceof CdmaCellLocation) {
					fill(map, ((CdmaCellLocation)cellLocation));
				}
				else {
					map.put("class", cellLocation.getClass().getName());
					map.put("string", cellLocation.toString());
				}
			}
			else {
				fill(map, neighboringCellInfoList.get(i));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		i++;
		if (DEBUG) Log.d(TAG, "next: map=" + map);
		return map;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	//////////////////////////////// older than api 17:
	public void fill(TheDictionary map, NeighboringCellInfo value) throws Exception {
		if (value != null) {
			map.put("mcc", mcc);
			map.put("mnc", mnc);
			int i;
			i = value.getPsc(); if (i != NeighboringCellInfo.UNKNOWN_CID) map.put("psc", i);
			i = value.getRssi(); if (i != NeighboringCellInfo.UNKNOWN_CID) map.put("rssi", i);
			i = value.getLac(); if (i != NeighboringCellInfo.UNKNOWN_CID) map.put("lac", i);
			i = value.getCid(); if (i != NeighboringCellInfo.UNKNOWN_CID) map.put("cid", i);
			map.put("registered", false);
			determine_type(map);
		}
	}

	public void fill(TheDictionary map, CdmaCellLocation value) throws Exception {
		if (value != null) {
			map.put("mcc", mcc);
			map.put("mnc", mnc);
			map.put("base_station_id", value.getBaseStationId());
			map.put("latitude", value.getBaseStationLatitude() / 14400.0);
			map.put("longitude", value.getBaseStationLongitude() / 14400.0);
			map.put("network_id", value.getNetworkId());
			map.put("systen_id", value.getSystemId());
			map.put("registered", true);
			determine_type(map);
		}
	}

	public void fill(TheDictionary map, GsmCellLocation value) throws Exception {
		if (value != null) {
			map.put("mcc", mcc);
			map.put("mnc", mnc);
			map.put("cid", value.getCid());
			map.put("lac", value.getLac());
			map.put("psc", value.getPsc());
			map.put("registered", true);
			determine_type(map);
		}
	}

	///////////////////// test
	static public String test(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		int a = 0;
		int b = 0;
		int c = 0;
		for (TheDictionary o: new CellIdPre17API(telephonyManager, telephonyManager.getCellLocation(), telephonyManager.getNeighboringCellInfo())) {
			a++;
			if (DEBUG) Log.d(TAG, "got: " + o);
		}
		return "counts: " + a + '/' + b + '/' + c;
	}
}
