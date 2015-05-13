package org.pyneo.maps.poi;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import org.pyneo.maps.R;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.track.TrackStorage;

import java.io.File;

public class PoiStorage extends TrackStorage implements Constants { // TODO
	public PoiStorage(Context ctx) {
		super(ctx);
	}
}
