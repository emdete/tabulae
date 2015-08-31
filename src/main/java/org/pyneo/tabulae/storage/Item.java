package org.pyneo.tabulae.storage;

public class Item {
	long id;

	public Item() {
		id = -1;
	}

	public Item(long id) {
		this.id = id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
}
