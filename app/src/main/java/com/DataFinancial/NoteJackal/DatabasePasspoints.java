package com.DataFinancial.NoteJackal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class DatabasePasspoints extends SQLiteOpenHelper {

	private static final String POINTS_TABLE = "POINTS";
	private static final String COL_ID = "ID";
	private static final String COL_X = "X";
	private static final String COL_Y = "Y";

	public DatabasePasspoints(Context context) {
		super(context, "notes.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}
	
	
	public void createPointsTable() {
		
		 SQLiteDatabase db = this.getWritableDatabase();
		 
			String sqlPasspointsTable = String
					.format("create table if not exists %s (%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL, %s INTEGER NOT NULL)",
							POINTS_TABLE, COL_ID, COL_X, COL_Y);
			db.execSQL(sqlPasspointsTable);
	}	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*	  Log.w(MySQLiteHelper.class.getName(),
	      "Upgrading database from version " + oldVersion + " to "
	          + newVersion + ", which will destroy all old data");
	  db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
	  onCreate(db);*/	}
	
	public void storePoints(List<Point> points) {
		
		SQLiteDatabase db = getWritableDatabase();
		
		db.delete(POINTS_TABLE, null, null);
		
		int i = 0;
		for (Point point: points) {
			ContentValues values = new ContentValues();
			
			values.put(COL_ID, i);
			values.put(COL_X, point.x);
			values.put(COL_Y, point.y);
			
			db.insert(POINTS_TABLE,  null,  values);
			
			i++;
		}
		
		db.close();
	}

	public List<Point> getPoints() {
		
		List<Point> points = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		
		String sql = String.format("SELECT %s, %s FROM %s ORDER BY %s",  COL_X, COL_Y, POINTS_TABLE, COL_ID);
		
		Cursor cursor = db.rawQuery(sql, null);
		
		while (cursor.moveToNext()) {
			int x = cursor.getInt(0);
			int y = cursor.getInt(1);
			
			points.add(new Point(x,y));		

		}
		
		db.close();
		
		return(points);
	}
}
