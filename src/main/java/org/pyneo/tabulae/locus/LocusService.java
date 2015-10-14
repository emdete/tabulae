package org.pyneo.tabulae.locus;

import android.app.Service;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.CellInfo;
import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import java.util.List;

public class LocusService extends Service implements Constants {
	public class BackgroundBinder extends Binder {
		public LocusService getService() {
			return LocusService.this;
		}
	}

	Binder binder = new BackgroundBinder();

	@Override public IBinder onBind(Intent intent) {
		Log.d(TAG, "LocusService.onBind: intent=" + intent);
		return binder;
	}
}
