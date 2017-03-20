package de.emdete.tabulae.fawlty;

import android.Manifest;
import android.os.Build;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static de.emdete.tabulae.fawlty.Constants.*;

public class Meta implements Iterator<TheDictionary>, Iterable<TheDictionary> {
	static final private boolean SEND_P1 = true;
	static final private boolean SEND_P2 = false;
	private TelephonyManager telephonyManager;

	public Meta(TelephonyManager telephonyManager) {
		this.telephonyManager = telephonyManager;
	}

	static String phone_type_text(int i) {
		switch (i) {
			case TelephonyManager.PHONE_TYPE_CDMA:
				return "PHONE_TYPE_CDMA";
			case TelephonyManager.PHONE_TYPE_GSM:
				return "PHONE_TYPE_GSM";
			case TelephonyManager.PHONE_TYPE_NONE:
				return "PHONE_TYPE_NONE";
			case TelephonyManager.PHONE_TYPE_SIP:
				return "PHONE_TYPE_SIP";
			default:
				return "PHONE_TYPE_" + i;
		}
	}

	static String sim_state_text(int i) {
		switch (i) {
			case TelephonyManager.SIM_STATE_ABSENT:
				return "SIM_STATE_ABSENT";
			case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
				return "SIM_STATE_NETWORK_LOCKED";
			case TelephonyManager.SIM_STATE_PIN_REQUIRED:
				return "SIM_STATE_PIN_REQUIRED";
			case TelephonyManager.SIM_STATE_PUK_REQUIRED:
				return "SIM_STATE_PUK_REQUIRED";
			case TelephonyManager.SIM_STATE_READY:
				return "SIM_STATE_READY";
			case TelephonyManager.SIM_STATE_UNKNOWN:
				return "SIM_STATE_UNKNOWN";
			default:
				return "SIM_STATE_" + i;
		}
	}

	static String call_state_text(int i) {
		switch (i) {
			case TelephonyManager.CALL_STATE_IDLE:
				return "CALL_STATE_IDLE";
			case TelephonyManager.CALL_STATE_OFFHOOK:
				return "CALL_STATE_OFFHOOK";
			case TelephonyManager.CALL_STATE_RINGING:
				return "CALL_STATE_RINGING";
			default:
				return "CALL_STATE_" + i;
		}
	}

	static String data_activity_text(int i) {
		switch (i) {
			case TelephonyManager.DATA_ACTIVITY_DORMANT:
				return "DATA_ACTIVITY_DORMANT";
			case TelephonyManager.DATA_ACTIVITY_IN:
				return "DATA_ACTIVITY_IN";
			case TelephonyManager.DATA_ACTIVITY_INOUT:
				return "DATA_ACTIVITY_INOUT";
			case TelephonyManager.DATA_ACTIVITY_NONE:
				return "DATA_ACTIVITY_NONE";
			case TelephonyManager.DATA_ACTIVITY_OUT:
				return "DATA_ACTIVITY_OUT";
			default:
				return "DATA_ACTIVITY_" + i;
		}
	}

	static String data_state_text(int i) {
		switch (i) {
			case TelephonyManager.DATA_CONNECTED:
				return "DATA_CONNECTED";
			case TelephonyManager.DATA_CONNECTING:
				return "DATA_CONNECTING";
			case TelephonyManager.DATA_DISCONNECTED:
				return "DATA_DISCONNECTED";
			case TelephonyManager.DATA_SUSPENDED:
				return "DATA_SUSPENDED";
			default:
				return "DATA_STATE_" + i;
		}
	}

