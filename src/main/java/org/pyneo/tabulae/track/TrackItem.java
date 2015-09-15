package org.pyneo.tabulae.track;

import co.uk.rushorm.core.RushSearch;
import org.mapsforge.core.model.LatLong;
import java.util.ArrayList;
import java.util.List;
import co.uk.rushorm.core.annotations.RushIgnore;
import java.util.Date;
import co.uk.rushorm.core.RushObject;
import co.uk.rushorm.core.annotations.RushList;

public class TrackItem extends RushObject implements Constants {
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
	//@RushList(classType = TrackPointItem.class)
	@RushIgnore List<TrackPointItem> trackPointItems;// = new ArrayList<>();

	public static class LatLongTagged extends LatLong {
		public TrackPointItem trackPointItem;
		public LatLongTagged(TrackPointItem trackPointItem) {
			super(trackPointItem.latitude, trackPointItem.longitude);
			this.trackPointItem = trackPointItem;
		}
	}

	public TrackItem() {
	}

	public TrackItem(String name, String description) {
		this.name = name;
		this.description = description;
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

	void add(TrackPointItem trackPointItem) {
		//trackPointItem.track = this;
		getTrackPointItems().add(trackPointItem);
		if (getId() != null) {
			trackPointItem.trackId = getId();
			trackPointItem.save();
		}
	}

	public List<TrackPointItem> getTrackPointItems() {
		if (trackPointItems == null) {
			trackPointItems = new RushSearch().whereEqual("trackId", getId()).find(TrackPointItem.class);
		}
		return trackPointItems;
	}

	public void save() {
		boolean create = getId() == null;
		super.save();
		if (create && trackPointItems != null) {
			for (TrackPointItem trackPointItem: getTrackPointItems()) {
				trackPointItem.trackId = getId();
				trackPointItem.save();
			}
		}
	}

	public List<LatLong> getTrackLatLongs() {
		List<LatLong> list = new ArrayList<LatLong>();
		for (TrackPointItem trackPointItem: getTrackPointItems()) {
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
}
