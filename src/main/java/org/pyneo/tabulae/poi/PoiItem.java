package org.pyneo.tabulae.poi;

import org.pyneo.tabulae.storage.Item;

public class PoiItem extends Item implements Constants {
	double latitude;
	double longitude;
	boolean visible;

	public PoiItem(int id_, String name, String description, double latitude, double longitude, boolean visible) {
		super(id_, name, description);
		this.latitude = latitude;
		this.longitude = longitude;
		this.visible = visible;
	}

	public PoiItem(String name, String description, double latitude, double longitude, boolean visible) {
		super(name, description);
		this.latitude = latitude;
		this.longitude = longitude;
		this.visible = visible;
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

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
