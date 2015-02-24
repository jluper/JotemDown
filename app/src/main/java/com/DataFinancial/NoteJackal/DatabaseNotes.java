package com.DataFinancial.NoteJackal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseNotes extends SQLiteOpenHelper {

    public static final String COL_ID = "ID";
    public static final String COL_PRIORITY = "PRIORITY";
    public static final String COL_BODY = "BODY";
    public static final String COL_CREATE_DATE = "CREATEDATE";
    public static final String COL_EDIT_DATE = "EDITDATE";
    public static final String COL_LAT = "LATITUDE";
    public static final String COL_LON = "LONGITUDE";
    public static final String COL_REMINDER = "REMINDER";
    public static final String COL_IMAGE = "IMAGE";
    public final String TABLE_NOTES = "NOTES";
    public static final String NOTES_DB = "notes.db";
    private SQLiteDatabase db;
    private Context context;

    public DatabaseNotes(Context context) {
        super(context, "notes.db", null, 1);
        this.context = context;
    }

    public String getDbPath() {

        db = this.getWritableDatabase();
        String dbPath = db.getPath();

        return dbPath;
    }

    public SQLiteDatabase getDb() {

        return this.getWritableDatabase();
    }


    public void setDb(SQLiteDatabase db) {

        this.db = db;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }


    public void createNotesTable() {

        db = this.getWritableDatabase();

        String sqlNotesTable = String.format("create table if not exists %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)",
                TABLE_NOTES, COL_ID, COL_PRIORITY, COL_BODY, COL_CREATE_DATE, COL_EDIT_DATE, COL_LAT, COL_LON, COL_REMINDER, COL_IMAGE);

        db.execSQL(sqlNotesTable);
    }

    public List<Note> getAllNotes(String select, String order, String dir) {
        List<Note> notes = new ArrayList<>();

        db = this.getWritableDatabase();

        String query;

        String searchText = (String) this.context.getResources().getText(R.string.txt_help_search);

        if (select == null) {
            query = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_BODY + " NOT LIKE '%" + searchText + "%' " + " ORDER BY " + order + " " + dir;
        } else {
            query = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COL_BODY + " LIKE '%" + select + "%' " + "ORDER BY " + order + " " + dir;
        }

        Cursor cursor = db.rawQuery(query, null);

        Note note;
        if (cursor.moveToFirst()) {
            do {
                note = new Note();
                note.setId(cursor.getInt(0));
                note.setPriority(cursor.getInt(1));
                note.setBody(cursor.getString(2));
                note.setCreateDate(cursor.getString(3));
                note.setEditDate(cursor.getString(4));
                note.setLatitude(cursor.getString(5));
                note.setLongitude(cursor.getString(6));
                note.setHasReminder(cursor.getString(7));
                note.setImage(cursor.getString(8));

                notes.add(note);
            } while (cursor.moveToNext());
        }
        // return books
        return notes;
    }


    public void recreateNotesTable() {

        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        createNotesTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }


    public long addNote(Note note) {

        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        //values.put(COL_ID, note.getId());
        values.put(COL_PRIORITY, note.getPriority());
        values.put(COL_BODY, note.getBody());
        values.put(COL_CREATE_DATE, note.getCreateDate());
        values.put(COL_EDIT_DATE, note.getEditDate());
        values.put(COL_REMINDER, note.getHasReminder());
        values.put(COL_IMAGE, note.getImage());

        long rowId = -1;
        try {
            rowId = db.insert(TABLE_NOTES, null, values);
        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }


    public long deleteNote(int id) {

        db = this.getWritableDatabase();
        DatabaseReminders dbReminders = new DatabaseReminders(context);

        long rowId = -1;
        try {
            dbReminders.deleteReminderByNoteId(id);
            db.delete(TABLE_NOTES, COL_ID + "=" + id, null);

        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }


    public long updateNote(Note note) {

        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        //values.put(COL_ID, note.getId());
        values.put(COL_PRIORITY, note.getPriority());
        values.put(COL_BODY, note.getBody());
        values.put(COL_CREATE_DATE, note.getCreateDate());
        values.put(COL_EDIT_DATE, note.getEditDate());
        values.put(COL_LAT, note.getLatitude());
        values.put(COL_LON, note.getLongitude());
        values.put(COL_REMINDER, note.getHasReminder());
        values.put(COL_IMAGE, note.getImage());

        long rowId = -1;
        try {
            rowId = db.update(TABLE_NOTES, values, COL_ID + "=" + note.getId(), null);
        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }


    public Note getNote(int id) {

        db = this.getWritableDatabase();

        String query = "select * from " + TABLE_NOTES + " where " + COL_ID + "=" + id;
        Cursor cursor = db.rawQuery(query, null);

        Note note = new Note();
        if (cursor != null) {
            cursor.moveToFirst();

            note.setId(cursor.getInt(0));
            note.setPriority(cursor.getInt(1));
            note.setBody(cursor.getString(2));
            note.setCreateDate(cursor.getString(3));
            note.setEditDate(cursor.getString(4));
            note.setLatitude(cursor.getString(5));
            note.setLongitude(cursor.getString(6));
            note.setHasReminder(cursor.getString(7));
            note.setImage(cursor.getString(8));
        }

        return note;
    }

//    public void clearNotesTable() {
//
//        //SQLiteDatabase db = this.getWritableDatabase();
//        db = this.getWritableDatabase();
//        db.delete(TABLE_NOTES, null, null);
//    }
}
