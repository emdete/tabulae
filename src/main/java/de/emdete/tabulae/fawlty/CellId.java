package de.emdete.tabulae.fawlty;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.Iterator;
import java.util.List;
import static de.emdete.tabulae.fawlty.Constants.*;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class CellId implements Iterator<TheDictionary>, Iterable<TheDictionary> {
	private List<CellInfo> cellInfoList;
	private int i = 0;

	public CellId(List<CellInfo> value) {
		this.cellInfoList = value;
	}

	//////////////////////////////// api 17 and younger:
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static void fill(TheDictionary map, CellInfo value) throws Exception {
		if (value != null) {
			map.put("time_stamp", value.getTimeStamp());
			map.put("registered", value.isRegistered());
			if (value instanceof CellInfoCdma) {
				fill(map, ((CellInfoCdma) value).getCellIdentity());
				fill(map, ((CellInfoCdma) value).getCellSignalStrength());
			} else if (value instanceof CellInfoGsm) {
				fill(map, ((CellInfoGsm) value).getCellIdentity());
				fill(map, ((CellInfoGsm) value).getCellSignalStrength());
			} else if (value instanceof CellInfoWcdma) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					fill(map, ((CellInfoWcdma) value).getCellIdentity());
					fill(map, ((CellInfoWcdma) value).getCellSignalStrength());
				}
			} else if (value instanceof CellInfoLte) {
				fill(map, ((CellInfoLte) value).getCellIdentity());
				fill(map, ((CellInfoLte) value).getCellSignalStrength());
			} else {
				map.put("class", value.getClass().getName());
				map.put("string", value.toString());
			}
		}
	}

	public static void fill(TheDictionary map, CellIdentityCdma value) throws Exception {
		if (value != null) {
			map.put("type", "1");
			int i;
			i = value.getBasestationId();
			if (i != Integer.MAX_VALUE) map.put("basestation_id", i);
			i = value.getLatitude();
			if (i != Integer.MAX_VALUE) map.put("latitude", i);
			i = value.getLongitude();
			if (i != Integer.MAX_VALUE) map.put("longitude", i);
			i = value.getNetworkId();
			if (i != Integer.MAX_VALUE) map.put("network_id", i);
			i = value.getSystemId();
			if (i != Integer.MAX_VALUE) map.put("system_id", i);
		}
	}

	public static void fill(TheDictionary map, CellIdentityGsm value) throws Exception {
		if (value != null) {
			map.put("type", "2");
			int i;
			i = value.getMcc();
			if (i != Integer.MAX_VALUE && i > 0 && i < 1000) map.put("mcc", i);
			i = value.getMnc();
			if (i != Integer.MAX_VALUE && i > 0 && i < 1000) map.put("mnc", i);
			i = value.getLac();
			if (i != Integer.MAX_VALUE && i > 0) map.put("lac", i);
			i = value.getCid();
			if (i != Integer.MAX_VALUE && i > 0) map.put("cid", i);
			//map.put("psc", value.getPsc()); // depricated
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static void fill(TheDictionary map, CellIdentityWcdma value) throws Exception {
		if (value != null) {
			map.put("type", "3");
			int i;
			i = value.getMcc();
			if (i != Integer.MAX_VALUE && i > 0 && i < 1000) map.put("mcc", i);
			i = value.getMnc();
			if (i != Integer.MAX_VALUE && i > 0 && i < 1000) map.put("mnc", i);
			i = value.getLac();
			if (i != Integer.MAX_VALUE && i > 0) map.put("lac", i);
			i = value.getPsc();
			if (i != Integer.MAX_VALUE && i > 0) map.put("psc", i);
			i = value.getCid();
			if (i != Integer.MAX_VALUE && i > 0) {
				if (i >= 0x10000) {
					map.put("rncid", i / 0x10000);
					i %= 0x10000;
				}
				map.put("cid", i);
			}
		}
	}

	public static void fill(TheDictionary map, CellIdentityLte value) throws Exception {
		if (value != null) {
			map.put("type", "4");
			int i;
			i = value.getMcc();
			if (i != Integer.MAX_VALUE && i > 0 && i < 1000) map.put("mcc", i);
			i = value.getMnc();
			if (i != Integer.MAX_VALUE && i > 0 && i < 1000) map.put("mnc", i);
			i = value.getTac();
			if (i != Integer.MAX_VALUE && i > 0) map.put("tac", i);
			i = value.getPci();
			if (i != Integer.MAX_VALUE && i > 0) map.put("pci", i);
			i = value.getCi();
			if (i != Integer.MAX_VALUE && i > 0) map.put("cid", i);
		}
	}

	public static void fill(TheDictionary map, CellSignalStrengthCdma value) throws Exception {
		if (value != null) {
			int i;
			i = value.getAsuLevel();
			if (i != 99) map.put("asu_level", i);
			map.put("dbm", value.getDbm());
			map.put("level", value.getLevel());
		}
	}

	public static void fill(TheDictionary map, CellSignalStrengthGsm value) throws Exception {
		if (value != null) {
			int i;
			i = value.getAsuLevel();
			if (i != 99) map.put("asu_level", i);
			map.put("dbm", value.getDbm());
			map.put("level", value.getLevel());
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static void fill(TheDictionary map, CellSignalStrengthWcdma value) throws Exception {
		if (value != null) {
			int i;
			i = value.getAsuLevel();
			if (i != 99) map.put("asu_level", i);
			map.put("dbm", value.getDbm());
			map.put("level", value.getLevel());
		}
	}

	public static void fill(TheDictionary map, CellSignalStrengthLte value) throws Exception {
		if (value != null) {
			int i;
			i = value.getTimingAdvance();
			if (i != Integer.MAX_VALUE) map.put("timing_advance", i);
			i = value.getAsuLevel();
			if (i != 99) map.put("asu_level", i);
			map.put("dbm", value.getDbm());
			map.put("level", value.getLevel());
		}
	}

	///////////////////// test
	static public String test(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int a = 0;
		int b = 0;
		int c = 0;
		for (TheDictionary o : new CellId(telephonyManager.getAllCellInfo())) {
			a++;
			if (DEBUG) Log.d(TAG, "got: " + o);
		}
		return "counts: " + a + '/' + b + '/' + c;
	}

	///////////////////////// enumerator stuff
	@Override
	public Iterator<TheDictionary> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return cellInfoList != null && i < cellInfoList.size();
	}

	@Override
	public TheDictionary next() {
		TheDictionary map = new TheDictionary();
		if (cellInfoList != null) {
			try {
				fill(map, cellInfoList.get(i));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			i++;
		}
		return map;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
