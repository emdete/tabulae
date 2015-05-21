package org.pyneo.maps.poi;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.pyneo.maps.utils.TableE;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.R;

public class PoiCategoryListActivity extends ListActivity implements Constants {
	private PoiManager mPoiManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.poi_category_list);
		registerForContextMenu(getListView());
		mPoiManager = new PoiManager(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPoiManager.FreeDatabases();
	}

	@Override
	protected void onResume() {
		FillData();
		super.onResume();
	}

	private void FillData() {
		Cursor c = mPoiManager.getPoiCategoryListCursor();
		startManagingCursor(c);

		ListAdapter adapter = new SimpleCursorAdapter(this,
			R.layout.poi_category_list_item, c,
			TableE.toString(new Object[]{category.name, category.iconid, category.hidden}),
			new int[]{R.id.title1, R.id.pic, R.id.checkbox});
		((SimpleCursorAdapter)adapter).setViewBinder(new SimpleCursorAdapter.ViewBinder(){
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(category.hidden.name())) {
				//if (columnIndex == category.hidden.ordinal()) { TODO:CATEGORY
					((CheckBox)view.findViewById(R.id.checkbox)).setChecked(cursor.getInt(columnIndex) == 1);
					return true;
				}
				else if (cursor.getColumnName(columnIndex).equalsIgnoreCase(category.iconid.name())) {
				//else if (columnIndex == category.iconid.ordinal()) {
					int id = cursor.getInt(columnIndex);
					Ut.d("setViewValue find id=" + id);
					((ImageView)view.findViewById(R.id.pic)).setImageResource(PoiActivity.resourceFromPoiIconId(id));
					return true;
				}
				return false;
			}

		});
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mPoiManager.setCategoryHidden((int)id);

//		final CheckBox ch = (CheckBox) v.findViewById(R.id.checkbox);
//		ch.setChecked(!ch.isChecked());
		((SimpleCursorAdapter)getListAdapter()).getCursor().requery();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.poicategorylist_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.menu_addpoi) {
			startActivity((new Intent(this, PoiCategoryActivity.class)));
			return true;
		}

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenuInfo menuInfo) {
		int id = (int)((AdapterView.AdapterContextMenuInfo)menuInfo).id;
		PoiCategory category = mPoiManager.getPoiCategory(id);

		menu.add(0, R.id.menu_editpoi, 0, getText(R.string.menu_edit));
		if (category.mHidden)
			menu.add(0, R.id.menu_show, 0, getText(R.string.menu_show));
		else
			menu.add(0, R.id.menu_hide, 0, getText(R.string.menu_hide));
		menu.add(0, R.id.menu_deletepoi, 0, getText(R.string.menu_delete));

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = (int)((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).id;
		PoiCategory category = mPoiManager.getPoiCategory(id);
		if (item.getItemId() == R.id.menu_editpoi) {
			startActivity((new Intent(this, PoiCategoryActivity.class)).putExtra("id", id));
		} else if (item.getItemId() == R.id.menu_deletepoi) {
			mPoiManager.deletePoiCategory(id);
			FillData();
		} else if (item.getItemId() == R.id.menu_hide) {
			category.mHidden = true;
			mPoiManager.updatePoiCategory(category);
			FillData();
		} else if (item.getItemId() == R.id.menu_show) {
			category.mHidden = false;
			mPoiManager.updatePoiCategory(category);
			FillData();
		}
		return super.onContextItemSelected(item);
	}
}
