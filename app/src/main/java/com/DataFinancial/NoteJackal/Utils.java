package com.DataFinancial.NoteJackal;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {


	
	static public String convertDate(String date, String fromFormat, String toFormat) {

		SimpleDateFormat oldDateFormat = new SimpleDateFormat(fromFormat, Locale.getDefault());
		SimpleDateFormat newDateFormat = new SimpleDateFormat(toFormat,	Locale.getDefault());
		Date oldDate = new Date();
		Date newDate = new Date();

		try {
			oldDate = oldDateFormat.parse(date);
		} catch (ParseException e) {
			return (date);
		}

		return (newDateFormat.format(oldDate));
	}
	
	  static public boolean isValidPhone(String target)  {
			
		  return !TextUtils.isEmpty(target) && android.util.Patterns.PHONE.matcher(target).matches();
	  }
	  
	  static public boolean isValidEmail(CharSequence target) {
		  
		  return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}	
	
	   static public boolean isValidURL(String pUrl) {

	        URL u = null;
	        try {
	            u = new URL(pUrl);
	        } catch (MalformedURLException e) {
	            return false;
	        }
	        try {
	            u.toURI();
	        } catch (URISyntaxException e) {
	            return false;
	        }
	        return true;
	    }

	  public boolean isValidDate(String target) {
		  
		int[] daysInMonth = { 31,29,31,30,31,30,31,31,30,31,30,31};
		
		  // valid date is in format mm/dd/yy		  
		  String[] dateParts = target.split("/");

		  if (dateParts.length != 3) {
			   return false;
		  }
		  
		  if (dateParts[0].isEmpty() || dateParts[1].isEmpty() || dateParts[2].isEmpty()) 
			  return false;
		  
	    //check the month
		  int mm = Integer.parseInt(dateParts[0]);
		try {			
			if (mm < 1 || mm > 12) 
				return false;
		} catch (NumberFormatException e) {
			return false;
		}

	    //check the day
		try {
			int dd = Integer.parseInt(dateParts[1]);
			if (dd < 1 || dd > daysInMonth[mm-1]) 
				return false;
		} catch (NumberFormatException e) {
			return false;
		}

	    //check the year
		try {
			int yy = Integer.parseInt(dateParts[2]);
			 int curYear = Calendar.getInstance().get(Calendar.YEAR);
			if ((2000 + yy) < curYear || dateParts[2].length() > 2) 
				return false;
		} catch (NumberFormatException e) {
			return false;
		}		

		  return true;
	}
	  
	  public boolean isValidTime(String target) {
		  
		  
		  // valid date is in format hh:mm		  
		  String[] timeParts = target.split(":");
		  if (timeParts.length != 2) {
			   return false;
		  }
		  
		  if (timeParts[0].isEmpty() || timeParts[1].isEmpty())  
			  return false;
		  
		//check the hr
		try {
			int hh = Integer.parseInt(timeParts[0]);
			if (hh < 0 || hh > 24) 
				return false;
		} catch (NumberFormatException e) {
			return false;
		}	  

	    //check the min
		try {
			int mm = Integer.parseInt(timeParts[1]);
			if (mm < 0 || mm > 60 || timeParts[1].length() != 2) 
				return false;
		} catch (NumberFormatException e) {
			return false;
		}			
		
		return true;
	}	  

		public String incrementDay(String date) {
			
			SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd");
			Date remDate;
			try {
				remDate = df.parse(date);
			} catch (ParseException e) {
				return(date);
			}
			
			Calendar c = Calendar.getInstance();
			c.setTime(remDate);
			c.add(Calendar.DATE, 1);  // number of days to add
			String strDate = df.format(c.getTime());  // dt is now the new date
						
			return(strDate);
		}
			
	
		public String incrementMinute(String time, int min) {

           	String[] timeParts = time.split(":");
			String strHour = timeParts[0];
			String strMinute = timeParts[1];
			int intMinute = (Integer.parseInt(strMinute) + min) % 60;
			int intHour = Integer.parseInt(strHour);
			if (intMinute == 0) {
				intHour = (intHour + 1) % 24;
			}

			strHour = String.valueOf(intHour);
			if (strHour.length() == 1) {
				strHour = "0" + strHour;
			}
			if (strMinute.length() == 1) {
				strMinute = "0" + strMinute;
			}

			String newTime = String.valueOf(intHour) + ":" + String.valueOf(intMinute);

			return newTime;
		}
		
		public void copyFile(File src, File dst) throws IOException {
		    InputStream in = new FileInputStream(src);
		    OutputStream out = new FileOutputStream(dst);

		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    in.close();
		    out.close();
		}
}
