package com.DataFinancial.NoteJackal;

public class Reminder {

	private int id;
	private int noteId;
	private String date;
	private String time;
	private String recur;
	private String phone;
	
	public String getPhone()
    {
		return phone;
	}

	public void setPhone(String phone)
    {
		this.phone = phone;
	}

	public int getId() {

        return id;
	}

	public void setId(int Id) {

        id = Id;
	}

	public int getNoteId() {

        return noteId;
	}

	public void setNoteId(int id) {

        noteId = id;
	}
    
	public String getDate() {

        return date;
	}


    public void setDate(String date) {

		int index = time.indexOf('/');
		if (index != 2) {
			if (index == 1)
				date = "0" + date;					
		}
		
		this.date = date;
	}
	public String getTime() {

		return time;
	}

	public void setTime(String time) {
		
		int index = time.indexOf(':');
		if (index != 2) {
			if (index == 1)
				time = "0" + time;
			else
				time = time.substring(0,3) + "0" + time.substring(2,4);			
		}
		
		this.time = time;
	}
	public String getRecur() {

        return recur;
	}

	public void setRecur(String recur)
    {
		this.recur = recur;
	}
	
	
	public Reminder() {
		
		noteId = -1;
		date = "01/01/2015";
		time = "00:00";
		recur = "false";
		phone = "";
		
	}
	
	public Reminder(int ntId, String dt, String tm, String rec, String ph) {
		
		noteId = ntId;
		date = dt;
		time = tm;
		recur = rec;
		phone = ph;
		
	}
	
	@Override
	public String toString() {		
		
		return id + "," + noteId + "," + date + "," +  time + "," + recur + "," + phone;
	}
	
}
