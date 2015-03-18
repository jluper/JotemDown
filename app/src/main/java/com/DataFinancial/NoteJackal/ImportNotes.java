package com.DataFinancial.NoteJackal;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportNotes extends ActionBarActivity {

    public static final int IMPORT_SUCCESSFUL = 1;
    private static final String LAST_IMPORT_FILE = "LAST_IMPORT_FILE";
    protected List<Note> notes = new ArrayList<>();
    private EditText importFile;
    private AlertDialog confirmImport;
    private int group;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_notes);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle("Import Notes");
        actionBar.setDisplayShowTitleEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = (extras.getInt("group"));
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }

        importFile = (EditText) findViewById(R.id.txtImportFile);

        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        String file = prefs.getString(LAST_IMPORT_FILE, "jedimportfile.txt");

        if (file != null) {
            importFile.setText(file);
        }

        buildConfirmDialog();

        int textLength = importFile.getText().length();
        importFile.setSelection(textLength, textLength);

        addListenerImportButton();
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(ImportNotes.this, MainActivity.class);

        i.putExtra("group", group);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;

    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void buildConfirmDialog() {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle("Confirm Import");
        dlgBuilder.setIcon(R.drawable.btn_check_buttonless_on);
        dlgBuilder.setMessage(R.string.dialog_import_notes);
        dlgBuilder.setCancelable(true);

        dlgBuilder.setPositiveButton(R.string.dialog_positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        importFromFile();
                        //if () {
                            //Intent i = new Intent(ImportNotes.this, MainActivity.class);
                            //startActivity(i);
                        //}

                    }
                });

        dlgBuilder.setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
            }
        });

        confirmImport = dlgBuilder.create();
    }

    public void addListenerImportButton() {

        Button btnImport = (Button) findViewById(R.id.btnImport);

        btnImport.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                confirmImport.show();
            }
        });
    }


    private void importFromFile() {

        final ProgressDialog ringProgressDialog;
        ringProgressDialog = ProgressDialog.show(ImportNotes.this, "Please wait ...", "Importing notes from file...", true);

        if (checkExternalMedia()) {

            boolean returnCode = false;
            importFile = (EditText) findViewById(R.id.txtImportFile);
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = importFile.getText().toString();
            final File file;
            if (fileName.substring(fileName.length() - 4).equals(".txt")) {
                file = new File(dir, importFile.getText().toString());
            } else {
                file = new File(dir, importFile.getText().toString() + ".txt");
            }

            if (!file.exists() || !file.canRead()) {
                ringProgressDialog.dismiss();
                Toast.makeText(this, "File not found...", Toast.LENGTH_LONG).show();
            }

            final DatabaseNotes db = new DatabaseNotes(this);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg;
                    String line;
                    Bundle bundle = new Bundle();
                    try {
                        // Open the file that is the first
                        // command line parameter
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd");
                        Date curr_date = new Date();

                        String today = dateFormat.format(curr_date);

                        FileInputStream fstream = new FileInputStream(file.getPath());
                        // Get the object of DataInputStream
                        DataInputStream in = new DataInputStream(fstream);
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
                            db.addNote(notes.get(i));
                        }

                        in.close();
                        ringProgressDialog.dismiss();

                        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(LAST_IMPORT_FILE, importFile.getText().toString());
                        editor.apply();

                        msg = handler.obtainMessage();
                        bundle.putInt("import_status", IMPORT_SUCCESSFUL);
                        bundle.putString("import_msg", "Import completed...");
                        msg.setData(bundle);
                        handler.sendMessage(msg);

                    } catch (Exception e) {
                        ringProgressDialog.dismiss();
                        msg = handler.obtainMessage();
                        bundle.putInt("import_status", 0);
                        bundle.putString("import_msg", "Exception importing notes." + e.getMessage());
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }
            });

            t.start();
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            int status = bundle.getInt("import_status");
            String message = bundle.getString("import_msg");

            if(status == IMPORT_SUCCESSFUL)
            {
                Toast.makeText(ImportNotes.this, message, Toast.LENGTH_LONG).show();
                Intent i = new Intent(ImportNotes.this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(ImportNotes.this, message, Toast.LENGTH_LONG).show();
            }
        }
    };

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

