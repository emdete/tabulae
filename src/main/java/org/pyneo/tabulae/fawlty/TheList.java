package org.pyneo.tabulae.fawlty;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

public class TheList implements Constants, JSONStreamAware, JSONAware, Iterable<TheDictionary> {
	private java.util.AbstractList<TheDictionary> list = new JSONArray();

	public TheList() {
	}

	public TheList(JSONArray arr) {
		for (Object obj: arr) {
			this.list.add(new TheDictionary((JSONObject)obj));
		}
	}

	@Override
	public Iterator<TheDictionary> iterator() {
		return this.list.iterator();
	}

	public int size() {
		return list.size();
	}

	public boolean add(TheDictionary e) {
		return this.list.add(e);
	}

	public String toJSONString() {
		return ((JSONArray)this.list).toJSONString();
	}

	public String toString() {
		return this.list == null ? null : this.list.toString();
	}

	public void writeJSONString(Writer out) throws IOException {
		((JSONArray)this.list).writeJSONString(out);
	}
}
