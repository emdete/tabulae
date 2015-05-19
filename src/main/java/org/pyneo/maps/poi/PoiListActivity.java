package org.pyneo.maps.poi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import org.pyneo.maps.R;
import org.pyneo.maps.utils.SimpleXML;
import org.pyneo.maps.utils.CoordFormatter;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.utils.GeoPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class PoiListActivity extends ListActivity implements Constants {
	private PoiManager mPoiManager;
	private ProgressDialog dlgWait;
	private String mSortOrder;
	private SimpleCursorAdapter.ViewBinder mViewBinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.poi_list);
		registerForContextMenu(getListView());
		mPoiManager = new PoiManager(this);
		mSortOrder = "lat asc, lon asc";
		mViewBinder = new PoiViewBinder();
	}

	@Override
	protected void onDestroy() {
		mPoiManager.FreeDatabases();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = uiState.edit();
		editor.putString("sort_order", mSortOrder);
		editor.commit();
		super.onPause();
	}

	@Override
	protected void onResume() {
		final SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		mSortOrder = uiState.getString("sort_order", mSortOrder);

		FillData();
		super.onResume();
	}

	private void FillData() {
		Cursor c = mPoiManager.getPoiListCursor(mSortOrder);
		startManagingCursor(c);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
			R.layout.poi_list_item, c,
			new String[]{"name", "iconid", "catname", "descr"},
			new int[]{R.id.title1, R.id.pic, R.id.title2, R.id.descr});
		adapter.setViewBinder(mViewBinder);
		setListAdapter(adapter);
		Ut.i("filled data");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.poilist_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.menu_addpoi) {
			final Intent PoiIntent = new Intent(this, PoiActivity.class);
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				PoiIntent.putExtra(LAT, extras.getDouble(LAT)).putExtra(LON, extras.getDouble(LON)).putExtra("title", extras.getString("title"));
			}
			startActivity(PoiIntent);
			return true;
		} else if (item.getItemId() == R.id.menu_categorylist) {
			startActivity((new Intent(this, PoiCategoryListActivity.class)));
			return true;
		} else if (item.getItemId() == R.id.menu_importpoi) {
			startActivity((new Intent(this, ImportPoiActivity.class)));
			return true;
		} else if (item.getItemId() == R.id.menu_delete) {
			showDialog(R.id.menu_delete);
			return true;
		} else if (item.getItemId() == R.id.menu_exportgpx) {
			DoExportGpx();
			return true;
		} else if (item.getItemId() == R.id.menu_exportkml) {
			DoExportKml();

		} else if (item.getItemId() == R.id.menu_sort_name) {
			if (mSortOrder.contains("p.name")) {
				if (mSortOrder.contains("asc"))
					mSortOrder = "p.name desc";
				else
					mSortOrder = "p.name asc";
			} else {
				mSortOrder = "p.name asc";
			}
			((SimpleCursorAdapter)getListAdapter()).changeCursor(mPoiManager.getPoiListCursor(mSortOrder));

		} else if (item.getItemId() == R.id.menu_sort_category) {
			if (mSortOrder.contains("c.name")) {
				if (mSortOrder.contains("asc"))
					mSortOrder = "c.name desc";
				else
					mSortOrder = "c.name asc";
			} else {
				mSortOrder = "c.name asc";
			}
			((SimpleCursorAdapter)getListAdapter()).changeCursor(mPoiManager.getPoiListCursor(mSortOrder));

		} else if (item.getItemId() == R.id.menu_sort_coord) {
			if (mSortOrder.contains(LAT)) {
				if (mSortOrder.contains("asc"))
					mSortOrder = "lat desc, lon desc";
				else
					mSortOrder = "lat asc, lon asc";
			} else {
				mSortOrder = "lat, lon asc";
			}
			((SimpleCursorAdapter)getListAdapter()).changeCursor(mPoiManager.getPoiListCursor(mSortOrder));
		}

		return true;
	}

	private void DoExportKml() {
		dlgWait = Ut.ShowWaitDialog(this, 0);

		new ExportKmlTask().execute();
	}

	private void DoExportGpx() {
		dlgWait = Ut.ShowWaitDialog(this, 0);

		new ExportGpxTask().execute();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == R.id.menu_delete) {
			return new AlertDialog.Builder(this)
				//.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.warning_delete_all_poi)
				.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							mPoiManager.deleteAllPoi();
							((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
						}
					}).setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

									/* User clicked Cancel so do some stuff */
						}
					}).create();
		}

		return super.onCreateDialog(id);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenuInfo menuInfo) {
		int pointid = (int)((AdapterView.AdapterContextMenuInfo)menuInfo).id;
		PoiPoint poi = mPoiManager.getPoiPoint(pointid);

		menu.add(0, R.id.menu_gotopoi, 0, getText(R.string.menu_goto));
		menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
		if (poi.mHidden)
			menu.add(0, R.id.menu_show, 0, getText(R.string.menu_show));
		else
			menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
		menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));
		menu.add(0, R.id.menu_share, 0, getText(R.string.menu_share));
		//menu.add(0, R.id.menu_toradar, 0, getText(R.string.menu_toradar));

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int pointid = (int)((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).id;
		PoiPoint poi = mPoiManager.getPoiPoint(pointid);

		if (item.getItemId() == R.id.menu_editpoi) {
			startActivity((new Intent(this, PoiActivity.class)).putExtra("pointid", pointid));
		} else if (item.getItemId() == R.id.menu_gotopoi) {
			setResult(RESULT_OK, (new Intent()).putExtra("pointid", pointid));
			finish();
		} else if (item.getItemId() == R.id.menu_deletepoi) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.app_name)
				.setMessage(getResources().getString(R.string.question_delete, getText(R.string.poi)))
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						mPoiManager.deletePoi(pointid);
						((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
					}
				}).setNegativeButton(R.string.no, null).create().show();

		} else if (item.getItemId() == R.id.menu_hide) {
			poi.mHidden = true;
			mPoiManager.updatePoi(poi);
			((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
		} else if (item.getItemId() == R.id.menu_show) {
			poi.mHidden = false;
			mPoiManager.updatePoi(poi);
			((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
		} else if (item.getItemId() == R.id.menu_share) {
			try {
				final GeoPoint point = poi.mGeoPoint;
				final String label = poi.mTitle;
				final int zoom = 16;
				double latitude = point.getLatitude();
				double longitude = point.getLongitude();
				Intent intent;
				if (false) {
					intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, new StringBuilder()
						.append(label)
						.append('\n')
						.append("http://www.openstreetmap.org/#map=")
						.append(zoom)
						.append('/')
						.append(latitude)
						.append('/')
						.append(longitude)
						.toString());
				}
				else {
					intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("geo:" + latitude + ',' + longitude + "?q=" + latitude + ',' + longitude + '(' + label + ')'));
				}
				startActivity(intent);
			}
			catch (Exception e) {
				Ut.e(e.toString(), e);
			}
		} else if (item.getItemId() == R.id.menu_toradar) {
			try {
				Intent i = new Intent("com.google.android.radar.SHOW_RADAR");
				i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				i.putExtra(NAME, poi.mTitle);
				i.putExtra(LATITUDE, poi.mGeoPoint.getLatitudeE6() / 1000000f);
				i.putExtra(LONGITUDE, poi.mGeoPoint.getLongitudeE6() / 1000000f);
				startActivity(i);
			}
			catch (Exception e) {
				Toast.makeText(this, R.string.message_noradar, Toast.LENGTH_LONG).show();
			}
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

	private class PoiViewBinder implements SimpleCursorAdapter.ViewBinder {
		private static final String CATNAME = "catname";
		private static final String ICONID = "iconid";
		private CoordFormatter mCf = new CoordFormatter(PoiListActivity.this.getApplicationContext());

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (cursor.getColumnName(columnIndex).equalsIgnoreCase(CATNAME)) {
				((TextView)view.findViewById(R.id.title2)).setText(cursor.getString(cursor.getColumnIndex(CATNAME))
					+ ", " + mCf.convertLat(cursor.getDouble(cursor.getColumnIndex(LAT)))
					+ ", " + mCf.convertLon(cursor.getDouble(cursor.getColumnIndex(LON)))
				);
				return true;
			}
			else if (cursor.getColumnName(columnIndex).equalsIgnoreCase(ICONID)) {
				int id = cursor.getInt(columnIndex);
				Ut.i("setViewValue find id=" + id);
				((ImageView)view.findViewById(R.id.pic)).setImageResource(PoiActivity.resourceFromPoiIconId(id));
				return true;
			}
			return false;
		}

	}

	class ExportKmlTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			SimpleXML xml = new SimpleXML("kml");
			xml.setAttr("xmlns:gx", "http://www.google.com/kml/ext/2.2");
			xml.setAttr("xmlns", "http://www.opengis.net/kml/2.2");
			SimpleXML fold = xml.createChild("Folder");

			Cursor c = mPoiManager.getPoiListCursor(LAT + ',' + LON);
			PoiPoint poi = null;

			if (c != null) {
				if (c.moveToFirst()) {
					do {
						poi = mPoiManager.getPoiPoint(c.getInt(4));

						SimpleXML wpt = fold.createChild("Placemark");
						wpt.createChild(Constants.NAME).setText(poi.mTitle);
						wpt.createChild(Constants.DESCRIPTION).setText(poi.mDescr);
						SimpleXML point = wpt.createChild("Point");
						point.createChild("coordinates").setText(new StringBuilder().append(poi.mGeoPoint.getLongitude()).append(",").append(poi.mGeoPoint.getLatitude()).toString());
						SimpleXML ext = wpt.createChild("ExtendedData");
						SimpleXML category = ext.createChild(Constants.CATEGORYID);
						final PoiCategory poiCat = mPoiManager.getPoiCategory(poi.mCategoryId);
						category.setAttr(Constants.CATEGORYID, Integer.toString(poiCat.getId()));
						category.setAttr(Constants.NAME, poiCat.Title);
						category.setAttr(Constants.ICONID, Integer.toString(poiCat.IconId));

					} while (c.moveToNext());
				}
				c.close();
			}

			File folder = Ut.getAppExportDir(PoiListActivity.this);
			String filename = folder.getAbsolutePath() + "/poilist.kml";
			File file = new File(filename);
			FileOutputStream out;
			try {
				file.createNewFile();
				out = new FileOutputStream(file);
				OutputStreamWriter wr = new OutputStreamWriter(out);
				wr.write(SimpleXML.saveXml(xml));
				wr.close();
				return PoiListActivity.this.getResources().getString(R.string.message_poiexported, filename);
			}
			catch (FileNotFoundException e) {
				Ut.e(e.toString(), e);
				return PoiListActivity.this.getResources().getString(R.string.message_error, e.getMessage());
			}
			catch (IOException e) {
				Ut.e(e.toString(), e);
				return PoiListActivity.this.getResources().getString(R.string.message_error, e.getMessage());
			}

		}

		@Override
		protected void onPostExecute(String result) {
			dlgWait.dismiss();
			Toast.makeText(PoiListActivity.this, result, Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
		}

	}

	class ExportGpxTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			SimpleXML xml = new SimpleXML("gpx");
			xml.setAttr("xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd");
			xml.setAttr("xmlns", "http://www.topografix.com/GPX/1/0");
			xml.setAttr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			xml.setAttr("creator", "Tabulae - https://github.com/emdete/Tabulae");
			xml.setAttr("version", "1.0");

			Cursor c = mPoiManager.getPoiListCursor(LAT + ',' + LON);
			PoiPoint poi = null;

			if (c != null) {
				if (c.moveToFirst()) {
					do {
						poi = mPoiManager.getPoiPoint(c.getInt(4));

						SimpleXML wpt = xml.createChild("wpt");
						wpt.setAttr(Constants.LAT, Double.toString(poi.mGeoPoint.getLatitude()));
						wpt.setAttr(Constants.LON, Double.toString(poi.mGeoPoint.getLongitude()));
						wpt.createChild(Constants.ELE).setText(Double.toString(poi.mAlt));
						wpt.createChild(Constants.NAME).setText(poi.mTitle);
						wpt.createChild(Constants.DESC).setText(poi.mDescr);
						wpt.createChild(Constants.TYPE).setText(mPoiManager.getPoiCategory(poi.mCategoryId).Title);
						SimpleXML ext = wpt.createChild("extensions");
						SimpleXML category = ext.createChild(Constants.CATEGORYID);
						final PoiCategory poiCat = mPoiManager.getPoiCategory(poi.mCategoryId);
						category.setAttr(Constants.CATEGORYID, Integer.toString(poiCat.getId()));
						category.setAttr(Constants.NAME, poiCat.Title);
						category.setAttr(Constants.ICONID, Integer.toString(poiCat.IconId));

					} while (c.moveToNext());
				}
				c.close();
			}

			File folder = Ut.getAppExportDir(PoiListActivity.this);
			String filename = folder.getAbsolutePath() + "/poilist.gpx";
			File file = new File(filename);
			FileOutputStream out;
			try {
				file.createNewFile();
				out = new FileOutputStream(file);
				OutputStreamWriter wr = new OutputStreamWriter(out);
				wr.write(SimpleXML.saveXml(xml));
				wr.close();
				return PoiListActivity.this.getResources().getString(R.string.message_poiexported, filename);
			}
			catch (FileNotFoundException e) {
				Ut.e(e.toString(), e);
				return PoiListActivity.this.getResources().getString(R.string.message_error, e.getMessage());
			}
			catch (IOException e) {
				Ut.e(e.toString(), e);
				return PoiListActivity.this.getResources().getString(R.string.message_error, e.getMessage());
			}
		}

		@Override
		protected void onPostExecute(String result) {
			dlgWait.dismiss();
			Toast.makeText(PoiListActivity.this, result, Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
		}

	}

}
