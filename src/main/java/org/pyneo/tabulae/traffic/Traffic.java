package org.pyneo.tabulae.traffic;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

public class Traffic implements Constants {
	private static int TIMEOUT_CONNECT = 5000;
	private static int TIMEOUT_READ = 10000;
	private static int CACHE_TIME = 999999;
	private static String user_agent = "Tabulae/2 (Android)";
	private static String referer = "http://www.ndr.de/nachrichten/verkehr/";
	private static boolean follow_redirects = false;
	private static final String NDR_TRAFFIC = "http://www.ndr.de/nachrichten/verkehr/verkehrsdaten100-extapponly.json";
	private static final SimpleDateFormat HUMAN_READABLE_TIMESTAMP = new SimpleDateFormat("dd.MM.yyyy', 'HH:mm' Uhr'", Locale.US);

	/** request trafficreport, http level */
	static Map<String,Object> request(URL url, File target) throws Exception {
		URLConnection urlConnection = url.openConnection();
		urlConnection.setConnectTimeout(TIMEOUT_CONNECT);
		urlConnection.setReadTimeout(TIMEOUT_READ);
		if (user_agent != null) {
			urlConnection.setRequestProperty("User-Agent", user_agent);
		}
		if (referer != null) {
			urlConnection.setRequestProperty("Referer", referer);
		}
		if (urlConnection instanceof HttpURLConnection) {
			((HttpURLConnection)urlConnection).setInstanceFollowRedirects(follow_redirects);
		}
		if (urlConnection instanceof HttpURLConnection
		&& ((HttpURLConnection)urlConnection).getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("http response code=" + ((HttpURLConnection)urlConnection).getResponseCode());
		}
		InputStream inputStream = urlConnection.getInputStream();
		if ("gzip".equals(urlConnection.getContentEncoding())) {
			inputStream = new GZIPInputStream(inputStream);
		}
		Object obj = JSONValue.parseWithException(new BufferedReader(new InputStreamReader(inputStream)));
		if (!(obj instanceof JSONObject)) {
			throw new Exception("Unexpected type");
		}
		try (java.io.Writer out = new java.io.OutputStreamWriter(new FileOutputStream(target))) {
			JSONValue.writeJSONString(obj, out);
			out.flush();
		}
		Log.d(TAG, "request stored target=" + target);
		return (JSONObject)obj;
	}

	/** request trafficreport, cache level */
	static Map<String,Object> request(File cache_dir) throws Exception {
		File target = new java.io.File(cache_dir, "verkehrsdaten100-extapponly.json");
		if (target.exists()) {
			long filetime = target.lastModified();
			long currenttime = new Date().getTime();
			if (currenttime - filetime < CACHE_TIME) {
				Object obj = JSONValue.parseWithException(new BufferedReader(new InputStreamReader(new FileInputStream(target))));
				if (obj instanceof JSONObject) {
					Log.d(TAG, "request loaded target=" + target);
					return (JSONObject)obj;
				}
				target.delete();
			}
		}
		return request(new URL(NDR_TRAFFIC), target);
	}

	/** request trafficreport */
	public static void go(File cache_dir) throws Exception {
		Map<String,Object> base = request(cache_dir);
		Date human_readable_timestamp = HUMAN_READABLE_TIMESTAMP.parse((String)base.get("human_readable_timestamp"));
		String type = (String)base.get("type");
		if ("FeatureCollection".equals(type)) {
			for (Object o: (List)base.get("features")) {
				// a feature is a report of a single incident
				Map m = (Map)o;
				String type_f = (String)m.get("type");
				String id = (String)m.get("id");
				Map properties = (Map)m.get("properties");
				// properties contain the textual information
				if (properties != null) {
					String category = (String)properties.get("category ");
					String description = (String)properties.get("description");
					String color = (String)properties.get("color");
					Long debugUrgency = (Long)properties.get("debugUrgency");
					List states = (List)properties.get("states");
					String category_id = (String)properties.get("category_id");
					String street_type = (String)properties.get("street_type");
				}
				Map geometry = (Map)m.get("geometry");
				// geometry contain lat/lon of the position of the incident
				if (geometry != null) {
					String type_g = (String)geometry.get("type");
					if ("LineString".equals(type_g)) {
						List coordinates = (List)geometry.get("coordinates");
						for (Object p: coordinates) {
							List pair = (List)p;
							// lon,lat
						}
					}
					else if ("Point".equals(type_g)) {
						List pair = (List)geometry.get("coordinates");
						// lon,lat
					}
					else {
						throw new Exception("unknwon geometry type_g=" + type_g);
					}
				}
			}
		}
		else {
			throw new Exception("unknwon base type=" + type);
		}
	}
}
