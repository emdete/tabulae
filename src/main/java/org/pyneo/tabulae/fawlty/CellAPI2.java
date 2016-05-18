package org.pyneo.tabulae.fawlty;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import static org.pyneo.tabulae.fawlty.Constants.*;

public class CellAPI2 {
	private static final String user = "pyneo";
	private static final String url = "https://cellsit.vfnet.de/cellapi/v2/_/" + user;
	private static final Random random = new Random();

	static public TheList retrieveLocation(TheDictionary meta, TheList list, String resolve) throws Exception {
		//if (DEBUG) Log.d(TAG, "retrieveLocation: retrieve list=" + list);
		TheList ret = null;
		Map<String, TheDictionary> map = new HashMap<>();
		String correlation_id = Long.toString(random.nextLong());
		meta.put("version", 2);
		meta.put("user", user);
		meta.put("method", resolve);
		meta.put("tower", 1);
		list.add(meta);
		if (DEBUG) Log.d(TAG, "retrieveLocation: request list=" + list);
		HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
		try {
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setReadTimeout(5000);
			connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			connection.setRequestProperty("User-Agent", "Tabulae " + org.pyneo.tabulae.BuildConfig.VERSION_NAME);
			connection.setRequestProperty("X-Correlation-Id", correlation_id);
			connection.setRequestProperty("Authorization", "Basic cHluZW86YU4zUGVpdjY=");
			connection.setRequestMethod("POST");
			try (java.io.Writer out = new java.io.OutputStreamWriter(connection.getOutputStream())) {
				list.writeJSONString(out);
				out.flush();
			}
			int httpResponseCode = connection.getResponseCode();
			//if (DEBUG) Log.d(TAG, "retrieveLocation: httpResponseCode=" + httpResponseCode);
			if (httpResponseCode != 200) {
				throw new Exception("httpResponseCode=" + httpResponseCode);
			}
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				Object obj = JSONValue.parseWithException(in);
				//if (DEBUG) Log.d(TAG, "retrieveLocation: response obj=" + obj);
				ret = new TheList((JSONArray) obj);
			}
			//if (DEBUG) Log.d(TAG, "retrieveLocation: ret=" + ret);
		}
		finally {
			try {
				connection.disconnect();
			}
			catch (Exception ignore) {
			}
		}
		if ("estimate".equals(resolve)) {
			list = ret;
		} else {
			for (TheDictionary entry : ret) {
				map.put(entry.getIdent(), entry);
			}
			for (TheDictionary entry : list) {
				TheDictionary r = map.get(entry.getIdent());
				entry.putAll(r);
			}
		}
		if (DEBUG) Log.d(TAG, "retrieveLocation: response list=" + list);
		return list;
	}
}
