package com.DataFinancial.JotemDown;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public static final String COL_GROUP = "GRP";
    public static final String NOTES_DB = "notes.db";
    public final String TABLE_NOTES = "NOTES";
    public final String TABLE_GROUPS = "GROUPS";
    public static final String COL_NAME = "NAME";
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

        String sqlNotesTable = String.format("create table if not exists %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER)",
                TABLE_NOTES, COL_ID, COL_PRIORITY, COL_BODY, COL_CREATE_DATE, COL_EDIT_DATE, COL_LAT, COL_LON, COL_REMINDER, COL_IMAGE, COL_GROUP);

        db.execSQL(sqlNotesTable);

        if (isNotesTableEmpty()) {
            InputStream in;
            try {
                AssetManager assetManager = context.getAssets();
                in = assetManager.open(MainActivity.HELP_FILE);
                importNotesFromAssets(in);
                in.close();
            } catch (IOException e) {
                Toast.makeText(context, "Exception importing help notes: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void createGroupsTable() {

        db = this.getWritableDatabase();

        String sqlGroupsTable = String.format("create table if not exists %s (%s INTEGER PRIMARY KEY, %s TEXT)", TABLE_GROUPS, COL_ID, COL_NAME);

        db.execSQL(sqlGroupsTable);

        if (isGroupsTableEmpty()) {
            NoteGroup ng = new NoteGroup("General");
            addGroup(ng);
        }
        //TEMP

//        NoteGroup group = new NoteGroup("ROOT");
//        addGroup(group);
//        group.setName("Personal");
//        addGroup(group);
//        group.setName("Work");
//        addGroup(group);
//        group.setName("Book Club");
//        addGroup(group);
//        group.setName("Pets");
//        addGroup(group);
//        group.setName("School");
//        addGroup(group);
//        group.setName("Travel");
//        addGroup(group);
//        group.setName("Friends");
//        addGroup(group);

    }

    public List<Note> getNotes(String search, String order, String dir, int group) {
        List<Note> notes = new ArrayList<>();

        db = this.getWritableDatabase();

        String query;

        //String helpText = (String) this.context.getResources().getText(R.string.txt_help_search);txt_help_search
        String helpText = (String) context.getResources().getText(R.string.txt_help_search); //context.getResources().getString(R.string.txt_help_search);
        if (search == null) {
            if (group != ExportNotes.NO_GROUP) {
                query = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_GROUP + " = " + group + " AND " + COL_BODY + " NOT LIKE '%" + helpText + "%' ORDER BY " + order + " " + dir;
            } else {
                query = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_BODY + " NOT LIKE '%" + helpText + "%' ORDER BY " + order + " " + dir;
            }
        } else {
            if (group != ExportNotes.NO_GROUP) {
                query = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COL_BODY + " LIKE '%" + search + "%' AND " + COL_GROUP + " = " + group + " ORDER BY " + order + " " + dir;
            } else {
                query = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COL_BODY + " LIKE '%" + search + "%'  ORDER BY " + order + " " + dir;
            }
        }

        //Log.d(MainActivity.DEBUGTAG, "Query = " + query);
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
                note.setGroup(cursor.getInt(9));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        return notes;
    }

    public List<Note> getNotesByGroupId(int groupId, String order, String dir) {
        List<Note> notes = new ArrayList<>();

        db = this.getWritableDatabase();

        String query;

        query = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_GROUP + " = " + groupId + " ORDER BY " + order + " " + dir;
        //Log.d(MainActivity.DEBUGTAG, "Query = " + query);
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
                note.setGroup(cursor.getInt(9));

                notes.add(note);

            } while (cursor.moveToNext());
        }

        return notes;
    }
    public List<Note> getNotesByGroupID(int id, String order, String dir) {
        List<Note> notes = new ArrayList<>();

        db = this.getWritableDatabase();

        String query;

        query = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_GROUP + " = " + id + " ORDER BY " + order + " " + dir;

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
                note.setGroup(cursor.getInt(9));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        return notes;
    }

    public List<NoteGroup> getGroups(String order, String dir) {
        List<NoteGroup> groups = new ArrayList<>();

        db = this.getWritableDatabase();

        String query;
        if (order == null) {
            query = "SELECT * FROM " + TABLE_GROUPS +  " ORDER BY " + order + " " + dir;;
        } else {
            query = "SELECT * FROM " + TABLE_GROUPS +  " ORDER BY " + order + " " + dir;
        }


        Cursor cursor = db.rawQuery(query, null);

        NoteGroup group;
        if (cursor.moveToFirst()) {
            do {
                group = new NoteGroup();
                group.setId(cursor.getInt(0));
                group.setName(cursor.getString(1));

                groups.add(group);
            } while (cursor.moveToNext());
        }

        return groups;
    }

    public void recreateNotesTable() {

        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        createNotesTable();
    }

    public void recreateGroupsTable() {

        db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        createGroupsTable();
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
        values.put(COL_GROUP, note.getGroup());

        long rowId = -1;
        try {
            rowId = db.insert(TABLE_NOTES, null, values);
        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }

    public long addGroup(NoteGroup grp) {

        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        //values.put(COL_ID, note.getId());
        values.put(COL_NAME, grp.getName());

        long rowId = -1;
        try {
            rowId = db.insert(TABLE_GROUPS, null, values);
        } catch (Exception e) {
            return rowId;   // should be -1
        }

        db.close();

        return rowId;
    }

    public long deleteGroup(int id) {

        db = this.getWritableDatabase();

        long rowId = -1;
        try {
            db.delete(TABLE_GROUPS, COL_ID + "=" + id, null);

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

    public long updateGroup(NoteGroup grp) {

        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_NAME, grp.getName());

        long rowId = -1;
        try {
            rowId = db.update(TABLE_GROUPS, values, COL_ID + "=" + grp.getId(), null);
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
        values.put(COL_GROUP, note.getGroup());

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
            note.setGroup(cursor.getInt(9));
        }

        return note;
    }

    public boolean isNotesTableEmpty() {

        db = this.getWritableDatabase();

        boolean empty = true;
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTES, null);
        if (cur != null && cur.moveToFirst()) {
            empty = (cur.getInt(0) == 0);
        }
        cur.close();

        return empty;
    }

    public boolean isGroupsTableEmpty() {

        db = this.getWritableDatabase();

        boolean empty = true;
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_GROUPS, null);
        if (cur != null && cur.moveToFirst()) {
            empty = (cur.getInt(0) == 0);
        }
        cur.close();

        return empty;
    }

    public boolean importNotesFromAssets(InputStream in) {

        String line;
        List<Note> notes = new ArrayList<>();
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd");
            Date curr_date = new Date();

            String today = dateFormat.format(curr_date);

            //add first help for version name and date of installation etc.
            Note verNote = new Note();
            verNote.setId(1);
            verNote.setPriority(0);
            verNote.setCreateDate(today);
            verNote.setEditDate(today);

            String versionName = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionName;
            int versionCode = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionCode;

            verNote.setBody("HELP: &nbsp <strong>Version Information</strong> <br> Version Name: " + versionName + " <br> Version Code: " + versionCode);
            //addNote(verNote);
            notes.add(verNote);

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((line = br.readLine()) != null && !line.isEmpty()) {

                Note note = new Note();
                note.setId(1);
                note.setPriority(0);
                note.setCreateDate(today);
                note.setEditDate(today);
                note.setBody(line.replace("|", "\n").replace("~", "|"));
                notes.add(note);
            }

            for (int i = 0; i < notes.size(); i++) {
                addNote(notes.get(i));
            }

        } catch (Exception e) {
            //Log.d(MainActivity.DEBUGTAG, "Exception adding Help: " + e.getMessage());
            return false;
        }

        return true;
    }

//    public void clearNotesTable() {
//
//        //SQLiteDatabase db = this.getWritableDatabase();
//        db = this.getWritableDatabase();
//        db.delete(TABLE_NOTES, null, null);
//    }
}
