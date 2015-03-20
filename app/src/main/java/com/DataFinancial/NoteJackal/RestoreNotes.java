package com.DataFinancial.JotemDown;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestoreNotes extends ActionBarActivity {

    private static final String LAST_BACKUP_FILE = "LAST_BACKUP_FILE";
    protected List<Note> notes = new ArrayList<Note>();
    private Button btnRestore;
    private EditText restoreFile;
    private AlertDialog confirmRestore;
    // public static final String ADDRESS = "address";
    private Note note = new Note();
    private String lastFile;
    private TextView lblFileName;
    private int group;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_notes);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle("Restore");
        actionBar.setDisplayShowTitleEnabled(true);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            group = (extras.getInt("group"));
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }
        restoreFile = (EditText) findViewById(R.id.txtRestoreFile);
        lblFileName = (TextView) findViewById(R.id.lblFileName);

        SharedPreferences prefs = getSharedPreferences(com.DataFinancial.JotemDown.LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        lastFile = prefs.getString(LAST_BACKUP_FILE, "JotemDownBackup");

        if (lastFile != null) {
            restoreFile.setText(lastFile);
        }

        buildConfirmDialog();

        int textLength = restoreFile.getText().length();
        restoreFile.setSelection(textLength, textLength);

        addListenerRestoreButton();
        addListenerOnChkGoogleDrive();
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(RestoreNotes.this, com.DataFinancial.JotemDown.MainActivity.class);

        i.putExtra("group", group);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;
    }

    public void addListenerOnChkGoogleDrive() {

        CheckBox chk = (CheckBox) findViewById(R.id.chkGoogleDrive);

        chk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (((CheckBox) v).isChecked()) {
                    restoreFile.setText("");
                    restoreFile.setEnabled(false);
                    restoreFile.setVisibility(View.INVISIBLE);
                    btnRestore = (Button) findViewById(R.id.btnRestore);
                    btnRestore.setText(R.string.btn_next);
                    lblFileName.setVisibility(View.INVISIBLE);

                } else {
                    restoreFile.setEnabled(true);
                    restoreFile.setText(lastFile);
                    btnRestore = (Button) findViewById(R.id.btnRestore);
                    btnRestore.setText(R.string.btn_restore);
                    restoreFile.setVisibility(View.VISIBLE);
                    lblFileName.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume(); // Always call the superclass method first

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public void buildConfirmDialog() {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle("Confirm Restore");
        dlgBuilder.setIcon(R.drawable.btn_check_buttonless_on);
        dlgBuilder.setMessage(R.string.dialog_restore_notes);
        dlgBuilder.setCancelable(true);

        dlgBuilder.setPositiveButton(R.string.dialog_positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        CheckBox chk = (CheckBox) findViewById(R.id.chkGoogleDrive);

                        if (chk.isChecked()) {
                            Intent i = new Intent(RestoreNotes.this, com.DataFinancial.JotemDown.DriveActivity.class);

                            i.putExtra("group", group);
                            i.putExtra("group_name", groupName);
                            i.putExtra("sort_col", sortCol);
                            i.putExtra("sort_name", sortName);
                            i.putExtra("sort_dir", sortDir);
                            startActivity(i);
                        } else {
                            if (restoreFromLocalBackup(restoreFile.getText().toString(), null)) {

                                Intent i = new Intent(RestoreNotes.this, com.DataFinancial.JotemDown.MainActivity.class);
                                i.putExtra("group", group);
                                i.putExtra("group_name", groupName);
                                i.putExtra("sort_col", sortCol);
                                i.putExtra("sort_name", sortName);
                                i.putExtra("sort_dir", sortDir);
                                startActivity(i);
                            }
                        }
                    }
                });

        dlgBuilder.setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                    }
                });

        confirmRestore = dlgBuilder.create();
    }

    public void addListenerRestoreButton() {

        btnRestore = (Button) findViewById(R.id.btnRestore);

        btnRestore.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                confirmRestore.show();
            }

        });
    }

    private boolean restoreFromLocalBackup(String fileName, String dbName) {

        if (checkExternalMedia()) {

            String sourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + fileName + ".db";
            File sourceFile = new File(sourcePath);

            File destinationFile;
            if (dbName != null) {
                destinationFile = new File(dbName);
            } else {
                destinationFile = getDatabasePath(com.DataFinancial.JotemDown.BackupNotes.DATABASE_NAME);
            }

            if (!sourceFile.exists() || !sourceFile.canRead()) {
                Toast.makeText(this, "File not found...", Toast.LENGTH_LONG).show();

                return false;
            }

            com.DataFinancial.JotemDown.Utils util = new com.DataFinancial.JotemDown.Utils();
            sourceFile.setWritable(true);
            try {
                util.copyFile(sourceFile, destinationFile);
            } catch (IOException e) {
                Toast.makeText(RestoreNotes.this, "Exception restoring notes from backup: " + e.toString(), Toast.LENGTH_LONG).show();
            }

            SharedPreferences prefs = getSharedPreferences(
                    com.DataFinancial.JotemDown.LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LAST_BACKUP_FILE, restoreFile.getText().toString());
            editor.commit();
        }

        return true;
    }


    private boolean checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
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
