package org.pyneo.tabulae.track;

import android.database.sqlite.SQLiteDatabase;
import org.pyneo.thinstore.StoreObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.mapsforge.core.model.LatLong;

public class TrackItem extends StoreObject implements Constants {
	// @Unique @NotNull
	String name;
	String description;
	String comment;
	Date timestamp;
	boolean visible = true;
	int pointcount = -1;
	long duration = -1;
	long distance = -1;
	int categoryid = -1;
	int activityid = -1;
	int cropto = -1;
	int cropfrom = -1;
	volatile List<TrackPointItem> trackPointItems;

	public TrackItem() {
	}

	public TrackItem(String name, String description) {
		this.name = name;
		this.description = description;
		this.trackPointItems = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void add(SQLiteDatabase db, TrackPointItem trackPointItem) throws Exception {
		//trackPointItem.track = this;
		getTrackPointItems(db).add(trackPointItem);
		if (getId() >= 0) {
			trackPointItem.trackId = getId();
			if (db != null) {
				trackPointItem.insert(db);
			}
		}
	}

	public List<TrackPointItem> getTrackPointItems(SQLiteDatabase db) throws Exception {
		if (trackPointItems == null) {
			trackPointItems = (List)query(db, TrackPointItem.class).where("trackId").equal(getId()).fetchAll();
		}
		return trackPointItems;
	}

	public StoreObject insert(SQLiteDatabase db) throws Exception {
		boolean create = getId() < 0;
		super.insert(db);
		if (create && trackPointItems != null) {
			for (TrackPointItem trackPointItem : getTrackPointItems(db)) {
				if (trackPointItem.trackId < 0) {
					throw new Exception("assertion failed trackId=" + trackPointItem.trackId);
				}
				trackPointItem.trackId = getId();
				trackPointItem.insert(db);
			}
		}
		return this;
	}

	public List<LatLong> getTrackLatLongs(SQLiteDatabase db) throws Exception {
		List<LatLong> list = new ArrayList<>();
		for (TrackPointItem trackPointItem : getTrackPointItems(db)) {
			list.add(new LatLongTagged(trackPointItem));
		}
		return list;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getPointcount() {
		return pointcount;
	}

	public void setPointcount(int pointcount) {
		this.pointcount = pointcount;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getDistance() {
		return distance;
	}

	public void setDistance(long distance) {
		this.distance = distance;
	}

	public int getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(int categoryid) {
		this.categoryid = categoryid;
	}

	public int getActivityid() {
		return activityid;
	}

	public void setActivityid(int activityid) {
		this.activityid = activityid;
	}

	public int getCropto() {
		return cropto;
	}

	public void setCropto(int cropto) {
		this.cropto = cropto;
	}

	public int getCropfrom() {
		return cropfrom;
	}

	public void setCropfrom(int cropfrom) {
		this.cropfrom = cropfrom;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public static class LatLongTagged extends LatLong {
		public TrackPointItem trackPointItem;

		public LatLongTagged(TrackPointItem trackPointItem) {
			super(trackPointItem.latitude, trackPointItem.longitude);
			this.trackPointItem = trackPointItem;
		}
	}
}
