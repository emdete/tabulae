package org.pyneo.maps.utils;

import org.pyneo.maps.utils.Ut;
import android.text.TextUtils;
import android.database.Cursor;

/**
* Helper class to get some compiler validation into database access.
*/
public class TableE {
	static final private String EQU = " = ";
	static final private String SEP = ", ";
	static final private String PLH = "?";

	public static String[] toString(final Object[] cols) {
		int i = 0;
		String[] ret = new String[cols.length];
		for (Object col: cols) {
			ret[i++] = col.toString();
		}
		return ret;
	}

	public static String equalsList(final Object[] cols) {
		String stmnt = "";
		for (int i=0;i<cols.length;i++) {
			if (i>0) stmnt += SEP;
			stmnt += cols[i] + EQU + PLH;
		}
		return stmnt;
	}

	public static String placeholderList(final Object[] cols) {
		String stmnt = "";
		for (int i=0;i<cols.length;i++) {
			if (i>0) stmnt += SEP;
			stmnt += PLH;
		}
		return stmnt;
	}

	public static String fieldList(final Object[] cols) {
		return TextUtils.join(", ", cols);
	}

	static public String selectStatement(Class table, Object[] cols, Object[] where, Object[] order) {
		String stmnt = "SELECT " + fieldList(cols) + " FROM " + table.getSimpleName();
		if (where != null)
			stmnt += " WHERE " + equalsList(where);
		if (order != null)
			stmnt += " ORDER BY " + fieldList(order);
		return stmnt;
	}

	static public String createStatement(Class table, Object[] cols) {
		String stmnt = "CREATE TABLE " + table.getSimpleName() + " (" + fieldList(cols) + ")";
		// TODO: add DEFAULT / NOT NULL PRIMARY KEY UNIQUE
		return stmnt;
	}

	static public String dropStatement(Class table, Object[] cols) {
		String stmnt = "DROP TABLE " + table.getSimpleName();
		return stmnt;
	}

	static public String insertStatement(Class table, Object[] cols) {
		String stmnt = "INSERT INTO TABLE " + table.getSimpleName() + " (" + fieldList(cols) + ") VALUES (" + placeholderList(cols) + ")";
		return stmnt;
	}

	static public String updateStatement(Class table, Object[] cols, Object[] where) {
		String stmnt = "UPDATE TABLE " + table.getSimpleName() + " SET " + equalsList(cols);
		if (where != null)
			stmnt += " WHERE " + equalsList(where);
		return stmnt;
	}

	static public String deleteStatement(Class table, Object[] cols, Object[] where) {
		String stmnt = "DELETE FROM TABLE " + table.getSimpleName();
		if (where != null)
			stmnt += " WHERE " + equalsList(where);
		return stmnt;
	}

	/*
	* here the sample code start. the columns of a table are defined as a java
	* enum. from that you can create the statements and use the ordinal for the
	* cursor getters. this method does not support col types (a dd) because
	* sqlite doesn't have one. sqlite has types of cols per row.
	*/
	enum thing {ID, LAT, LON, NAME, DESCRIPTION};

	public static void test(Cursor cursor ) {
		for (thing e: thing.values()) {
			Ut.i("TableE.test(): name=" + e.name() + ", ordinal=" + e.ordinal());
			switch (e) {
				case ID:
				break;
			}
		}
		Ut.i("TableE.test(): valueOf=" + thing.valueOf("NAME"));
		Ut.i("TableE.test(): createStatement=" + TableE.createStatement(thing.class, thing.values()));
		Ut.i("TableE.test(): insertStatement=" + TableE.insertStatement(thing.class, thing.values()));
		Ut.i("TableE.test(): updateStatement=" + TableE.updateStatement(thing.class, thing.values(), null));
		Ut.i("TableE.test(): selectStatement=" + TableE.selectStatement(thing.class, thing.values(), null, new Object[]{thing.ID}));
		Ut.i("TableE.test(): deleteStatement=" + TableE.deleteStatement(thing.class, thing.values(), new Object[]{thing.ID}));
		Ut.i("TableE.test(): dropStatement=" + TableE.dropStatement(thing.class, thing.values()));
		if (cursor != null) {
			cursor.getInt(thing.ID.ordinal());
		}
	}
}
