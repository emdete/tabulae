package org.pyneo.maps.poi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.pyneo.maps.R;

public class PoiIconSetActivity extends Activity implements Constants {
	private GridView mGridInt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.poi_iconset);
		mGridInt = (GridView)findViewById(R.id.GridInt);
		mGridInt.setAdapter(new AppsAdapter());
		mGridInt.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//Toast.makeText(PoiIconSetActivity.this, "sel="+arg3, Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK, (new Intent()).putExtra("iconid", POI_ICON_RESOURCE_IDS[arg2]));
				finish();
			}
		});
	}

	public class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i;

			if (convertView == null) {
				i = new ImageView(PoiIconSetActivity.this);
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				i.setLayoutParams(new GridView.LayoutParams(50, 50));
			} else {
				i = (ImageView)convertView;
			}

			i.setImageResource(POI_ICON_RESOURCE_IDS[position]);

			return i;
		}

		public final int getCount() {
			return 5;
		}

		public final Object getItem(int position) {
			return null;
		}

		public final long getItemId(int position) {
			return POI_ICON_RESOURCE_IDS[position];
		}
	}
}
