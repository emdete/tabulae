package org.pyneo.tabulae.poi;

import org.pyneo.thinstore.StoreObject;

public class PoiItem extends StoreObject implements Constants {
	// @Unique @NotNull
	String name;
	String description;
	double latitude;
	double longitude;
	boolean visible;

	public PoiItem() {
	}

	public PoiItem(String name, String description, double latitude, double longitude, boolean visible) {
		this.name = name;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
		this.visible = visible;
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
