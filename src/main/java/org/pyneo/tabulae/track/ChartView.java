package org.pyneo.tabulae.track;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import org.pyneo.tabulae.R;

public class ChartView extends View {
	private static final int BOTTOM_BORDER = 12;
	private static final int LEFT_BORDER = 12;
	private static final int RIGHT_BORDER = 1;
	private static final int TOP_BORDER = 1;
	private static final int MAX_INTERVALS = 5;
	private final Paint borderPaint = new Paint();
	private final Paint gridPaint = new Paint();
	private final Paint[] graphPaint = {null, null};
	private int bottomBorder = 0;
	private int leftBorder = 0;
	private int rightBorder = 0;
	private int topBorder = 0;
	private int lastWidth = 0;
	private int lastHeight = 0;
	private int effectiveWidth = 0;
	private int effectiveHeight = 0;
	private Path[] path = {new Path(), new Path()};
	private Path[] path_transformed = {null, null};

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final float density = getContext().getResources().getDisplayMetrics().density;
		final Resources res = getResources();
		borderPaint.setStyle(Style.STROKE);
		//noinspection deprecation
		borderPaint.setColor(res.getColor(R.color.black));
		//borderPaint.setAntiAlias(true);
		gridPaint.setStyle(Style.STROKE);
		gridPaint.setPathEffect(new DashPathEffect(new float[]{density * 3, density * 9}, 0));
		gridPaint.setColor(Color.WHITE);
		//gridPaint.setAntiAlias(false);
		graphPaint[0] = new Paint();
		//noinspection deprecation
		graphPaint[0].setColor(res.getColor(R.color.blue));
		graphPaint[0].setStyle(Style.STROKE);
		graphPaint[0].setStrokeWidth(Math.max(1, (int) (density * .6)));
		//graphPaint[0].setAntiAlias(true);
		//graphPaint[0].setAlpha(180);
		graphPaint[0].setStrokeCap(Paint.Cap.ROUND);
		//noinspection deprecation
		graphPaint[0].setShadowLayer(10.0f, 0, 0, res.getColor(R.color.blue));
		graphPaint[1] = new Paint(graphPaint[0]);
		//noinspection deprecation
		graphPaint[1].setColor(res.getColor(R.color.green));
		//noinspection deprecation
		graphPaint[1].setShadowLayer(10.0f, 0, 0, res.getColor(R.color.green));
		leftBorder = (int) (density * LEFT_BORDER);
		rightBorder = (int) (density * RIGHT_BORDER);
		bottomBorder = (int) (density * BOTTOM_BORDER);
		topBorder = (int) (density * TOP_BORDER);
	}

	public ChartView(Context context) {
		super(context);
	}

	public void setTrack(final Track tr) {
		//float[] results = {0};
		//float distance = 0.0f;
		double minSpeed = Double.MAX_VALUE;
		double minAlt = Double.MAX_VALUE;
		/*
		TrackPoint lastpt = null;
		for (TrackPoint pt : tr.getPoints()) {
			if (lastpt == null) {
				path[0].moveTo(0, (float) pt.getSpeed());
				path[1].moveTo(0, (float) pt.getAlt());
			} else {
				Location.distanceBetween(lastpt.getLat(), lastpt.getLon(), pt.getLat(), pt.getLon(), results);
				distance += results[0];
				path[0].lineTo(distance, (float) pt.getSpeed());
				path[1].lineTo(distance, (float) pt.getAlt());
			}
			if (minSpeed > pt.getSpeed()) minSpeed = pt.getSpeed();
			if (minAlt > pt.getAlt()) minAlt = pt.getAlt();
			lastpt = pt;
		}
		*/
		final Matrix m = new Matrix();
		m.setTranslate(0, (float) -minSpeed);
		path[0].transform(m);
		m.setScale(1, -1);
		path[0].transform(m);
		m.setTranslate(0, (float) -minAlt);
		path[1].transform(m);
		m.setScale(1, -1);
		path[1].transform(m);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (lastWidth != canvas.getWidth() || lastHeight != canvas.getHeight()) {
			// Dimensions have changed (for example due to orientation change).
			lastWidth = getWidth();
			lastHeight = getHeight();
			effectiveWidth = Math.max(0, lastWidth - (leftBorder + rightBorder));
			effectiveHeight = Math.max(0, lastHeight - (topBorder + bottomBorder));
			//setUpPath();
			for (int i = 0; i < path.length; i++) {
				RectF r = new RectF();
				final Matrix m = new Matrix();
				path_transformed[i] = new Path(path[i]);
				path_transformed[i].computeBounds(r, true);
				m.setScale(effectiveWidth / r.width(), /*-*/ effectiveHeight / r.height());
				path_transformed[i].transform(m);
				m.setTranslate(leftBorder, effectiveHeight + bottomBorder);
				path_transformed[i].transform(m);
			}
		}
		canvas.save();
		for (int i = 0; i < path.length; i++) {
			canvas.drawPath(path_transformed[i], graphPaint[i]);
		}
		canvas.drawLine(leftBorder, effectiveHeight + bottomBorder, effectiveWidth + leftBorder, effectiveHeight + bottomBorder, borderPaint);
		canvas.drawLine(leftBorder, bottomBorder, leftBorder, effectiveHeight + bottomBorder, borderPaint);
		for (int i = 1; i < MAX_INTERVALS; ++i) {
			int y = i * effectiveHeight / MAX_INTERVALS + topBorder;
			canvas.drawLine(leftBorder, y, effectiveWidth + leftBorder, y, gridPaint);
		}
		canvas.restore();
	}
}
