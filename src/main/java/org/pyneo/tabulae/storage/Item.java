package org.pyneo.tabulae.storage;

public class Item extends ItemBase {
	String name;
	String description;

	public Item(String name, String description) {
		super(-1);
		this.name = name;
		this.description = description;
	}

	public Item(long id, String name, String description) {
		super(id);
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
}
