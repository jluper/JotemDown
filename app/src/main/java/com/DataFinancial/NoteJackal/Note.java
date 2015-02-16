package com.DataFinancial.NoteJackal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class Note  {
	
	private int id;
	//private String title;
	private int priority;
	private String body;
	private String createDate;
	private String editDate;
	private String latitude;
	private String longitude;
    private String hasReminder;

    public String getHasReminder() {
        return hasReminder;
    }

    public void setHasReminder(String hasReminder) {
        this.hasReminder = hasReminder;
    }

    public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		if (latitude == null) {
			latitude = "";
		}
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		if (longitude == null) {
			longitude = "";
		}
		this.longitude = longitude;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	
	public String getEditDate() {
		return editDate;
	}

	public void setEditDate(String editDate) {
		this.editDate = editDate;
	}
	
	
//	public String getTitle() {
//		return title;
//	}
	
	public int getId() {
		return id;
		
	}
	public void setId(int id) {
		this.id = id;
	}
	
//	public void setTitle(String title) {
//		this.title = title;
//	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getCreateDate() {
		return createDate;
	}
	
		
	@Override
	public String toString() {
		
		String bdy = body.replace(System.getProperty("line.separator"), "|");
		return id + "," + priority + "," + createDate + "," +  editDate + "," + bdy + ", " + latitude + ", " + longitude + ", " + hasReminder;
	}
	
	public String toStringWoNewLine() {
		
		String bdy = body.replace(System.getProperty("line.separator"), "|");
		return id + "," + priority + "," + createDate + "," +  editDate + "," + bdy+ ", " + latitude + ", " + longitude + ", " + hasReminder;
	}
	
	
	public void setCreateDate(String date) {
		this.createDate = date;
	}
	
	public Note() {
		
		priority = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd", Locale.getDefault());
		Date curr_date = new Date();

		this.createDate = dateFormat.format(curr_date); 
		this.editDate = this.createDate;
		this.latitude = "";
		this.longitude = "";
        this.hasReminder = "false";
	}
		
	public Note(String body) {
				
		priority = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd", Locale.getDefault());
		Date curr_date = new Date();

		this.createDate = dateFormat.format(curr_date); 
		this.editDate = this.createDate;
		this.latitude = "";
		this.longitude = "";
        this.hasReminder = "false";
				
		
//		int index = body.indexOf("\n");
//		if (index > 0) {
//			this.title = body.substring(0, body.indexOf("\n")) + " ...";
//		} else {
//			this.title = body;
//		}
	
		this.body = body;
				
		////Log.d(MainActivity.DEBUGTAG, "Note constructor");
	}

}
