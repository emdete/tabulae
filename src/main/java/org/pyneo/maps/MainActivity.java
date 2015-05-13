package org.pyneo.maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pyneo.maps.dashboard.IndicatorManager;
import org.pyneo.maps.dashboard.IndicatorView;
import org.pyneo.maps.dashboard.IndicatorView.IndicatorViewMenuInfo;
import org.pyneo.maps.downloader.AreaSelectorActivity;
import org.pyneo.maps.downloader.FileDownloadListActivity;
import org.pyneo.maps.poi.GeoDataActivity;
import org.pyneo.maps.poi.PoiActivity;
import org.pyneo.maps.poi.PoiListActivity;
import org.pyneo.maps.poi.PoiManager;
import org.pyneo.maps.poi.PoiPoint;
import org.pyneo.maps.track.Track;
import org.pyneo.maps.track.TrackListActivity;
import org.pyneo.maps.map.PredefMapsParser;
import org.pyneo.maps.track.CurrentTrackOverlay;
import org.pyneo.maps.map.MeasureOverlay;
import org.pyneo.maps.map.MyLocationOverlay;
import org.pyneo.maps.poi.PoiOverlay;
import org.pyneo.maps.map.SearchResultOverlay;
import org.pyneo.maps.map.TileOverlay;
import org.pyneo.maps.track.TrackOverlay;
import org.pyneo.maps.preference.MixedMapsPreference;
import org.pyneo.maps.tileprovider.TileSource;
import org.pyneo.maps.tileprovider.TileSourceBase;
import org.pyneo.maps.utils.CompassView;
import org.pyneo.maps.utils.CrashReportHandler;
import org.pyneo.maps.utils.RException;
import org.pyneo.maps.utils.SearchSuggestionsProvider;
import org.pyneo.maps.utils.SimpleThreadFactory;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.utils.IMoveListener;
import org.pyneo.maps.map.MapView;
import org.pyneo.maps.map.TileView;
import org.pyneo.maps.map.TileViewOverlay;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.util.StreamUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
*
*/
public class MainActivity extends Activity implements Constants {
	private boolean mAutoFollow = true;
	private boolean mCompassEnabled = false;
	private boolean mDrivingDirectionUp = true;
	private boolean mGPSFastUpdate = true;
	private boolean mNorthDirectionUp = true;
	private boolean mShowOverlay = false;
	private CompassView mCompassView;
	private CurrentTrackOverlay mCurrentTrackOverlay;
	private ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new SimpleThreadFactory("MainActivity.Search"));
	private float mLastBearing;
	private float mLastSpeed;
	private Handler mCallbackHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case Ut.TILEPROVIDER_SUCCESS_ID: {
					mMap.invalidate(); //postInvalidate();
				}
				case R.id.user_moved_map: {
					// setAutoFollow(false);
				}
				case R.id.set_title: {
					setTitle();
				}
				case Ut.TILEPROVIDER_ERROR_MESSAGE: {
					if (msg.obj != null)
						Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
				}
			}
		}
	};
	private ImageView ivAutoFollow;
	private ImageView mOverlayView;
	private IndicatorManager mIndicatorManager;
	private int mGpsStatusSatCnt = 0;
	private int mGpsStatusState = 0;
	private int mMarkerIndex;
	private int mPrefOverlayButtonBehavior;
	private int mPrefOverlayButtonVisibility;
	private MapView mMap;
	private MeasureOverlay mMeasureOverlay;
	private MyLocationOverlay mMyLocationOverlay;
	private PoiManager mPoiManager;
	private PoiOverlay mPoiOverlay;
	private PowerManager.WakeLock myWakeLock;
	private SampleLocationListener mLocationListener;
	private SampleLocationListener mNetListener;
	private SearchResultOverlay mSearchResultOverlay;
	private SensorManager mOrientationSensorManager;
	private String mStatusLocationProviderName = "";
	private String mMapId = null;
	private String mOverlayId = "";
	private TileOverlay mTileOverlay = null;
	private TileSource mTileSource;
	private TrackOverlay mTrackOverlay;
	private IMoveListener mMoveListener = new IMoveListener() {
		@Override
		public void onMoveDetected() {
			if (mIndicatorManager != null)
				mIndicatorManager.setCenter(mMap.getMapCenter());
			if (mAutoFollow)
				setAutoFollow(false);
		}
		@Override
		public void onZoomDetected() {
			setTitle();
		}
		@Override
		public void onCenterDetected() {
			if (mIndicatorManager != null)
				mIndicatorManager.setCenter(mMap.getMapCenter());
		}
	};
	private final SensorEventListener mListener = new SensorEventListener() {
		private int iOrientation = -1;
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (iOrientation < 0) {
				iOrientation = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
			}
			mCompassView.setAzimuth(event.values[0] + 90 * iOrientation);
			mCompassView.invalidate();
			if (mCompassEnabled && mNorthDirectionUp) {
				if (!mDrivingDirectionUp || mLastSpeed == 0) {
					mMap.setBearing(updateBearing(event.values[0]) + 90 * iOrientation);
					mMap.invalidate();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Ut.d("onCreate savedInstanceState=" + savedInstanceState);
		super.onCreate(savedInstanceState);
		if (!LOGDEBUG) {
			CrashReportHandler.attach(this);
		}
		setContentView(R.layout.main);
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final RelativeLayout rl = (RelativeLayout)findViewById(R.id.map_area);
		final int pref_zoomctrl = Integer.parseInt(pref.getString("pref_zoomctrl", "1"));
		final boolean pref_showtitle = pref.getBoolean("pref_showtitle", true);
		final boolean pref_show_autofollow_button = pref.getBoolean("pref_show_autofollow_button", true);
		final boolean pref_showscalebar = pref.getBoolean("pref_showscalebar", true);
		if (!pref_showtitle)
			findViewById(R.id.screen).setVisibility(View.GONE);
		mMap = new MapView(this, pref_zoomctrl, pref_showscalebar? 1: 0);
		mMap.setId(R.id.main);
		final RelativeLayout.LayoutParams pMap = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		rl.addView(mMap, pMap);
		mCompassView = new CompassView(this, pref_zoomctrl != 2);
		mCompassView.setVisibility(mCompassEnabled? View.VISIBLE: View.INVISIBLE);
		final RelativeLayout.LayoutParams compassParams = new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		compassParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		if (pref_zoomctrl == 2) {
			compassParams.addRule(RelativeLayout.ABOVE, R.id.scale_bar);
		}
		else {
			compassParams.addRule(RelativeLayout.BELOW, R.id.dashboard_area);
		}
		mMap.addView(mCompassView, compassParams);
		if (pref_show_autofollow_button) {
			ivAutoFollow = new ImageView(this);
			ivAutoFollow.setImageResource(R.drawable.autofollow);
			ivAutoFollow.setVisibility(ImageView.INVISIBLE);
			final RelativeLayout.LayoutParams followParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
			followParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			if (pref_zoomctrl == 2) {
				followParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			}
			else {
				followParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			}
			((RelativeLayout)findViewById(R.id.right_area)).addView(ivAutoFollow, followParams);
			ivAutoFollow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					setAutoFollow(true);
					mSearchResultOverlay.Clear();
					setLastKnownLocation();
				}
			});
		}
		mOverlayView = new ImageView(this);
		mOverlayView.setImageResource(R.drawable.b_overlays);
		final int pad = getResources().getDimensionPixelSize(R.dimen.zoom_ctrl_padding);
		mOverlayView.setPadding(0, pad, 0, pad);
		((LinearLayout)mMap.findViewById(R.id.right_panel)).addView(mOverlayView);
		mOverlayView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTileSource.YANDEX_TRAFFIC_ON == 1) {
					mShowOverlay = !mShowOverlay;
					fillOverlays();
				}
				else {
					if (mPrefOverlayButtonBehavior == 1) {
						v.showContextMenu();
					}
					else if (mPrefOverlayButtonBehavior == 2) {
						setTileSource(mTileSource.ID, mOverlayId, !mShowOverlay);
					}
					else if (mOverlayId.equalsIgnoreCase("") && mTileSource.MAP_TYPE != TileSourceBase.MIXMAP_PAIR) {
						v.showContextMenu();
					}
					else {
						setTileSource(mTileSource.ID, mOverlayId, !mShowOverlay);
					}
				}
				mMap.invalidate(); //postInvalidate();
			}
		});
		mOverlayView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (mTileSource.YANDEX_TRAFFIC_ON != 1 && mPrefOverlayButtonBehavior == 0) {
					mMap.getTileView().mPoiMenuInfo.EventGeoPoint = null;
					v.showContextMenu();
				}
				return true;
			}
		});
		mOverlayView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				mMap.getTileView().mPoiMenuInfo.EventGeoPoint = null;
				menu.setHeaderTitle(R.string.menu_title_overlays);
				menu.add(Menu.NONE, R.id.hide_overlay, Menu.NONE, R.string.menu_hide_overlay);
				//SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				File folder = Ut.getAppMapsDir(MainActivity.this);
				if (folder.exists()) {
					File[] files = folder.listFiles();
					if (files != null) {
						for (File file: files) {
							if (
								file.getName().toLowerCase().endsWith(".mnm") ||
								file.getName().toLowerCase().endsWith(".tar") ||
								file.getName().toLowerCase().endsWith(".sqlitedb")
							) {
								String name = Ut.FileName2ID(file.getName());
								if (
									pref.getBoolean("pref_usermaps_" + name + "_enabled", false) &&
									// (mTileSource.PROJECTION == 0 || mTileSource.PROJECTION == Integer.parseInt(pref.getString("pref_usermaps_" + name + "_projection", "1"))) &&
									pref.getBoolean("pref_usermaps_" + name + "_isoverlay", false)
								) {
									MenuItem item = menu.add(R.id.isoverlay, Menu.NONE, Menu.NONE,
										pref.getString("pref_usermaps_" + name + "_name", file.getName()));
									item.setTitleCondensed("usermap_" + name);
								}
							}
						}
					}
				}
				Cursor c = mPoiManager.getGeoDatabase().getMixedMaps();
				if (c != null) {
					if (c.moveToFirst()) {
						do {
							if (pref.getBoolean("PREF_MIXMAPS_" + c.getInt(0) + "_enabled", false) && c.getInt(2) == 3) {
								final JSONObject json = MixedMapsPreference.getMapCustomParams(c.getString(3));
								//if(mTileSource.PROJECTION == 0 || mTileSource.PROJECTION == json.optInt(MixedMapsPreference.MAPPROJECTION)) {
								MenuItem item = menu.add(R.id.isoverlay, Menu.NONE, Menu.NONE, c.getString(1));
								item.setTitleCondensed("mixmap_" + c.getInt(0));
								//}
							}
						}
						while (c.moveToNext());
					}
					c.close();
				}
				final SAXParserFactory fac = SAXParserFactory.newInstance();
				SAXParser parser = null;
				try {
					parser = fac.newSAXParser();
					if (parser != null) {
						final InputStream in = getResources().openRawResource(R.raw.predefmaps);
						parser.parse(in, new PredefMapsParser(menu, pref, true, mTileSource.PROJECTION));
					}
				}
				catch (Exception e) {
					Ut.e(e.toString(), e);
				}

			}
		});
		registerForContextMenu(mMap);
		mPoiManager = new PoiManager(this);
		mLocationListener = new SampleLocationListener();
		mMap.setMoveListener(mMoveListener);
		mOrientationSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		mPrefOverlayButtonBehavior = Integer.parseInt(pref.getString("pref_overlay_button_behavior", "0"));
		mPrefOverlayButtonVisibility = Integer.parseInt(pref.getString("pref_overlay_button_visibility", "0"));
		if (mPrefOverlayButtonVisibility == 1) { // Always hide
			mOverlayView.setVisibility(View.GONE);
		}
		mCompassEnabled = uiState.getBoolean("CompassEnabled", mCompassEnabled);
		mCompassView.setVisibility(mCompassEnabled? View.VISIBLE: View.INVISIBLE);
		mMap.setCenter(new GeoPoint(uiState.getInt("Latitude", 0), uiState.getInt("Longitude", 0)));
		mGPSFastUpdate = pref.getBoolean("pref_gpsfastupdate", mGPSFastUpdate);
		mAutoFollow = uiState.getBoolean("AutoFollow", mAutoFollow);
		setAutoFollow(mAutoFollow, true);
		mTrackOverlay = new TrackOverlay(this, mPoiManager, mCallbackHandler);
		mCurrentTrackOverlay = new CurrentTrackOverlay(this, mPoiManager);
		mPoiOverlay = new PoiOverlay(this, mPoiManager, null, pref.getBoolean("pref_hidepoi", false));
		mPoiOverlay.setTapIndex(uiState.getInt("curShowPoiId", NO_TAP));
		mMyLocationOverlay = new MyLocationOverlay(this);
		mSearchResultOverlay = new SearchResultOverlay(this, mMap);
		mSearchResultOverlay.fromPref(uiState);
		fillOverlays();
		mDrivingDirectionUp = pref.getBoolean("pref_drivingdirectionup", mDrivingDirectionUp);
		mNorthDirectionUp = pref.getBoolean("pref_northdirectionup", mNorthDirectionUp);
		final int screenOrientation = Integer.parseInt(pref.getString("pref_screen_orientation", "-1"));
		setRequestedOrientation(screenOrientation);
		final boolean showstatusbar = pref.getBoolean("pref_showstatusbar", true);
		if (showstatusbar) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().getDecorView().setSystemUiVisibility(0);
		}
		else {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().getDecorView().setSystemUiVisibility(0
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // hide action bar
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				| View.SYSTEM_UI_FLAG_IMMERSIVE // remove for version < API 19
			);
		}
		if (uiState.getString("error", "").length() > 0) {
			showDialog(R.id.error);
		}
		if (!uiState.getString("app_version", "").equalsIgnoreCase(Ut.getAppVersion(this))) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			showDialog(R.id.whatsnew);
		}
		final Intent queryIntent = getIntent();
		Ut.i("onCreate process intent=" + queryIntent);
		final String queryAction = queryIntent.getAction();
		Ut.i("onCreate process action=" + queryAction);
		if (Intent.ACTION_MAIN.equals(queryAction)) {
			;
		}
		else if (Intent.ACTION_SEARCH.equals(queryAction)) {
			doSearchQuery(queryIntent);
			Ut.i("onCreate doSearchQuery");
		}
		else if (ACTION_SHOW_POINTS.equalsIgnoreCase(queryAction)) {
			doShowPoints(queryIntent);
			Ut.i("onCreate doShowPoints");
		}
		else if (Intent.ACTION_VIEW.equalsIgnoreCase(queryAction)) {
			Uri uri = queryIntent.getData();
			if (uri.getScheme().equalsIgnoreCase("geo")) {
				final String latlon = uri.getEncodedSchemeSpecificPart().replace("?" + uri.getEncodedQuery(), "");
				if (latlon.equals("0,0")) {
					final String query = uri.getEncodedQuery().replace("q=", "");
					queryIntent.putExtra(SearchManager.QUERY, query);
					doSearchQuery(queryIntent);

				}
				else {
					GeoPoint point = GeoPoint.fromDoubleString(latlon);
					mPoiOverlay.clearPoiList();
					mPoiOverlay.setGpsStatusGeoPoint(0, point, "GEO", "");
					setAutoFollow(false);
					mMap.setCenter(point);
				}
			}
			Ut.i("onCreate setGpsStatusGeoPoint");
		}
		else if (ACTION_SHOW_MAP_ID.equalsIgnoreCase(queryAction)) {
			final Bundle bundle = queryIntent.getExtras();
			mMapId = bundle.getString(MAPNAME);
			if (bundle.containsKey("center")) {
				try {
					final GeoPoint geo = GeoPoint.fromDoubleString(bundle.getString("center"));
					mMap.setCenter(geo);
				}
				catch (Exception e) {
					Ut.e(e.toString(), e);
				}
			}
			if (bundle.containsKey("zoom")) {
				try {
					final int zoom = Integer.valueOf(bundle.getString("zoom"));
					mMap.setZoom(zoom);
					SharedPreferences.Editor editor = uiState.edit();
					editor.putInt("ZoomLevel", mMap.getZoomLevel());
					editor.commit();
				}
				catch (Exception e) {
					Ut.e(e.toString(), e);
				}
			}
			queryIntent.setAction("");
			Ut.i("onCreate SharedPreferences");
		}
		else if (ACTION_CONVERSATIONS_SHOW.equals(queryAction)) {
			if (queryIntent.hasExtra(LONGITUDE) && queryIntent.hasExtra(LATITUDE)) {
				String name = queryIntent.getStringExtra(NAME);
				double longitude = queryIntent.getDoubleExtra(LONGITUDE, 0);
				double latitude = queryIntent.getDoubleExtra(LATITUDE, 0);
				// altitude?
				// accuracy?
				Location location = new Location(name);
				location.setLatitude(latitude);
				location.setLongitude(longitude);
				GeoPoint point = GeoPoint.fromDoubleString("" + latitude + ',' + longitude); // TODO
				mPoiOverlay.clearPoiList();
				mPoiOverlay.setGpsStatusGeoPoint(0, point, name, name);
				setAutoFollow(false);
				mMap.setCenter(point);
				Ut.i("onCreate location received");
			}
			else
				Ut.w("onCreate conversations intent recceived with no latitude/longitude");
		}
		else if (ACTION_CONVERSATIONS_REQUEST.equals(queryAction)) {
			Location location = mMyLocationOverlay.getLastLocation();
			Intent result = new Intent();
			if (location != null) {
				result.putExtra(LATITUDE, location.getLatitude());
				result.putExtra(LONGITUDE, location.getLongitude());
				result.putExtra(ALTITUDE, location.getAltitude());
				result.putExtra(ACCURACY, (int)location.getAccuracy());
				Ut.i("onCreate location sent");
			}
			else {
				// TODO defere location determination
				result.putExtra(LATITUDE, 52.0);
				result.putExtra(LONGITUDE, 7.0);
				result.putExtra(ALTITUDE, 90.0);
				result.putExtra(ACCURACY, 300);
				Ut.w("onCreate cannot send latitude/longitude to conversations, dummy sent");
			}
			setResult(RESULT_OK, result);
			finish();
		}
		else
			Ut.i("onCreate no fit");
		Ut.i("onCreate done");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final String queryAction = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			doSearchQuery(intent);
		}
		else if (ACTION_SHOW_POINTS.equalsIgnoreCase(queryAction)) {
			doShowPoints(intent);
		}
		else if (ACTION_SHOW_MAP_ID.equalsIgnoreCase(queryAction)) {
			final Bundle bundle = intent.getExtras();
			mMapId = bundle.getString(MAPNAME);
			if (bundle.containsKey("center")) {
				try {
					final GeoPoint geo = GeoPoint.fromDoubleString(bundle.getString("center"));
					mMap.setCenter(geo);
				}
				catch (Exception e) {
					Ut.e(e.toString(), e);
				}
			}
			if (bundle.containsKey("zoom")) {
				try {
					final int zoom = Integer.valueOf(bundle.getString("zoom"));
					mMap.setZoom(zoom);
					SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = uiState.edit();
					editor.putInt("ZoomLevel", mMap.getZoomLevel());
					editor.commit();
				}
				catch (Exception e) {
					Ut.e(e.toString(), e);
				}
			}
		}
	}

	private void doSearchQuery(Intent queryIntent) {
		try {
			mSearchResultOverlay.Clear();
			mMap.invalidate();
			final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
			// Record the query string in the recent queries suggestions provider.
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
			suggestions.saveRecentQuery(queryString, null);
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					Handler handler = mCallbackHandler;
					Resources resources = getApplicationContext().getResources();
					InputStream in = null;
					OutputStream out = null;
					try {
						// TODO: replaced by https://developers.google.com/places/webservice/, OSM?
						//final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
						Configuration config = getBaseContext().getResources().getConfiguration();
						final String lang = config.locale.getLanguage();
						//handler.obtainMessage(Ut.TILEPROVIDER_SEARCH_OK_MESSAGE, res);
						final String address = "";
						setAutoFollow(false, true);
						final GeoPoint point = new GeoPoint(0, 0);
						mSearchResultOverlay.setLocation(point, address);
						//mMap.setZoom(0);
						mMap.setCenter(point);
						setTitle();

					}
					catch (Exception e) {
						try {
							handler.obtainMessage(Ut.TILEPROVIDER_ERROR_MESSAGE, resources.getString(R.string.no_inet_conn));
						}
						catch (NotFoundException e1) {
							Ut.e(e.toString(), e);
						}
					}
					finally {
						StreamUtils.closeStream(in);
						StreamUtils.closeStream(out);
					}
				}
			});
		}
		catch (Exception e) {
			Ut.e(e.toString(), e);
		}
	}

	private void fillOverlays() {
		mMap.getOverlays().clear();
		if (mMeasureOverlay != null)
			mMap.getOverlays().add(mMeasureOverlay);
		if (mTileOverlay != null)
			mMap.getOverlays().add(mTileOverlay);
		if (mTrackOverlay != null)
			mMap.getOverlays().add(mTrackOverlay);
		if (mCurrentTrackOverlay != null)
			mMap.getOverlays().add(mCurrentTrackOverlay);
		if (mPoiOverlay != null)
			mMap.getOverlays().add(mPoiOverlay);
		mMap.getOverlays().add(mMyLocationOverlay);
		mMap.getOverlays().add(mSearchResultOverlay);
	}

	private void setAutoFollow(boolean autoFollow) {
		setAutoFollow(autoFollow, false);
	}

	private void setAutoFollow(boolean autoFollow, final boolean supressToast) {
		mAutoFollow = autoFollow;
		if (autoFollow) {
			if (ivAutoFollow != null)
				ivAutoFollow.setVisibility(ImageView.INVISIBLE);
			if (!supressToast)
				Toast.makeText(this, R.string.auto_follow_enabled, Toast.LENGTH_SHORT).show();
		}
		else {
			if (ivAutoFollow != null)
				ivAutoFollow.setVisibility(ImageView.VISIBLE);
			if (!supressToast)
				Toast.makeText(this, R.string.auto_follow_disabled, Toast.LENGTH_SHORT).show();
		}
	}

	private void setLastKnownLocation() {
		GeoPoint p = mMyLocationOverlay.getLastGeoPoint();
		if (p != null) {
			if (mAutoFollow)
				mMap.setCenter(p);
		}
		else {
			final LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			final Location locGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			final Location locNlp = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			String str = null;
			Location loc = null;
			if (locGps == null && locNlp != null)
				loc = locNlp;
			else if (locGps != null && locNlp == null)
				loc = locGps;
			else if (locGps == null && locNlp == null)
				loc = null;
			else
				loc = locGps.getTime() > locNlp.getTime()? locGps: locNlp;
				// TODO: from API17 up use: loc = locGps.getElapsedRealtimeNanos() > locNlp.getElapsedRealtimeNanos()? locGps: locNlp;
			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			}
			else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				str = getString(R.string.message_gpsdisabled);
			else if (loc == null)
				str = getString(R.string.message_locationunavailable);
			else
				str = getString(R.string.message_lastknownlocation);
			if (str != null)
				Toast.makeText(this, str, Toast.LENGTH_LONG).show();
			if (loc != null) {
				p = TypeConverter.locationToGeoPoint(loc);
				if (mAutoFollow)
					mMap.setCenter(p);
				mMyLocationOverlay.setLocation(loc);
				mMap.invalidate();
			}
		}
	}

	private void setTitle() {
		if (mIndicatorManager != null)
			mIndicatorManager.setMapName(
				mMap.getTileSource().CATEGORY + ": " +
				mMap.getTileSource().NAME
				);
		try {
			final TextView leftText = (TextView)findViewById(R.id.left_text);
			if (leftText != null) {
				if ( // do we have an overlay?
					mMap.getTileSource() != null &&
					mMap.getTileSource().MAP_TYPE != TileSourceBase.MIXMAP_PAIR &&
					mMap.getTileSource().getTileSourceBaseOverlay() != null
				)
					leftText.setText(mMap.getTileSource().CATEGORY + ": " + mMap.getTileSource().NAME
						+ " / " + mMap.getTileSource().getTileSourceBaseOverlay().NAME);
				else
					leftText.setText(mMap.getTileSource().CATEGORY + ": " + mMap.getTileSource().NAME);
			}
			final TextView statusLocationProvider = (TextView)findViewById(R.id.gps_text);
			if (statusLocationProvider != null) {
				statusLocationProvider.setText(mStatusLocationProviderName);
			}
			final TextView rightText = (TextView)findViewById(R.id.right_text);
			if (rightText != null) {
				final double zoom = mMap.getZoomLevelScaled();
				if (zoom > mMap.getTileSource().ZOOM_MAXLEVEL) {
					rightText.setText("" + (mMap.getTileSource().ZOOM_MAXLEVEL + 1) + "+");
					if (mIndicatorManager != null)
						mIndicatorManager.setZoom(mMap.getTileSource().ZOOM_MAXLEVEL + 1);
				}
				else {
					rightText.setText("" + (1 + Math.round(zoom)));
					if (mIndicatorManager != null)
						mIndicatorManager.setZoom((int)(1 + Math.round(zoom)));
				}
			}
		}
		catch (Exception e) {
			Ut.e(e.toString(), e);
		}
	}

	@Override
	protected void onResume() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		if (mMapId == null)
			mMapId = uiState.getString(MAPNAME, TileSource.MAPNIK);
		mOverlayId = uiState.getString("OverlayID", "");
		mShowOverlay = uiState.getBoolean("ShowOverlay", mShowOverlay);
		mMyLocationOverlay.setTargetLocation(GeoPoint.fromDoubleStringOrNull(uiState.getString("targetlocation", "")));
		setTileSource(mMapId, mOverlayId, mShowOverlay);
		mMapId = null;
		if (uiState.getBoolean("show_dashboard", true) && mIndicatorManager == null) {
			mIndicatorManager = new IndicatorManager(this);
			mIndicatorManager.setCenter(mMap.getMapCenter());
			mIndicatorManager.setLocation(mMyLocationOverlay.getLastLocation());
			mIndicatorManager.setTargetLocation(mMyLocationOverlay.getTargetLocation());
		}
		mMap.setZoom(uiState.getInt("ZoomLevel", 0));
		setTitle();
		fillOverlays();
		if (mCompassEnabled)
			mOrientationSensorManager.registerListener(mListener,
				mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		if (mTrackOverlay != null)
			mTrackOverlay.setStopDraw(false);
		if (mCurrentTrackOverlay != null)
			mCurrentTrackOverlay.onResume();
		Ut.d("onResume getBestProvider");
		mLocationListener.getBestProvider();
		if (mIndicatorManager != null)
			mIndicatorManager.Resume(this);
		if (pref.getBoolean("pref_keepscreenon", true)) {
			myWakeLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "Tabulae");
			myWakeLock.acquire();
		}
		else {
			myWakeLock = null;
		}
		setLastKnownLocation();
		super.onResume();
	}

	@Override
	protected void onRestart() {
		if (mTrackOverlay != null)
			mTrackOverlay.clearTrack();
		super.onRestart();
	}

	@Override
	protected void onStop() {
		Ut.d("onStop");
		super.onStop();
	}

	@Override
	protected void onPause() {
		Ut.d("onPause");
		final GeoPoint point = mMap.getMapCenter();
		SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = uiState.edit();
		if (mTileSource != null) {
			editor.putString(MAPNAME, mTileSource.ID);
			try {
				editor.putString("OverlayID", mTileOverlay == null? mTileSource.getOverlayName(): mTileOverlay.getTileSource().ID);
			}
			catch (Exception e) {
				Ut.e(e.toString(), e);
			}
		}
		editor.putBoolean("ShowOverlay", mShowOverlay);
		editor.putInt("Latitude", point.getLatitudeE6());
		editor.putInt("Longitude", point.getLongitudeE6());
		editor.putInt("ZoomLevel", mMap.getZoomLevel());
		editor.putBoolean("CompassEnabled", mCompassEnabled);
		editor.putBoolean("AutoFollow", mAutoFollow);
		editor.putString("app_version", Ut.getAppVersion(this));
		if (mPoiOverlay != null)
			editor.putInt("curShowPoiId", mPoiOverlay.getTapIndex());
		mSearchResultOverlay.toPref(editor);
		editor.putBoolean("show_dashboard", mIndicatorManager != null);
		editor.putString("targetlocation", mMyLocationOverlay.getTargetLocation() == null? "": mMyLocationOverlay.getTargetLocation().toDoubleString());
		editor.commit();
		uiState = getSharedPreferences(MAPNAME, Activity.MODE_PRIVATE);
		editor = uiState.edit();
		if (mTileSource != null)
			editor.putString(MAPNAME, mTileSource.ID);
		editor.putInt("Latitude", point.getLatitudeE6());
		editor.putInt("Longitude", point.getLongitudeE6());
		editor.putInt("ZoomLevel", mMap.getZoomLevel());
		editor.putBoolean("CompassEnabled", mCompassEnabled);
		editor.putBoolean("AutoFollow", mAutoFollow);
		editor.commit();
		if (myWakeLock != null)
			myWakeLock.release();
		if (mOrientationSensorManager != null)
			mOrientationSensorManager.unregisterListener(mListener);
		if (mCurrentTrackOverlay != null)
			mCurrentTrackOverlay.onPause();
		if (mTileSource != null)
			mTileSource.Free();
		mTileSource = null;
		mPoiManager.FreeDatabases();
		if (mTileOverlay != null)
			mTileOverlay.Free();
		mLocationListener.getLocationManager().removeUpdates(mLocationListener);
		if (mNetListener != null) {
			mLocationListener.getLocationManager().removeUpdates(mNetListener);
			mNetListener = null;
		}
		if (mIndicatorManager != null)
			mIndicatorManager.Pause(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Ut.d("onDestroy");
		if (mIndicatorManager != null) {
			mIndicatorManager.Dismiss(this);
			mIndicatorManager = null;
		}
		for (TileViewOverlay osmvo : mMap.getOverlays())
			osmvo.Free();
		if (mTileSource != null)
			mTileSource.Free();
		mTileSource = null;
		mMap.setMoveListener(null);
		mThreadPool.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_option_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Menu submenu = menu.findItem(R.id.mapselector).getSubMenu();
		submenu.clear();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (mTileSource == null) {
			menu.findItem(R.id.reload).setVisible(false);
		}
		else if (mTileSource.MAP_TYPE == TileSourceBase.PREDEF_ONLINE || mTileSource.MAP_TYPE == TileSourceBase.MIXMAP_CUSTOM) {
			menu.findItem(R.id.reload).setVisible(true);
		}
		else {
			menu.findItem(R.id.reload).setVisible(false);
		}
		File folder = Ut.getAppMapsDir(this);
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file: files) {
					if (file.getName().toLowerCase().endsWith(".mnm")
					|| file.getName().toLowerCase().endsWith(".tar")
					|| file.getName().toLowerCase().endsWith(".sqlitedb")) {
						String name = Ut.FileName2ID(file.getName());
						if (pref.getBoolean("pref_usermaps_" + name + "_enabled", false)
						&& !pref.getBoolean("pref_usermaps_" + name + "_isoverlay", false)) {
							MenuItem item = submenu.add(pref.getString("pref_usermaps_" + name + "_name",
								file.getName()));
							item.setTitleCondensed("usermap_" + name);
						}
					}
				}
			}
		}
		Cursor c = mPoiManager.getGeoDatabase().getMixedMaps();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					if (pref.getBoolean("PREF_MIXMAPS_" + c.getInt(0) + "_enabled", true) && c.getInt(2) < 3) {
						MenuItem item = submenu.add(c.getString(1));
						item.setTitleCondensed("mixmap_" + c.getInt(0));
					}
				} while (c.moveToNext());
			}
			c.close();
		}
		final SAXParserFactory fac = SAXParserFactory.newInstance();
		SAXParser parser = null;
		try {
			parser = fac.newSAXParser();
			if (parser != null) {
				final InputStream in = getResources().openRawResource(R.raw.predefmaps);
				parser.parse(in, new PredefMapsParser(submenu, pref));
			}
		}
		catch (Exception e) {
			Ut.e(e.toString(), e);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		final GeoPoint point = mMap.getMapCenter();
		switch (item.getItemId()) {
			case R.id.area_selector: {
				startActivity(new Intent(this, AreaSelectorActivity.class)
					.putExtra("new", true).putExtra(MAPNAME, mTileSource.ID)
					.putExtra("Latitude", point.getLatitudeE6())
					.putExtra("Longitude", point.getLongitudeE6())
					.putExtra("ZoomLevel", mMap.getZoomLevel()));
				return true;
			}
			case R.id.menu_show_dashboard: {
				if (mIndicatorManager == null) {
					mIndicatorManager = new IndicatorManager(this);
					mIndicatorManager.setCenter(mMap.getMapCenter());
					mIndicatorManager.setMapName(
						mTileSource.CATEGORY + ": " +
						mTileSource.NAME);
					mIndicatorManager.setZoom(mMap.getZoomLevel());
					mIndicatorManager.setLocation(mMyLocationOverlay.getLastLocation());
					mIndicatorManager.setTargetLocation(mMyLocationOverlay.getTargetLocation());
					mIndicatorManager.Resume(this);
				}
				else {
					mIndicatorManager.Dismiss(this);
					mIndicatorManager = null;
				}
				return true;
			}
			case R.id.downloadprepared: {
				startActivity(new Intent(this, FileDownloadListActivity.class));
				return true;
			}
	//		case R.id.tools: {
	//			return true;
	//		}
			case R.id.findthemap: {
				doFindTheMap();
				return true;
			}
			case R.id.reload: {
				mTileSource.setReloadTileMode(true);
				mMap.invalidate(); //postInvalidate();
				return true;
			}
			case R.id.measure: {
				doMeasureStart();
				return true;
			}
			case R.id.gpsstatus: {
				try {
					startActivity(new Intent(Intent.ACTION_MAIN) // act
						.addCategory(Intent.CATEGORY_LAUNCHER) // cat
						.setComponent(new ComponentName("com.vonglasow.michael.satstat", "com.vonglasow.michael.satstat.MainActivity")) // cmp
						);
				}
				catch (ActivityNotFoundException e) {
					Toast.makeText(this,
						R.string.message_nogpsstatus,
						Toast.LENGTH_LONG).show();
					try {
						startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri
							.parse("market://search?q=pname:com.vonglasow.michael.satstat")));
					}
					catch (Exception e1) {
						Ut.e(e.toString(), e);
					}
				}
				return true;
			}
			case R.id.poilist: {
				startActivityForResult((new Intent(this, PoiListActivity.class))
					.putExtra(LAT, point.getLatitude())
					.putExtra(LON, point.getLongitude())
					.putExtra("title", "POI"),
					R.id.poilist);
				return true;
			}
			case R.id.tracks: {
				startActivityForResult(new Intent(this, TrackListActivity.class), R.id.tracks);
				return true;
			}
			case R.id.routes: {
				startActivityForResult((new Intent(this, GeoDataActivity.class)), R.id.poilist);
				return true;
			}
			case R.id.search: {
				onSearchRequested();
				return true;
			}
			case R.id.settings: {
				startActivityForResult(new Intent(this, MainPreferences.class), R.id.settings_activity_closed);
				return true;
			}
			case R.id.about: {
				showDialog(R.id.about);
				return true;
			}
			case R.id.mapselector: {
				return true;
			}
			case R.id.menu_share: {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, new StringBuilder()
					.append("")
					.append('\n')
					.append("http://www.openstreetmap.org/?mlat=")
					.append(point.getLatitude())
					.append("&mlon=")
					.append(point.getLongitude())
					.append("#map=")
					.append(mMap.getZoomLevel())
					.append('/')
					.append(point.getLatitude())
					.append('/')
					.append(point.getLongitude())
					.append("&layers=T")
					.toString());
				startActivity(intent);
				return true;
			}
			case R.id.compass: {
				mCompassEnabled = !mCompassEnabled;
				mCompassView.setVisibility(mCompassEnabled? View.VISIBLE: View.INVISIBLE);
				if (mCompassEnabled)
					mOrientationSensorManager.registerListener(mListener, mOrientationSensorManager
						.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
				else {
					mOrientationSensorManager.unregisterListener(mListener);
					mMap.setBearing(0);
				}
				return true;
			}
			case R.id.mylocation: {
				setAutoFollow(true);
				setLastKnownLocation();
				return true;
			}
			case R.id.exit: {
				onPause();
				System.exit(10);
				return true;
			}
			default: {
				final String mapid = (String)item.getTitleCondensed();
				setTileSource(mapid, "", true);
				fillOverlays();
				setTitle();
				return true;
			}
		}
	}

	private void doFindTheMap() {
		final GeoPoint geo = mTileSource.findTheMap(mMap.getZoomLevel());
		if (geo != null)
			mMap.setCenter(geo);
	}

	private void doMeasureStart() {
		if (mMeasureOverlay == null)
			mMeasureOverlay = new MeasureOverlay(this, findViewById(R.id.bottom_area));
		final View viewBottomArea = findViewById(R.id.bottom_area);
		viewBottomArea.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMeasureOverlay.addPointOnCenter(mMap.getTileView());
				mMap.invalidate(); //postInvalidate();
			}
		});
		viewBottomArea.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMeasureOverlay = null;
				((ViewGroup)findViewById(R.id.bottom_area)).removeAllViews();
				fillOverlays();
			}
		});
		final View viewMenuButton = viewBottomArea.findViewById(R.id.menu);
		viewMenuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.showContextMenu();
			}
		});
		viewMenuButton.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				{
					final MenuItem item = menu.add(Menu.NONE, R.id.menu_showinfo, Menu.NONE, R.string.menu_showinfo);
					item.setCheckable(true);
					final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
					item.setChecked(pref.getBoolean("pref_show_measure_info", true));
				}
				{
					final MenuItem item = menu.add(Menu.NONE, R.id.menu_showlineinfo, Menu.NONE, R.string.menu_showlineinfo);
					item.setCheckable(true);
					final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
					item.setChecked(pref.getBoolean("pref_show_measure_line_info", true));
				}
				menu.add(Menu.NONE, R.id.menu_addmeasurepoint, Menu.NONE, R.string.menu_add);
				menu.add(Menu.NONE, R.id.menu_undo, Menu.NONE, R.string.menu_undo);
				menu.add(Menu.NONE, R.id.clear, Menu.NONE, R.string.clear);
			}
		});
		fillOverlays();
	}

	private void setTileSource(String aMapId, String aOverlayId, boolean aShowOverlay) {
		final String mapId = aMapId == null? (mTileSource == null? TileSource.MAPNIK: mTileSource.ID): aMapId;
		final String overlayId = aOverlayId == null? mOverlayId: aOverlayId;
		final String lastMapID = mTileSource == null? TileSource.MAPNIK: mTileSource.ID;
		if (mTileSource != null)
			mTileSource.Free();
		if (overlayId != null && !overlayId.equalsIgnoreCase("") && aShowOverlay) {
			mOverlayId = overlayId;
			mShowOverlay = true;
			try {
				mTileSource = new TileSource(this, mapId, overlayId);

			}
			catch (RException e) {
				mTileSource = null;
				addMessage(e);
			}
			catch (Exception e) {
				mTileSource = null;
				addMessage(new RException(R.string.error_other, e.getMessage()));
			}
		}
		else {
			if (mTileOverlay != null) {
				mTileOverlay.Free();
				mTileOverlay = null;
			}
			try {
				mTileSource = new TileSource(this, mapId, aShowOverlay);
				mShowOverlay = aShowOverlay;
				if (mapId != lastMapID)
					mOverlayId = "";
			}
			catch (RException e) {
				mTileSource = null;
				addMessage(e);
			}
			catch (Exception e) {
				mTileSource = null;
				addMessage(new RException(R.string.error_other, e.getMessage()));
			}
		}
		if (mTileSource != null) {
			final TileSource tileSource = mTileSource.getTileSourceForTileOverlay();
			if (tileSource != null) {
				if (mTileOverlay == null)
					mTileOverlay = new TileOverlay(mMap.getTileView(), true);
				mTileOverlay.setTileSource(tileSource);
			}
			else if (mTileOverlay != null) {
				mTileOverlay.Free();
				mTileOverlay = null;
			}
		}
		else {
			try {
				mTileSource = new TileSource(this, TileSource.MAPNIK);
			}
			catch (SQLiteException e) {
				Ut.e(e.toString(), e);
			}
			catch (RException e) {
				Ut.e(e.toString(), e);
			}
		}
		mMap.setTileSource(mTileSource);
		fillOverlays();
		if (mMyLocationOverlay != null && mTileSource != null)
			mMyLocationOverlay.setScale(mTileSource.MAPTILE_SIZE_FACTOR, mTileSource.GOOGLESCALE_SIZE_FACTOR);
		if (mPrefOverlayButtonVisibility == 2)
			mOverlayView.setVisibility(mTileSource.MAP_TYPE == TileSourceBase.MIXMAP_PAIR || mTileSource.YANDEX_TRAFFIC_ON == 1? View.VISIBLE: View.GONE);
	}

	private void addMessage(RException e) {
		LinearLayout msgbox = (LinearLayout)findViewById(e.getID());
		if (msgbox == null) {
			msgbox = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.error_message_box, (ViewGroup)findViewById(R.id.message_list));
			msgbox.setId(e.getID());
			msgbox.setVisibility(View.VISIBLE);
			msgbox.findViewById(R.id.message).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (v.findViewById(R.id.descr).getVisibility() == View.GONE)
						v.findViewById(R.id.descr).setVisibility(View.VISIBLE);
					else
						v.findViewById(R.id.descr).setVisibility(View.GONE);
				}
			});
			msgbox.findViewById(R.id.btn).setTag(Integer.valueOf(e.getID()));
			msgbox.findViewById(R.id.btn).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					final int id = (Integer)v.getTag();
					findViewById(id).setVisibility(View.GONE);
				}
			});
		}
		((TextView)msgbox.findViewById(R.id.descr)).setText(e.getStringRes(this));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (menuInfo instanceof TileView.PoiMenuInfo) {
			final TileView.PoiMenuInfo info = (TileView.PoiMenuInfo)menuInfo;
			if (info.EventGeoPoint != null) {
				if (info.MarkerIndex > NO_TAP) {
					mMarkerIndex = info.MarkerIndex;
					if (info.MarkerIndex >= 0) {
						menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
						menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
						menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
					}
					menu.add(0, R.id.menu_share, 0, getText(R.string.menu_share));
					menu.add(0, R.id.menu_toradar, 0, getText(R.string.menu_toradar));
				}
				else {
					menu.add(0, R.id.menu_addpoi, 0, getText(R.string.menu_addpoi));
					menu.add(0, R.id.menu_i_am_here, 0, getText(R.string.menu_i_am_here));
					menu.add(0, R.id.menu_add_target_point, 0, getText(R.string.menu_add_target_point));
					if (mMyLocationOverlay.getTargetLocation() != null)
						menu.add(0, R.id.menu_remove_target_point, 0, getText(R.string.menu_remove_target_point));
				}
			}
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getGroupId()) {
			case R.id.isoverlay: {
				final String overlayid = (String)item.getTitleCondensed();
				setTileSource(mTileSource.ID, overlayid, true);
				fillOverlays();
				setTitle();
				break;
			}
			case R.id.menu_dashboard_edit: {
				final IndicatorViewMenuInfo info = (IndicatorViewMenuInfo)item.getMenuInfo();
				final IndicatorView iv = info.IndicatorView;
				mIndicatorManager.putTagToIndicatorView(iv, item.getTitleCondensed().toString());
				mMap.invalidate(); //postInvalidate();
				break;
			}
			default: {
				switch (item.getItemId()) {
					case R.id.clear: {
						mMeasureOverlay.Clear();
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_dashboard_delete: {
						final IndicatorViewMenuInfo info = (IndicatorViewMenuInfo)item.getMenuInfo();
						final IndicatorView iv = info.IndicatorView;
						mIndicatorManager.removeIndicatorView(this, iv);
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_add_target_point: {
						TileView.PoiMenuInfo info = (TileView.PoiMenuInfo)item.getMenuInfo();
						mMyLocationOverlay.setTargetLocation(info.EventGeoPoint);
						if (mIndicatorManager != null)
							mIndicatorManager.setTargetLocation(info.EventGeoPoint);
						mMap.invalidate();
						break;
					}
					case R.id.menu_remove_target_point: {
						mMyLocationOverlay.setTargetLocation(null);
						if (mIndicatorManager != null)
							mIndicatorManager.setTargetLocation(null);
						mMap.invalidate();
						break;
					}
					case R.id.menu_dashboard_add: {
						final IndicatorViewMenuInfo info = (IndicatorViewMenuInfo)item.getMenuInfo();
						final IndicatorView iv = info.IndicatorView;
						mIndicatorManager.addIndicatorView(this, iv, iv.getIndicatorTag(), false);
						mMap.invalidate(); //postInvalidate();

						break;
					}
					case R.id.menu_dashboard_add_line: {
						final IndicatorViewMenuInfo info = (IndicatorViewMenuInfo)item.getMenuInfo();
						final IndicatorView iv = info.IndicatorView;
						mIndicatorManager.addIndicatorView(this, iv, iv.getIndicatorTag(), true);
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_undo: {
						mMeasureOverlay.Undo();
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_showinfo: {
						item.setChecked(!item.isChecked());
						final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
						Editor editor = pref.edit();
						editor.putBoolean("pref_show_measure_info", item.isChecked());
						editor.commit();
						mMeasureOverlay.setShowInfoBubble(item.isChecked());
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_showlineinfo: {
						item.setChecked(!item.isChecked());
						final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
						Editor editor = pref.edit();
						editor.putBoolean("pref_show_measure_line_info", item.isChecked());
						editor.commit();
						mMeasureOverlay.setShowLineInfo(item.isChecked());
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_addmeasurepoint: {
						mMeasureOverlay.addPointOnCenter(mMap.getTileView());
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.hide_overlay: {
						setTileSource(mTileSource.ID, mOverlayId, false);
						fillOverlays();
						setTitle();
						break;
					}
					case R.id.menu_i_am_here: {
						final Location loc = new Location("gps");
						TileView.PoiMenuInfo info = (TileView.PoiMenuInfo)item.getMenuInfo();
						loc.setLatitude(info.EventGeoPoint.getLatitude());
						loc.setLongitude(info.EventGeoPoint.getLongitude());
						mMyLocationOverlay.setLocation(loc);
						mSearchResultOverlay.setLocation(loc);
						if (mIndicatorManager != null)
							mIndicatorManager.setLocation(loc);
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_addpoi: {
						TileView.PoiMenuInfo info = (TileView.PoiMenuInfo)item.getMenuInfo(); //).EventGeoPoint;
						startActivityForResult((new Intent(this, PoiActivity.class))
							.putExtra(LAT, info.EventGeoPoint.getLatitude())
							.putExtra(LON, info.EventGeoPoint.getLongitude())
							.putExtra("alt", info.Elevation)
							.putExtra("title", "POI"), R.id.menu_addpoi);
						break;
					}
					case R.id.menu_editpoi: {
						startActivityForResult((new Intent(this, PoiActivity.class)).putExtra("pointid", mMarkerIndex),
							R.id.menu_editpoi);
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_deletepoi: {
						final int pointid = mPoiOverlay.getPoiPoint(mMarkerIndex).getId();
						new AlertDialog.Builder(this)
							.setTitle(R.string.app_name)
							.setMessage(getResources().getString(R.string.question_delete, getText(R.string.poi)))
							.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mPoiManager.deletePoi(pointid);
									mPoiOverlay.UpdateList();
									mMap.invalidate(); //postInvalidate();
								}
							}).setNegativeButton(R.string.no, null).create().show();
						break;
					}
					case R.id.menu_hide: {
						final PoiPoint poi = mPoiOverlay.getPoiPoint(mMarkerIndex);
						poi.mHidden = true;
						mPoiManager.updatePoi(poi);
						mPoiOverlay.UpdateList();
						mMap.invalidate(); //postInvalidate();
						break;
					}
					case R.id.menu_share: {
						try {
							final PoiPoint poi = mPoiOverlay.getPoiPoint(mMarkerIndex);
							final GeoPoint point = poi.mGeoPoint;
							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.setType("text/plain");
							intent.putExtra(Intent.EXTRA_TEXT, new StringBuilder()
								.append(poi.mTitle)
								.append('\n')
								.append("http://www.openstreetmap.org/?mlat=")
								.append(point.getLatitude())
								.append("&mlon=")
								.append(point.getLongitude())
								.append("#map=")
								.append(16) // zoom
								.append('/')
								.append(point.getLatitude())
								.append('/')
								.append(point.getLongitude())
								.append("&layers=T")
								.toString());
							startActivity(intent);
						}
						catch (Exception e) {
							Ut.e(e.toString(), e);
						}
						break;
					}
					case R.id.menu_toradar: {
						final PoiPoint poi1 = mPoiOverlay.getPoiPoint(mMarkerIndex);
						try {
							Intent i = new Intent("com.google.android.radar.SHOW_RADAR");
							i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							i.putExtra(NAME, poi1.mTitle);
							i.putExtra(LATITUDE, poi1.mGeoPoint.getLatitudeE6() / 1000000f);
							i.putExtra(LONGITUDE, poi1.mGeoPoint.getLongitudeE6() / 1000000f);
							startActivity(i);
						}
						catch (Exception e) {
							Toast.makeText(this, R.string.message_noradar, Toast.LENGTH_LONG).show();
							Ut.e(e.toString(), e);
						}
						break;
					}
					default: {
					}
				}
				break;
			}
		}
		final ContextMenuInfo menuInfo = item.getMenuInfo();
		if (menuInfo != null && menuInfo instanceof TileView.PoiMenuInfo) {
			((TileView.PoiMenuInfo)menuInfo).EventGeoPoint = null;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case R.id.whatsnew: {
				return new AlertDialog.Builder(this)
					//.setIcon( R.drawable.alert_dialog_icon)
					.setTitle(R.string.about_dialog_whats_new)
					.setMessage(R.string.whats_new_dialog_text)
					.setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							/* User clicked Cancel so do some stuff */
						}
					}).create();
			}
			case R.id.about: {
				return new AlertDialog.Builder(this)
					//.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.menu_about)
					.setMessage(getText(R.string.app_name) + " v." + Ut.getAppVersion(this) + "\n\n"
						+ getText(R.string.about_dialog_text))
					.setPositiveButton(R.string.about_dialog_whats_new, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							showDialog(R.id.whatsnew);
						}
					}).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							/* User clicked Cancel so do some stuff */
						}
					}).create();
			}
			case R.id.error: {
				return new AlertDialog.Builder(this)
					//.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.error_title)
					.setMessage(getText(R.string.error_text))
					.setPositiveButton(R.string.error_send, new DialogInterface.OnClickListener() {
						@SuppressWarnings("static-access")
						public void onClick(DialogInterface dialog, int whichButton) {
							SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
							String text = settings.getString("error", "");
							String subj = "Tabulae error: ";
							try {
								final String[] lines = text.split("\n", 2);
								final Pattern p = Pattern.compile("[.][\\w]+[:| |\\t|\\n]");
								final Matcher m = p.matcher(lines[0] + "\n");
								if (m.find())
									subj += m.group().replace(".", "").replace(":", "").replace("\n", "") + " at ";
								final Pattern p2 = Pattern.compile("[.][\\w]+[(][\\w| |\\t]*[)]");
								final Matcher m2 = p2.matcher(lines[1]);
								if (m2.find())
									subj += m2.group().substring(2);
							}
							catch (Exception ignored) {
								Ut.e(ignored.toString(), ignored);
							}
							final Build b = new Build();
							final Build.VERSION v = new Build.VERSION();
							text = "Your message:"
								+ "\n\nTabulae: " + Ut.getAppVersion(MainActivity.this)
								+ "\nAndroid: " + v.RELEASE
								+ "\nDevice: " + b.BOARD + " " + b.BRAND + " " + b.DEVICE +/*" "+b.MANUFACTURER+*/" " + b.MODEL + " " + b.PRODUCT
								+ "\n\n" + text;
							startActivity(Ut.SendMail(subj, text));
							SharedPreferences uiState = getPreferences(0);
							SharedPreferences.Editor editor = uiState.edit();
							editor.putString("error", "");
							editor.commit();

						}
					}).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							SharedPreferences uiState = getPreferences(0);
							SharedPreferences.Editor editor = uiState.edit();
							editor.putString("error", "");
							editor.commit();
						}
					}).create();
			}
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case R.id.menu_editpoi:
			case R.id.menu_addpoi: {
				mPoiOverlay.UpdateList();
				mMap.invalidate(); //postInvalidate();
				break;
			}
			case R.id.poilist: {
				if (resultCode == RESULT_OK) {
					PoiPoint point = mPoiManager.getPoiPoint(data.getIntExtra("pointid", EMPTY_ID));
					if (point != null) {
						setAutoFollow(false);
						mPoiOverlay.UpdateList();
						mMap.setCenter(point.mGeoPoint);
					}
				}
				else {
					mPoiOverlay.UpdateList();
					mMap.invalidate(); //postInvalidate();
				}
				break;
			}
			case R.id.tracks: {
				if (resultCode == RESULT_OK) {
					Track track = mPoiManager.getTrack(data.getIntExtra("trackid", EMPTY_ID));
					if (track != null) {
						setAutoFollow(false);
						mMap.setCenter(track.getBeginGeoPoint());
					}
				}
				break;
			}
			case R.id.settings_activity_closed: {
				finish();
				startActivity(new Intent(this, getClass()));
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private float updateBearing(float newBearing) {
		float dif = newBearing - mLastBearing;
		// find difference between new and current position
		if (Math.abs(dif) > 180)
			dif = 360 - dif;
		// if difference is bigger than 180 degrees,
		// it's faster to rotate in opposite direction
		if (Math.abs(dif) < 1)
			return mLastBearing;
		// if difference is less than 1 degree, leave things as is
		if (Math.abs(dif) >= 90)
			return mLastBearing = newBearing;
		// if difference is bigger than 90 degress, just update it
		mLastBearing += 90 * Math.signum(dif) * Math.pow(Math.abs(dif) / 90, 2);
		// bearing is updated proportionally to the square of the difference
		// value
		// sign of difference is paid into account
		// if difference is 90(max. possible) it is updated exactly by 90
		while (mLastBearing > 360)
			mLastBearing -= 360;
		while (mLastBearing < 0)
			mLastBearing += 360;
		// prevent bearing overrun/underrun
		return mLastBearing;
	}

	private void doShowPoints(Intent queryIntent) {
		final ArrayList<String> locations = queryIntent.getStringArrayListExtra("locations");
		if (!locations.isEmpty()) {
			GeoPoint point = null;
			mPoiOverlay.clearPoiList();
			int id = -1;
			for (String location : locations) {
				final String[] fields = location.split(";");
				String locns = "", title = "", descr = "";
				if (fields.length > 0) locns = fields[0];
				if (fields.length > 1) title = fields[1];
				if (fields.length > 2) descr = fields[2];

				point = GeoPoint.fromDoubleString(locns);
				mPoiOverlay.setGpsStatusGeoPoint(id--, point, title, descr);
			}
			setAutoFollow(false);
			if (point != null)
				mMap.setCenter(point);
		}
	}

	private class SampleLocationListener implements LocationListener {
		public static final String OFF = "off";

		public void onLocationChanged(Location loc) {
			Ut.d("onLocationChanged loc=" + loc);
			mMyLocationOverlay.setLocation(loc);
			mSearchResultOverlay.setLocation(loc);
			if (loc.getProvider().equals(LocationManager.GPS_PROVIDER) && mNetListener != null) {
				getLocationManager().removeUpdates(mNetListener);
				mNetListener = null;
				mStatusLocationProviderName = LocationManager.GPS_PROVIDER;
				Ut.d(LocationManager.NETWORK_PROVIDER + " removed");
				// TODO when to reenable?
			}
			//int cnt = loc.getExtras().getInt("satellites", Integer.MIN_VALUE);
			mStatusLocationProviderName = loc.getProvider(); // + " 2 " + (cnt >= 0 ? cnt : 0);
			setTitle();
			mLastSpeed = loc.getSpeed();
			if (mAutoFollow) {
				if (mDrivingDirectionUp)
					if (loc.getSpeed() > 0.5)
						mMap.setBearing(loc.getBearing());
				mMap.setCenter(TypeConverter.locationToGeoPoint(loc));
			}
			mMap.invalidate();
			setTitle();
		}

		public void onProviderDisabled(String provider) {
			Ut.d("onProviderDisabled provider=" + provider);
			if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER) && mNetListener != null)
				mStatusLocationProviderName = LocationManager.NETWORK_PROVIDER;
			else
				mStatusLocationProviderName = OFF;
			if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER) && mNetListener != null) {
				getLocationManager().removeUpdates(mNetListener);
				mNetListener = null;
				if (getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER))
					mStatusLocationProviderName = LocationManager.GPS_PROVIDER;
				else
					mStatusLocationProviderName = OFF;
			}
			setTitle();
		}

		public void onProviderEnabled(String provider) {
			Ut.d("onProviderEnabled provider=" + provider);
			if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER) && mNetListener == null)
				mStatusLocationProviderName = LocationManager.GPS_PROVIDER;
			setTitle();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Ut.d("onStatusChanged provider=" + provider);
			mGpsStatusSatCnt = extras.getInt("satellites", Integer.MIN_VALUE);
			mGpsStatusState = status;
			mStatusLocationProviderName = provider;
			Ut.d(provider + " status: " + status + " cnt: " + extras.getInt("satellites", Integer.MIN_VALUE));
			setTitle();
		}

		private LocationManager getLocationManager() {
			return (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		}

		private void getBestProvider() {
			int minTime = 0;
			int minDistance = 0;
			final LocationManager lm = getLocationManager();
			final List<String> listProviders = lm.getAllProviders();
			mStatusLocationProviderName = OFF;
			if (!mGPSFastUpdate) {
				minTime = 2000;
				minDistance = 20;
			}
			lm.removeUpdates(mLocationListener);
			if (mNetListener != null)
				lm.removeUpdates(mNetListener);
			if (listProviders.contains(LocationManager.GPS_PROVIDER)) {
				Ut.d("SATELLITE Provider available");
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);
				if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
					mStatusLocationProviderName = LocationManager.GPS_PROVIDER;

				try {
					if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						Ut.d("NETWORK Provider Enabled");
						mNetListener = new SampleLocationListener();
						lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mNetListener);
						mStatusLocationProviderName = LocationManager.NETWORK_PROVIDER;
					}
				}
				catch (Exception e) {
					Ut.e(e.toString(), e);
				}

			}
			else if (listProviders.contains(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Ut.d("only NETWORK Provider Enabled");
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mLocationListener);
				mStatusLocationProviderName = LocationManager.NETWORK_PROVIDER;
			}
			else {
				Ut.d("NO Provider Enabled");
			}
			setTitle();
		}
	}
}
