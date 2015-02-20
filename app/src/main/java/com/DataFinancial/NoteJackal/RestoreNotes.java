package com.DataFinancial.NoteJackal;

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

	private Button btnRestore;
	private EditText restoreFile;
	private AlertDialog confirmRestore;
	// public static final String ADDRESS = "address";
	private Note note = new Note();
	private String lastFile;
	private TextView lblFileName;
	
	private static final String LAST_BACKUP_FILE = "LAST_BACKUP_FILE";
	protected List<Note> notes = new ArrayList<Note>();

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

		restoreFile = (EditText) findViewById(R.id.txtRestoreFile);
		lblFileName = (TextView) findViewById(R.id.lblFileName);
		
		SharedPreferences prefs = getSharedPreferences(
		LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
		lastFile = prefs.getString(LAST_BACKUP_FILE, "NoteJackalBackup");

		if (lastFile != null) {
			restoreFile.setText(lastFile);
		}

		buildConfirmDialog();

		int textLength = restoreFile.getText().length();
		restoreFile.setSelection(textLength, textLength);

		addListenerRestoreButton();
		addListenerOnChkGoogleDrive();
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
							Intent i = new Intent(RestoreNotes.this, DriveActivity.class);
							startActivity(i);
						} else {							
						if (restoreFromLocalBackup(restoreFile.getText().toString(), null)) {
														
							Intent i = new Intent(RestoreNotes.this, MainActivity.class);
							startActivity(i);
						}
						}
					}
				});

		dlgBuilder.setNegativeButton(R.string.dialog_negative,
				new DialogInterface.OnClickListener() {
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
            if (dbName != null ) {
            	 destinationFile = new File(dbName);
            } else {
            	 destinationFile = getDatabasePath(BackupNotes.DATABASE_NAME);
            }
              
			if (!sourceFile.exists() || !sourceFile.canRead()) {
				Toast.makeText(this, "File not found...", Toast.LENGTH_LONG).show();
				
				return false;
			}

            Utils util = new Utils();
			sourceFile.setWritable(true); 
			try {
				util.copyFile(sourceFile, destinationFile);
			} catch (IOException e) {
				Toast.makeText(RestoreNotes.this, "Unable to restore notes...", Toast.LENGTH_LONG).show();
			} 
			
    	SharedPreferences prefs = getSharedPreferences(
		LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
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
