package org.pyneo.tabulae.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.DefaultResourceProxyImpl;
import org.pyneo.tabulae.R;

public class ResourceProxyImpl extends DefaultResourceProxyImpl implements Constants {
	private final Context mContext;

	public ResourceProxyImpl(final Context pContext) {
		super(pContext);
		mContext = pContext;
	}

	private static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	@Override
	public String getString(final string pResId) {
		try {
			final int res;
			switch (pResId) {
				default:
					res = R.string.class.getDeclaredField(pResId.name()).getInt(null);
			}
			return mContext.getString(res);
		} catch (final Exception e) {
			return super.getString(pResId);
		}
	}

	@Override
	public String getString(final string pResId, final Object... formatArgs) {
		try {
			final int res;
			switch (pResId) {
				default:
					res = R.string.class.getDeclaredField(pResId.name()).getInt(null);
			}
			return mContext.getString(res, formatArgs);
		} catch (final Exception e) {
			return super.getString(pResId, formatArgs);
		}
	}

	@Override
	public Bitmap getBitmap(final bitmap pResId) {
		try {
			final int res;
			switch (pResId) {
				case person:
					res = R.drawable.map_needle_pinned;
					break;
				case direction_arrow:
					res = R.drawable.map_needle;
					break;
				default:
					res = R.drawable.class.getDeclaredField(pResId.name()).getInt(null);
			}
			return drawableToBitmap(mContext.getResources().getDrawable(res));
			//return BitmapFactory.decodeResource(mContext.getResources(), res);
		} catch (final Exception e) {
			Log.e(TAG, "missing resource =" + pResId.name());
			return super.getBitmap(pResId);
		}
	}

	@Override
	public Drawable getDrawable(final bitmap pResId) {
		try {
			final int res;
			switch (pResId) {
				default:
					res = R.drawable.class.getDeclaredField(pResId.name()).getInt(null);
			}
			return mContext.getResources().getDrawable(res);
		} catch (final Exception e) {
			Log.e(TAG, "missing resource =" + pResId.name());
			return super.getDrawable(pResId);
		}
	}
}
