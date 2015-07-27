package org.pyneo.tabulae.fawlty;

import android.util.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class CellAPI2 implements Constants {
	private static final String rid = "pyneo";
	private static final String url = "https://x.vfnet.de/cellapi/v2/_/" + rid;
	private static final String secret = "aN3Peiv6";
	private static final Random random = new Random();

	static public TheList retrieveLocation(TheDictionary meta, TheList list, String resolve) throws Exception {
		//if (DEBUG) Log.d(TAG, "retrieveLocation: retrieve list=" + list);
		TheList ret = null;
		Map<String,TheDictionary> map = new HashMap<String,TheDictionary>();
		String correlation_id = Long.toString(random.nextLong());
		meta.put("version", 2);
		meta.put("user", rid);
		meta.put("resolve", resolve);
		list.add(meta);
		if (DEBUG) Log.d(TAG, "retrieveLocation: request list=" + list);
		HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
		try {
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setReadTimeout(5000);
			connection.setRequestProperty("Content-Type", "text/json");
			connection.setRequestProperty("User-Agent", "proprorep");
			connection.setRequestProperty("X-Correlation-Id", correlation_id);
			connection.setRequestMethod("POST");
			java.io.Writer out = new java.io.OutputStreamWriter(connection.getOutputStream());
			try {
				list.writeJSONString(out);
			}
			finally {
				out.flush();
				out.close();
			}
			int httpResponseCode = connection.getResponseCode();
			//if (DEBUG) Log.d(TAG, "retrieveLocation: httpResponseCode=" + httpResponseCode);
			if (httpResponseCode != 200) {
				throw new Exception("httpResponseCode=" + httpResponseCode);
			}
			java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
			try {
				Object obj = JSONValue.parseWithException(in);
				//if (DEBUG) Log.d(TAG, "retrieveLocation: response obj=" + obj);
				ret = new TheList((JSONArray)obj);
			}
			finally {
				in.close();
			}
			//if (DEBUG) Log.d(TAG, "retrieveLocation: ret=" + ret);
		}
		finally {
			try { connection.disconnect(); } catch (Exception ignore) { }
		}
		if ("single".equals(resolve)) {
			list = ret;
		}
		else {
			for (TheDictionary entry: ret) {
				map.put(entry.getIdent(), entry);
			}
			for (TheDictionary entry: list) {
				TheDictionary r = map.get(entry.getIdent());
				entry.putAll(r);
			}
		}
		if (DEBUG) Log.d(TAG, "retrieveLocation: response list=" + list);
		return list;
	}
}
