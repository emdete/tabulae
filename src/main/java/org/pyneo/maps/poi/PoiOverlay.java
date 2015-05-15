package org.pyneo.maps.poi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import org.pyneo.maps.R;
import org.pyneo.maps.utils.Ut;
import org.pyneo.maps.map.TileView;
import org.pyneo.maps.map.TileViewOverlay;
import org.pyneo.maps.map.TileView.OpenStreetMapViewProjection;

import org.pyneo.maps.utils.GeoPoint;

public class PoiOverlay extends TileViewOverlay implements Constants {
	private final Point mMarkerHotSpot;
	private final int mMarkerWidth;
	private final int mMarkerHeight;
	private SparseArray<PoiPoint> mItemList = new SparseArray<PoiPoint>();
	private final SparseArray<Drawable> mMarkerCache = new SparseArray<Drawable>();
	private Context mCtx;
	private PoiManager mPoiManager;
	private int mTapId;
	private GeoPoint mLastMapCenter;
	private int mLastZoom;
	private RelativeLayout mT;
	private float mDensity;
	private boolean mListUpdateNeeded = false;
	private boolean mCanUpdateList = true;

	public PoiOverlay(Context ctx, PoiManager poiManager, boolean hidepoi) {
		mCtx = ctx;
		mPoiManager = poiManager;
		mCanUpdateList = !hidepoi;
		mTapId = NO_TAP;
		Drawable marker = ctx.getResources().getDrawable(PoiActivity.resourceFromPoiIconId(0));
		mMarkerWidth = marker.getIntrinsicWidth();
		mMarkerHeight = marker.getIntrinsicHeight();
		mMarkerHotSpot = new Point(mMarkerWidth/2, mMarkerHeight);
		//mOnItemTapListener = onItemTapListener;
		mLastMapCenter = null;
		mLastZoom = -1;
		mT = (RelativeLayout)LayoutInflater.from(ctx).inflate(R.layout.poi_descr, null);
		mT.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDensity = metrics.density;
	}

	public int getTapIndex() {
		return mTapId;
	}

	public void setTapIndex(int mTapIndex) {
		mTapId = mTapIndex;
	}

	public void UpdateList() {
		mListUpdateNeeded = true;
	}

	public void clearPoiList() {
		mItemList.clear();
	}

	public void showTemporaryPoi(final int id, final GeoPoint geopoint, final String title, final String descr) {
		mItemList.put(id, new PoiPoint(id, title, descr, geopoint, 0, 0));
		mCanUpdateList = false;
		mTapId = id;
	}

