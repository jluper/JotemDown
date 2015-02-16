package com.DataFinancial.NoteJackal;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

	public class ReminderService extends Service {
		
		static boolean listEmpty = false;
		static boolean running;
		static Thread t;
		static int startID;
		WakeLock wakeLock;
		SmsManager smsManager = SmsManager.getDefault();
		
		@Override
		public IBinder onBind(Intent arg0) {
			
			return null;
		}

		@Override
		  public void onCreate() {
			 super.onCreate();
				//Log.d(MainActivity.DEBUGTAG, "***onCreate");
				
				PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
				wakeLock.acquire();	
		}

		@Override
		  public void onDestroy() {			
			//Log.d(MainActivity.DEBUGTAG, "*** onDestroy");
//			
//			if(myService.getThread()!=null){
//			      myService.getThread().interrupt();
//			      myService.setThread(null);

			running = false;
			t.interrupt();
			t = null;

			wakeLock.release();
			
			super.onDestroy();	
		}

		
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			//Log.d(MainActivity.DEBUGTAG, "***onStart");
			
			
			
			startID = startId;	
			running = true;
									
			final DatabaseReminders db = new DatabaseReminders(this);
			final DatabaseNotes dbNotes = new DatabaseNotes(this);
			List<Reminder> reminders = new ArrayList<Reminder>();
			
			reminders = db.getUnexpiredReminders();
			//Log.d(MainActivity.DEBUGTAG, "Srvc: list reminders size before loop = " + reminders.size());
			final List<Reminder> remList = reminders;			
			Log.d(MainActivity.DEBUGTAG, "Srvc: list remlist size before loop = " + remList.size());
			
			 Runnable r = new Runnable() {
			       public void run() {
			        	
			    	   //Log.d(MainActivity.DEBUGTAG, "list size = " + remList.size() + "running = " + running); 
//				    	while (remList.size() > 0 && running == true) {   
//					    	try {
//					    		//Log.d(MainActivity.DEBUGTAG, "Srvc: SLEEP");
//								Thread.sleep(1000*60);
//								
//							} catch (InterruptedException e) {
//								//Log.d(MainActivity.DEBUGTAG, "Srvc: Sleep failed: " + e.getMessage()); 		
//								running = false;
//								continue;
//							}
					    	//Log.d(MainActivity.DEBUGTAG, "Srvc: list size in loop = " + remList.size());
					    	
					    	SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
							Date today = new Date();
							
							for (int i=remList.size(); i>0; i--) {
																	
								String reminderDateTime = remList.get(i-1).getDate() + " " + remList.get(i-1).getTime();								
								String strToday = df.format(today);
								Log.d(MainActivity.DEBUGTAG, "Srvc: Today: " + df.format(today) + " Reminder date: " + reminderDateTime);
								
								if (strToday.compareTo(reminderDateTime) >= 0) {
									Log.d(MainActivity.DEBUGTAG, "Srvc: **** MATCH... Today: " + df.format(today) + " Rem date: " + reminderDateTime);
									
									Note note = new Note();
									note = dbNotes.getNote(remList.get(i-1).getNoteId());
									
									if (!remList.get(i-1).getPhone().isEmpty()) {
										smsManager.sendTextMessage(remList.get(i-1).getPhone(), null, note.getBody(), null, null);
										
									}
									
									reminderNotify(remList.get(i-1));
									
									db.deleteReminder(remList.get(i-1).getId());
									
									
									//Log.d(MainActivity.DEBUGTAG, "recur: " + remList.get(i-1).getRecur());
									if (remList.get(i-1).getRecur().equals("true")) {
										Utils utils = new Utils();
									    String newDate = utils.incrementDay(remList.get(i-1).getDate());
                                        //String newTime = utils.incrementMinute(remList.get(i-1).getTime(), 2);
                                        //remList.get(i-1).setTime(newTime);
                                        remList.get(i-1).setDate(newDate);
									    String dateTime = remList.get(i-1).getDate() + " " + remList.get(i-1).getTime();

									    Log.d(MainActivity.DEBUGTAG, "reminder date Time before :" + remList.get(i-1).toString());	
									    
									    //remList.get(i-1).setDate("15/02/13");
									    //remList.get(i-1).setTime("15:24");
									    
									    Log.d(MainActivity.DEBUGTAG, "reminder date Time after :" + remList.get(i-1).toString());	
									    long rowId = db.addReminder(remList.get(i-1));

									    //addReminderAlarm("15/02/13 15:25");
									    addReminderAlarm(dateTime);
									    
									    //Log.d(MainActivity.DEBUGTAG, "rowId on update = " + rowId);
									}
									
									remList.remove(i-1);
									
								}	
								
//								if (reminderDateTime.compareTo(strToday) == -1) {
//									remList.remove(i-1);
//								}
								
								//Log.d(MainActivity.DEBUGTAG, "Srvc: # rem in list: " + remList.size());
							}
//						}	
				    	//listEmpty = true;
				    	
				    	stopSelfResult(startID);
					}					
		      };
//			    
//			if (listEmpty) {
//				Log.d(MainActivity.DEBUGTAG, "StopSelf...");
//				this.stopSelf();
//			}
		    	
			
			Log.d(MainActivity.DEBUGTAG, "Started...");
		    
		    t = new Thread(r);
			t.start();		

			return Service.START_NOT_STICKY;	
		}

				
		protected void onPostExecute(Boolean pass) {
				return;
		}			
	
	
		 protected void onPostExecute() {
		 
		 }
	
		 private void addReminderAlarm(String dateTime) {
			 
			 Log.d(MainActivity.DEBUGTAG, "in addReminderAlarm in service");
			 Intent intent = new Intent("com.DataFinancial.NoteJackal.reminder");
			  // add data
			  intent.putExtra("message", "add_reminder");
			  intent.putExtra("date_time", dateTime);
			  //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			  sendBroadcast(intent);
		 }
		 
		 void reminderNotify(Reminder rem) {
				
				DatabaseNotes db = new DatabaseNotes(this);
				
				Note note = db.getNote(rem.getNoteId());
				NotificationCompat.Builder mBuilder =
					    new NotificationCompat.Builder(this)
					    .setSmallIcon(R.drawable.note_yellow)
					    .setContentTitle(getString(R.string.notification_title))
					    .setContentText(note.getBody());		
				
				Intent resultIntent = new Intent(this, MainActivity.class);
				
				// Because clicking the notification opens a new ("special") activity, there's
				// no need to create an artificial back stack.
				PendingIntent resultPendingIntent =
				    PendingIntent.getActivity(this, rem.getNoteId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				
				mBuilder.setContentIntent(resultPendingIntent);		
				
				// Sets an ID for the notification
				int mNotificationId = rem.getNoteId();
				//Log.d(MainActivity.DEBUGTAG,"NotificationId = " + mNotificationId);
				// Gets an instance of the NotificationManager service
				NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				// Builds the notification and issues it.
				mNotifyMgr.notify(mNotificationId, mBuilder.build());
				
				//Log.d(MainActivity.DEBUGTAG, "Notificatin sent...");
			}		 
	
}	


