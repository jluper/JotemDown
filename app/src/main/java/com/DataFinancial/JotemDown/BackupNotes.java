package com.DataFinancial.JotemDown;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class BackupNotes extends ActionBarActivity {

    public static final String DATABASE_NAME = "notes.db";
    private static final String LAST_BACKUP_FILE = "LAST_BACKUP_FILE";
    private static final String LAST_BACKUP_ADDRESS = "LAST_BACKUP_ADDRESS";
    protected List<Note> notes = new ArrayList<>();
    private EditText address;
    private EditText backupFile;
    private int group;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_notes);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle("Backup");
        actionBar.setDisplayShowTitleEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = (extras.getInt("group"));
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }

        backupFile = (EditText) findViewById(R.id.txtBackupFile);
        address = (EditText) findViewById(R.id.txtBackupAddress);

        SharedPreferences prefs = getSharedPreferences(com.DataFinancial.JotemDown.LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        String file = prefs.getString(LAST_BACKUP_FILE, "JotemDownBackup");
        String addr = prefs.getString(BackupNotes.LAST_BACKUP_ADDRESS, null);

        if (file != null) {
            backupFile.setText(file);
        }

        if (addr != null) {
            address.setText(addr);
        }

        int textLength = backupFile.getText().length();
        backupFile.setSelection(textLength, textLength);

        addListenerBackupButton();
        addListenerOnChkGoogleDrive();

    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(BackupNotes.this, com.DataFinancial.JotemDown.MainActivity.class);

        i.putExtra("group", group);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;

    }
    @Override
    public void onResume() {
        super.onResume(); // Always call the superclass method first

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void addListenerBackupButton() {

        Button btnBackup = (Button) findViewById(R.id.btnBackup);

        btnBackup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                CheckBox chk = (CheckBox) findViewById(R.id.chkGoogleDrive);

                if (!chk.isChecked()) {
                    makeDatabaseBackup();
                    sendDatabaseBackup();
                } else {
                    makeDatabaseBackup();

                    String filePath = getBackupFileDir().getAbsolutePath() + "/" + backupFile.getText().toString() + ".db";
                    Intent i = new Intent(BackupNotes.this, com.DataFinancial.JotemDown.DriveActivity.class);
                    i.putExtra("filepath", filePath);
                    startActivity(i);

                    //store on Google Drive
                }

                SharedPreferences prefs = getSharedPreferences(
                        com.DataFinancial.JotemDown.LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(BackupNotes.LAST_BACKUP_ADDRESS, address.getText().toString());
                editor.putString(LAST_BACKUP_FILE, backupFile.getText().toString());

                editor.apply();
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_backup_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public void addListenerOnChkGoogleDrive() {

        CheckBox chk = (CheckBox) findViewById(R.id.chkGoogleDrive);

        chk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (((CheckBox) v).isChecked()) {

                    address.setText("");
                    address.setEnabled(false);
                } else {
                    address.setEnabled(true);
                }
            }
        });
    }

    private void sendDatabaseBackup() {

        String[] TO = {"jotemdown.notes@gmail.com"};

        TO[0] = address.getText().toString();

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        if (com.DataFinancial.JotemDown.Utils.isValidEmail(TO[0])) {

            String fileName = backupFile.getText().toString() + ".db";
            File file = new File(getBackupFileDir(), fileName);

            if (!file.exists() || !file.canRead()) {
                Toast.makeText(this,
                        "Backup file does not exist or not readable...",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Uri uri = Uri.parse(file.toString());

            com.DataFinancial.JotemDown.Utils utils = new com.DataFinancial.JotemDown.Utils();
            List<Intent> emailIntents = utils.filterIntents(this);
            for (Intent i : emailIntents) {
                i.setData(Uri.parse("mailto:" + TO[0]));
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.putExtra(Intent.EXTRA_EMAIL, TO);
                i.putExtra(Intent.EXTRA_SUBJECT, "JotemDown backup");
                i.putExtra(Intent.EXTRA_TEXT, "Backup file attached.");
            }
            Intent chooserIntent = Intent.createChooser(emailIntents.remove(0), "Select app to send...");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailIntents.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);

            //startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        } else {
            Toast.makeText(BackupNotes.this, "Invalid email...",
                    Toast.LENGTH_LONG).show();
        }
    }

    private File getBackupFileDir() {

        File backupDir;
        if (checkExternalMedia()) {
            backupDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            backupDir = getFilesDir();
        }

        return backupDir;
    }

    private void makeDatabaseBackup() {

        File backupDir;
        if (checkExternalMedia()) {
            backupDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            // Environment.getExternalStorageDirectory();
        } else {
            backupDir = getFilesDir();
        }

        FileChannel source;
        FileChannel destination;

        File currentDB = getDatabasePath(DATABASE_NAME);

        String fileName = backupFile.getText().toString() + ".db";

        File backupDB = new File(backupDir, fileName);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            Toast.makeText(this, "Exception creating notes backup: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkExternalMedia() {
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable & mExternalStorageWriteable;
    }

}
