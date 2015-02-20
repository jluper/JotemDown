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

public class ChangePassword extends ActionBarActivity {

	  private EditText password;
	  private EditText passwordRepeat;
	  private Button btnSubmit;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reset_password);
	 
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle("Change Password");
		actionBar.setDisplayShowTitleEnabled(true);
		
		addListenerOnButton();	 
	  }
	 
	  
	  public void addListenerOnButton() {
	 
		password = (EditText) findViewById(R.id.txtPassword);
		passwordRepeat = (EditText) findViewById(R.id.txtPasswordRepeat);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
	 
		
		SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
		String savedPassword = prefs.getString(Password.PASSWORD, getString(R.string.default_password));
		
		password.setText(savedPassword);
		
		btnSubmit.setOnClickListener(new OnClickListener() {
	 
			@Override
			public void onClick(View v) {
			  
				SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
				String savedPassword = prefs.getString(Password.PASSWORD, getString(R.string.default_password));

				if (password.getText().toString().equals(passwordRepeat.getText().toString())) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(Password.PASSWORD, password.getText().toString());
					editor.commit();
										
					Intent i = new Intent(ChangePassword.this, Password.class);
					startActivity(i);					
				} else {
					Toast.makeText(ChangePassword.this, "Password don't match, try again...",	Toast.LENGTH_LONG).show();									
				}
			}
		});
	  }
}

