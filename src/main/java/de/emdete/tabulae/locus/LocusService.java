package de.emdete.tabulae.locus;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.location.GpsSatellite;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.emdete.tabulae.Constants;
import java.util.ArrayList;
import de.emdete.tabulae.R;
import de.emdete.tabulae.Tabulae;

public class LocusService extends Service implements LocationListener {
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	NotificationManager mNotificationManager;
	ArrayList<Messenger> mClients = new ArrayList<>();
	LocationManager locationManager;
	boolean myLocationEnabled;
	Context context;
	Location lastLocation;
	float minDistance = 0.0f;
	long minTime = 0;

	@Override public void onCreate() {
		Log.d(de.emdete.tabulae.Constants.TAG, "LocusService.onCreate");
		context = getBaseContext();
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		CharSequence text = getText(R.string.remote_service_started);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Tabulae.class), 0);
		Notification notification = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_service)
				.setLargeIcon(Icon.createWithResource(this, R.drawable.ic_service))
				.setTicker(text)
				.setColor(getResources().getColor(R.color.primary))
				.setWhen(System.currentTimeMillis())
				.setContentTitle(getText(R.string.app_label))
				.setContentText(text)
				.setContentIntent(contentIntent)
				.setDeleteIntent(contentIntent)
				.setDefaults(0)
				.setLights(getResources().getColor(R.color.primary, null), 600, 400)
				.setOngoing(true)
				.setAutoCancel(false)
			.build();
		mNotificationManager.notify(R.id.service_notification_id_location, notification);
		myLocationEnabled = enable();
	}

	@Override public void onDestroy() {
		Log.d(Constants.TAG, "LocusService.onDestroy");
		mNotificationManager.cancel(R.id.service_notification_id_location);
		disable();
	}

	@Override public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public synchronized void disable() {
		if (myLocationEnabled) {
			myLocationEnabled = false;
			locationManager.removeUpdates(this);
		}
	}

	protected synchronized boolean enable() {
		boolean result = false;
		disable();
		for (String provider : locationManager.getProviders(true)) {
			Location location = locationManager.getLastKnownLocation(provider);
			lastLocation = betterLocation(lastLocation, location);
		}
		for (String provider : locationManager.getProviders(true)) {
			if (LocationManager.GPS_PROVIDER.equals(provider) || LocationManager.NETWORK_PROVIDER.equals(provider)) {
				locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
				result = true;
			}
		}
		if (lastLocation != null) {
			onLocationChanged(lastLocation);
		}
		if (!locationManager.registerGnssStatusCallback(new GnssStatus.Callback(){
			@Override public void onFirstFix(int ttffMillis) {
			}
			@Override public void onSatelliteStatusChanged(GnssStatus status) {
				Bundle b = toBundle(null, status);
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						mClients.get(i).send(Message.obtain(null, R.id.message_locus_set_value, R.id.event_notify_location, 0, b));
					}
					catch (RemoteException e) {
						mClients.remove(i);
					}
				}
			}
			@Override public void onStarted() {
			}
			@Override public void onStopped() {
			}
		})) {
			Log.d(Constants.TAG, "LocusService.enable registerGnssStatusCallback=" + false);
		}
		return result;
	}

	// LocationListener:
	@Override public void onLocationChanged(Location location) {
		//Log.d(Constants.TAG, "LocusService.onLocationChanged location=" + location);
		if (location != null) {
			Bundle b = toBundle(null, location);
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					mClients.get(i).send(Message.obtain(null, R.id.message_locus_set_value, R.id.event_notify_location, 0, b));
				}
				catch (RemoteException e) {
					mClients.remove(i);
				}
			}
		}
	}

	//
	@Override public void onProviderDisabled(String provider) {
		myLocationEnabled = enable();
	}

	@Override public void onProviderEnabled(String provider) {
		myLocationEnabled = enable();
	}

	@Override public void onStatusChanged(String provider, int status, Bundle extras) {
		// do nothing
	}

	class IncomingHandler extends Handler {
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
				case R.id.message_locus_register_client:
					mClients.add(msg.replyTo);
					onLocationChanged(lastLocation);
					break;
				case R.id.message_locus_unregister_client:
					mClients.remove(msg.replyTo);
					break;
				case R.id.message_locus_set_value:
					break;
				default:
					super.handleMessage(msg);
					return;
			}
			if (mClients.size() == 0) {
				stopSelf();
			}
		}
	}

	// helpers
	public static Location betterLocation(Location l1, Location l2) {
		if (l1 == null) return l2;
		if (l2 == null) return null;
		if (l2.isFromMockProvider())
			if (l1.isFromMockProvider())
				return null;
			else
				return l1;
		if (Math.abs(l1.getElapsedRealtimeNanos() - l2.getElapsedRealtimeNanos()) < 3E9 && l1.hasAccuracy() && l2.hasAccuracy()) {
			return l1.getAccuracy() < l2.getAccuracy() ? l1 : l2;
		}
		if (l1.getElapsedRealtimeNanos() < l2.getElapsedRealtimeNanos()) return l2;
		return l1;
	}

	public static Bundle toBundle(Bundle ret, Location location) {
		if (location != null) {
			if (ret == null) {
				ret = new Bundle(location.getExtras());
			}
			ret.putString("provider", location.getProvider());
			ret.putLong("elapsed", location.getElapsedRealtimeNanos());
			ret.putDouble("latitude", location.getLatitude());
			ret.putDouble("longitude", location.getLongitude());
			ret.putLong("time", location.getTime());
			if (location.hasSpeed()) {
				double speed = location.getSpeed() * 3.6;
				ret.putDouble("speed", speed);
				if (speed != 0) {
					ret.putDouble("pace", 60 / speed);
				}
				else {
					ret.putDouble("pace", Double.POSITIVE_INFINITY);
				}
			}
			if (location.hasAccuracy() && location.getAccuracy() != 0) {
				ret.putDouble("accuracy", location.getAccuracy());
			}
			if (location.hasAltitude()) {
				ret.putDouble("altitude", location.getAltitude());
			}
			if (location.hasBearing()) {
				ret.putDouble("bearing", location.getBearing());
			}
			Bundle extras = location.getExtras();
			if (extras != null) {
				ret.putInt("satellites", extras.getInt("satellites", 0));
			}
		}
		return ret;
	}

	private Bundle toBundle(Bundle ret, GnssStatus status) {
		if (ret == null) {
			ret = new Bundle();
		}
		int count_seen = status.getSatelliteCount();
		int count_fix = 0;
		for (int i=0;i<count_seen;i++) {
			if (status.usedInFix(i)) {
				count_fix++;
			}
		}
		ret.putInt("satellites", count_fix);
		ret.putInt("satellites_seen", count_seen);
		return ret;
	}
}
