package com.DataFinancial.JotemDown;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePassword extends ActionBarActivity {

    private EditText password;
    private EditText passwordRepeat;
    private int group;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle("Change Password");
        actionBar.setDisplayShowTitleEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = (extras.getInt("group"));
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }

        addListenerOnButton();
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(ChangePassword.this, MainActivity.class);

        i.putExtra("group", group);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;

    }
    public void addListenerOnButton() {

        password = (EditText) findViewById(R.id.txtPassword);
        passwordRepeat = (EditText) findViewById(R.id.txtPasswordRepeat);
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);

        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        String savedPassword = prefs.getString(Password.PASSWORD, getString(R.string.default_password));

        password.setText(savedPassword);

        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);

                if (password.getText().toString().equals(passwordRepeat.getText().toString())) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Password.PASSWORD, password.getText().toString());
                    editor.apply();

                    Intent i = new Intent(ChangePassword.this, MainActivity.class);
                    i.putExtra("group", group);
                    i.putExtra("group_name", groupName);
                    i.putExtra("sort_col", sortCol);
                    i.putExtra("sort_name", sortName);
                    i.putExtra("sort_dir", sortDir);
                    startActivity(i);
                } else {
                    Toast.makeText(ChangePassword.this, "Passwords don't match, try again...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

