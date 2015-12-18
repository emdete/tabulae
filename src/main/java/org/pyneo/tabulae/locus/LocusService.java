package org.pyneo.tabulae.locus;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
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
import java.util.ArrayList;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;

public class LocusService extends Service implements LocationListener, Constants {
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	NotificationManager mNotificationManager;
	ArrayList<Messenger> mClients = new ArrayList<>();
	LocationManager locationManager;
	boolean myLocationEnabled;
	Context context;
	Location lastLocation;
	float minDistance = 0.0f;
	long minTime = 0;

	@Override
	public void onCreate() {
		Log.d(TAG, "LocusService.onCreate");
		context = getBaseContext();
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		CharSequence text = getText(R.string.remote_service_started);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Tabulae.class), 0);
		Notification notification = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_service)
				// .setLargeIcon(Icon.createWithResource(this, R.drawable.ic_service))
				.setTicker(text)
				.setColor(getResources().getColor(R.color.primary))
				.setWhen(System.currentTimeMillis())
				.setContentTitle(getText(R.string.app_label))
				.setContentText(text)
				.setContentIntent(contentIntent)
				.setDeleteIntent(contentIntent)
				.setDefaults(0)
				.setLights(getResources().getColor(R.color.primary), 600, 400)
				.setOngoing(true)
				.setAutoCancel(false)
			.build();
		mNotificationManager.notify(R.id.notification_id, notification);
		myLocationEnabled = enable();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "LocusService.onDestroy");
		mNotificationManager.cancel(R.id.notification_id);
		disable();
	}

	@Override
	public IBinder onBind(Intent intent) {
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
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
				|| context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
		}
		return result;
	}

	// LocationListener:
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "LocusService.onLocationChanged location=" + location);
		if (location != null) {
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					mClients.get(i).send(Message.obtain(null, R.id.msg_set_value, R.id.location, 0, toBundle(location)));
				}
				catch (RemoteException e) {
					mClients.remove(i);
				}
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		myLocationEnabled = enable();
	}

	@Override
	public void onProviderEnabled(String provider) {
		myLocationEnabled = enable();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// do nothing
	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case R.id.msg_register_client:
					mClients.add(msg.replyTo);
					onLocationChanged(lastLocation);
					break;
				case R.id.msg_unregister_client:
					mClients.remove(msg.replyTo);
					break;
				case R.id.msg_set_value:
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

	public static Bundle toBundle(Location location) {
		Bundle ret = null;
		if (location != null) {
			ret = new Bundle(location.getExtras());
			ret.putString("provider", location.getProvider());
			ret.putLong("elapsed", location.getElapsedRealtimeNanos());
			ret.putDouble("latitude", location.getLatitude());
			ret.putDouble("longitude", location.getLongitude());
			ret.putLong("time", location.getTime());
			if (location.hasSpeed()) {
				ret.putDouble("speed", location.getSpeed() * 3600);
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
		}
		return ret;
	}
}
