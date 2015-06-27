package org.pyneo.tabulae;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.views.MapView;
import org.pyneo.tabulae.geolocation.Locus;
import org.pyneo.tabulae.gui.Base;
import org.pyneo.tabulae.gui.Controller;
import org.pyneo.tabulae.gui.Dashboard;
import org.pyneo.tabulae.poi.Poi;
import org.pyneo.tabulae.track.Track;
import org.pyneo.tabulae.map.Map;

public class Tabulae extends Activity implements Constants {
	private Base[] fragments;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "Tabulae.onCreate");
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		setContentView(R.layout.base);
		fragments = new Base[]{new Map(), new Locus(), new Track(), new Controller(), new Poi(), new Dashboard(),};
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "Tabulae.onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (DEBUG) Log.d(TAG, "Tabulae.onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Tabulae.onResume");
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction tx = fragmentManager.beginTransaction();
		for (Base b : fragments) {
			tx.add(R.id.base, b, b.getClass().getSimpleName());
		}
		tx.commit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Tabulae.onPause");
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction tx = fragmentManager.beginTransaction();
		for (Base b : fragments) {
			tx.remove(b);
		}
		tx.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "Tabulae.onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "Tabulae.onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main_option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		inform(item.getItemId(), null);
		return true;
	}

	public void inform(int event, Bundle extra) {
		for (Base b : fragments) {
			b.inform(event, extra);
		}
	}

	public MapView getMapView() {
		return ((Map)fragments[0]).getMapView();
	}
}
