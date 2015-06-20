package org.pyneo.maps.poi;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.pyneo.maps.R;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.utils.TableE;
import org.pyneo.maps.utils.CoordFormatter;

import org.pyneo.maps.utils.GeoPoint;

import java.util.Locale;

public class PoiActivity extends Activity implements Constants {
	EditText mTitle, mLat, mLon, mDescr, mAlt;
	Spinner mSpinner;
	CheckBox mHidden;
	private PoiPoint mPoiPoint;
	private PoiManager mPoiManager;
	private CoordFormatter mCf;

	public static int resourceFromPoiIconId(int id) {
		if (id >= 0 && id < POI_ICON_RESOURCE_IDS.length)
			return POI_ICON_RESOURCE_IDS[id];
		Ut.e("resourceFromPoiIconId find id=" + id, new Exception());
		return POI_ICON_RESOURCE_IDS[0];
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.poi);
		if (mPoiManager == null)
			mPoiManager = new PoiManager(this);
		mCf = new CoordFormatter(this);
		mTitle = (EditText)findViewById(R.id.Title);
		mLat = (EditText)findViewById(R.id.Lat);
		mLon = (EditText)findViewById(R.id.Lon);
		mAlt = (EditText)findViewById(R.id.Alt);
		mDescr = (EditText)findViewById(R.id.Descr);
		mHidden = (CheckBox)findViewById(R.id.Hidden);
		mLat.setHint(mCf.getHint());
		mLat.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					try {
						mLat.setText(mCf.convertLat(CoordFormatter.convertTrowable(mLat.getText().toString())));
					}
					catch (Exception e) {
						mLat.setText(EMPTY);
					}
				}
			}
		});
		mLon.setHint(mCf.getHint());
		mLon.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					try {
						mLon.setText(mCf.convertLon(CoordFormatter.convertTrowable(mLon.getText().toString())));
					}
					catch (Exception e) {
						mLon.setText(EMPTY);
					}
				}
			}
		});
		mSpinner = (Spinner)findViewById(R.id.spinnerCategory);
		Cursor c = mPoiManager.getPoiCategories();
		startManagingCursor(c);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
			R.layout.poi_category_spinner, //android.R.layout.simple_spinner_item,
			c,
			TableE.toString(new Object[]{category.name, category.iconid}),
			new int[]{android.R.id.text1, R.id.pic});
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == category.iconid.ordinal()) {
					int id = cursor.getInt(columnIndex);
					Ut.d("setViewValue find id=" + id);
					((ImageView)view.findViewById(R.id.pic)).setImageResource(PoiActivity.resourceFromPoiIconId(id));
					return true;
				}
				return false;
			}
		});
		adapter.setDropDownViewResource(R.layout.poi_category_spinner_dropdown);
		mSpinner.setAdapter(adapter);
		Bundle extras = getIntent().getExtras();
		if (extras == null) extras = new Bundle();
		int id = extras.getInt(POINTID, Constants.EMPTY_ID);
		if (id < 0) {
			mPoiPoint = new PoiPoint();
			mTitle.setText(extras.getString(TITLE));
			mSpinner.setSelection(0);
			mLat.setText(mCf.convertLat(extras.getDouble(LAT)));
			mLon.setText(mCf.convertLon(extras.getDouble(LON)));
			mAlt.setText(String.format(Locale.UK, "%.1f", extras.getDouble(ALT, 0.0)));
			mDescr.setText(extras.getString(DESCR));
			mHidden.setChecked(false);
		}
		else {
			mPoiPoint = mPoiManager.getPoiPoint(id);
			if (mPoiPoint == null)
				finish();
			mTitle.setText(mPoiPoint.mTitle);
			for (int pos = 0; pos < mSpinner.getCount(); pos++) {
				if (mSpinner.getItemIdAtPosition(pos) == mPoiPoint.mCategoryId) {
					mSpinner.setSelection(pos);
					break;
				}
			}
			mLat.setText(mCf.convertLat(mPoiPoint.mGeoPoint.getLatitude()));
			mLon.setText(mCf.convertLon(mPoiPoint.mGeoPoint.getLongitude()));
			mAlt.setText(String.format(Locale.UK, "%.1f", mPoiPoint.mAlt));
			mDescr.setText(mPoiPoint.mDescr);
			mHidden.setChecked(mPoiPoint.mHidden);
		}
		findViewById(R.id.saveButton)
			.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					doSaveAction();
				}
			});
		findViewById(R.id.discardButton)
			.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					PoiActivity.this.finish();
				}
			});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPoiManager.FreeDatabases();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK: {
				doSaveAction();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void doSaveAction() {
		mPoiPoint.mTitle = mTitle.getText().toString();
		mPoiPoint.mCategoryId = (int)mSpinner.getSelectedItemId();
		mPoiPoint.mDescr = mDescr.getText().toString();
		mPoiPoint.mGeoPoint = new GeoPoint(
			CoordFormatter.convertTrowable(mLat.getText().toString()),
			CoordFormatter.convertTrowable(mLon.getText().toString()));
		mPoiPoint.mHidden = mHidden.isChecked();
		try {
			mPoiPoint.mAlt = Double.parseDouble(mAlt.getText().toString());
		}
		catch (NumberFormatException e) {
			Ut.e(e.toString(), e);
		}
		mPoiManager.updatePoi(mPoiPoint);
		finish();
		Toast.makeText(PoiActivity.this, R.string.message_saved, Toast.LENGTH_SHORT).show();
	}
}
