package com.DataFinancial.NoteJackal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    //private enum sortOption { COL_PRIORITY, COL_CREATE_DATE, COL_EDIT_DATE, COL_BODY }
    public static final String DEBUGTAG = "JFL";
    public static final String HELP_FILE = "JeDHelpImport.txt";
    //public static final String FILESAVED = "FileSaved";
     public static final String PASSPOINTS_SET = "RESET_PASSPOINTS";
    public static final int PHOTO_TAKEN_REQUEST = 2;
    public static final int EDIT_NOTE = 4;
    public static final int ROOT = 1;
    public static final int BROWSE_GALLERY_REQUEST = 3;
    private static int selectedRow = 0;
    protected List<Note> notes = new ArrayList<Note>();
    private File imageFile;
    private ImageButton searchButton;
    private ImageButton sortButton;
    private TextView lblSort;
    private TextView lblGroup;
    private TextView lblNumNotes;
    private DatabaseNotes db = new DatabaseNotes(this);
    private DatabaseReminders dbReminders = new DatabaseReminders(this);
    private String fromHelp = null;
    private ListView noteList;
    private static int groupId = ROOT;
    private static int groupIdx = ROOT;
    private String searchText = null;
    private static String sortCol = DatabaseNotes.COL_CREATE_DATE;
    private static String sortDir = "DESC";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle(getResources().getString(R.string.main_activity_title));
        actionBar.setDisplayShowTitleEnabled(true);

        lblSort = (TextView) findViewById(R.id.lbl_sort);
        lblSort.setText("Created");
        lblGroup = (TextView) findViewById(R.id.lbl_group);

        noteList = (ListView) findViewById(R.id.note_list);

        addSearchButtonListener();
        addSortButtonListener();
        addGroupButtonListener();

        db.createNotesTable();
        db.createGroupsTable();
        dbReminders.createRemindersTable();

        searchText = null;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            if (extras.getString("help") != null) {
                fromHelp = extras.getString("help");
                searchText = (String) getResources().getText(R.string.txt_help_search);
                loadNotes(searchText, DatabaseNotes.COL_ID, "ASC", groupId);
            } else {
                groupId = extras.getInt("group");

                List<NoteGroup> grps = db.getGroups(DatabaseNotes.COL_NAME, "ASC");
                for (int i = 0; i < grps.size(); i++) {
                    if (grps.get(i).getId() == groupId) {
                        groupIdx = i;
                        break;
                    }
                    groupIdx = -1;
                }
                lblGroup.setText(extras.getString("group_name"));
                loadNotes(null, DatabaseNotes.COL_CREATE_DATE, "DESC", groupId);
            }
        } else {
            groupId = ROOT;
            lblGroup.setText("General");
            loadNotes(null, DatabaseNotes.COL_CREATE_DATE, "DESC", groupId);
        }
    }


    private boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }


    public boolean isPrivate() {

        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        boolean isPrivate = prefs.getBoolean(LockImageActivity.PRIVATE, true);

        return isPrivate;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void addSearchButtonListener() {

        searchButton = (ImageButton) findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectedRow = 0;

                String searchText = ((EditText) findViewById(R.id.searchText)).getText().toString();

                loadNotes(searchText, DatabaseNotes.COL_CREATE_DATE, "DESC", groupId);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            }
        });

    }


    public void addSortButtonListener() {

        sortButton = (ImageButton) findViewById(R.id.sortButton);

        sortButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectedRow = 0;

                sortDir = "DESC";

                lblSort = (TextView) findViewById(R.id.lbl_sort);

                switch (sortCol) {
                    case DatabaseNotes.COL_CREATE_DATE:
                        sortCol = DatabaseNotes.COL_PRIORITY;
                        lblSort.setText("Priority");
                        break;
                    case DatabaseNotes.COL_EDIT_DATE:
                        sortCol = DatabaseNotes.COL_CREATE_DATE;
                        lblSort.setText("Created");
                        break;
                    case DatabaseNotes.COL_BODY:
                        sortCol = DatabaseNotes.COL_EDIT_DATE;
                        lblSort.setText("Edited");
                        break;
                    case DatabaseNotes.COL_PRIORITY:
                        sortCol = DatabaseNotes.COL_BODY;
                        lblSort.setText("Content");
                        sortDir = "ASC";
                        break;
                    default:
                        sortCol = DatabaseNotes.COL_CREATE_DATE;
                        lblSort.setText("Content");
                        sortDir = "ASC";
                }
                  if (sortCol.equals(DatabaseNotes.COL_BODY)) {
                    loadNotes(null, sortCol + " COLLATE NOCASE", sortDir, groupId);
                } else {
                    loadNotes(null, sortCol, sortDir, groupId);
                }
            }
        });
    }

    public void addGroupButtonListener() {

        ImageButton groupButton = (ImageButton) findViewById(R.id.groupButton);

        groupButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //get a list of all the groups
                List<NoteGroup> grps;
                grps = db.getGroups(DatabaseNotes.COL_NAME, "ASC");

//                for (int i = 0; i < grps.size(); i++) {
//                    Log.d(MainActivity.DEBUGTAG, "grp: " + grps.get(i).getId() + ", " + grps.get(i).getName());
//                }

                groupIdx = (groupIdx + 1) % grps.size();
                groupId = grps.get(groupIdx).getId();
//                Log.d(MainActivity.DEBUGTAG, "groupIdx: " + groupIdx + " groupId: " + groupId);
                lblGroup.setText(grps.get(groupIdx).getName());
                loadNotes(null, sortCol, sortDir, grps.get(groupIdx).getId());
            }
        });

    }

    private void loadNotes(String search, String order, String dir, int group) {

        notes = db.getNotes(search, order, dir, group);

        lblNumNotes = (TextView) findViewById(R.id.lbl_num_notes);

        lblNumNotes.setText("(" + Integer.toString(notes.size()) + ")");

        NoteAdapter adapter = new NoteAdapter(this, notes);

        noteList.setAdapter(adapter);

        noteList.setSelectionFromTop(selectedRow, 0);

        noteList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int pos, long arg3) {

                selectedRow = pos;

                Note note = (Note) adapter.getItemAtPosition(pos);

                Intent i = new Intent(MainActivity.this, NewNote.class);
                i.putExtra("id", note.getId());
                i.putExtra("priority", note.getPriority());
                i.putExtra("createDate", note.getCreateDate());
                i.putExtra("editDate", note.getEditDate());
                i.putExtra("body", note.getBody());
                i.putExtra("latitude", note.getLatitude());
                i.putExtra("longitude", note.getLongitude());
                i.putExtra("hasReminder", note.getHasReminder());
                i.putExtra("image", note.getImage());
                i.putExtra("group", note.getGroup());
                startActivityForResult(i, EDIT_NOTE);
            }
        });
    }

    private void loadSavedFile() {
//
//        try {
//            FileInputStream fis = openFileInput(TEXTFILE);
//
//            fis.close();
//        } catch (Exception e) {
//            Toast.makeText(this, "Problem reading file", Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (isPrivate()) {
            menu.findItem(R.id.menu_disable_privacy).setVisible(true);
            menu.findItem(R.id.menu_enable_privacy).setVisible(false);
            menu.findItem(R.id.menu_lock).setVisible(true);
        } else {
            menu.findItem(R.id.menu_disable_privacy).setVisible(false);
            menu.findItem(R.id.menu_enable_privacy).setVisible(true);
            menu.findItem(R.id.menu_lock).setVisible(false);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent i;

        switch (id) {
            case R.id.menu_lock:
                i = new Intent(MainActivity.this, LockImageActivity.class);
                startActivity(i);
                break;
            case R.id.menu_new:
                i = new Intent(MainActivity.this, NewNote.class);
                startActivity(i);
                break;
            case R.id.menu_passpoints_reset:
                setPassPointsSaved(false);
                i = new Intent(MainActivity.this, LockImageActivity.class);
                startActivity(i);
                break;
            case R.id.menu_replace_lock_image:
                ReplaceLockImage();
                break;
            case R.id.menu_password_reset:
                i = new Intent(MainActivity.this, ChangePassword.class);
                startActivity(i);
                break;
            case R.id.menu_disable_privacy:
                SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(LockImageActivity.PRIVATE, false);
                editor.putBoolean(PASSPOINTS_SET, false);
                editor.apply();
                this.invalidateOptionsMenu();
                break;
            case R.id.menu_enable_privacy:
                prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                editor = prefs.edit();
                editor.putBoolean(LockImageActivity.PRIVATE, true);
                editor.putBoolean(PASSPOINTS_SET, false);
                editor.apply();
                this.invalidateOptionsMenu();
                break;
            case R.id.menu_backup:
                i = new Intent(MainActivity.this, BackupNotes.class);
                startActivity(i);
                break;
            case R.id.menu_restore:
                i = new Intent(MainActivity.this, RestoreNotes.class);
                startActivity(i);
                break;
            case R.id.menu_import:
                i = new Intent(MainActivity.this, ImportNotes.class);
                startActivity(i);
                break;
            case R.id.menu_export:
                i = new Intent(MainActivity.this, ExportNotes.class);
                startActivity(i);
                break;
            case R.id.menu_groups:
                i = new Intent(MainActivity.this, GroupMaintenance.class);
                startActivity(i);
                break;
            case R.id.menu_help_main:
                String searchText = this.getResources().getString(R.string.txt_help_search);
                loadNotes(searchText, DatabaseNotes.COL_ID, "ASC", ExportNotes.NO_GROUP);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ReplaceLockImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.replace_lock_image, null);
        builder.setTitle(R.string.replace_lock_image);
        builder.setView(v);
        builder.setMessage(R.string.replace_image_dialog);

        final AlertDialog dlg = builder.create();
        dlg.show();

        Button takePhoto = (Button) dlg.findViewById(R.id.take_photo);
        Button browseGallery = (Button) dlg.findViewById(R.id.browse_gallery);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhoto();
            }
        });

        browseGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                browseGallery();

            }
        });

    }

    private void browseGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, BROWSE_GALLERY_REQUEST);
    }

    private void takePhoto() {

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        imageFile = new File(dir, getString(R.string.PASSPOINTS_PHOTO)+".jpg");

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(i, PHOTO_TAKEN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {


        if (requestCode == BROWSE_GALLERY_REQUEST) {

            if (resultCode == RESULT_OK) {
                String[] columns = {MediaStore.Images.Media.DATA};
                Uri imageUri = intent.getData();
                Cursor cursor = getContentResolver().query(imageUri, columns, null, null, null);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(columns[0]);
                String imagePath = cursor.getString(columnIndex);

                cursor.close();

                Uri image = Uri.parse(imagePath);

                Toast.makeText(this, "Gallery result" + image, Toast.LENGTH_LONG).show();

                try {
                    copyImageFile(imagePath);

                } catch (IOException e) {
                    Toast.makeText(this, "Exception creating new lock image from gallery: " + e.toString(), Toast.LENGTH_LONG).show();
                }

                setPassPointsSaved(false);
                Intent i = new Intent(MainActivity.this, LockImageActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(this, "No image  selected.", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == PHOTO_TAKEN_REQUEST) {

            if (resultCode == RESULT_OK) {
                  String imagePath = imageFile.getAbsolutePath();
                try {
                    copyImageFile(imagePath);
                    setPassPointsSaved(false);
                    Intent i = new Intent(MainActivity.this, LockImageActivity.class);
                    startActivity(i);
                } catch (IOException e) {
                    Toast.makeText(this, "Unable to replace lock image. " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "No image  selected.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void copyImageFile(String sourcePath) throws IOException {

        File picDirectory = getFilesDir();

        File destinationFile = new File(picDirectory, getString(R.string.PASSPOINTS_PHOTO));

        File sourceFile = new File(sourcePath);

        InputStream in = new FileInputStream(sourceFile);
        OutputStream out = new FileOutputStream(destinationFile);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();

    }

    protected void setPassPointsSaved(boolean state) {

        SharedPreferences prefs = getSharedPreferences(
                LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(MainActivity.PASSPOINTS_SET, state);
        editor.commit();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // enable visible icons in action bar
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Field field = menu.getClass().
                            getDeclaredField("mOptionalIconsVisible");
                    field.setAccessible(true);
                    field.setBoolean(menu, true);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    Log.d(MainActivity.DEBUGTAG, "onMenuOpened(" + featureId + ", " + menu + ")", e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

}
