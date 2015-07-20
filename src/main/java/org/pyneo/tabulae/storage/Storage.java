package org.pyneo.tabulae.storage;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.pyneo.tabulae.Tabulae;
import java.io.File;

public class Storage extends SQLiteOpenHelper implements Constants {
	protected final static int mCurrentVersion = 1;
	static final private String ID = "tabulae";

	enum tracks {_id, name, description, timestamp, visible, pointcount, duration, distance, categoryid, activityid, };
	enum trackpoints {_id, trackid, latitude, longitude, altitude, speed, timestamp, attribute, };
	enum pois {_id, name, description, latitude, longitude, altitude, visible, categoryid, iconid, };
	enum category {_id, name, description, visible, iconid, minzoom, };
	enum activity {_id, name, description, };

	Storage(final Tabulae context) {
		super(context, new File(context.getBaseDir(), ID + ".db").getAbsolutePath(), null, mCurrentVersion);
	}

	@Override public void onCreate(final SQLiteDatabase db) {
		db.execSQL(TableE.createStatement(tracks.class, tracks.values()));
	}

	@Override public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
	}

	public SQLiteDatabase getDatabase(final Tabulae activity) {
		return new Storage(activity).getWritableDatabase();
	}
}
