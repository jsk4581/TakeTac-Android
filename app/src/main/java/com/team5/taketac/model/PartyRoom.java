package com.team5.taketac.model;

public class PartyRoom {
    private String id;
    private String title;
    private String location;
    private long timestamp;
    private String creatorUid;

    public PartyRoom() {
        // 기본 생성자 (Firebase 용)
    }

    public PartyRoom(String id, String title, String location, long timestamp, String creatorUid) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.timestamp = timestamp;
        this.creatorUid = creatorUid;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }
}
