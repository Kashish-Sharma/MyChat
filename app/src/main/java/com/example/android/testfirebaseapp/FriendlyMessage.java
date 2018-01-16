package com.example.android.testfirebaseapp;

import java.sql.Timestamp;



public class FriendlyMessage {

    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String text;
    private String name;
    private String photoUrl;
    private long timeInMillis;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.timeInMillis = timestamp.getTime();
    }

    public long getTimeInMillis(){
        return timeInMillis;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
