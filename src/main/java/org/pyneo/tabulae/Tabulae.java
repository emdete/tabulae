package org.pyneo.tabulae;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.fawlty.Fawlty;
import org.pyneo.tabulae.gui.Controller;
import org.pyneo.tabulae.gui.Dashboard;
import org.pyneo.tabulae.gui.DocumentAvtivity;
import org.pyneo.tabulae.locus.Locus;
import org.pyneo.tabulae.map.Map;
import org.pyneo.tabulae.poi.Poi;
import org.pyneo.tabulae.screencapture.ScreenCaptureFragment;
import org.pyneo.tabulae.track.Track;

public class Tabulae extends Activity implements Constants {
	protected Base[] fragments;
	protected File baseStorageFile = null;
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(@NonNull Runnable r) {
			return new Thread(r, "inform");
		}
	});

	static private String deKay(long l) {
		double d = l;
		final String[] fs = new String[]{"%.2fB", "%.2fKB", "%.2fMB", "%.2fGB",};
		for (String f : fs) {
			if (d < 1024.0) {
				return String.format(f, d);
			}
			d /= 1024.0;
		}
		return String.format("%.2fTB", d);
	}

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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.tabulae);
		fragments = new Base[]{
				new Map(),
				new Track(),
				new Poi(),
				new Fawlty(),
				new Locus(),
				new Controller(),
				new Dashboard(),
				new ScreenCaptureFragment(),
		};
		//noinspection StatementWithEmptyBody
		if (savedInstanceState != null) {
			// .. = savedInstanceState.getString("..", null);
		}
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		String baseStorage = preferences.getString("baseStorage", null);
		Log.d(TAG, "Tabulae.onCreate preferences baseStorage=" + baseStorage);
		if (baseStorage != null) {
			baseStorageFile = new File(baseStorage);
			if (!Environment.getExternalStorageState(baseStorageFile).equals(Environment.MEDIA_MOUNTED)) {
				Log.e(TAG, "Tabulae.onCreate not mounted: baseStorage=" + baseStorage);
				Toast.makeText(this, "Storage gone! Please reinsert SD.", Toast.LENGTH_LONG).show();
				// TODO: ask for different storage
				finish();
			}
		}
		long baseStorageSpace = 0;
		if (baseStorageFile == null) {
			// look for the largest storage to begin with
			for (File dir : getApplicationContext().getExternalFilesDirs(null)) {
				Log.d(TAG, "Tabulae.onCreate getExternalFilesDirs baseStorage=" + baseStorage);
				if (dir != null && Environment.getExternalStorageState(dir).equals(Environment.MEDIA_MOUNTED)) {
					long dirSpace = new StatFs(dir.getPath()).getAvailableBytes();
					if (dirSpace > baseStorageSpace) {
						baseStorageFile = dir;
						baseStorageSpace = dirSpace;
					}
				}
			}
			Editor editor = preferences.edit();
			editor.putString("baseStorage", baseStorage);
			editor.commit();
		} else {
			baseStorageSpace = new StatFs(baseStorageFile.getPath()).getAvailableBytes();
		}
		Log.d(TAG, "Tabulae.onCreate using baseStorageFile=" + baseStorageFile + ", baseStorageSpace=" + deKay(baseStorageSpace));
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (DEBUG)
			Log.d(TAG, "Tabulae.onCreate process intent=" + queryIntent + ", action=" + queryAction);
		//noinspection StatementWithEmptyBody
		if (Intent.ACTION_MAIN.equals(queryAction)) {
			// nothing more to do
		} else if (ACTION_CONVERSATIONS_REQUEST.equals(queryAction)) {
			String package_ = getCallingPackage();
			String activity = getCallingActivity().flattenToString();
			if (DEBUG)
				Log.d(TAG, "Tabulae.onCreate package_=" + package_ + ", activity=" + activity);
			//Bundle extra = queryIntent.getExtras();
			MapView mapView = getMapView();
			if (mapView == null) {
				setResult(Activity.RESULT_CANCELED, null);
			} else {
				LatLong location = mapView.getModel().mapViewPosition.getCenter(); // TODO defere location determination?
				Intent result = new Intent();
				result.putExtra(LATITUDE, location.latitude);
				result.putExtra(LONGITUDE, location.longitude);
				//result.putExtra(ALTITUDE, .getAltitude());
				//result.putExtra(ACCURACY, .getAccuracy());
				setResult(Activity.RESULT_OK, result);
			}
			finish();
		} else if (ACTION_CONVERSATIONS_SHOW.equals(queryAction)) {
			Bundle extra = queryIntent.getExtras();
			if (extra.containsKey(LONGITUDE) && extra.containsKey(LATITUDE)) {
				String jid = extra.getString(JID);
				String name = extra.getString(NAME);
				if (name == null || name.length() == 0) {
					if (jid != null && jid.length() > 0) {
						name = jid.split("@")[0]; // TODO avoid regex
					} else {
						name = "Jabber";
						jid = "@xmpp";
					}
				}
				double latitude = extra.getDouble(LATITUDE, 0);
				double longitude = extra.getDouble(LONGITUDE, 0);
				String id = Poi.storePointPosition(this, jid, name + ", on " + new Date(), latitude, longitude, true);
				Log.w(TAG, "onCreate.ACTION_CONVERSATIONS_SHOW id=" + id);
			} else
				Log.w(TAG, "onCreate conversations intent recceived with no latitude/longitude");
		} else if (Intent.ACTION_VIEW.equalsIgnoreCase(queryAction)) {
			try {
				//List<ActivityManager.RecentTaskInfo> recentTasks = ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getRecentTasks(99, ActivityManager.RECENT_WITH_EXCLUDED);
				Uri uri = queryIntent.getData();
				if (uri.getScheme().equalsIgnoreCase(GEO)) {
					final String part = uri.getEncodedSchemeSpecificPart().split("\\?")[0];
					final String[] latlon = part.split(","); // TODO avoid regex
					Bundle extra = new Bundle();
					extra.putDouble(LATITUDE, Double.parseDouble(latlon[0]));
					extra.putDouble(LONGITUDE, Double.parseDouble(latlon[1]));
					extra.putString(NAME, "poi");
					extra.putString(DESCRIPTION, "shared poi");
					if (DEBUG) Log.d(TAG, "Tabulae.onCreate new poi extra=" + extra);
					inform(R.id.event_poi_new, extra);
				}
			}
			catch (Exception e) {
				Log.e(TAG, "Tabulae.onCreate", e);
				// Toast?
			}
		} else
			Log.e(TAG, "Tabulae.onCreate no fit action=" + queryAction);
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
		for (Base b : fragments) {
			FragmentTransaction tx = fragmentManager.beginTransaction();
			//if (DEBUG) Log.d(TAG, "Tabulae.onResume b=" + b.getClass().getSimpleName());
			tx.add(R.id.tabulae, b, b.getClass().getSimpleName());
			tx.commit();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		if (DEBUG)
			Log.d(TAG, "Tabulae.onActivityResult resultCode=" + resultCode + ", requestCode=" + requestCode);
		for (Base b : fragments) {
			b.onActivityResult(requestCode, resultCode, resultData);
		}
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
		if (DEBUG) {
			menu.findItem(R.id.event_screencapture).setVisible(true);
			menu.findItem(R.id.event_fawlty).setVisible(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		inform(item.getItemId(), null);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		if (DEBUG) Log.d(TAG, "Tabulae.onSaveInstanceState bundle=" + bundle);
		// bundle.putString("..", ..);
	}

	/**
	 * directory for storage
	 */
	public File getBaseDir() {
		return baseStorageFile;
	}

	/**
	 * directory for gpx exchange
	 */
	public File getGpxDir() {
		File ret = new File(baseStorageFile, "gpx");
		//noinspection ResultOfMethodCallIgnored
		ret.mkdirs();
		return ret;
	}

	/**
	 * directory for the tiles(cache)
	 */
	public File getTilesDir() {
		File ret = new File(baseStorageFile, "tiles");
		//noinspection ResultOfMethodCallIgnored
		ret.mkdirs();
		return ret;
	}

	/**
	 * directory for the mapsforge map files
	 */
	public File getMapsDir() {
		File ret = new File(baseStorageFile, "maps");
		//noinspection ResultOfMethodCallIgnored
		ret.mkdirs();
		return ret;
	}

	/**
	 * directory for the screen movies
	 */
	public File getMoviesDir() {
		File ret = new File(baseStorageFile, "movies");
		//noinspection ResultOfMethodCallIgnored
		ret.mkdirs();
		return ret;
	}

	public MapView getMapView() {
		return ((Map) fragments[0]).getMapView();
	}

	public void inform(final int event, final Bundle extra) {
		switch (event) {
		case R.id.event_help:
			Intent intent = new Intent(this, DocumentAvtivity.class);
			String lang = getResources().getConfiguration().locale.getLanguage();
			Bundle extras = new Bundle();
			// TODO check if exists and fallback
			extras.putString("url", "file:///android_asset/documents-" + lang + "/index.html");
			intent.putExtras(extras);
			startActivity(intent);
		break;
		default:
			for (Base b : fragments) {
				b.inform(event, extra);
			}
		}
	}

	public void asyncInform(final int event, final Bundle extra) {
		this.mThreadPool.execute(new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						inform(event, extra);
					}
				});
			}
		});
	}
}
