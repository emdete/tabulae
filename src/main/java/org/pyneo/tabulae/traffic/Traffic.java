package org.pyneo.tabulae.traffic;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.track.TrackItem;
import org.pyneo.tabulae.track.TrackPointItem;
import static org.pyneo.tabulae.traffic.Constants.*;

public class Traffic extends Base {
	private static int TIMEOUT_CONNECT = 5000;
	private static int TIMEOUT_READ = 10000;
	private static int CACHE_TIME = 999999;
	private static String user_agent = "Tabulae/2 (Android)";
	private static String referer = "http://www.ndr.de/nachrichten/verkehr/";
	private static boolean follow_redirects = false;
	private static final String NDR_TRAFFIC = "http://www.ndr.de/nachrichten/verkehr/verkehrsdaten100-extapponly.json";
	private static final SimpleDateFormat HUMAN_READABLE_TIMESTAMP = new SimpleDateFormat("dd.MM.yyyy', 'HH:mm' Uhr'", Locale.US);
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(@NonNull Runnable r) {
			return new Thread(r, "inform");
		}
	});
	protected boolean enabled = false;

	/**
	 * request trafficreport, http level
	 */
	static Map<String, Object> request(URL url, File target) throws Exception {
		Log.d(TAG, "request url=" + url);
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
			((HttpURLConnection) urlConnection).setInstanceFollowRedirects(follow_redirects);
		}
		if (urlConnection instanceof HttpURLConnection
				&& ((HttpURLConnection) urlConnection).getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("http response code=" + ((HttpURLConnection) urlConnection).getResponseCode());
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
		return (JSONObject) obj;
	}

	/**
	 * request trafficreport, cache level
	 */
	static Map<String, Object> request(File cache_dir) throws Exception {
		File target = new java.io.File(cache_dir, "verkehrsdaten100-extapponly.json");
		if (target.exists()) {
			long filetime = target.lastModified();
			long currenttime = new Date().getTime();
			if (currenttime - filetime < CACHE_TIME) {
				Object obj = JSONValue.parseWithException(new BufferedReader(new InputStreamReader(new FileInputStream(target))));
				if (obj instanceof JSONObject) {
					Log.d(TAG, "request loaded target=" + target);
					return (JSONObject) obj;
				}
				target.delete();
			}
		}
		return request(new URL(NDR_TRAFFIC), target);
	}

	static class AppLoc extends Location {
		AppLoc(double lat, double lon) {
			super("");
			setLatitude(lat);
			setLongitude(lon);
		}
	}

	public static class Incidents extends ArrayList<Incident> {
		Incidents() {
		}
	}

	public static class Incident {
		String id;
		String category;
		String category_id;
		String description;
		String color;
		Long debugUrgency;
		List states;
		String street_type;
		String type_g;
		List<Location> position;

		Incident(
			String id,
			String category,
			String category_id,
			String description,
			String color,
			Long debugUrgency,
			List states,
			String street_type,
			String type_g,
			List<Location> position
		) {
			this.id = id;
			this.category = category;
			this.category_id = category_id;
			this.description = description.replaceAll("[\n\r\t]", " ");
			this.color = color;
			this.debugUrgency = debugUrgency;
			this.states = states;
			this.street_type = street_type;
			this.type_g = type_g;
			this.position = position;
		}

		public String getName() {
			return id;
		}

		public String getCategory() {
			return category;
		}

		public String getCategoryId() {
			return category_id;
		}

		public String getDescription() {
			return description;
		}

		public String getColor() {
			return color;
		}

		public Long getDebugUrgency() {
			return debugUrgency;
		}

		public List getStates() {
			return states;
		}

		public String getStreetType() {
			return street_type;
		}

		public String getGeoType() {
			return type_g;
		}

		public List<Location> getPosition() {
			return position;
		}

		public String toString() {
			return "Incident"
				+ ", id=" + id
				+ ", category=" + category
				+ ", category_id=" + category_id
				+ ", description=" + description
				+ ", color=" + color
				+ ", debugUrgency=" + debugUrgency
				+ ", states=" + states
				+ ", street_type=" + street_type
				+ ", type_g=" + type_g
				+ ", position=" + (position==null?null:position.get(0))
				;
		}
	}

	/** request trafficreport */
	public static Incidents go(File cache_dir, Incidents incidents) throws Exception {
		if (incidents == null) {
			incidents = new Incidents();
		}
		else {
			incidents.clear();
		}
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
				// properties contain the textual informations
				if (properties == null) {
					continue;
				}
				String category = (String)properties.get("category");
				String category_id = (String)properties.get("category_id"); // construction_area, ferry, missing_person_report, traffic_jam, transport
				String description = (String)properties.get("description");
				String color = (String)properties.get("color");
				Long debugUrgency = (Long)properties.get("debugUrgency");
				List states = (List)properties.get("states");
				String street_type = (String)properties.get("street_type"); // town, street, aroad, m1road, ferry, NULL
				List<Location> position = new ArrayList<>();
				Map geometry = (Map)m.get("geometry");
				// geometry contain lat/lon of the position of the incident
				String type_g = null;
				if (geometry != null) {
					type_g = (String)geometry.get("type");
					if ("LineString".equals(type_g)) {
						List coordinates = (List)geometry.get("coordinates");
						for (Object p: coordinates) {
							List pair = (List)p;
							position.add(new AppLoc((Double)pair.get(1), (Double)pair.get(0)));
						}
					}
					else if ("Point".equals(type_g)) {
						List pair = (List)geometry.get("coordinates");
						position.add(new AppLoc((Double)pair.get(1), (Double)pair.get(0)));
					}
					else {
						throw new Exception("unknwon geometry type_g=" + type_g);
					}
				}
				if ("traffic_jam".equals(category_id) && ("aroad".equals(street_type) || "m1road".equals(street_type))) {
					incidents.add(new Incident(
						id, category, category_id, description, color,
						debugUrgency, states, street_type, type_g, position));
				}
			}
		}
		else {
			throw new Exception("unknwon base type=" + type);
		}
		return incidents;
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_request_traffic: {
				Bundle b = new Bundle();
				b.putBoolean("enabled", enabled);
				((Tabulae)getActivity()).inform(R.id.event_notify_traffic, b);
			}
			break;
			case R.id.event_do_traffic: {
				try {
					final File cache_dir = new File(((Tabulae) getActivity()).getBaseDir(), "cache");
					//noinspection ResultOfMethodCallIgnored
					cache_dir.mkdirs();
					mThreadPool.execute(new Runnable() {
						public void run() {
							try {
								Traffic.Incidents incidents = Traffic.go(cache_dir, null);
								for (Traffic.Incident incident: incidents) {
									TrackItem trackItem = new TrackItem(incident.getName(), incident.getDescription());
									Log.d(TAG, "incident=" + incident);
									for (Location position: incident.getPosition()) {
										trackItem.add(null, new TrackPointItem(position.getLatitude(), position.getLongitude()));
									}
									trackItem.insert(null);
								}
								enabled = true;
								Bundle b = new Bundle();
								b.putBoolean("enabled", enabled);
								((Tabulae)getActivity()).inform(R.id.event_notify_traffic, b);
							}
							catch (Exception e) {
								Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
								Log.d(TAG, "traffic load e=" + e, e);
							}
						}
					});
				}
				catch (Exception e) {
					Log.d(TAG, "traffic load e=" + e);
				}
			}
			break;
		}
	}
}
