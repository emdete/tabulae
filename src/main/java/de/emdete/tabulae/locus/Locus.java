package de.emdete.tabulae.locus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.emdete.tabulae.Base;
import org.mapsforge.map.android.view.MapView;
import de.emdete.tabulae.R;
import de.emdete.tabulae.Tabulae;
import static de.emdete.tabulae.locus.Constants.*;

public class Locus extends Base {
	Messenger mService = null;
	boolean mIsBound;
	ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, R.id.message_locus_register_client);
				msg.replyTo = mMessenger;
				mService.send(msg);
				msg = Message.obtain(null, R.id.message_locus_set_value, this.hashCode(), 0, new Bundle());
				mService.send(msg);
			}
			catch (RemoteException ignore) {
			}
			Log.d(TAG, "Sample.ServiceConnection.onServiceConnected: remote service connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			Log.d(TAG, "Sample.ServiceConnection.onServiceDisconnected: remote service disconnected");
		}
	};
	private ThreeStateLocationOverlay myLocationOverlay;
	final Messenger mMessenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case R.id.message_locus_set_value:
					//if (DEBUG) Log.d(TAG, "Locus.handleMessage event=" + msg.arg1 + ", extra=" + msg.obj);
					((Tabulae) getActivity()).inform(msg.arg1, (Bundle) msg.obj);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	});

	public static Location toLocation(Bundle location) {
		Location ret = null;
		if (location != null) {
			ret = new Location(location.getString("provider"));
			ret.setElapsedRealtimeNanos(location.getLong("elapsed"));
			ret.setLatitude(location.getDouble("latitude"));
			ret.setLongitude(location.getDouble("longitude"));
			ret.setTime(location.getLong("time"));
			if (location.containsKey("speed")) {
				ret.setSpeed((float) (location.getDouble("speed") / 3.6));
			}
			if (location.containsKey("accuracy")) {
				ret.setAccuracy((float) location.getDouble("accuracy"));
			}
			if (location.containsKey("altitude")) {
				ret.setAltitude(location.getDouble("altitude"));
			}
			if (location.containsKey("bearing")) {
				ret.setBearing((float) location.getDouble("bearing"));
			}
		}
		return ret;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (DEBUG) Log.d(TAG, "Locus.onCreate");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		myLocationOverlay = new ThreeStateLocationOverlay(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Locus.onResume");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		mapView.getLayerManager().getLayers().add(myLocationOverlay);
		myLocationOverlay.enable();
		doBindService();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Locus.onPause");
		MapView mapView = ((Tabulae) getActivity()).getMapView();
		mapView.getLayerManager().getLayers().remove(myLocationOverlay);
		doUnbindService();
	}

	void doBindService() {
		getActivity().bindService(new Intent(getActivity(), LocusService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			mIsBound = false;
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, R.id.message_locus_unregister_client);
					msg.replyTo = mMessenger;
					mService.send(msg);
				}
				catch (RemoteException ignore) {
				}
			}
			getActivity().unbindService(mConnection);
		}
	}

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Locus.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_notify_location: {
				//if (DEBUG) Log.d(TAG, "Locus.inform event=" + event + ", extra=" + extra);
				if (myLocationOverlay != null) {
					myLocationOverlay.onLocationChanged(toLocation(extra));
				}
			}
			break;
		}
	}
}
