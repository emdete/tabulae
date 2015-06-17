package org.pyneo.maps.track;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.pyneo.maps.R;
import org.pyneo.maps.poi.PoiManager;
import org.pyneo.maps.track.Track.TrackPoint;
import org.pyneo.maps.utils.SimpleThreadFactory;
import org.pyneo.maps.utils.SimpleXML;
import org.pyneo.maps.utils.Ut;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.TimeZone;

public class TrackListActivity extends ListActivity implements Constants {
	private PoiManager mPoiManager;

	private ProgressDialog dlgWait;
	private SimpleInvalidationHandler mHandler;
	private boolean mNeedTracksStatUpdate = false;
	private ExecutorService mThreadExecutor = null;
	private int mUnits = 0;
	private String mSortOrder;
	private SimpleCursorAdapter.ViewBinder mViewBinder = new CheckBoxViewBinder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.track_list);
		registerForContextMenu(getListView());
		mPoiManager = new PoiManager(this);
		mSortOrder = "trackid DESC";

		mHandler = new SimpleInvalidationHandler();

		findViewById(R.id.startButton)
			.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					startService(new Intent(TrackListActivity.this, TrackWriterService.class));
					finish();
				}
			});
		findViewById(R.id.pauseButton)
			.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					stopService(new Intent(TrackListActivity.this, TrackWriterService.class));
					finish();
				}
			});
		findViewById(R.id.stopButton)
			.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					stopService(new Intent(TrackListActivity.this, TrackWriterService.class));
					doSaveTrack();
				}
			});

		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
		final int versionDataUpdate = settings.getInt(VERSION_DATA_UPDATE, 0);
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		mUnits = Integer.parseInt(pref.getString(PREF_UNITS, "0"));

		if (versionDataUpdate < 8) {
			mNeedTracksStatUpdate = true;
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(VERSION_DATA_UPDATE, 8);
			editor.commit();
		}

	}

	@Override
	protected void onDestroy() {
		if (mThreadExecutor != null)
			mThreadExecutor.shutdown();
		super.onDestroy();
		mPoiManager.FreeDatabases();
	}

	private void doSaveTrack() {
		dlgWait = Ut.ShowWaitDialog(this, 0);
		if (mThreadExecutor == null)
			mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("doSaveTrack"));

		this.mThreadExecutor.execute(new Runnable() {
			public void run() {
				SQLiteDatabase db = null;
				File folder = Ut.getAppMainDir(TrackListActivity.this, DATA);
				if (folder.canRead()) {
					try {
						db = new DatabaseHelper(TrackListActivity.this, folder.getAbsolutePath() + '/' + WRITE_TRACK_DB).getWritableDatabase();
					}
					catch (Exception e) {
						db = null;
					}
				}
				int res = 0;
				if (db != null) {
					try {
						res = mPoiManager.saveTrackFromWriter(db);
					}
					catch (Exception e) {
						Ut.e(e.toString(), e);
					}
					db.close();
					if (res > 0) {
						Track tr = mPoiManager.getTrack(res);
						tr.CalculateStat();
						mPoiManager.updateTrack(tr);
					}
				}
				dlgWait.dismiss();
				Message.obtain(mHandler, R.id.tracks, res, 0).sendToTarget();
			}
		});
	}

	private void doJoinTracks() {
		dlgWait = Ut.ShowWaitDialog(this, 0);
		if (mThreadExecutor == null)
			mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("doSaveTrack"));

		this.mThreadExecutor.execute(new Runnable() {
			public void run() {
				int res = -1;
				try {
					res = (int)mPoiManager.JoinTracks();
				}
				catch (Exception e) {
					Ut.e(e.toString(), e);
				}
				if (res > 0) {
					Track tr = mPoiManager.getTrack(res);
					tr.CalculateStat();
					mPoiManager.updateTrack(tr);
				} else {
					res = 0; // Nothing to save
				}
				dlgWait.dismiss();
				Message.obtain(mHandler, R.id.tracks, res, 0).sendToTarget();
			}
		});
	}

	@Override
	protected void onPause() {
		SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = uiState.edit();
		editor.putString(SORT_ORDER, mSortOrder);
		editor.commit();
		super.onPause();
	}

	@Override
	protected void onResume() {
		final SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		mSortOrder = uiState.getString(SORT_ORDER, mSortOrder);
		FillData();
		super.onResume();
	}

	private void FillData() {
		Cursor c = mPoiManager.getTrackListCursor(mUnits == 0? getResources().getString(R.string.km): getResources().getString(R.string.ml), mSortOrder);
		if (mNeedTracksStatUpdate) {
			mNeedTracksStatUpdate = false;
			if (c != null) {
				if (c.moveToFirst()) {
					if (c.getInt(8) == -1) {
						UpdateTracksStat();
					}
				}
			}
		}
		if (c != null) {
			startManagingCursor(c);
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.track_list_item
				, c,
				new String[]{NAME, TITLE2, SHOW, CNT, DISTANCE + mUnits, DURATION, UNITS/*, "descr"*/},
				new int[]{R.id.title1, R.id.title2, R.id.checkbox, R.id.data_value1, R.id.data_value2, R.id.data_value3, R.id.data_unit2 /*, R.id.descr*/});
			adapter.setViewBinder(mViewBinder);
			setListAdapter(adapter);
		}
	}

	private void UpdateTracksStat() {
		if (mThreadExecutor == null)
			mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("UpdateTracksStat"));
		dlgWait = Ut.ShowWaitDialog(this, 0);
		mThreadExecutor.execute(new Runnable() {

			public void run() {
				Cursor c = mPoiManager.getTrackListCursor(EMPTY, TRACKID + " DESC");
				if (c != null) {
					if (c.moveToFirst()) {
						Track tr = null;
						do {
							tr = mPoiManager.getTrack(c.getInt(3));
							if (tr != null) {
								tr.Category = 0;
								tr.Activity = 0;
								final List<Track.TrackPoint> tps = tr.getPoints();
								if (tps.size() > 0) {
									tr.Date = tps.get(0).getDate();
								}
								tr.CalculateStat();
								mPoiManager.updateTrack(tr);
							}
						} while (c.moveToNext());
					}
					c.close();
				}

				TrackListActivity.this.dlgWait.dismiss();
				Message.obtain(mHandler, R.id.about, 0, 0).sendToTarget();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.track_list, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.menu_importpoi) {
			startActivity((new Intent(this, ImportTrackActivity.class)));
			return true;
		} else if (item.getItemId() == R.id.menu_sort_name) {
			if (mSortOrder.contains("tracks.name")) {
				if (mSortOrder.contains("asc"))
					mSortOrder = "tracks.name desc";
				else
					mSortOrder = "tracks.name asc";
			} else {
				mSortOrder = "tracks.name asc";
			}
			((SimpleCursorAdapter)getListAdapter()).changeCursor(mPoiManager.getTrackListCursor(mUnits == 0? getResources().getString(R.string.km): getResources().getString(R.string.ml), mSortOrder));
		} else if (item.getItemId() == R.id.menu_sort_category) {
			if (mSortOrder.contains("activity.name")) {
				if (mSortOrder.contains("asc"))
					mSortOrder = "activity.name desc";
				else
					mSortOrder = "activity.name asc";
			} else {
				mSortOrder = "activity.name asc";
			}
			((SimpleCursorAdapter)getListAdapter()).changeCursor(mPoiManager.getTrackListCursor(mUnits == 0? getResources().getString(R.string.km): getResources().getString(R.string.ml), mSortOrder));
		} else if (item.getItemId() == R.id.menu_sort_date) {
			if (mSortOrder.contains("date")) {
				if (mSortOrder.contains("asc"))
					mSortOrder = "date desc";
				else
					mSortOrder = "date asc";
			} else {
				mSortOrder = "date asc";
			}
			((SimpleCursorAdapter)getListAdapter()).changeCursor(mPoiManager.getTrackListCursor(mUnits == 0? getResources().getString(R.string.km): getResources().getString(R.string.ml), mSortOrder));
		} else if (item.getItemId() == R.id.menu_join) {
			doJoinTracks();
		}

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenuInfo menuInfo) {

		menu.add(0, R.id.menu_gotopoi, 0, getText(R.string.menu_goto_track));
		menu.add(0, R.id.menu_stat, 0, getText(R.string.menu_stat));
		menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
		menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
		menu.add(0, R.id.menu_share, 0, getText(R.string.menu_share));
		menu.add(0, R.id.menu_exporttogpxpoi, 0, getText(R.string.menu_exporttogpx));
		menu.add(0, R.id.menu_exporttokmlpoi, 0, getText(R.string.menu_exporttokml));

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int id = (int)((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).id;

		if (item.getItemId() == R.id.menu_stat) {
			startActivity((new Intent(this, TrackStatActivity.class)).putExtra(ID, id));
		} else if (item.getItemId() == R.id.menu_editpoi) {
			startActivity((new Intent(this, TrackActivity.class)).putExtra(ID, id));
		} else if (item.getItemId() == R.id.menu_gotopoi) {
			setResult(RESULT_OK, (new Intent()).putExtra(TRACKID, id));
			finish();
		} else if (item.getItemId() == R.id.menu_deletepoi) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.app_name)
				.setMessage(getResources().getString(R.string.question_delete, getText(R.string.track)))
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mPoiManager.deleteTrack(id);
						((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
					}
				}).setNegativeButton(R.string.no, null).create().show();

		} else if (item.getItemId() == R.id.menu_share) {
			File file = getTrackExportFileName(id, ".gpx");
			if (file.exists()) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("application/gpx+xml");
				intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
				startActivity(intent);
			}
			else {
				Toast.makeText(this, R.string.trackwriter_export_before_send, Toast.LENGTH_LONG).show();
			}
		} else if (item.getItemId() == R.id.menu_exporttogpxpoi) {
			doExportTrackGPX(id);
		} else if (item.getItemId() == R.id.menu_exporttokmlpoi) {
			doExportTrackKML(id);
		}

		return super.onContextItemSelected(item);
	}

	private void doExportTrackKML(int id) {
		dlgWait = Ut.ShowWaitDialog(this, 0);
		final int trackid = id;
		if (mThreadExecutor == null)
			mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("doExportTrackKML"));
		this.mThreadExecutor.execute(new Runnable() {
			public void run() {
				final Track track = mPoiManager.getTrack(trackid);
				SimpleXML xml = new SimpleXML(KML);
				xml.setAttr("xmlns:gx", "http://www.google.com/kml/ext/2.2");
				xml.setAttr("xmlns", "http://www.opengis.net/kml/2.2");
				SimpleXML Placemark = xml.createChild("Placemark");
				Placemark.createChild(NAME).setText(track.Name);
				Placemark.createChild(DESCRIPTION).setText(track.Descr);
				SimpleXML LineString = Placemark.createChild(LINE_STRING);
				SimpleXML coordinates = LineString.createChild(COORDINATES);
				StringBuilder builder = new StringBuilder();
				for (TrackPoint tp : track.getPoints()) {
					builder.append(tp.getLon()).append(",").append(tp.getLat()).append(",").append(tp.getAlt()).append(" ");
				}
				coordinates.setText(builder.toString().trim());
				File file = getTrackExportFileName(trackid, ".kml");
				FileOutputStream out;
				try {
					file.createNewFile();
					out = new FileOutputStream(file);
					OutputStreamWriter wr = new OutputStreamWriter(out);
					wr.write(SimpleXML.saveXml(xml));
					wr.close();
					Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0, file.toString()).sendToTarget();
				}
				catch (FileNotFoundException e) {
					Ut.e(e.toString(), e);
					Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
				}
				catch (IOException e) {
					Ut.e(e.toString(), e);
					Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
				}
				dlgWait.dismiss();
			}
		});
	}

	private void doExportTrackGPX(final int trackid) {
		dlgWait = Ut.ShowWaitDialog(this, 0);
		if (mThreadExecutor == null)
			mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("doExportTrackGPX"));
		this.mThreadExecutor.execute(new Runnable() {
			public void run() {
				final Track track = mPoiManager.getTrack(trackid);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
				SimpleXML xml = new SimpleXML(GPX);
				xml.setAttr("xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd");
				xml.setAttr("xmlns", "http://www.topografix.com/GPX/1/0");
				xml.setAttr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				xml.setAttr("creator", "Tabulae - https://github.com/emdete/Tabulae");
				xml.setAttr("version", "1.0");
				xml.createChild(NAME).setText(track.Name);
				xml.createChild(DESC).setText(track.Descr);
				SimpleXML trk = xml.createChild(TRK);
				SimpleXML trkseg = trk.createChild(TRKSEG);
				SimpleXML trkpt = null;
				for (TrackPoint tp : track.getPoints()) {
					trkpt = trkseg.createChild(TRKPT);
					trkpt.setAttr(LAT, Double.toString(tp.getLat()));
					trkpt.setAttr(LON, Double.toString(tp.getLon()));
					trkpt.createChild("ele").setText(Double.toString(tp.getAlt()));
					trkpt.createChild("time").setText(formatter.format(tp.getDate()));
				}
				File file = getTrackExportFileName(trackid, ".gpx");
				FileOutputStream out;
				try {
					file.createNewFile();
					out = new FileOutputStream(file);
					OutputStreamWriter wr = new OutputStreamWriter(out);
					wr.write(SimpleXML.saveXml(xml));
					wr.close();
					Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 1, 0, file.toString()).sendToTarget();
				}
				catch (FileNotFoundException e) {
					Ut.e(e.toString(), e);
					Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
				}
				catch (IOException e) {
					Ut.e(e.toString(), e);
					Message.obtain(mHandler, R.id.menu_exporttogpxpoi, 0, 0, e.getMessage()).sendToTarget();
				}

				dlgWait.dismiss();
			}
		});

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mPoiManager.setTrackChecked((int)id);

		final CheckBox ch = (CheckBox)v.findViewById(R.id.checkbox);
		ch.setChecked(!ch.isChecked());
		((SimpleCursorAdapter)getListAdapter()).getCursor().requery();

		super.onListItemClick(l, v, position, id);
	}

	private class SimpleInvalidationHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			if (msg.what == R.id.about) {
				((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
			} else if (msg.what == R.id.tracks) {
				if (msg.arg1 == 0)
					Toast.makeText(TrackListActivity.this, R.string.trackwriter_nothing, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(TrackListActivity.this, R.string.trackwriter_saved, Toast.LENGTH_LONG).show();

				((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
			} else if (msg.what == R.id.menu_exporttogpxpoi) {
				if (msg.arg1 == 0)
					Toast
						.makeText(TrackListActivity.this,
							getString(R.string.message_error) + " " + msg.obj,
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(TrackListActivity.this,
						getString(R.string.message_trackexported) + " " + msg.obj,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private class CheckBoxViewBinder implements SimpleCursorAdapter.ViewBinder {
		private static final String SHOW = org.pyneo.maps.Constants.SHOW;

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (cursor.getColumnName(columnIndex).equalsIgnoreCase(SHOW)) {
				((CheckBox)view.findViewById(R.id.checkbox)).setChecked(cursor.getInt(columnIndex) == 1);
				return true;
			}
			return false;
		}

	}

	private final File getTrackExportFileName(int id, String ext) {
		return new File(Ut.getAppExportDir(this).getAbsolutePath(), "track" + id + ext); // TODO: use iso date in name
	}
}