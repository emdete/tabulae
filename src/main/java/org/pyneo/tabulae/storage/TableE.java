package org.pyneo.tabulae.storage;

import android.database.Cursor;
import android.util.Log;

/**
 * Helper class to get some compiler validation into database access.
 */
public class TableE implements Constants {
	static final private String EQU = " = ";
	static final private String SEP = ", ";
	static final private String PLH = "@";

	public static String[] toString(final Object[] cols) {
		int i = 0;
		String[] ret = new String[cols.length];
		for (Object col : cols) {
			ret[i++] = col.toString();
		}
		return ret;
	}

	static String equalsList(final Object[] cols) {
		String stmnt = "";
		for (int i = 0; i < cols.length; ) {
			if (i > 0) stmnt += SEP;
			stmnt += cols[i] + EQU + (PLH + ++i);
		}
		return stmnt;
	}

	static String placeholderList(final Object[] cols) {
		String stmnt = "";
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) stmnt += SEP;
			stmnt += (PLH + ++i);
		}
		return stmnt;
	}

	public static String fieldList(final Object[] cols, boolean doId) {
		String stmnt = "";
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) stmnt += SEP;
			stmnt += cols[i];
			if (doId && i == 0) stmnt += " _id";
		}
		return stmnt;
		//return TextUtils.join(", ", cols);
	}

	static public String selectStatement(Class table, Object[] cols, Object[] where, Object[] order) {
		String stmnt = "SELECT " + fieldList(cols, true) + " FROM " + table.getSimpleName();
		if (where != null) stmnt += " WHERE " + equalsList(where);
		if (order != null) stmnt += " ORDER BY " + fieldList(order, false);
		if (DEBUG) Log.d(TAG, "stmnt=" + stmnt);
		return stmnt;
	}

	static public String dropStatement(Class table, Object[] cols) {
		String stmnt = "DROP TABLE " + table.getSimpleName();
		if (DEBUG) Log.d(TAG, "stmnt=" + stmnt);
		return stmnt;
	}

	static public String insertStatement(Class table, Object[] cols) {
		String stmnt = "INSERT INTO TABLE " + table.getSimpleName() + " (" + fieldList(cols, false) + ") VALUES (" + placeholderList(cols) + ")";
		if (DEBUG) Log.d(TAG, "stmnt=" + stmnt);
		return stmnt;
	}

	static public String updateStatement(Class table, Object[] cols, Object[] where) {
		String stmnt = "UPDATE TABLE " + table.getSimpleName() + " SET " + equalsList(cols);
		if (where != null) stmnt += " WHERE " + equalsList(where);
		if (DEBUG) Log.d(TAG, "stmnt=" + stmnt);
		return stmnt;
	}

	static public String deleteStatement(Class table, Object[] where) {
		String stmnt = "DELETE FROM TABLE " + table.getSimpleName();
		if (where != null) stmnt += " WHERE " + equalsList(where);
		if (DEBUG) Log.d(TAG, "stmnt=" + stmnt);
		return stmnt;
	}

	public static void test(Cursor cursor) {
		for (thing e : thing.values()) {
			if (DEBUG) Log.d(TAG, "TableE.test(): name=" + e.name() + ", ordinal=" + e.ordinal());
			switch (e) {
				case id:
					break;
			}
		}
		if (DEBUG) Log.d(TAG, "TableE.test(): valueOf=" + thing.valueOf("NAME"));
	/*
		if (DEBUG) {
			Log.d(TAG, "TableE.test(): createStatement=" + TableE.createStatement(thing.class, thing.values()));
		} */
		if (DEBUG) {
			Log.d(TAG, "TableE.test(): insertStatement=" + TableE.insertStatement(thing.class, thing.values()));
		}
		if (DEBUG) {
			Log.d(TAG, "TableE.test(): updateStatement=" + TableE.updateStatement(thing.class, thing.values(), null));
		}
		if (DEBUG) {
			Log.d(TAG, "TableE.test(): selectStatement=" + TableE.selectStatement(thing.class, thing.values(), null, new Object[]{thing.id}));
		}
		if (DEBUG) {
			Log.d(TAG, "TableE.test(): deleteStatement=" + TableE.deleteStatement(thing.class, new Object[]{thing.id}));
		}
		if (DEBUG) {
			Log.d(TAG, "TableE.test(): dropStatement=" + TableE.dropStatement(thing.class, thing.values()));
		}
		if (cursor != null) {
			cursor.getInt(thing.id.ordinal());
		}
	}

	/*
	* here the sample code starts. the columns of a table are defined as a java
	* enum. from that you can create the statements and use the ordinal for the
	* cursor getters. this method does not support col types (a dd) because
	* sqlite doesn't have one. sqlite has types of cols per row.
	*/
	enum thing {
		id, lat, lon, name, description
	}
}
