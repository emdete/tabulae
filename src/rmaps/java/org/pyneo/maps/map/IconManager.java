package org.pyneo.maps.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import org.pyneo.maps.R;
import org.pyneo.maps.utils.Ut;

import java.io.File;

class IconManager {
	private static IconManager mIconManager = null;
	private Context mAppContext;

	private IconManager(Context ctx) {
		super();
		mAppContext = ctx.getApplicationContext();
	}

	public static IconManager getInstance(Context ctx) {
		if (mIconManager == null)
			mIconManager = new IconManager(ctx);
		return mIconManager;
	}

	public Bitmap getNolocationIcon() {
		final Bitmap bmp = getBitmapFileFromProp("pref_icon", "icons/cursors");
		if (bmp != null)
			return bmp;
		return getBitmap(R.drawable.map_needle_off);
	}

	public Bitmap getLocationIcon() {
		final Bitmap bmp = getBitmapFileFromProp("pref_person_icon", "icons/cursors");
		if (bmp != null)
			return bmp;
		return getBitmap(R.drawable.map_needle_pinned);
	}

	public Bitmap getArrowIcon() {
		final Bitmap bmp = getBitmapFileFromProp("pref_arrow_icon", "icons/cursors");
		if (bmp != null)
			return bmp;
		return getBitmap(R.drawable.map_needle);
	}

	public Bitmap getTargetIcon() {
		final Bitmap bmp = getBitmapFileFromProp("pref_target_icon", "icons/cursors");
		if (bmp != null)
			return bmp;
		return getBitmap(R.drawable.map_location);
	}

	private Bitmap getBitmapFileFromProp(String propName, String folderName) {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
		final String prefPersonFileName = pref.getString(propName, "");
		if (!prefPersonFileName.equalsIgnoreCase("")) {
			final File folder = Ut.getAppMainDir(mAppContext, folderName);
			if (folder.exists()) {
				final String file = folder.getAbsolutePath() + "/" + prefPersonFileName;
				if (new File(file).exists()) {
					try {
						final Bitmap bmp = BitmapFactory.decodeFile(file);
						if (bmp != null)
							return bmp;
					}
					catch (Exception e) {
						Ut.e(e.toString(), e);
					}
				}
			}
		}
		return null;
	}

	private Drawable getDrawable(int resId) {
		return mAppContext.getResources().getDrawable(resId);
	}

	private Bitmap getBitmap(int resId) {
		try {
			return BitmapFactory.decodeResource(mAppContext.getResources(), resId);
		}
		catch (Exception e) {
			Ut.e(e.toString(), e);
			return null;
		}
	}
}
