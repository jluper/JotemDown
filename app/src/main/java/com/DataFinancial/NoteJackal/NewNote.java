package com.DataFinancial.NoteJackal;

//import android.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewNote extends ActionBarActivity {

    private DatabaseNotes db = new DatabaseNotes(this);
    private DatabaseReminders dbReminders = new DatabaseReminders(this);
    private boolean editFunction = false;
    private Note note = new Note();
    private Menu optionsMenu;
    private AlertDialog confirmDelete;
    private boolean confirmDeleteReminder = false;
    private LocationManager locManager;
    private String phoneContact;
    private String emailContact;
    private String urlContact;
    private boolean help = false;
    private EditText noteText;
    private File imageFile;
    private ArrayAdapter grpAdapter;
    private ListView groupList;
    private String noteImagePath;
    private int selectedGroupRow;
    private EditText editText;
    private int groupId;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle(getResources().getString(R.string.edit_note_activity_title));
        actionBar.setDisplayShowTitleEnabled(true);

        noteText = (EditText) findViewById(R.id.note_text);
        noteText.setGravity(Gravity.TOP);
        groupList = (ListView) findViewById(R.id.group_list);

        //groupList.setSelectionFromTop(2, 0);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editFunction = extras.getBoolean("edit");
            if (editFunction == true) {
                note.setId(extras.getInt("id"));
                note.setPriority(extras.getInt("priority"));
                note.setCreateDate(extras.getString("createDate"));
                note.setEditDate(extras.getString("editDate"));
                note.setBody(extras.getString("body"));
                note.setLatitude(extras.getString("latitude"));
                note.setLongitude(extras.getString("longitude"));
                note.setHasReminder(extras.getString("hasReminder"));
                note.setImage(extras.getString("image"));
                note.setGroup(extras.getInt("group"));
                noteText.setText(note.getBody());

                String strHelp = (String) getResources().getText(R.string.txt_help_search);
                if (note.getBody().length() >= strHelp.length()) {

                    if (note.getBody().substring(0, strHelp.length()).equals(strHelp)) {
                        help = true;
                        noteText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        noteText.setText(Html.fromHtml(note.getBody()));
                        noteText.setFocusable(false);
                        actionBar.setTitle(getResources().getString(R.string.help_title));
                        ListView groupList = (ListView) findViewById(R.id.group_list);
                        groupList.setVisibility(View.GONE);
                    }
                }
            }
            groupId = extras.getInt("group");
            groupName = (extras.getString("group_name"));
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }

        populategroupList();

        List<NoteGroup> grps = db.getGroups(DatabaseNotes.COL_ID, "ASC");
        int grp = note.getGroup();
        for (int i = 0; i < grps.size(); i++) {
            if (grps.get(i).getId() == note.getGroup()) {
                groupList.setItemChecked(i, true);
                break;
            }
            groupList.setItemChecked(MainActivity.ROOT, true);
        }

        TextView lblPriority = (TextView) findViewById(R.id.lbl_priority);
        TextView lblCreateDate = (TextView) findViewById(R.id.lbl_create_date);
        TextView lblGeocode = (TextView) findViewById(R.id.lbl_geocoded);
        TextView lblImage = (TextView) findViewById(R.id.lbl_note_image);
        lblCreateDate.setText(Utils.convertDate(note.getCreateDate(), "yy/MM/dd", "MM/dd/yy"));
        lblPriority.setText(note.getPriority() == 0 ? "" : "Priority");

        if (!note.getLatitude().isEmpty()) {
            lblGeocode.setText("Geotag");
        }

        if (!note.getImage().isEmpty()) {
            lblImage.setText("Image");
        }

        editText = (EditText) findViewById(R.id.note_text);
        int textLength = editText.getText().length();
        editText.setSelection(textLength, textLength);

        Reminder reminder = new Reminder();
        reminder = dbReminders.getReminder(note.getId());
        if (reminder != null) {
            TextView txtReminder = (TextView) findViewById(R.id.lbl_note_reminder);
            txtReminder.setText("Reminder");
            SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
            Date today = new Date();
            try {
                Date reminderDate = df.parse(reminder.getDate() + " " + reminder.getTime());

//
            } catch (ParseException e) {
                Toast.makeText(NewNote.this, "Exception parsing reminder date: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private String getPhoneContact(String text) {

        String[] lines = text.split("\n");
        if (lines.length == 0) {
            return null;
        }

        for (int i = 0; i < lines.length; i++) {

            if (Utils.isValidPhone(lines[i])) {
                phoneContact = lines[i];
                return lines[i];
            }
        }
        return null;

    }

    private String getEmailContact(String text) {

        String[] lines = text.split("\n");
        if (lines.length == 0) {
            return null;
        }

        for (int i = 0; i < lines.length; i++) {

            if (Utils.isValidEmail(lines[i])) {
                emailContact = lines[i];
                return lines[i];
            }
        }
        return null;

    }

    private String getUrlContact(String text) {

        String[] lines = text.split("\n");
        if (lines.length == 0) {
            return null;
        }

        for (int i = 0; i < lines.length; i++) {

            if (Utils.isValidURL(lines[i])) {
                urlContact = lines[i];
                return lines[i];
            }
        }
        return null;

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        super.onResume(); // Always call the superclass method first

        Intent i = new Intent(NewNote.this, MainActivity.class);

        i.putExtra("group_name",  ((TextView)(groupList.getAdapter().getView(groupList.getCheckedItemPosition(), null, groupList)).findViewById(R.id.group_row_text)).getText());
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);
        i.putExtra("help", help);
        if (help == false) {
            i.putExtra("group", note.getGroup());
        } else {
            i.putExtra("group", groupId);
        }


        return i;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (editFunction == false) {
            getMenuInflater().inflate(R.menu.menu_new_note, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        optionsMenu = menu;

        if (help) {
            optionsMenu.findItem(R.id.menu_delete).setVisible(false);
            optionsMenu.findItem(R.id.menu_save).setVisible(false);
            optionsMenu.findItem(R.id.menu_image).setVisible(false);
            optionsMenu.findItem(R.id.menu_geotag).setVisible(false);
            optionsMenu.findItem(R.id.menu_map).setVisible(false);
            optionsMenu.findItem(R.id.menu_priority).setVisible(false);
            optionsMenu.findItem(R.id.menu_reminder).setVisible(false);
        }

        if (editFunction == true) {
            if (note.getLatitude().isEmpty() || note.getLongitude().isEmpty()) {
                // optionsMenu.getItem(R.id.menu_map).setVisible(false);
                optionsMenu.findItem(R.id.menu_map).setVisible(false);
            }

            phoneContact = getPhoneContact(note.getBody());
            if (phoneContact == null) {
                optionsMenu.findItem(R.id.menu_call_phone).setVisible(false);
            }

            emailContact = getEmailContact(note.getBody());
            if (emailContact == null) {
                optionsMenu.findItem(R.id.menu_send_email).setVisible(false);
            }

            urlContact = getUrlContact(note.getBody());
            if (urlContact == null) {
                optionsMenu.findItem(R.id.menu_goto_webpage).setVisible(false);
            }

            if (note.getImage().isEmpty()) {
                optionsMenu.findItem(R.id.menu_image).setIcon(R.drawable.ic_action_new_picture);
                optionsMenu.findItem(R.id.menu_remove_image).setVisible(false);
            } else {
                optionsMenu.findItem(R.id.menu_remove_image).setVisible(true);
                optionsMenu.findItem(R.id.menu_image).setIcon(R.drawable.ic_action_picture);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_save) {

            saveNote();

            return true;
        }

        if (id == R.id.menu_send_note) {

            Intent i = new Intent(NewNote.this, SendNote.class);
            i.putExtra("id", note.getId());
            i.putExtra("priority", Integer.toString(note.getPriority()));
            i.putExtra("createDate", note.getCreateDate());
            i.putExtra("editDate", note.getEditDate());
            i.putExtra("body", note.getBody());
            i.putExtra("image", note.getImage());
            i.putExtra("group", note.getGroup());
            i.putExtra("group_name",  ((TextView)(groupList.getAdapter().getView(groupList.getCheckedItemPosition(), null, groupList)).findViewById(R.id.group_row_text)).getText());
            i.putExtra("sort_col", sortCol);
            i.putExtra("sort_name", sortName);
            i.putExtra("sort_dir", sortDir);
            startActivity(i);

            return true;
        }

        if (id == R.id.menu_delete) {

            buildConfirmDeleteDialog("Confirm Delete Note");
            confirmDelete.show();

            return true;
        }

        if (id == R.id.menu_priority) {

            note.setPriority((note.getPriority() + 1) % 2);
            db.updateNote(note);

            return true;
        }

        if (id == R.id.menu_reminder) {

            Intent i = new Intent(NewNote.this, ReminderActivity.class);
            i.putExtra("id", note.getId());
            i.putExtra("group", note.getGroup());
            i.putExtra("group_name",  ((TextView)(groupList.getAdapter().getView(groupList.getCheckedItemPosition(), null, groupList)).findViewById(R.id.group_row_text)).getText());
            i.putExtra("sort_col", sortCol);
            i.putExtra("sort_name", sortName);
            i.putExtra("sort_dir", sortDir);
            startActivity(i);

            return true;
        }

        if (id == R.id.menu_geotag) {

            GetLocation geoTagger = new GetLocation(this);
            Location loc = geoTagger.getLocation();

            if (loc != null) {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();

                EditText v = (EditText) findViewById(R.id.note_text);

                note.setLatitude(Double.toString(lat));
                note.setLongitude(Double.toString(lon));

                //note.setBody(note.getBody() + "\n\nGeotag: " + note.getLatitude() + ", " + note.getLongitude());
                long rowId = db.updateNote(note);

                if (rowId != -1) {
                    Toast.makeText(NewNote.this, "Note location updated...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(NewNote.this, "Unable to update location...", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(NewNote.this, "Unable to get location...", Toast.LENGTH_LONG).show();
            }

            return true;
        }

        if (id == R.id.menu_map) {
            Double lat = 0.0;
            Double lon = 0.0;

            if (!note.getLatitude().isEmpty() && !note.getLongitude().isEmpty()) {
                lat = Double.parseDouble(note.getLatitude());
                lon = Double.parseDouble(note.getLongitude());
            } else {
                Toast.makeText(NewNote.this,
                        "Unable to show map, location unavailable...", Toast.LENGTH_LONG).show();
            }

            EditText v = (EditText) findViewById(R.id.note_text);
            //noteText.setText(note.getBody() +"\n" +  "latitude: " + lat + " longitude: " + lon);

            Intent i = new Intent(NewNote.this, MapActivity.class);
            i.putExtra("edit", editFunction);
            i.putExtra("latitude", lat);
            i.putExtra("longitude", lon);
            i.putExtra("title", "Note: " + note.getEditDate());
            i.putExtra("body", note.getBody());
            i.putExtra("group", note.getGroup());
            i.putExtra("image", note.getImage());
            i.putExtra("group_name",  ((TextView)(groupList.getAdapter().getView(groupList.getCheckedItemPosition(), null, groupList)).findViewById(R.id.group_row_text)).getText());
            i.putExtra("sort_col", sortCol);
            i.putExtra("sort_name", sortName);
            i.putExtra("sort_dir", sortDir);
            startActivity(i);

            return true;
        }

        if (id == R.id.menu_call_phone) {

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneContact));
            startActivity(intent);
        }

        if (id == R.id.menu_send_email) {
            String[] TO = {"jluper@triad.rr.com"};
            ;
            String[] CC;

            TO[0] = emailContact;
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");

            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "test text");
            Intent intent = Intent.createChooser(emailIntent, "Send email...");
            startActivity(intent);

        }

        if (id == R.id.menu_goto_webpage) {

            Intent i = new Intent(NewNote.this, WebviewActivity.class);
            i.putExtra("url", urlContact);

            startActivity(i);
        }

        if (id == R.id.menu_image) {

            if (note.getImage().isEmpty()) {
                browseGallery();
            } else {
                Intent i = new Intent(NewNote.this, NoteImageActivity.class);
                i.putExtra("edit", editFunction);
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
                //i.putExtra("group_name",  ((TextView)(groupList.getAdapter().getView(groupList.getCheckedItemPosition(), null, groupList)).findViewById(R.id.group_row_text)).getText());
                i.putExtra("group_name", groupName);
                i.putExtra("sort_col", sortCol);
                i.putExtra("sort_name", sortName);
                i.putExtra("sort_dir", sortDir);
                startActivity(i);
            }
        }

        if (id == R.id.menu_remove_image) {

            buildConfirmDeleteDialog("Confirm Delete Image");
            confirmDelete.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private long saveNote() {

        EditText editText = (EditText) findViewById(R.id.note_text);
        String noteBody = editText.getText().toString();
        note.setBody(noteBody);

        long noteId = -1;

        Intent i = new Intent(NewNote.this, MainActivity.class);
        try {

            i.putExtra("group", note.getGroup());
            //i.putExtra("group_name",  ((TextView) groupList.getAdapter().getView(groupList.getCheckedItemPosition(), null, groupList)).getText());
            i.putExtra("group_name",  ((TextView)(groupList.getAdapter().getView(groupList.getCheckedItemPosition(), null, groupList)).findViewById(R.id.group_row_text)).getText());
            if (editFunction == true) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd");
                Date curr_date = new Date();
                note.setEditDate(dateFormat.format(curr_date));
                noteId = db.updateNote(note);
                Toast.makeText(NewNote.this, "Note edited...",
                        Toast.LENGTH_SHORT).show();
            } else {
                noteId = db.addNote(note);
                Toast.makeText(NewNote.this, "Note added...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(NewNote.this, "Exception updating note: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);
        startActivity(i);

        return noteId;

    }

    public void buildConfirmDeleteDialog(String title) {

        final String dialogTitle = title;
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle(title);
        dlgBuilder.setIcon(R.drawable.btn_check_buttonless_on);
        dlgBuilder.setMessage(R.string.dialog_delete_item);
        dlgBuilder.setCancelable(true);

        dlgBuilder.setPositiveButton(R.string.dialog_positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (dialogTitle.equals("Confirm Delete Image")) {
                            note.setImage("");
                            db.updateNote(note);
                           //optionsMenu = (Menu) findViewById(R.id.menu_edit_note);
                            optionsMenu.findItem(R.id.menu_image).setIcon(R.drawable.ic_action_new_picture);
                            dialog.cancel();
                        } else {
                            db.deleteNote(note.getId());
                            dialog.cancel();
                            Intent i = new Intent(NewNote.this, MainActivity.class);
                            startActivity(i);
                        }
                    }
                });

                dlgBuilder.setNegativeButton(R.string.dialog_negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        confirmDelete = dlgBuilder.create();
    }


    private void browseGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, MainActivity.BROWSE_GALLERY_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == MainActivity.BROWSE_GALLERY_REQUEST) {
            if (intent != null) {
                String[] columns = {MediaStore.Images.Media.DATA};
                Uri imageUri = intent.getData();
                Cursor cursor = getContentResolver().query(imageUri, columns, null, null, null);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(columns[0]);
                noteImagePath = cursor.getString(columnIndex);

                note.setImage(noteImagePath);
                db.updateNote(note);

                cursor.close();

                Uri image = Uri.parse(noteImagePath);

                optionsMenu.findItem(R.id.menu_image).setIcon(R.drawable.ic_action_picture);

                Toast.makeText(this, "Image path: " + image, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Unable to get image from gallery.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void populategroupList() {

                List<NoteGroup> grps;

                grps = db.getGroups(DatabaseNotes.COL_ID, "ASC");
//        for (NoteGroup temp : grps) {
//            Log.d(MainActivity.DEBUGTAG, "Group: " + temp.toString());
//        }
                GroupAdapter grpAdapter = new GroupAdapter(this, grps);

                groupList.setAdapter(grpAdapter);

        groupList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long arg3) {

                selectedGroupRow = pos;
                NoteGroup grp = (NoteGroup) adapter.getItemAtPosition(pos);
                note.setGroup(grp.getId());
                groupList.setItemChecked(pos, true);
            }
        });
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