	static String network_type_text(int i) {
		switch (i) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return "NETWORK_TYPE_1xRTT";
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return "NETWORK_TYPE_CDMA";
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return "NETWORK_TYPE_EDGE";
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				return "NETWORK_TYPE_EHRPD";
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return "NETWORK_TYPE_EVDO_0";
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return "NETWORK_TYPE_EVDO_A";
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
				return "NETWORK_TYPE_EVDO_B";
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return "NETWORK_TYPE_GPRS";
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return "NETWORK_TYPE_HSDPA";
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return "NETWORK_TYPE_HSPA";
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return "NETWORK_TYPE_HSPAP";
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return "NETWORK_TYPE_HSUPA";
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return "NETWORK_TYPE_IDEN";
			case TelephonyManager.NETWORK_TYPE_LTE:
				return "NETWORK_TYPE_LTE";
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return "NETWORK_TYPE_UMTS";
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return "NETWORK_TYPE_UNKNOWN";
			default:
				return "NETWORK_TYPE_" + i;
		}
	}

	/////////////////////
	public static void fill(TheDictionary map, TelephonyManager value) throws Exception {
		if (value != null) {
			map.put("type", "m");
			if (SEND_P1) {
				map.put("android_version", Build.VERSION.SDK_INT);
				try {
					map.put("imei", value.getDeviceId().substring(0, 8));
				}
				catch (Exception ignore) {
				}
				try {
					map.put("network_operator", value.getNetworkOperator());
				}
				catch (Exception ignore) {
				}
				try {
					map.put("sim_operator", value.getSimOperator());
				}
				catch (Exception ignore) {
				}
				if (CellIdPre17API.fallback_pre17api) {
					map.put("android_pre17api", CellIdPre17API.fallback_pre17api);
				}
				if (SEND_P2) {
					try {
						map.put("imei", value.getDeviceId());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("msisdn", value.getLine1Number());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("imsi", value.getSubscriberId());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_call_state", call_state_text(value.getCallState()));
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_data_activity", data_activity_text(value.getDataActivity()));
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_data_state", data_state_text(value.getDataState()));
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_device_software_version", value.getDeviceSoftwareVersion());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_group_id_level1", value.getGroupIdLevel1());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_network_country_iso", value.getNetworkCountryIso());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_network_operator_name", value.getNetworkOperatorName());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_network_type", network_type_text(value.getNetworkType()));
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_phone_type", phone_type_text(value.getPhoneType()));
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_sim_country_iso", value.getSimCountryIso());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_sim_operator_name", value.getSimOperatorName());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_sim_serial_number", value.getSimSerialNumber());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_sim_state", sim_state_text(value.getSimState()));
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_voice_mail_alpha_tag", value.getVoiceMailAlphaTag());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_voice_mail_number", value.getVoiceMailNumber());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_icc_card", value.hasIccCard());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_network_roaming", value.isNetworkRoaming());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_mms_ua_prof_url", value.getMmsUAProfUrl());
					}
					catch (Exception ignore) {
					}
					try {
						map.put("android_mms_user_agent", value.getMmsUserAgent());
					}
					catch (Exception ignore) {
					}
				}
			}
		}
	}

	///////////////////// test
	static public String test(Context context) {
		List<TheDictionary> arr = new ArrayList<>();
		int post17cells = 0;
		int pre17cells = 0;
		int gps = 0;
		int wlans = 0;
		int mobiles = 0;
		for (TheDictionary o : new CellId(((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getAllCellInfo())) {
			post17cells++;
			if (DEBUG) Log.d(TAG, "got: " + o);
			arr.add(o);
		}
		if (post17cells <= 0) {
			Log.e(TAG, "test: post-17 android, pre-17 api!");
		}
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		//noinspection deprecation
		for (TheDictionary o : new CellIdPre17API(telephonyManager, telephonyManager.getCellLocation(), telephonyManager.getNeighboringCellInfo())) {
			pre17cells++;
			if (DEBUG) Log.d(TAG, "got: " + o);
			arr.add(o);
		}
		for (TheDictionary o : new Satellite(((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER))) {
			gps++;
			if (DEBUG) Log.d(TAG, "got: " + o);
			arr.add(o);
		}
		for (TheDictionary o : new WifiId(((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getScanResults())) {
			wlans++;
			if (DEBUG) Log.d(TAG, "got: " + o);
			arr.add(o);
		}
		for (TheDictionary o : new Meta((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))) {
			mobiles++;
			if (DEBUG) Log.d(TAG, "got: " + o);
			arr.add(o);
		}
		if (DEBUG) Log.d(TAG, "json=" + arr.toString());
		return "post17cells=" + post17cells + '\n' +
				"pre17cells=" + pre17cells + '\n' +
				"gps=" + gps + '\n' +
				"wlans=" + wlans + '\n' +
				"mobiles=" + mobiles + '\n';
	}

	///////////////////////// enumerator stuff
	@Override
	public Iterator<TheDictionary> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return telephonyManager != null;
	}

	@Override
	public TheDictionary next() {
		TheDictionary map = new TheDictionary();
		try {
			fill(map, this.telephonyManager);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		telephonyManager = null;
		return map;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
