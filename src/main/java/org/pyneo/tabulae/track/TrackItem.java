package org.pyneo.tabulae.track;

import org.pyneo.tabulae.storage.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class TrackItem extends Item {
	Date timestamp;
	boolean visible;
	int pointcount;
	long duration;
	long distance;
	int categoryid;
	int activityid;
	int cropto;
	int cropfrom;
	List<TrackPointItem> trackPoints = new ArrayList<TrackPointItem>();

	public TrackItem(int id_, String name, String description) {
		super(id_, name, description);
	}

	public TrackItem(String name, String description) {
		super(name, description);
	}

	public List<TrackPointItem> getTrackPoints() {
		return trackPoints;
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
}
