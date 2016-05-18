package org.pyneo.tabulae.track;

import android.database.sqlite.SQLiteDatabase;
import java.util.Date;
import org.pyneo.thinstore.StoreObject;
import org.mapsforge.core.model.LatLong;
import static org.pyneo.tabulae.track.Constants.*;

public class TrackPointItem extends StoreObject {
	// @Unique @NotNull
	int sequence;
	Date timestamp;
	double latitude;
	double longitude;
	double altitude;
	double speed;
	int attribute;
	long trackId;
	//TrackItem track;

	public TrackPointItem() {
	}

	public TrackPointItem(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public TrackItem getMeta(SQLiteDatabase db) throws Exception {
		return (TrackItem)query(db, TrackItem.class).where("id").equal(trackId).fetchOne();
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
		attribute %= 5;
		this.attribute = attribute;
	}

	public LatLong getLatLon() {
		return new LatLong(latitude, longitude);
	}
}
