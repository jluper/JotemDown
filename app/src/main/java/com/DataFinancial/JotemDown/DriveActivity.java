package com.DataFinancial.JotemDown;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveApi;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
    private int group;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;
    private GoogleApiClient googleApiClient;
    private DriveApi driveApi;
    private Utils utils;

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
        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
        btnRestore = (Button) findViewById(R.id.btnRestoreDrive);
        addListenerRestoreButton();

        utils = new Utils();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(com.google.android.gms.drive.Drive.API)
                .addScope(com.google.android.gms.drive.Drive.SCOPE_FILE)
                .build();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = (extras.getInt("group"));
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
            uploadFilePath = extras.getString("filepath");
        }
        if (!(uploadFilePath == null)) {
            ListView files = (ListView) findViewById(R.id.lstFiles);
            files.setVisibility(View.INVISIBLE);
            btnRestore.setVisibility(View.INVISIBLE);
            TextView lblSelectFile = (TextView) findViewById(R.id.lblSelectFile);
            lblSelectFile.setVisibility(View.INVISIBLE);

            try {
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            } catch (Exception e) {
                Toast.makeText(DriveActivity.this, "Exception accessing Google Drive account. " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            mContext = getApplicationContext();
            mListView = (ListView) findViewById(R.id.lstFiles);

            OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {

                public void onItemClick(AdapterView parent, View v, int position, long id) {

                    btnRestore.setEnabled(true);
                    selectedFilePosition = position;

                }
            };

            mListView.setOnItemClickListener(mMessageClickedHandler);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(DriveActivity.this, RestoreNotes.class);

        i.putExtra("group", group);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;
    }

    public void addListenerRestoreButton() {

        btnRestore = (Button) findViewById(R.id.btnRestoreDrive);

        btnRestore.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                downloadItemFromList(selectedFilePosition);

                String fileName = mDLVal.substring(0, mDLVal.length() - 3);

                restoreFromLocalBackup(fileName, null);

                Intent i = new Intent(DriveActivity.this, MainActivity.class);
                i.putExtra("group", group);
                i.putExtra("group_name", groupName);
                i.putExtra("sort_col", sortCol);
                i.putExtra("sort_name", sortName);
                i.putExtra("sort_dir", sortDir);
                startActivity(i);
            }

        });
    }

    private void getDriveContents() {

        final ProgressDialog ringProgressDialog = ProgressDialog.show(DriveActivity.this, "Please wait ...", "Getting list of files from Google Drive...", true);
        ringProgressDialog.setCancelable(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mResultList = new ArrayList<>();
                mResultList.clear();
                com.google.api.services.drive.Drive.Files f1 = mService.files();

                com.google.api.services.drive.Drive.Files.List request = null;

                do {
                    try {
                        request = f1.list();

                        request.setQ("title contains '.db' and title contains '_JED' and trashed = false");
                        com.google.api.services.drive.model.FileList fileList = new com.google.api.services.drive.model.FileList();

                        for (int i = 0; i<5; i++) {

                            fileList.clear();
                            fileList = request.execute();

                            mResultList.addAll(fileList.getItems());
                            Log.d(MainActivity.DEBUGTAG, "fileList size = " + fileList.size());
                            Log.d(MainActivity.DEBUGTAG, "mresultList size = " + mResultList.size());

                            if (mResultList.size() > 0) break;

                            for (File f: mResultList) {
                                Log.d(MainActivity.DEBUGTAG, "file = " + f.getTitle());
                            }

                            request.setPageToken(fileList.getNextPageToken());
                        }
                    } catch (UserRecoverableAuthIOException e) {
                        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    } catch (IOException e) {
                        showToast("Exception (3) getting file list from Google Drive: " + e.toString());
                        utils.backupNotify(DriveActivity.this, "Exception backing up your notes to Google Drive: " + e.getMessage());
                        if (request != null) {
                            request.setPageToken(null);
                        }
                    }
                } while (request.getPageToken() != null && request.getPageToken().length() > 0);

                populateListView();
                ringProgressDialog.dismiss();
            }

        });
        t.start();
    }

    private void downloadItemFromList(int position) {
        mDLVal = (String) mListView.getItemAtPosition(position);

        final ProgressDialog ringProgressDialog = ProgressDialog.show(DriveActivity.this, "Please wait ...", "Downloading file from Google Drive...", true);
        ringProgressDialog.setCancelable(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream iStream;
                for (File tmp : mResultList) {
                    if (tmp.getTitle().equalsIgnoreCase(mDLVal)) {
                        if (tmp.getDownloadUrl() != null && tmp.getDownloadUrl().length() > 0) {

                            try {
                                com.google.api.client.http.HttpResponse resp = mService.getRequestFactory().buildGetRequest(new GenericUrl(tmp.getDownloadUrl())).execute();
                                iStream = resp.getContent();
                                final java.io.File file = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), tmp.getTitle());
                                storeFile(file, iStream);
                                iStream.close();
                            } catch (IOException e) {
                                showToast("Exception (1) getting file from Google Drive: " + e.toString());
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
                List<ParentReference> parents = new ArrayList<>();
                mFileArray = new String[mResultList.size()];
                int i = 0;
                for (File tmp : mResultList) {
                    mFileArray[i] = tmp.getTitle();
                    parents = tmp.getParents();

                    i++;
                }
                mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_activated_1, mFileArray);

                mListView.setAdapter(mAdapter);
            }
        });
    }

    private void storeFile(java.io.File file, InputStream iStream) {
        try {
            final OutputStream oStream = new FileOutputStream(file, false);
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
                showToast("Exception (1) saving file to Google Drive: " + e.toString());
            }
        } catch (IOException e) {
            showToast("Exception (2) saving file to Google Drive: " + e.toString());
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
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        mService = utils.getDriveService(mCredential);
                        Log.d(MainActivity.DEBUGTAG, "creds = " + mCredential.toString());

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

                    startActivityForResult(mCredential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
                }
                break;
            case RESULT_STORE_FILE:
                mFileUri = data.getData();

                // Save the file to Google Drive
                saveFileToDrive();
                break;
        }
    }

