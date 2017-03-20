package de.emdete.tabulae.fawlty;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import java.util.Iterator;
import static de.emdete.tabulae.fawlty.Constants.*;

public class Satellite implements Iterator<TheDictionary>, Iterable<TheDictionary> {
	private Location location;

	public Satellite(Location location) {
		this.location = location;
	}

	/////////////////////
	public static void fill(TheDictionary map, Location value) throws Exception {
		if (value != null) {
			map.put("type", "g");
			map.put("latitude", value.getLatitude());
			map.put("longitude", value.getLongitude());
			if (value.hasAccuracy()) {
				map.put("accuracy", (double) value.getAccuracy());
			}
			if (value.hasAltitude()) {
				map.put("altitude", value.getAltitude());
			}
			map.put("time", value.getTime());
			if (value.hasBearing()) {
				map.put("bearing", value.getBearing());
			}
			map.put("age_nanos", SystemClock.elapsedRealtimeNanos() - value.getElapsedRealtimeNanos());
			map.put("provider", value.getProvider());
			if (value.hasSpeed()) {
				map.put("speed", (double) value.getSpeed() * 3.6);
			}
			map.put("fromMockProvider", value.isFromMockProvider());
		}
	}

	///////////////////// test
	public static String test(Context context) {
		int a = 0;
		int b = 0;
		int c = 0;
		//if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		for (TheDictionary o : new Satellite(((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER))) {
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
		return location != null;
	}

	@Override
	public TheDictionary next() {
		TheDictionary map = new TheDictionary();
		try {
			fill(map, location);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		location = null;
		return map;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

