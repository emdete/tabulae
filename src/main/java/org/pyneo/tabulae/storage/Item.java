package org.pyneo.tabulae.storage;

public class Item {
	long _id;

	public Item() {
		_id = -1;
	}

	public Item(long _id) {
		this._id = _id;
	}

	public void setId(long _id) {
		this._id = _id;
	}

	public long getId() {
		return _id;
	}
}