//    private Drive getDriveService(GoogleAccountCredential credential) {
//        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
//    }


    private void saveFileToDrive() {

        final ProgressDialog ringProgressDialog = ProgressDialog.show(DriveActivity.this, "Please wait ...", "Creating backup on Google Drive...", true);
        ringProgressDialog.setCancelable(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                mResultList = new ArrayList<>();
                mResultList.clear();
                com.google.api.services.drive.Drive.Files f1 = mService.files();
                com.google.api.services.drive.Drive.Files.List request = null;
                ContentResolver cR = DriveActivity.this.getContentResolver();

                mFileUri = Uri.fromFile(new java.io.File(uploadFilePath));
                java.io.File fileContent = new java.io.File(mFileUri.getPath());
                FileContent mediaContent = new FileContent(cR.getType(mFileUri), fileContent);

                try {
                    request = f1.list();

                    request.setQ("title = '" +  fileContent.getName() + "'"  + "and trashed = false");
                    com.google.api.services.drive.model.FileList fileList = new com.google.api.services.drive.model.FileList();

                    fileList.clear();
                    fileList = request.execute();
                    mResultList.clear();
                    mResultList.addAll(fileList.getItems());

                    File savedFile;
                    // check if file already exists
                    // if so, then update it rather than create new one
                    if (mResultList.size() > 0) {
                        File file = mResultList.get(0);

                        //backup file already exists so update it
                        file.setTitle(file.getTitle());
                        file.setDescription(file.getDescription());
                        file.setMimeType(file.getMimeType());

                        savedFile = mService.files().update(file.getId(), file, mediaContent).execute();

                    } else {
                        //backup file doesn't exists so create it
                        mFileUri = Uri.fromFile(new java.io.File(uploadFilePath));

                        File body = new File();
                        body.setTitle(fileContent.getName());
                        body.setMimeType(cR.getType(mFileUri));

                        savedFile = mService.files().insert(body, mediaContent).execute();
                    }
                    ringProgressDialog.dismiss();

                    if (savedFile != null) {
                        showToast("File uploaded to Google Drive: " + savedFile.getTitle());
                        Intent i = new Intent(DriveActivity.this, MainActivity.class);
                        startActivity(i);
                    } else {
                        showToast("Unable to save file to Google Drive");
                    }

                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("Exception (3) getting file list from Google Drive: " + e.getMessage());
                }

                ringProgressDialog.dismiss();
            }
        });

        t.start();
    }



    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getPathFromUri(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    private boolean restoreFromLocalBackup(String fileName, String dbName) {

        String sourcePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + fileName + ".db";

        java.io.File sourceFile = new java.io.File(sourcePath);

        java.io.File destinationFile;
        if (dbName != null) {
            destinationFile = new java.io.File(dbName);

        } else {
            destinationFile = getDatabasePath(BackupNotes.DATABASE_NAME);
        }

        if (!sourceFile.exists() || !sourceFile.canRead()) {
            showToast("File not found.");

            return false;
        }

        Utils util = new Utils();
        sourceFile.setWritable(true);
        try {
            util.copyFile(sourceFile, destinationFile);
        } catch (IOException e) {
            showToast("Exception (4) restoring notes from Google Drive: " + e.toString());
        }

        return true;
    }
}
