package it.feio.android.omninotes.utils;

import com.google.api.client.util.DateTime;

public class DriveFileHolder {

    private String id;
    private String name;
    private DateTime modifiedTime;
    private long size;
    private DateTime createdTime;
    private Boolean starred;


    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Boolean getStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getNoteModifiedTime() {
        String[] parts = this.getName().split("_");
        return  parts.length > 1 ? Long.parseLong(parts[2]) : Long.MAX_VALUE;
    };

    public Long getNoteId() {
        String[] parts = this.getName().split("_");
        return  parts.length > 0 ? Long.parseLong(parts[1]) : Long.MAX_VALUE;
    };

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean exists() { return this.id != null; }
}