	@Override
	public void onDraw(Canvas c, TileView mapView) {
		final OpenStreetMapViewProjection pj = mapView.getProjection();
		final Point curScreenCoords = new Point();
		if (mCanUpdateList) {
			boolean listUpdateNeeded = mListUpdateNeeded;
			GeoPoint center = mapView.getMapCenter();
			GeoPoint lefttop = pj.fromPixels(0, 0);
			double deltaX = Math.abs(center.getLongitude() - lefttop.getLongitude());
			double deltaY = Math.abs(center.getLatitude() - lefttop.getLatitude());
			if (!listUpdateNeeded) {
				if (mLastMapCenter == null || mLastZoom != mapView.getZoomLevel()) {
					listUpdateNeeded = true;
				}
				else if (0.7 * deltaX < Math.abs(center.getLongitude() - mLastMapCenter.getLongitude())
				|| 0.7 * deltaY < Math.abs(center.getLatitude() - mLastMapCenter.getLatitude())) {
					listUpdateNeeded = true;
				}
			}
			if (listUpdateNeeded) {
				mListUpdateNeeded = false;
				mLastMapCenter = center;
				mLastZoom = mapView.getZoomLevel();
				Ut.i("updating list");
				mItemList = mPoiManager.getPoiListNotHidden(mLastZoom, mLastMapCenter, 1.5 * deltaX, 1.5 * deltaY); // TODO thread!
				Ut.i("updated list count=" + mItemList.size());
			}
		}
		Ut.d("onDraw mItemList=" + mItemList);
		if (mItemList != null) {
			// Draw in backward cycle, so the items with the least index are on the front:
			for (int i = mItemList.size() - 1; i >= 0; i--) {
				Ut.i("draw item i=" + i);
				PoiPoint item = mItemList.valueAt(i);
				pj.toPixels(item.mGeoPoint, curScreenCoords);
				c.save();
				c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);
				drawPoi(c, item.getId(), curScreenCoords);
				c.restore();
			}
			// paint tapped item last:
			if (mTapId != NO_TAP) {
				PoiPoint item = mItemList.get(mTapId);
				if (item != null) {
					Ut.i("draw item id=" + mTapId);
					pj.toPixels(item.mGeoPoint, curScreenCoords);
					c.save();
					c.rotate(mapView.getBearing(), curScreenCoords.x, curScreenCoords.y);
					drawPoiDescr(c, mTapId, curScreenCoords);
					c.restore();
				}
				else {
					Ut.w("item disapeared");
					mTapId = NO_TAP; // oups...
				}
			}
		}
	}

	private void drawPoiDescr(Canvas c, int id, Point screenCoords) {
		final PoiPoint paintItem = mItemList.get(id);
		Ut.d("drawPoi screenCoords=" + screenCoords);
		final ImageView pic = (ImageView)mT.findViewById(R.id.pic);
		pic.setImageResource(PoiActivity.resourceFromPoiIconId(paintItem.mIconId));
		((TextView)mT.findViewById(R.id.poi_title)).setText(paintItem.mTitle);
		((TextView)mT.findViewById(R.id.descr)).setText(paintItem.mDescr);
		((TextView)mT.findViewById(R.id.coord)).setText(Ut.formatGeoPoint(paintItem.mGeoPoint, mCtx));
		mT.measure(0, 0);
		mT.layout(0, 0, mT.getMeasuredWidth(), mT.getMeasuredHeight());
		c.save();
		c.translate(screenCoords.x - pic.getMeasuredWidth()/2, screenCoords.y - pic.getMeasuredHeight() - pic.getTop());
		mT.draw(c);
		c.restore();
	}

	private void drawPoi(Canvas c, int id, Point screenCoords) {
		final PoiPoint paintItem = mItemList.get(id);
		if (paintItem.getId() == mTapId) { // we draw not tapped items only
			return;
		}
		final int left = screenCoords.x - mMarkerHotSpot.x;
		final int right = left + mMarkerWidth;
		final int top = screenCoords.y - mMarkerHotSpot.y;
		final int bottom = top + mMarkerHeight;
		Ut.d("drawPoiDescr left=" + left + ", right=" + right + ", top=" + top + ", bottom=" + bottom);
		Drawable marker = null;
		if (mMarkerCache.indexOfKey(PoiActivity.resourceFromPoiIconId(paintItem.mIconId)) < 0) {
			marker = mCtx.getResources().getDrawable(PoiActivity.resourceFromPoiIconId(paintItem.mIconId));
			mMarkerCache.put(PoiActivity.resourceFromPoiIconId(paintItem.mIconId), marker);
		}
		else {
			marker = mMarkerCache.get(PoiActivity.resourceFromPoiIconId(paintItem.mIconId));
		}
		marker.setBounds(left, top, right, bottom);
		marker.draw(c);
	}

	public PoiPoint getPoiPoint(final int id) {
		return mItemList.get(id);
	}

	public int getMarkerAtPoint(final int eventX, final int eventY, TileView mapView) {
		if (mItemList != null) {
			final OpenStreetMapViewProjection pj = mapView.getProjection();
			final Rect curMarkerBounds = new Rect();
			final Point mCurScreenCoords = new Point();
			for (int i = 0; i < mItemList.size(); i++) {
				final PoiPoint mItem = mItemList.valueAt(i);
				pj.toPixels(mItem.mGeoPoint, mapView.getBearing(), mCurScreenCoords);
				final int delta = 50;
				curMarkerBounds.set(
					mCurScreenCoords.x - delta,
					mCurScreenCoords.y - delta,
					mCurScreenCoords.x + delta,
					mCurScreenCoords.y + delta);
				if (curMarkerBounds.contains(eventX, eventY)) {
					Ut.i("poi found id=" + mItem.getId());
					return mItem.getId();
				}
			}
		}
		Ut.i("poi not found");
		return NO_TAP;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event, TileView mapView) {
		int id = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (mTapId == id) // same poi tapped again: toggle
			id = NO_TAP;
		mTapId = id;
		Ut.i("poi tapped id=" + mTapId);
		return mTapId != NO_TAP || super.onSingleTapUp(event, mapView);
	}

	@Override
	public int onLongPress(MotionEvent event, TileView mapView) {
		final int id = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		// TODO: fix the following global nightmare!
		mapView.mPoiMenuInfo.MarkerIndex = id;
		mapView.mPoiMenuInfo.EventGeoPoint = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY(), mapView.getBearing());
		if (id != NO_TAP)
			return 1;
		return super.onLongPress(event, mapView);
	}

	private boolean onLongLongPress(int id) {
		return false;
	}
}
