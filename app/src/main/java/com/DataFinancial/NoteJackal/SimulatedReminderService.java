package com.DataFinancial.NoteJackal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

	public class SimulatedReminderService extends AsyncTask<List<Reminder>, Void, Void> {
		
		protected Void doInBackground(List<Reminder>... passing) {
			
			while (true) {			
				try {
					Thread.sleep(1000*30);
					
					List<Reminder> reminders = passing[0];
					
					for (int i=0; i<reminders.size(); i++) {
						//Log.d(MainActivity.DEBUGTAG, "reminders in service:" + reminders.get(i).toString());
						SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
						Date today = new Date();
						String reminderDateTime = reminders.get(i).getDate() + " " + reminders.get(i).getTime();
						if (df.format(today).equals(reminderDateTime)) {
						Log.d(MainActivity.DEBUGTAG, "Today = " + df.format(today));						
						Log.d(MainActivity.DEBUGTAG, "Reminder date = " + reminders.get(i).getDate() + " " + reminders.get(i).getTime());
						}					
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
				//return null;		
		}

		
		protected void onPostExecute(Boolean pass) {
				return;
		}			
	
	
		 protected void onPostExecute() {
		 
		 }
	
}	


