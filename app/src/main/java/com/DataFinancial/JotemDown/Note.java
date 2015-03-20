package com.DataFinancial.JotemDown;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note {

    private int id;
    //private String title;
    private int priority;
    private String body;
    private String createDate;
    private String editDate;
    private String latitude;
    private String longitude;
    private String hasReminder;
    private String image;
    private int group;

    public Note() {

        priority = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd", Locale.getDefault());
        Date curr_date = new Date();

        this.createDate = dateFormat.format(curr_date);
        this.editDate = this.createDate;
        this.latitude = "";
        this.longitude = "";
        this.hasReminder = "false";
        this.image = "";
        this.group = MainActivity.ROOT+1;
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
        this.image = "";
        this.group = MainActivity.ROOT;
        this.body = body;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {

        return body;
    }

    public void setBody(String body) {

        this.body = body;
    }

    public String getCreateDate() {

        return createDate;
    }

    public void setCreateDate(String date) {
        this.createDate = date;
    }

    @Override
    public String toString() {

        String bdy = body.replace(System.getProperty("line.separator"), "|");
        return id + "," + priority + "," + createDate + "," + editDate + "," + bdy + ", " + latitude + ", " + longitude + ", " + hasReminder + ", " + image + ", " + group;
    }

    public String toStringWoNewLine() {

        String bdy = body.replace(System.getProperty("line.separator"), "|");
        return id + "," + priority + "," + createDate + "," + editDate + "," + bdy + ", " + latitude + ", " + longitude + ", " + hasReminder + ", " + image + ", " + group;
    }

}
