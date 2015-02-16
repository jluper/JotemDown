package com.DataFinancial.NoteJackal;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ImportNotes extends ActionBarActivity {

	  
	  private Button btnImport;
	  private EditText importFile;
	  private AlertDialog confirmImport; 
	  //public static final String ADDRESS = "address";
	  private Note note = new Note();
	 
	  private static final String LAST_BACKUP_FILE = "LAST_BACKUP_FILE";
	  protected List<Note> notes = new ArrayList<Note>();
	 

	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_notes);
	 
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle("Import Notes");
		actionBar.setDisplayShowTitleEnabled(true);
		
		
			
		//Log.d(MainActivity.DEBUGTAG,"onCreate 1");
		importFile = (EditText) findViewById(R.id.txtImportFile);	
				
		SharedPreferences prefs = getSharedPreferences(ImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
		String file = prefs.getString(LAST_BACKUP_FILE,  "NoteJackalBackup");
		String addr = prefs.getString(SendNote.LAST_SEND_ADDRESS,  null);
		//Log.d(MainActivity.DEBUGTAG,"onCreate 2");
		if (file != null) {
			importFile.setText(file);			
		}

		buildConfirmDialog();
		
		
		
		int textLength = importFile.getText().length();
		importFile.setSelection(textLength, textLength);
		
		addListenerImportButton();	
		
	  }
	 
		@Override
		public void onResume() {
		    super.onResume();  // Always call the superclass method first

			////Log.d(MainActivity.DEBUGTAG, "in create newnote");
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		    
		}
		
	  public void buildConfirmDialog() {
		  
		   AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
		   dlgBuilder.setTitle("Confirm Import");
		   dlgBuilder.setIcon(R.drawable.btn_check_buttonless_on);
		   dlgBuilder.setMessage(R.string.dialog_import_notes);
	       dlgBuilder.setCancelable(true);
	      
	       dlgBuilder.setPositiveButton(R.string.dialog_positive,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
       				
                    if (importFromFile()) {
                        Intent i = new Intent(ImportNotes.this, MainActivity.class);
          				startActivity(i);
                    }
       
                }
            });
	       
	        dlgBuilder.setNegativeButton(R.string.dialog_negative,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	
                	//Log.d(MainActivity.DEBUGTAG, "in confirm dialogue negative");
                    dialog.cancel();
                    
 
                    
                }
            });

            confirmImport = dlgBuilder.create();
            
	  
	  }
	  
	  public void addListenerImportButton() {
	 
		btnImport = (Button) findViewById(R.id.btnImport);
				  
		btnImport.setOnClickListener(new OnClickListener() {
						
			@Override
			public void onClick(View v) {
					
				
				confirmImport.show();
				
				//importFromFile();						
			}	 
		});	 
	  }
	  
	 	  
	  private boolean importFromFile() {
		  
			if (checkExternalMedia()) {
				
				importFile = (EditText) findViewById(R.id.txtImportFile);	
				File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			    File file = new File(dir, importFile.getText().toString() + ".txt");
			    Log.d(MainActivity.DEBUGTAG, "import file = " + file);
		        if (!file.exists() || !file.canRead()) {
		        	Log.d(MainActivity.DEBUGTAG, "file exists = " + file.exists());
		        	Toast.makeText(this, "File not found...",Toast.LENGTH_LONG).show();			  
		        	return false;
		        }	        
		        
		        DatabaseNotes db = new DatabaseNotes(this);
		   		        
		        String line;
		        String[] splitNote; 
		        try {
		            // Open the file that is the first 
		            // command line parameter
		        	SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd");
	        		Date curr_date = new Date();
	        		
	        		String today = dateFormat.format(curr_date);
	        		//Log.d(MainActivity.DEBUGTAG, "todays date in import: " + today);
		            FileInputStream fstream = new FileInputStream(file.getPath());
		            // Get the object of DataInputStream
		            DataInputStream in = new DataInputStream(fstream);
		            BufferedReader br = new BufferedReader(new InputStreamReader(in));
		            while ((line = br.readLine()) != null && !line.isEmpty()) {
		            	
		            	//Log.d(MainActivity.DEBUGTAG, "line: " + line + "  ");
		            	//splitNote = line.split(",");
		            	Note note = new Note();
		            	note.setId(1);
		            	note.setPriority(0);	        		
		            	note.setCreateDate(today);
		            	note.setEditDate(today);
		            	//note.setBody(splitNote[1].replace("|", "\n"));
		            	//note.setBody(splitNote[1].replace("~", "|"));
		            	
		            	//note.setBody(line.replace("|", "\n"));
		            	note.setBody(line.replace("|", "\n").replace("~", "|"));
		            	notes.add(note);		            	
		            }		            		  
								
		            for (int i = 0; i < notes.size(); i++) {		            	
		            	db.addNote(notes.get(i));		            			    			
		    		}

		            in.close();
		            
					Toast.makeText(ImportNotes.this, 
		    		        "Import completed...", Toast.LENGTH_LONG).show();
					
		        } catch (Exception e) {
		    	  Toast.makeText(getBaseContext(), "Error importing notes..." + e.getMessage(), Toast.LENGTH_LONG).show();
		    	 
		    	  return false; 
		        }		        
			}	
			
		    SharedPreferences prefs = getSharedPreferences(ImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(LAST_BACKUP_FILE, importFile.getText().toString());
			editor.commit();
		    
			return true;
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
		    //        +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
		    
		    return mExternalStorageAvailable & mExternalStorageWriteable;
		}
		

}

