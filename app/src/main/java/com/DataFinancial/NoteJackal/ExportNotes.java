package com.DataFinancial.NoteJackal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ExportNotes extends ActionBarActivity {

	  private EditText address;
	  private Button btnExport;
	  private EditText exportFile;
	  
	  //public static final String ADDRESS = "address";
	  private Note note = new Note();
		 
	  private static final String LAST_EXPORT_FILE = "LAST_EXPORT_FILE";
	  public static final String DATABASE_NAME = "notes.db";
	  protected List<Note> notes = new ArrayList<Note>();
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export_notes);
	 
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle(R.string.lbl_export_title);
		actionBar.setDisplayShowTitleEnabled(true);
				
			
		//Log.d(MainActivity.DEBUGTAG,"onCreate 1");
		exportFile = (EditText) findViewById(R.id.txtExportFile);	
		address = (EditText) findViewById(R.id.txtExportAddress);	
		
		SharedPreferences prefs = getSharedPreferences(ImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
		String file = prefs.getString(LAST_EXPORT_FILE,  "JotemDownExport");
		String addr = prefs.getString(SendNote.LAST_SEND_ADDRESS,  null);
		//Log.d(MainActivity.DEBUGTAG,"onCrate 2");
		if (file != null) {
			exportFile.setText(file);			
		}
		//Log.d(MainActivity.DEBUGTAG,"address = " + address.toString());
		if (addr != null) {
			address.setText(addr);			
		}
		
		int textLength = exportFile.getText().length();
		exportFile.setSelection(textLength, textLength);
		
		addListenerExportButton();	
		
	  }
	 
		@Override
		public void onResume() {
		    super.onResume();  // Always call the superclass method first

			////Log.d(MainActivity.DEBUGTAG, "in create newnote");
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		    
		}	  
	  
	  public void addListenerExportButton() {
	 
		  btnExport = (Button) findViewById(R.id.btnExport);
				  
		btnExport.setOnClickListener(new OnClickListener() {
						
			@Override
			public void onClick(View v) {
								
				makeTextExport();
				sendTextExport();

			    SharedPreferences prefs = getSharedPreferences(ImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(SendNote.LAST_SEND_ADDRESS,  address.getText().toString());
				editor.putString(LAST_EXPORT_FILE, exportFile.getText().toString());
				editor.commit();
				
				Intent i = new Intent(ExportNotes.this, MainActivity.class);
				startActivity(i);				
			}	 
		});
	 
	  }
	  
		  
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			//getMenuInflater().inflate(R.menu.menu_export_notes, menu);
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
	  
	  
	  private void sendTextExport() {
	      
		  String[] TO = {"jluper@triad.rr.com"};
	      String[] CC;
	      
	       	TO[0] = address.getText().toString();
	      	
	      	Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setData(Uri.parse("mailto:"));
			emailIntent.setType("text/plain");							

			String emailSubject;
			String emailText;			
		   		    	
		    	if (Utils.isValidEmail(TO[0])) {		
		    				    		
					File exportDir;
					if (checkExternalMedia()) {
						exportDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);				
					}
					else {
						exportDir = getFilesDir ();
					}
					
		    		 //File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					 //dir.mkdirs();
		    		 File file = new File(exportDir, exportFile.getText().toString() + ".txt");
		    		 
				     if (!file.exists() || !file.canRead()) {
				        	Toast.makeText(this, "Unable to access export file...",Toast.LENGTH_LONG).show();			
				        	return;
				     }	        

					 Uri uri = Uri.parse(file.toString());					 
					 emailIntent.putExtra(Intent.EXTRA_STREAM, uri);	    			
					 emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
				     emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NoteJackal export");
				     emailIntent.putExtra(Intent.EXTRA_TEXT, "NoteJackal export file attached.");
		    		
				     startActivity(Intent.createChooser(emailIntent, "Send mail..."));			        		 			
	
		    	} else {
		    		Toast.makeText(ExportNotes.this, 
		    		        "Invalid email...", Toast.LENGTH_LONG).show();
		    	}		    		
		    }
	      		  
	  
	  private void makeTextExport() {
		  
			if (checkExternalMedia()) {
				
				exportFile = (EditText) findViewById(R.id.txtExportFile);	
				
				DatabaseNotes db = new DatabaseNotes(this);
				notes = db.getAllNotes(null, DatabaseNotes.COL_CREATE_DATE, "DESC");  

			    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			     File file = new File(dir, exportFile.getText().toString() + ".txt");
		
			    try {
			        FileOutputStream f = new FileOutputStream(file);
			        PrintWriter pw = new PrintWriter(f);
			       
			        Iterator itr = notes.iterator();
			        while(itr.hasNext()) {
			        	pw.println(itr.next().toString());
			        }			        
	
			        pw.flush();
			        pw.close();
			        f.close();
			        
			        if (!file.exists() || !file.canRead()) {
			        	Toast.makeText(this, "Unable to create export file...",Toast.LENGTH_LONG).show();			        	
			        }	        
			        
			    } catch (Exception e) {
			    	Toast.makeText(this, "Exception creating export file. " + e.getMessage(),Toast.LENGTH_LONG).show();			    	
			    }
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
		    //Log.d(MainActivity.DEBUGTAG,"\n\nExternal Media: readable="
		     //       +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
		    
		    return mExternalStorageAvailable & mExternalStorageWriteable;
		}
		
}

