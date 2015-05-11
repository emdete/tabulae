/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pyneo.maps.track;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class TrackStyleDrawable extends Drawable {
	private Paint mPaint = new Paint();
	private Bitmap mBitmap;

	public TrackStyleDrawable(int color, int width, int colorshadow, double shadowradius) {
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(width);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(color);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setShadowLayer((float) shadowradius, 0, 0, colorshadow);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, null, getBounds(), mPaint);
	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		throw new UnsupportedOperationException("Alpha is not supported by this drawwable.");
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		throw new UnsupportedOperationException("ColorFilter is not supported by this drawwable.");
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		if (getBounds().width() <= 0 || getBounds().height() <= 0) {
			return;
		}
		mBitmap = Bitmap.createBitmap(getBounds().width(), getBounds().height(), Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		final int left = getBounds().width() / 10;
		final int step = (getBounds().width() - 2 * left) / 3;
		final int top = getBounds().height() / 4;
		final int cent_v = getBounds().height() / 2;
		Path mPath = new Path();
		mPath.setLastPoint(left, cent_v);
		mPath.lineTo(left + step, top);
		mPath.lineTo(left + 2 * step, getBounds().height() - top);
		mPath.lineTo(left + 3 * step, cent_v);
		canvas.drawPath(mPath, mPaint);
	}
}
