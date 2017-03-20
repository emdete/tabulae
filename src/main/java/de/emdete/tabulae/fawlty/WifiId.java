package de.emdete.tabulae.fawlty;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import java.util.Iterator;
import java.util.List;
import static de.emdete.tabulae.fawlty.Constants.*;

public class WifiId implements Iterator<TheDictionary>, Iterable<TheDictionary> {
	private List<ScanResult> scanResults;
	private int i;

	public WifiId(List<ScanResult> scanResults) {
		this.scanResults = scanResults;
		this.i = 0;
	}

	///////////////////// test
	static public String test(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		int a = 0;
		int b = 0;
		int c = 0;
		for (TheDictionary o : new WifiId(wifiManager.getScanResults())) {
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
		return scanResults != null && i < scanResults.size();
	}

	@Override
	public TheDictionary next() {
		TheDictionary map = new TheDictionary();
		try {
			fill(map, scanResults.get(i));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		i++;
		return map;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/////////////////////
	private String unify(String bssid) {
		return bssid;
	}

	private void fill_API17(TheDictionary map, ScanResult value) throws Exception {
		map.put("timestamp", value.timestamp);
	}

	private void fill(TheDictionary map, ScanResult value) throws Exception {
		map.put("type", "w");
		map.put("bssid", unify(value.BSSID));
		map.put("ssid", unify(value.SSID));
		map.put("capabilities", unify(value.capabilities));
		map.put("frequency", value.frequency);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			fill_API17(map, value);
		}
		map.put("level", value.level);
	}
}
