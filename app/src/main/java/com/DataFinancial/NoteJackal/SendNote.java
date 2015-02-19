package com.DataFinancial.NoteJackal;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author jluper
 *
 */
public class SendNote extends ActionBarActivity {

	  private EditText address;
	  private Button btnSend;
	  //public static final String ADDRESS = "address";
	  private Note note = new Note();
	  public static final String LAST_SEND_ADDRESS = "LAST_ADDRESS";
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_note);
	 
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle("Send Note");
		actionBar.setDisplayShowTitleEnabled(true);
		
		addListenerOnButton();	
		
		Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
			note.setId(extras.getInt("id"));
			note.setCreateDate(extras.getString("createDate"));
			note.setEditDate(extras.getString("editDate"));
			note.setBody(extras.getString("body"));
			////Log.d(MainActivity.DEBUGTAG, "note info in NewNote: " + note.getId() + ", " + note.getCreateDate() + ", " + note.getEditDate() + ", " + note.getBody());
		}
		
		address = (EditText) findViewById(R.id.txtAddress);	
		SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
		String addr = prefs.getString(LAST_SEND_ADDRESS,  null);
		if (addr != null) {
			address.setText(addr);			
		}	
		
		
		int textLength = address.getText().length();
		address.setSelection(textLength, textLength);

	  }
	 
		@Override
		public void onResume() {
		    super.onResume();  // Always call the superclass method first

//			//Log.d(MainActivity.DEBUGTAG, "in onresume");
//			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
//			
//			InputMethodManager im = (InputMethodManager) this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//			im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_IMPLICIT);
		    
		}
	  
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.menu_send_note, menu);
			return true;
		}
		

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();
			

//			if (id == R.id.menu_send_done) {
//
//				Intent i = new Intent(SendNote.this, MainActivity.class);
//				startActivity(i);
//				
//				return true;
//			}

			return super.onOptionsItemSelected(item);
		}

		
	public void addListenerOnButton() {
	 
		//address = (EditText) findViewById(R.id.txtAddress);	
		btnSend = (Button) findViewById(R.id.btnSend);
	 
		btnSend.setOnClickListener(new OnClickListener() {
	 
			@Override
			public void onClick(View v) {
	 
				Utils utils = new Utils();
		        String[] TO = {"jluper@triad.rr.com"};;
		        String[] CC;
		        
		        TO[0] = address.getText().toString();
		        
		        //Log.d(MainActivity.DEBUGTAG, "valid email 1: " + TO[0]);
			    try {
			    	
			    	if (utils.isValidEmail(TO[0])) {		
			    		////Log.d(MainActivity.DEBUGTAG, "address: " + TO[0] + "Valid:" + isValidEmail(TO[0]));			 
			    		
				      	
						Intent emailIntent = new Intent(Intent.ACTION_SEND);
						emailIntent.setData(Uri.parse("mailto:"));
						emailIntent.setType("text/plain");	
			    		//Log.d(MainActivity.DEBUGTAG, "valid email: " + TO[0]);			    		
			    		String emailSubject = "Note from NoteJackal";
			    		String emailText = "Note from NoteJackal...\nID: " + note.getId() + "\nCreated Date: " + note.getCreateDate() + "\nLast Edit Date:" + note.getEditDate() + "\n\n" + note.getBody();			    		
						emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
					    emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
					    emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);				   
					    
				        try {
				        	//Log.d(MainActivity.DEBUGTAG, "before chooser");
				        	
						     Intent intent = Intent.createChooser(emailIntent, "Send mail...");
						     //Log.d(MainActivity.DEBUGTAG, "chooser intent = " +intent.toString());
					         startActivity(intent);
							//startActivity(Intent.createChooser(emailIntent, "Send mail..."));
							//finish();
							
							 //Toast.makeText(SendNote.this,"Email sent...", Toast.LENGTH_LONG).show();
							 
								
						} catch (Exception e) {
							Toast.makeText(SendNote.this, "Error Sending email. " + e.getMessage(), Toast.LENGTH_LONG).show();			
						}
			    			
			    	} else {			    		
			    		if (utils.isValidPhone(TO[0])) {
			    			//Log.d(MainActivity.DEBUGTAG, "valid phone: " + TO[0]);
			    			String msg = note.getBody();
			    			if (msg.length() > 160) {
			    				msg = msg.substring(0, 159);
			    			}
			    						    			
			    			SmsManager smsManager = SmsManager.getDefault();
			    			try {
								smsManager.sendTextMessage(TO[0], null, "Note from NoteJackal...\n" + msg, null, null);
								Toast.makeText(SendNote.this, 
								        "Text sent...", Toast.LENGTH_LONG).show();
								
								Intent i = new Intent(SendNote.this, MainActivity.class);
								startActivity(i);
							   
							} catch (Exception e) {
								Toast.makeText(SendNote.this, 
								        "Error Sending text. " + e.getMessage(), Toast.LENGTH_LONG).show();								
							}			 
			    		} else {
			    			Toast.makeText(SendNote.this, 
			    			        "Invalid phone or email...", Toast.LENGTH_LONG).show();
			    		}
			    		
			    	}
			    	
			    	
			        //Log.d(MainActivity.DEBUGTAG,"Finished sending note...");
			     } catch (android.content.ActivityNotFoundException ex) {
			        Toast.makeText(SendNote.this, 
			        "Unable to send note...", Toast.LENGTH_LONG).show();
			     }
			
				 
			    SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(LAST_SEND_ADDRESS,  TO[0]);
				editor.commit();
			    
	 
			}
	 
		});
	 
	  }
 
}

