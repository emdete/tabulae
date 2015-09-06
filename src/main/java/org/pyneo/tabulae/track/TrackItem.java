package org.pyneo.tabulae.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import co.uk.rushorm.core.RushObject;
import co.uk.rushorm.core.annotations.RushList;

public class TrackItem extends RushObject implements Constants {
	// @Unique @NotNull
	String name;
	String description;
	Date timestamp;
	boolean visible;
	int pointcount;
	long duration;
	long distance;
	int categoryid;
	int activityid;
	int cropto;
	int cropfrom;
	@RushList(classType = TrackPointItem.class)
	List<TrackPointItem> trackPoints = new ArrayList<>();

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
