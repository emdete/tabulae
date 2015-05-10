package org.pyneo.maps.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import org.pyneo.maps.R;

import java.io.File;

public class IconManager {
	public static int poi = 0x7f02000a;
	public static int poiblue = 0x7f02000c;
	public static int poigreen = 0x7f02000d;
	public static int poiwhite = 0x7f02000e;
	public static int poiyellow = 0x7f02000f;
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
		return getBitmap(R.drawable.needle_off);
	}

	public Bitmap getLocationIcon() {
		final Bitmap bmp = getBitmapFileFromProp("pref_person_icon", "icons/cursors");
		if (bmp != null)
			return bmp;
		return getBitmap(R.drawable.needle_pinned);
	}

	public Bitmap getArrowIcon() {
		final Bitmap bmp = getBitmapFileFromProp("pref_arrow_icon", "icons/cursors");
		if (bmp != null)
			return bmp;
		return getBitmap(R.drawable.needle);
	}

	public Bitmap getTargetIcon() {
		final Bitmap bmp = getBitmapFileFromProp("pref_target_icon", "icons/cursors");
		if (bmp != null)
			return bmp;
		return getBitmap(R.drawable.r_mark);
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

	public int getPoiIconResId(int id) {
		if (id == poi) {
			return R.drawable.poi_red;
		} else if (id == poiblue) {
			return R.drawable.poiblue;
		} else if (id == poigreen) {
			return R.drawable.poigreen;
		} else if (id == poiwhite) {
			return R.drawable.poiwhite;
		} else if (id == poiyellow) {
			return R.drawable.poiyellow;
		} else {
			return 0;
		}
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
