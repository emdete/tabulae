package de.emdete.tabulae;

import android.content.ComponentName;
import android.content.Context;
import de.emdete.tabulae.fawlty.Fawlty;
import de.emdete.tabulae.gui.Controller;
import de.emdete.tabulae.gui.Dashboard;
import de.emdete.tabulae.gui.DocumentAvtivity;
import de.emdete.tabulae.locus.Locus;
import de.emdete.tabulae.poi.Poi;
import de.emdete.tabulae.poi.PoiItem;
import de.emdete.tabulae.screencapture.ScreenCaptureFragment;
import de.emdete.tabulae.track.TrackItem;
import de.emdete.tabulae.track.TrackPointItem;
import de.emdete.tabulae.traffic.Traffic;
import de.emdete.thinstore.StoreObject;
import android.app.Activity;
import android.app.FragmentManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import de.emdete.tabulae.map.Map;
import de.emdete.tabulae.track.Track;

public class Tabulae extends Activity {
	protected Base[] fragments;
	protected Menu menu;
	protected File baseStorageFile = null;
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "inform");
		}
	});
	SQLiteOpenHelper dbHelper;

	static private String deKay(long l) {
		double d = l;
		final String[] fs = new String[]{"%.2fB", "%.2fKB", "%.2fMB", "%.2fGB",};
		for (String f : fs) {
			if (d < 1024.0) {
				return String.format(Locale.US, f, d);
			}
			d /= 1024.0;
		}
		return String.format(Locale.US, "%.2fTB", d);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onCreate");
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e(Constants.TAG, "error e=" + e, e);
				finish();
			}
		});
        dbHelper = new SQLiteOpenHelper(getApplicationContext(), "tabulae.db", null, 3){
			@Override public void onCreate(SQLiteDatabase db) {
				if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onCreate.SQLiteOpenHelper.onCreate");
				//Log.d(TAG, "create=" +
				StoreObject.create(db, PoiItem.class);
				StoreObject.create(db, TrackItem.class);
				StoreObject.create(db, TrackPointItem.class);
			}
			@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onCreate.SQLiteOpenHelper.onUpgrade");
				StoreObject.alter(db, PoiItem.class);
				StoreObject.alter(db, TrackItem.class);
				StoreObject.alter(db, TrackPointItem.class);
			}
		};
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
				new Traffic(),
		};
		//noinspection StatementWithEmptyBody
		if (savedInstanceState != null) {
			// .. = savedInstanceState.getString("..", null);
		}
		SharedPreferences preferences = getSharedPreferences("map", Context.MODE_PRIVATE);
		String baseStorage = preferences.getString("baseStorage", null);
		Log.d(Constants.TAG, "Tabulae.onCreate preferences baseStorage=" + baseStorage);
		if (baseStorage != null) {
			baseStorageFile = new File(baseStorage);
			if (!Environment.getExternalStorageState(baseStorageFile).equals(Environment.MEDIA_MOUNTED)) {
				Log.e(Constants.TAG, "Tabulae.onCreate not mounted: baseStorage=" + baseStorage);
				Toast.makeText(this, "Storage gone! Please reinsert SD.", Toast.LENGTH_LONG).show();
				// TODO: ask for different storage
				finish();
			}
		}
		long baseStorageSpace = 0;
		if (baseStorageFile == null) {
			// look for the largest storage to begin with
			for (File dir : getApplicationContext().getExternalFilesDirs(null)) {
				Log.d(Constants.TAG, "Tabulae.onCreate getExternalFilesDirs baseStorage=" + baseStorage);
				if (dir != null && !Environment.getExternalStorageState(dir).equals(Environment.MEDIA_MOUNTED)) {
					continue;
				}
				long dirSpace = new StatFs(dir.getPath()).getAvailableBytes();
				if (dirSpace > baseStorageSpace) {
					baseStorageFile = dir;
					baseStorageSpace = dirSpace;
				}
			}
			Editor editor = preferences.edit();
			editor.putString("baseStorage", baseStorage);
			editor.apply();
		} else {
			baseStorageSpace = new StatFs(baseStorageFile.getPath()).getAvailableBytes();
		}
		Log.d(Constants.TAG, "Tabulae.onCreate using baseStorageFile=" + baseStorageFile + ", baseStorageSpace=" + deKay(baseStorageSpace));
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Constants.DEBUG)
			Log.d(Constants.TAG, "Tabulae.onCreate process intent=" + queryIntent + ", action=" + queryAction);
		//noinspection StatementWithEmptyBody
		if (Intent.ACTION_MAIN.equals(queryAction)) {
			// nothing more to do
		} else if (Constants.ACTION_CONVERSATIONS_REQUEST.equals(queryAction)) {
			{
				String package_ = getCallingPackage();
				ComponentName name = getCallingActivity();
				String activity = name==null?null:name.flattenToString();
				if (Constants.DEBUG)
					Log.d(Constants.TAG, "Tabulae.onCreate package_=" + package_ + ", activity=" + activity);
			}
			//Bundle extra = queryIntent.getExtras();
			MapView mapView = getMapView();
			if (mapView == null) {
				setResult(Activity.RESULT_CANCELED, null);
			} else {
				LatLong location = mapView.getModel().mapViewPosition.getCenter(); // TODO defere location determination?
				Intent result = new Intent();
				result.putExtra(Constants.LATITUDE, location.latitude);
				result.putExtra(Constants.LONGITUDE, location.longitude);
				//result.putExtra(ALTITUDE, .getAltitude());
				//result.putExtra(ACCURACY, .getAccuracy());
				setResult(Activity.RESULT_OK, result);
			}
			finish();
		} else if (Constants.ACTION_CONVERSATIONS_SHOW.equals(queryAction)) {
			Bundle extra = queryIntent.getExtras();
			if (extra.containsKey(Constants.LONGITUDE) && extra.containsKey(Constants.LATITUDE)) {
				String jid = extra.getString(Constants.JID);
				String name = extra.getString(Constants.NAME);
				if (name == null || name.length() == 0) {
					if (jid != null && jid.length() > 0) {
						name = jid.split("@")[0]; // TODO avoid regex
					} else {
						name = "Jabber";
						jid = "@xmpp";
					}
				}
				double latitude = extra.getDouble(Constants.LATITUDE, 0);
				double longitude = extra.getDouble(Constants.LONGITUDE, 0);
				long id = Poi.storePointPosition(this, jid, name + ", on " + new Date(), latitude, longitude, true);
				Log.w(Constants.TAG, "onCreate.ACTION_CONVERSATIONS_SHOW id=" + id);
			} else
				Log.w(Constants.TAG, "onCreate conversations intent recceived with no latitude/longitude");
		} else if (Intent.ACTION_VIEW.equalsIgnoreCase(queryAction)) {
			try {
				//List<ActivityManager.RecentTaskInfo> recentTasks = ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getRecentTasks(99, ActivityManager.RECENT_WITH_EXCLUDED);
				Uri uri = queryIntent.getData();
				if (uri.getScheme().equalsIgnoreCase(Constants.GEO)) {
					final String part = uri.getEncodedSchemeSpecificPart().split("\\?")[0];
					final String[] latlon = part.split(","); // TODO avoid regex
					Bundle extra = new Bundle();
					extra.putDouble(Constants.LATITUDE, Double.parseDouble(latlon[0]));
					extra.putDouble(Constants.LONGITUDE, Double.parseDouble(latlon[1]));
					extra.putString(Constants.NAME, "poi");
					extra.putString(Constants.DESCRIPTION, "shared poi");
					if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onCreate new poi extra=" + extra);
					asyncInform(R.id.event_do_poi_new, extra);
				}
				else if (uri.getScheme().equalsIgnoreCase(Constants.HTTP)
				 || uri.getScheme().equalsIgnoreCase(Constants.HTTPS)) {
					if (Constants.DEBUG) Log.d(Constants.TAG, "http/https, part=" + uri.getEncodedSchemeSpecificPart());
					// //www.openstreetmap.org/?mlat=52.66272&mlon=10.920636
					if ("www.openstreetmap.org".equals(uri.getHost())) {
						Bundle extra = new Bundle();
						extra.putDouble(Constants.LATITUDE, Double.parseDouble(uri.getQueryParameter("mlat")));
						extra.putDouble(Constants.LONGITUDE, Double.parseDouble(uri.getQueryParameter("mlon")));
						extra.putString(Constants.NAME, "poi");
						extra.putString(Constants.DESCRIPTION, "shared poi");
						if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onCreate new poi extra=" + extra);
						asyncInform(R.id.event_do_poi_new, extra);
					}
					else {
						if (Constants.DEBUG) Log.e(Constants.TAG, "http/https, unknown host=" + uri.getHost());
					}
				}
			}
			catch (Exception e) {
				Log.e(Constants.TAG, "Tabulae.onCreate", e);
				// Toast?
			}
		} else
			Log.e(Constants.TAG, "Tabulae.onCreate no fit action=" + queryAction);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onResume");
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
		if (Constants.DEBUG)
			Log.d(Constants.TAG, "Tabulae.onActivityResult resultCode=" + resultCode + ", requestCode=" + requestCode);
		for (Base b : fragments) {
			b.onActivityResult(requestCode, resultCode, resultData);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onPause");
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
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onDestroy");
		AndroidGraphicFactory.clearResourceMemoryCache();
	}

	public void setVisible(int id, boolean v) {
		if (menu != null) {
			menu.findItem(id).setVisible(v);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main_option_menu, menu);
		this.menu = menu;
		if (Constants.DEBUG) {
			setVisible(R.id.event_do_screencapture, true);
			setVisible(R.id.event_do_fawlty, true);
			setVisible(R.id.event_do_traffic, true);
		}
		for (int e: new int[]{
			R.id.event_request_dashboard,
			R.id.event_request_fawlty,
			R.id.event_request_screencapture,
			R.id.event_request_traffic,
			}) {
			inform(e, null);
		}
		Map.onCreateOptionsMenu(this); // enable/disable maps in menu
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
		if (Constants.DEBUG) Log.d(Constants.TAG, "Tabulae.onSaveInstanceState bundle=" + bundle);
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

	public SQLiteDatabase getWritableDatabase() {
		return dbHelper.getWritableDatabase();
	}

	public SQLiteDatabase getReadableDatabase() {
		return dbHelper.getReadableDatabase();
	}

	public MapView getMapView() {
		return ((Map) fragments[0]).getMapView();
	}

	public void inform(final int event, final Bundle extra) {
		switch (event) {
		case R.id.event_do_help:
			Intent intent = new Intent(this, DocumentAvtivity.class);
			String lang = getResources().getConfiguration().locale.getLanguage();
			Bundle extras = new Bundle();
			String path = "documents-" + lang;
			try {
				if (getResources().getAssets().list(path).length == 0) {
					throw new IOException();
				}
			}
			catch (IOException ignore) {
				path = "documents";
			}
			// TODO check if exists and fallback
			extras.putString("url", "file:///android_asset/" + path + "/index.html");
			intent.putExtras(extras);
			startActivity(intent);
		break;
		case R.id.event_notify_screencapture:
			if (menu != null) {
				menu.findItem(R.id.event_do_screencapture).setChecked(extra.getBoolean("enabled"));
			}
		break;
		case R.id.event_notify_fawlty:
			if (menu != null) {
				menu.findItem(R.id.event_do_fawlty).setChecked(extra.getBoolean("enabled"));
			}
		break;
		case R.id.event_notify_traffic:
			if (menu != null) {
				menu.findItem(R.id.event_do_traffic).setChecked(extra.getBoolean("enabled"));
			}
		break;
		case R.id.event_notify_dashboard:
			if (menu != null) {
				menu.findItem(R.id.event_do_dashboard).setChecked(extra.getBoolean("enabled"));
			}
		break;
		}
		for (Base b : fragments) {
			b.inform(event, extra);
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
