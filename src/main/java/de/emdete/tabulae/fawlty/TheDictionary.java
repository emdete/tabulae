package de.emdete.tabulae.fawlty;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import static de.emdete.tabulae.fawlty.Constants.*;

public class TheDictionary implements JSONStreamAware, JSONAware, Map<String, Object> {
	private Map<String, Object> map = new JSONObject();

	public TheDictionary() {
	}

	public TheDictionary(JSONObject map) {
		this.map = map;
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public boolean containsKey(Object o) {
		return this.map.containsKey(o);
	}

	@Override
	public boolean containsValue(Object o) {
		return this.map.containsValue(o);
	}

	@NonNull
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return this.map.entrySet();
	}

	public Object pop(String o) {
		Object r = this.map.get(o);
		this.map.remove(o);
		return r;
	}

	@Override
	public Object get(Object o) {
		return this.map.get(o);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@NonNull
	@Override
	public Set<String> keySet() {
		return this.map.keySet();
	}

	public Object put(String key, Object value) {
		return this.map.put(key, value);
	}

	@Override
	public void putAll(@NonNull Map<? extends String, ?> map) {
		if (map != null) {
			this.map.putAll(map);
		}
	}

	public void putAll(TheDictionary map) {
		if (map != null) {
			this.map.putAll(map.map);
		}
	}

	@Override
	public Object remove(Object o) {
		return this.map.remove(o);
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@NonNull
	public Collection<Object> values() {
		return this.map.values();
	}

	public boolean getBoolean(String key) {
		if (!this.map.containsKey(key))
			return false;
		return (Boolean) this.map.get(key);
	}

	public long getLong(String key) {
		if (!this.map.containsKey(key))
			return 0;
		return (Long) this.map.get(key);
	}

	public double getDouble(String key) {
		if (!this.map.containsKey(key))
			return Double.NaN;
		Object o = this.map.get(key);
		if (o instanceof Double) {
			return (Double) o;
		}
		return Double.NaN;
	}

	public String getString(String key) {
		if (!this.map.containsKey(key))
			return "";
		Object obj = this.map.get(key);
		return obj == null ? null : obj.toString();
	}

	public String getIdent() {
		String ident = null;
		if (containsKey("ident")) {
			ident = getString("ident");
		} else {
			char type = getString("type").charAt(0);
			switch (type) {
				case '\0':
					// dummy null value, no ident
					break;
				case '1':
				case '2':
					if (containsKey("mcc") && containsKey("mnc") && containsKey("lac") && containsKey("cid")) {
						ident = getString("type") + ":" + getString("mcc") + "." + getString("mnc") + "." + getString("lac") + "." + getString("cid");
					}
					break;
				case '3':
					if (containsKey("mcc") && containsKey("mnc") && containsKey("lac") && containsKey("rncid") && containsKey("cid")) {
						ident = getString("type") + ":" + getString("mcc") + "." + getString("mnc") + "." + getString("lac") + "." + getString("rncid") + "." + getString("cid");
					}
					break;
				case '4':
					if (containsKey("mcc") && containsKey("mnc") && containsKey("tac") && containsKey("cid")) {
						ident = getString("type") + ":" + getString("mcc") + "." + getString("mnc") + "." + getString("tac") + "." + getString("cid");
					}
					break;
				case 'w':
					if (containsKey("bssid")) {
						ident = getString("type") + ":" + getString("bssid");
					}
					break;
				case 'm':
					ident = getString("type") + ":";
					break;
				default:
					Log.e(TAG, "getIdent: type=" + type);
					break;
			}
			put("ident", ident);
		}
		return ident;
	}

	public int getStatus() { // TODO
		if (DEBUG) Log.d(TAG, "getStatus:");
		double distance = getDouble("distance");
		double radius = getDouble("radius");
		if (Double.isNaN(distance)) {
			this.map.put("status", "unknown");
			return -1;
		}
		double accuracy = getDouble("accuracy");
		if (distance < accuracy) {
			this.map.put("status", "excelent");
			return 2;
		}
		if (radius != Double.NaN && distance < radius) {
			this.map.put("status", "good");
			return 1;
		}
		this.map.put("status", "bad");
		return 0;
	}

	public String getDescription() {
		if (DEBUG) Log.d(TAG, "getDescription:");
		if (Double.isNaN(getDouble("radius"))) {
			return "No location for this id available.";
		}
		if (Double.isNaN(getDouble("accuracy"))) {
			return "No accurate location to compare with.";
		}
		if (Double.isNaN(getDouble("distance"))) {
			return "No data.";
		}
		return "The id-position is " +
				(int) getDouble("distance") + "m away from your position (which is accurate by " +
				(int) getDouble("accuracy") + "m) while the radius is " +
				(int) getDouble("radius") + "m.";
	}

	public String toJSONString() {
		return ((JSONObject) this.map).toJSONString();
	}

	public String toString() {
		return this.map == null ? null : this.map.toString();
	}

	public void writeJSONString(Writer out) throws IOException {
		((JSONObject) this.map).writeJSONString(out);
	}
}
