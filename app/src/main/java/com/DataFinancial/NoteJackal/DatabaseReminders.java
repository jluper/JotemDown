package com.DataFinancial.NoteJackal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseReminders extends SQLiteOpenHelper {

    private static final String COL_ID = "ID";
    private static final String COL_NOTEID = "NOTEID";
    private static final String COL_DATE = "REMDATE";
    private static final String COL_TIME = "REMTIME";
    private static final String COL_RECUR = "RECUR";
    private static final String COL_PHONE = "PHONE";
    private final String TABLE_REMINDERS = "REMINDERS";


    public DatabaseReminders(Context context) {

        super(context, "reminders.db", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }


    public void createRemindersTable() {

        SQLiteDatabase db = this.getWritableDatabase();

        String sqlRemindersTable = String
                .format("create table if not exists %s (%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL, %s STRING NOT NULL, %s STRING NOT NULL, %s STRING NOT NULL, %s STRING)",
                        TABLE_REMINDERS, COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE);

        db.execSQL(sqlRemindersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*	  Log.w(MySQLiteHelper.class.getName(),
          "Upgrading database from version " + oldVersion + " to "
	          + newVersion + ", which will destroy all old data");
	  db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
	  onCreate(db);*/
    }

    public long addReminder(Reminder reminder) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_NOTEID, reminder.getNoteId());
        values.put(COL_DATE, reminder.getDate());
        values.put(COL_TIME, reminder.getTime());
        values.put(COL_RECUR, reminder.getRecur());
        values.put(COL_PHONE, reminder.getPhone());

        long row = db.insert(TABLE_REMINDERS, null, values);

        db.close();

        return row;
    }

    public List<Reminder> getReminders() {

        List<Reminder> reminders = new ArrayList<Reminder>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s ORDER BY %s", COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE, TABLE_REMINDERS, COL_NOTEID);

        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            Reminder reminder = new Reminder();
            reminder.setId(cursor.getInt(0));
            reminder.setNoteId(cursor.getInt(1));
            reminder.setDate(cursor.getString(2));
            reminder.setTime(cursor.getString(3));
            reminder.setRecur(cursor.getString(4));
            reminder.setPhone(cursor.getString(5));

            reminders.add(reminder);
        }

        db.close();

        return (reminders);
    }

    public List<Reminder> getUnexpiredReminders() {

        List<Reminder> reminders = new ArrayList<Reminder>();
        SQLiteDatabase db = getReadableDatabase();

        SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
        Date today = new Date();
        String strToday = df.format(today);
        String[] strTodayParts = strToday.split(" ");
        String date = strTodayParts[0];
        String time = strTodayParts[1];

        String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s ORDER BY %s ASC", COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE, TABLE_REMINDERS, COL_DATE);

        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            Reminder reminder = new Reminder();
            reminder.setId(cursor.getInt(0));
            reminder.setNoteId(cursor.getInt(1));
            reminder.setDate(cursor.getString(2));
            reminder.setTime(cursor.getString(3));
            reminder.setRecur(cursor.getString(4));
            reminder.setPhone(cursor.getString(5));

            reminders.add(reminder);
        }

        db.close();

        return (reminders);
    }

    public Reminder getReminder(int noteID) {

        String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %s ORDER BY %s", COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE, TABLE_REMINDERS, COL_NOTEID, noteID, COL_NOTEID);

        SQLiteDatabase db = getReadableDatabase();
        Reminder reminder = new Reminder();

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            reminder.setId(cursor.getInt(0));
            reminder.setNoteId(cursor.getInt(1));
            reminder.setDate(cursor.getString(2));
            reminder.setTime(cursor.getString(3));
            reminder.setRecur(cursor.getString(4));
            reminder.setPhone(cursor.getString(5));
        } else {
            return null;
        }

        db.close();

        return (reminder);
    }

    public void recreateRemindersTable() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        createRemindersTable();
    }

    public long updateReminder(Reminder reminder) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_NOTEID, reminder.getNoteId());
        values.put(COL_DATE, reminder.getDate());
        values.put(COL_TIME, reminder.getTime());
        values.put(COL_RECUR, reminder.getRecur());
        values.put(COL_PHONE, reminder.getPhone());

        long rowId = -1;
        try {
            rowId = db.update(TABLE_REMINDERS, values, COL_ID + "=" + reminder.getId(), null);
        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }


    public long deleteReminder(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        long rowId = -1;
        try {
            db.delete(TABLE_REMINDERS, COL_ID + "=" + id, null);
        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }

    public long deleteReminderByNoteId(int noteId) {

        SQLiteDatabase db = this.getWritableDatabase();

        long rowId = -1;
        try {
            db.delete(TABLE_REMINDERS, COL_NOTEID + "=" + noteId, null);
        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }

}

