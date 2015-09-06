package org.pyneo.tabulae.track;

import java.util.Date;
import co.uk.rushorm.core.RushObject;

public class TrackPointItem extends RushObject implements Constants {
	// @Unique @NotNull
	int sequence;
	long trackid;
	double latitude;
	double longitude;
	double altitude;
	double speed;
	Date timestamp;
	int attribute;
	TrackItem track;

	public TrackPointItem() {
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public long getTrackid() {
		return trackid;
	}

	public void setTrackid(long trackid) {
		this.trackid = trackid;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}
}
