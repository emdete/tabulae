package org.pyneo.tabulae.storage;

import android.database.Cursor;

import java.util.Iterator;

public class CursorI implements Iterable<Cursor>, Constants {
	Cursor cursor;

	public CursorI(Cursor cursor) {
		this.cursor = cursor;
	}

	/**
	 * short sample on how to use the above code. the foreach will work, if a
	 * cursor is wrapped into a CursorI as shown below.
	 */
	static void test(Cursor cursor) {
		for (Cursor o : new CursorI(cursor)) {
			; // do something with the row
		}
	}

	public IteratorI iterator() {
		IteratorI ret = new IteratorI(cursor);
		cursor = null; // make sure it's used once only
		return ret;
	}

	class IteratorI implements Iterator<Cursor> {
		Cursor cursor;

		IteratorI(Cursor cursor) {
			this.cursor = cursor;
		}

		public boolean hasNext() {
			if (cursor == null) return false;
			boolean ret = cursor.moveToNext();
			if (!ret) {
				cursor.close();
				cursor = null;
			}
			return ret;
			// getCount() moveToPosition(offset)
		}

		public Cursor next() {
			return cursor;
		}

		public void remove() {
			throw new UnsupportedOperationException("can't remove from cursors");
		}
	}
}
