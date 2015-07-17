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

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "Tabulae.onCreate");
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		setContentView(R.layout.tabulae);
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
				if (dir != null && Environment.getExternalStorageState(dir).equals(Environment.MEDIA_MOUNTED)) {
					long dirSpace = new StatFs(dir.getPath()).getAvailableBytes();
					if (dirSpace > baseStorageSpace) {
						baseStorageFile = dir;
						baseStorageSpace = dirSpace;
					}
				}
			}
			Log.d(TAG, "using baseStorageFile=" + baseStorageFile + ", baseStorageSpace=" + deKay(baseStorageSpace));
		}
	}

	@Override protected void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "Tabulae.onStart");
	}

	@Override protected void onRestart() {
		super.onRestart();
		if (DEBUG) Log.d(TAG, "Tabulae.onRestart");
	}

	@Override protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "Tabulae.onResume");
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction tx = fragmentManager.beginTransaction();
		for (Base b : fragments) {
			tx.add(R.id.tabulae, b, b.getClass().getSimpleName());
		}
		tx.commit();
	}

	@Override protected void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "Tabulae.onPause");
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction tx = fragmentManager.beginTransaction();
		for (Base b : fragments) {
			tx.remove(b);
		}
		tx.commit();
	}

	@Override protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "Tabulae.onStop");
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "Tabulae.onDestroy");
		AndroidGraphicFactory.clearResourceMemoryCache();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main_option_menu, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		inform(item.getItemId(), null);
		return true;
	}

	@Override protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		Log.d(TAG, "onSaveInstanceState bundle=" + bundle);
		bundle.putString("baseStorage", baseStorageFile.getPath());
	}

	/**
	* directory for gpx exchange
	*/
	public File getGpxDir() {
		File ret = new File(baseStorageFile, "gpx");
		ret.mkdirs();
		return ret;
	}

	/**
	* directory for the tiles(cache)
	*/
	public File getTilesDir() {
		File ret = new File(baseStorageFile, "tiles");
		ret.mkdirs();
		return ret;
	}

	/**
	* directory for the mapsforge map files
	*/
	public File getMapsDir() {
		File ret = new File(baseStorageFile, "maps/mapsforge");
		ret.mkdirs();
		return ret;
	}

	/**
	* directory for the themes
	*/
	public File getThemesDir() {
		File ret = new File(baseStorageFile, "maps/mapsforge/themes");
		ret.mkdirs();
		return ret;
	}

	public MapView getMapView() {
		return ((Map)fragments[0]).getMapView();
	}

	static private final String deKay(long l) {
		double d = l;
		String[] fs = new String[]{"%.2fB", "%.2fKB", "%.2fMB", "%.2fGB", };
		for (String f: fs) {
			if (d < 1024.0) {
				return String.format(f, d);
			}
			d /= 1024.0;
		}
		return String.format("%.2fTB", d);
	}

	public void inform(int event, Bundle extra) {
		for (Base b : fragments) {
			b.inform(event, extra);
		}
	}
}
