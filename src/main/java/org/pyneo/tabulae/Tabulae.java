package org.pyneo.tabulae;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.geolocation.Locus;
import org.pyneo.tabulae.gui.Controller;
import org.pyneo.tabulae.gui.Dashboard;
import org.pyneo.tabulae.poi.Poi;
import org.pyneo.tabulae.track.Track;
import org.pyneo.tabulae.map.Map;

public class Tabulae extends Activity implements Constants {
	protected Base[] fragments;
	protected File baseStorageFile = null;

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
		fragments = new Base[]{
			new Map(),
			new Track(),
			new Poi(),
			new Locus(),
			new Controller(),
			new Dashboard(),
			};
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		String baseStorage = preferences.getString("baseStorage", null);
		if (savedInstanceState != null) {
			baseStorage = savedInstanceState.getString("baseStorage", baseStorage);
		}
		if (baseStorage != null) {
			baseStorageFile = new File(baseStorage);
			if (!Environment.getExternalStorageState(baseStorageFile).equals(Environment.MEDIA_MOUNTED)) {
				baseStorage = null; // not accessable anymore
			}
		}
		if (baseStorageFile == null) {
			// look for the largest storage to begin with
			long baseStorageSpace = 0;
			for (File dir: getApplicationContext().getExternalFilesDirs(null)) {
				if (Environment.getExternalStorageState(dir).equals(Environment.MEDIA_MOUNTED)) {
					long dirSpace = new StatFs(dir.getPath()).getAvailableBytes();
					if (dirSpace > baseStorageSpace) {
						baseStorageFile = dir;
						baseStorageSpace = dirSpace;
					}
				}
			}
			Log.d(TAG, "found baseStorageFile=" + baseStorageFile + ", baseStorageSpace=" + baseStorageSpace);
		}
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
		AndroidGraphicFactory.clearResourceMemoryCache();
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

	@Override protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		Log.d(TAG, "onSaveInstanceState bundle=" + bundle);
		bundle.putString("baseStorage", baseStorageFile.getPath());
	}

	public void inform(int event, Bundle extra) {
		for (Base b : fragments) {
			b.inform(event, extra);
		}
	}

	public File getThemesDir() {
		return new File(baseStorageFile, "themes");
	}

	public File getTilesDir() {
		return new File(baseStorageFile, "tiles");
	}

	public File getMapsDir() {
		return new File(baseStorageFile, "maps");
	}

	public MapView getMapView() {
		return ((Map)fragments[0]).getMapView();
	}
}
