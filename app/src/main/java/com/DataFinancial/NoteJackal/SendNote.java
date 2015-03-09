package com.DataFinancial.NoteJackal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * @author jluper
 */
public class SendNote extends ActionBarActivity {

    public static final String LAST_SEND_ADDRESS = "LAST_ADDRESS";
    private EditText address;
    private Button btnSend;
    private Note note = new Note();
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_note);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle("Share Note");
        actionBar.setDisplayShowTitleEnabled(true);

        addListenerOnButton();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            note.setId(extras.getInt("id"));
            note.setCreateDate(extras.getString("createDate"));
            note.setEditDate(extras.getString("editDate"));
            note.setBody(extras.getString("body"));
            note.setImage(extras.getString("image"));
            note.setGroup(extras.getInt("group"));
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }

        address = (EditText) findViewById(R.id.txtAddress);
        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        String addr = prefs.getString(LAST_SEND_ADDRESS, null);
        if (addr != null) {
            address.setText(addr);
        }

        int textLength = address.getText().length();
        address.setSelection(textLength, textLength);
    }


    @Override
    public void onResume() {

        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_note, menu);
        return true;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        super.onResume();
        Intent i = new Intent(SendNote.this, MainActivity.class);

        i.putExtra("group", note.getGroup());
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    public void addListenerOnButton() {

        btnSend = (Button) findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Utils utils = new Utils();
                String[] TO = {"jluper@triad.rr.com"};
                ;
                String[] CC;

                TO[0] = address.getText().toString();

                try {

                    if (utils.isValidEmail(TO[0])) {

                        //Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        List<Intent> emailIntents = utils.filterIntents(SendNote.this);

                        for (Intent i : emailIntents) {

                            i.setData(Uri.parse("mailto:"));
                            i.setType("message/rfc822");

                            if (!note.getImage().isEmpty()) {
                                File imageFile = new File(note.getImage());
                                Uri uri = Uri.parse(imageFile.toString());
                                i.putExtra(Intent.EXTRA_STREAM, uri);
                            }

                            String emailSubject = "Note from Jot'emDown";
                            String emailText = "Note from Jot'emDown...\n\nCreated Date: " + note.getCreateDate() + "\nLast Edit Date:" + note.getEditDate() + "\n\n" + note.getBody();
                            i.putExtra(Intent.EXTRA_EMAIL, TO);
                            i.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                            i.putExtra(Intent.EXTRA_TEXT, emailText);
                        }
                        try {

                            Intent chooserIntent = Intent.createChooser(emailIntents.remove(0), "Select app to share...");
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailIntents.toArray(new Parcelable[]{}));
                            startActivity(chooserIntent);
                            //Intent intent = Intent.createChooser(emailIntent, "Send mail...");
                            //startActivity(intent);

                        } catch (Exception e) {
                            Toast.makeText(SendNote.this, "Exception sending note email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    } else {
                        if (utils.isValidPhone(TO[0])) {

                            String msg = note.getBody();
                            if (msg.length() > 160) {
                                msg = msg.substring(0, 134);
                            }

                            SmsManager smsManager = SmsManager.getDefault();
                            try {
                                smsManager.sendTextMessage(TO[0], null, "Note from NoteJackal...\n" + msg, null, null);
                                Toast.makeText(SendNote.this, "Text sent...", Toast.LENGTH_LONG).show();

                                Intent i = new Intent(SendNote.this, MainActivity.class);
                                startActivity(i);

                            } catch (Exception e) {
                                Toast.makeText(SendNote.this, "Exception sending note text message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SendNote.this, "Invalid phone or email...", Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SendNote.this, "Exception sending note: " + ex.toString(), Toast.LENGTH_LONG).show();
                }

                SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(LAST_SEND_ADDRESS, TO[0]);
                editor.apply();
            }
        });
    }
}

