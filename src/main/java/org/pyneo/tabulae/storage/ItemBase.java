package org.pyneo.tabulae.storage;

public class ItemBase {
	long id;

	public ItemBase(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
