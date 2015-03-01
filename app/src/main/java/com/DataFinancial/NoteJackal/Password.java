package com.DataFinancial.NoteJackal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Password extends ActionBarActivity {

    public static final String PASSWORD = "password";
    private static String pw;
    private EditText password;
    private Button btnSubmit;
    private DatabaseNotes db = new DatabaseNotes(this);
    private DatabaseReminders db2 = new DatabaseReminders(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);

        addListenerOnButton();
    }


    public String getPassword() {

        return pw;
    }

    public void setPassword(String pwd) {

        pw = pwd;

    }

    public void addListenerOnButton() {

        password = (EditText) findViewById(R.id.txtPassword);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                String savedPassword = prefs.getString(PASSWORD, getString(R.string.default_password));

                pw = password.getText().toString();
                if (pw.equals("DeleteNotes")) {
                    db.recreateNotesTable();

                    db2.recreateRemindersTable();
                    Toast.makeText(Password.this, "Notes and Reminders tables deleted...", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Password.this, MainActivity.class);
                    startActivity(i);
                }

                if (pw.equals("DeleteGroups")) {
                    db.recreateGroupsTable();

                    Toast.makeText(Password.this, "Groups table deleted...", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Password.this, MainActivity.class);
                    startActivity(i);
                }

                if (pw.equals("DeleteReminders")) {
                    db2.recreateRemindersTable();
                    Toast.makeText(Password.this, "Reminders table deleted...", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Password.this, MainActivity.class);
                    startActivity(i);
                }

                if (pw.equals(savedPassword)) {
                    Intent i = new Intent(Password.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(Password.this, "Invalid password...", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Password.this, LockImageActivity.class);
                    startActivity(i);
                }
            }
        });
    }
}


