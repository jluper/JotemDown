package com.DataFinancial.NoteJackal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

	private EditText address;
	private Button btnBackup;
	private EditText googlePW;
	private EditText backupFile;
	private Button btnBackupAndSend;
	// public static final String ADDRESS = "address";
	private Note note = new Note();

	private static final String LAST_BACKUP_FILE = "LAST_BACKUP_FILE";
	public static final String DATABASE_NAME = "notes.db";
	protected List<Note> notes = new ArrayList<Note>();

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

		backupFile = (EditText) findViewById(R.id.txtBackupFile);
		address = (EditText) findViewById(R.id.txtBackupAddress);
		
		SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
		String file = prefs.getString(LAST_BACKUP_FILE, "NoteJackalBackup");
		String addr = prefs.getString(SendNote.LAST_SEND_ADDRESS, null);

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
	public void onResume() {
		super.onResume(); // Always call the superclass method first

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public void addListenerBackupButton() {

		btnBackup = (Button) findViewById(R.id.btnBackup);

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
					Intent i = new Intent(BackupNotes.this, DriveActivity.class);
					i.putExtra("filepath", filePath);
					startActivity(i);
					
					//store on Google Drive
				}
					
					SharedPreferences prefs = getSharedPreferences(
							LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(SendNote.LAST_SEND_ADDRESS, address
							.getText().toString());
					editor.putString(LAST_BACKUP_FILE, backupFile.getText()
							.toString());

					editor.commit();
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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

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
				}
			}
		});
	}

	private void sendDatabaseBackup() {

		String[] TO = { "jluper@triad.rr.com" };
		String[] CC;

		TO[0] = address.getText().toString();

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.setType("text/plain");

		String emailSubject;
		String emailText;

		if (Utils.isValidEmail(TO[0])) {

			String fileName = backupFile.getText().toString() + ".db";
			File file = new File(getBackupFileDir(), fileName);

			if (!file.exists() || !file.canRead()) {
				Toast.makeText(this,
						"Backup file does not exist or not readable...",
						Toast.LENGTH_LONG).show();
				return;
			}

			Uri uri = Uri.parse(file.toString());
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
			emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Jot'emDown backup");
			emailIntent.putExtra(Intent.EXTRA_TEXT, "Backup file attached.");

			startActivity(Intent.createChooser(emailIntent, "Send mail..."));

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

		FileChannel source = null;
		FileChannel destination = null;
		String dbName = DATABASE_NAME;

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
			Toast.makeText(this, "Unable to backup database. " + e.getMessage(),	Toast.LENGTH_LONG).show();
		}
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
