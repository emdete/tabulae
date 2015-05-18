package org.pyneo.maps.utils;

import android.database.Cursor;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CursorI implements Iterable<Cursor> {
	Cursor cursor;
	class IteratorI implements Iterator<Cursor> {
		Cursor cursor;
		IteratorI(Cursor cursor) {
			this.cursor = cursor;
		}
		public boolean hasNext() {
			if (cursor == null)
				return false;
			boolean ret = cursor.moveToNext();
			if (!ret)
				cursor.close();
			return ret;
			// getCount()
			// moveToPosition(offset)
		}
		public Cursor next() {
			return cursor;
		}
		public void remove() {
			throw new UnsupportedOperationException("can't remove from cursors");
		}
	}

	public CursorI(Cursor cursor) {
		this.cursor = cursor;
	}

	public IteratorI iterator() {
		IteratorI ret = new IteratorI(cursor);
		cursor = null; // make sure it's used once only
		return ret;
	}

	static void test() {
		CursorI cursorI = new CursorI(null);
		for (Cursor o: cursorI) {
			;
		}
	}
}
