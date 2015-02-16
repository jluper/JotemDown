package com.DataFinancial.NoteJackal;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.util.Property;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

//import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentList;
import com.google.api.services.drive.model.ParentReference;

public class DriveActivity extends ActionBarActivity {
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int RESULT_STORE_FILE = 4;
	private static Uri mFileUri;
	private static Drive mService;
	private GoogleAccountCredential mCredential;
	private Context mContext;
	private List<File> mResultList;
	private ListView mListView;
	private String[] mFileArray;
	private String mDLVal;
	private ArrayAdapter mAdapter;
	private String uploadFilePath = null;
	private int selectedFilePosition = -1;
	private Button btnRestore;
	private DatabaseNotes notesDb = new DatabaseNotes(this);
	private TextView lblSelectFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drive_activity);

		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle("Google Drive");
		actionBar.setDisplayShowTitleEnabled(true);

		// Connect to Google Drive
		mCredential = GoogleAccountCredential.usingOAuth2(this,
				Arrays.asList(DriveScopes.DRIVE));
		// mCredential = GoogleAccountCredential.usingOAuth2(this,
		// Arrays.asList(DriveScopes.DRIVE_APPDATA));

		btnRestore = (Button) findViewById(R.id.btnRestoreDrive);
		addListenerRestoreButton();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			uploadFilePath = extras.getString("filepath");
			ListView files = (ListView) findViewById(R.id.lstFiles);
			files.setVisibility(View.INVISIBLE);
			btnRestore.setVisibility(View.INVISIBLE);
			lblSelectFile = (TextView) findViewById(R.id.lblSelectFile);
			lblSelectFile.setVisibility(View.INVISIBLE);
			startActivityForResult(mCredential.newChooseAccountIntent(),
					REQUEST_ACCOUNT_PICKER);
		} else {

			mContext = getApplicationContext();
			mListView = (ListView) findViewById(R.id.lstFiles);
			Log.d(MainActivity.DEBUGTAG, "mlistView = " + mListView.toString());
			OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
				public void onItemClick(AdapterView parent, View v,
						int position, long id) {

					btnRestore.setEnabled(true);
					selectedFilePosition = position;

				}
			};

			mListView.setOnItemClickListener(mMessageClickedHandler);
			// mListView.setSelector(R.drawable.list_item_selector);

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);

			startActivityForResult(mCredential.newChooseAccountIntent(),
					REQUEST_ACCOUNT_PICKER);

		}
	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
		// Log.d(MainActivity.DEBUGTAG, "in onresume DriveActivity");
	}

	public void addListenerRestoreButton() {

		final RestoreNotes restoreNotes = new RestoreNotes();

		btnRestore = (Button) findViewById(R.id.btnRestoreDrive);

		btnRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				downloadItemFromList(selectedFilePosition);

				Log.d(MainActivity.DEBUGTAG, "filename with ext = " + mDLVal);
				String fileName = mDLVal.substring(0, mDLVal.length() - 3);
				Log.d(MainActivity.DEBUGTAG, "filename wo ext = " + fileName);

				restoreFromLocalBackup(fileName, null);

				Intent i = new Intent(DriveActivity.this, MainActivity.class);
				startActivity(i);

			}

		});

	}

	private void getDriveContents() {

		final ProgressDialog ringProgressDialog = ProgressDialog.show(
				DriveActivity.this, "Please wait ...",
				"Getting list of files from Google Drive...", true);
		ringProgressDialog.setCancelable(true);

		Log.d(MainActivity.DEBUGTAG, "getDriveContents");
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				mResultList = new ArrayList<File>();
				mResultList.clear();
				com.google.api.services.drive.Drive.Files f1 = mService.files();
				com.google.api.services.drive.Drive.Files.List request = null;

				do {
					try {
						request = f1.list();

						// request.setQ("'appfolder' in parents");
						request.setQ("title contains '.db' and trashed = false");
						com.google.api.services.drive.model.FileList fileList = new com.google.api.services.drive.model.FileList();
						fileList.clear();
						fileList = request.execute();

						mResultList.addAll(fileList.getItems());

						for (File tmp : mResultList) {

							Log.d(MainActivity.DEBUGTAG, "results filename"
									+ tmp.getTitle());
						}

						request.setPageToken(fileList.getNextPageToken());
					} catch (UserRecoverableAuthIOException e) {
						startActivityForResult(e.getIntent(),
								REQUEST_AUTHORIZATION);
					} catch (IOException e) {
						showToast("Retrieval ERROR: " + e.toString());
						if (request != null) {
							request.setPageToken(null);
						}
					}
				} while (request.getPageToken() != null
						&& request.getPageToken().length() > 0);

				populateListView();
				ringProgressDialog.dismiss();
			}

		});
		t.start();
	}

	private void downloadItemFromList(int position) {
		mDLVal = (String) mListView.getItemAtPosition(position);
		// showToast("You just pressed: " + mDLVal);

		final ProgressDialog ringProgressDialog = ProgressDialog.show(
				DriveActivity.this, "Please wait ...",
				"Downloading file from Google Drive...", true);
		ringProgressDialog.setCancelable(true);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (File tmp : mResultList) {
					if (tmp.getTitle().equalsIgnoreCase(mDLVal)) {
						if (tmp.getDownloadUrl() != null
								&& tmp.getDownloadUrl().length() > 0) {
							try {
								com.google.api.client.http.HttpResponse resp = mService
										.getRequestFactory()
										.buildGetRequest(
												new GenericUrl(tmp
														.getDownloadUrl()))
										.execute();
								InputStream iStream = resp.getContent();
								try {

									final java.io.File file = new java.io.File(
											Environment
													.getExternalStoragePublicDirectory(
															Environment.DIRECTORY_DOWNLOADS)
													.getPath(), tmp.getTitle());
									// showToast("Downloading: " +
									// tmp.getTitle() + " to " + Environment
									// .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());

									storeFile(file, iStream);

								} finally {
									iStream.close();

								}

							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				ringProgressDialog.dismiss();
			}
		});
		t.start();
	}

	private void populateListView() {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				List<ParentReference> parents = new ArrayList<ParentReference>();
				mFileArray = new String[mResultList.size()];
				Log.d(MainActivity.DEBUGTAG, "mResultList size = "
						+ mResultList.size());
				int i = 0;
				for (File tmp : mResultList) {
					mFileArray[i] = tmp.getTitle();
					parents = tmp.getParents();
					i++;
				}
				// Log.d(MainActivity.DEBUGTAG, "mFileArry size = " +
				// mFileArray.length);
				// Log.d(MainActivity.DEBUGTAG, "mFileArry: " +
				// mFileArray[0].toString());
				// Log.d(MainActivity.DEBUGTAG, "mFileArry: " +
				// mFileArray[1].toString());
				// Log.d(MainActivity.DEBUGTAG, "mFileArry: " +
				// mFileArray[2].toString());
				// Log.d(MainActivity.DEBUGTAG, "mFileArry: " +
				// mFileArray[3].toString());
				// Log.d(MainActivity.DEBUGTAG, "mFileArry: " +
				// mFileArray[4].toString());
				// Log.d(MainActivity.DEBUGTAG, "mFileArry: " +
				// mFileArray[5].toString());

				mAdapter = new ArrayAdapter<String>(mContext,
						android.R.layout.simple_list_item_activated_1,
						mFileArray);
				Log.d(MainActivity.DEBUGTAG,
						"adapter length = " + mAdapter.getCount());
				mListView.setAdapter(mAdapter);
				Log.d(MainActivity.DEBUGTAG, "populateListView 4");
			}
		});
	}

	private void storeFile(java.io.File file, InputStream iStream) {
		try {
			final OutputStream oStream = new FileOutputStream(file);
			try {
				try {
					final byte[] buffer = new byte[1024];
					int read;
					while ((read = iStream.read(buffer)) != -1) {
						oStream.write(buffer, 0, read);
					}
					oStream.flush();
				} finally {
					oStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null
					&& data.getExtras() != null) {
				String accountName = data
						.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					mCredential.setSelectedAccountName(accountName);
					mService = getDriveService(mCredential);

					Log.d(MainActivity.DEBUGTAG, "uploadFilePath = "
							+ uploadFilePath);
					if (uploadFilePath != null) {
						saveFileToDrive();
					} else {
						getDriveContents();
					}
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				// account already picked
			} else {

				startActivityForResult(mCredential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
			}
			break;
		case RESULT_STORE_FILE:
			mFileUri = data.getData();

			// Save the file to Google Drive
			saveFileToDrive();
			break;
		}
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	private void saveFileToDrive() {

		final ProgressDialog ringProgressDialog = ProgressDialog.show(
				DriveActivity.this, "Please wait ...",
				"Creating backup on Google Drive...", true);
		ringProgressDialog.setCancelable(true);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(MainActivity.DEBUGTAG, "path = " + uploadFilePath);
					mFileUri = Uri.fromFile(new java.io.File(uploadFilePath));

					ContentResolver cR = DriveActivity.this
							.getContentResolver();

					// File's binary content
					java.io.File fileContent = new java.io.File(mFileUri
							.getPath());
					FileContent mediaContent = new FileContent(cR
							.getType(mFileUri), fileContent);

					// showToast("Selected " + mFileUri.getPath() +
					// " to upload");

					// File's meta data.
					File body = new File();
					body.setTitle(fileContent.getName());
					body.setMimeType(cR.getType(mFileUri));

					// body.setParents(Arrays.asList(new
					// ParentReference().setId("appdata")));

					com.google.api.services.drive.Drive.Files f1 = mService
							.files();
					com.google.api.services.drive.Drive.Files.Insert i1 = f1
							.insert(body, mediaContent);
					File file = i1.execute();

					if (file != null) {
						ringProgressDialog.dismiss();
						showToast("File uploaded to Google Drive: "
								+ file.getTitle());
						Intent i = new Intent(DriveActivity.this,
								MainActivity.class);
						startActivity(i);
					}
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
					showToast("Transfer ERROR: " + e.toString());
				}

				ringProgressDialog.dismiss();
			}
		});
		t.start();
	}

	private static void printParents(Drive service, String fileId) {
		try {
			ParentList parents = service.parents().list(fileId).execute();

			for (ParentReference parent : parents.getItems()) {
				Log.d(MainActivity.DEBUGTAG, "File Id: " + parent.getId());
			}
		} catch (IOException e) {
			Log.d(MainActivity.DEBUGTAG,
					"An error occurred getting file parents: " + e);
		}
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast,
						Toast.LENGTH_SHORT).show();
				Log.d(MainActivity.DEBUGTAG, toast);
			}
		});
	}

	public String getPathFromUri(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private boolean restoreFromLocalBackup(String fileName, String dbName) {

		String sourcePath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ "/" + fileName + ".db";
		java.io.File sourceFile = new java.io.File(sourcePath);

		// Log.d(MainActivity.DEBUGTAG, "file = " + file.toString());
		Log.d(MainActivity.DEBUGTAG, "source file = " + sourceFile);

		java.io.File destinationFile;
		if (dbName != null) {
			Log.d(MainActivity.DEBUGTAG, "db path = " + dbName);
			destinationFile = new java.io.File(dbName);
			Log.d(MainActivity.DEBUGTAG, "dest file from path = "
					+ destinationFile.toString());
		} else {
			destinationFile = getDatabasePath(BackupNotes.DATABASE_NAME);
			Log.d(MainActivity.DEBUGTAG, "dest file from constant = "
					+ destinationFile.toString());
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
			Toast.makeText(DriveActivity.this, "Unable to restore notes...",
					Toast.LENGTH_LONG).show();
		}

		return true;
	}

}
