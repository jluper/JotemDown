package com.DataFinancial.NoteJackal;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author jluper
 *
 */
public class HelpActivity extends ActionBarActivity {
		  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_activity);
	 
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setTitle("Help: List");
		actionBar.setDisplayShowTitleEnabled(true);
	 }
	 
		@Override
		public void onResume() {
		    super.onResume();  // Always call the superclass method first
		}
	  
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.menu_help, menu);

			return true;
		}
		

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();

			if (id == R.id.menu_next_help) {
				
				return true;
			}

			return super.onOptionsItemSelected(item);
		}
  
}

