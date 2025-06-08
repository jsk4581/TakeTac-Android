package com.team5.taketac.model;

import java.util.Map;
import java.util.UUID;

public class PartyRoom {

    private String id;
    private String title;
    private String location;
    private long timestamp;
    private String creatorUid;

    // 참가자 목록 (키: 참가자 UID, 값: 참가자 이름 등)
    private Map<String, String> users;

    public PartyRoom() {
        // Firebase 역직렬화용 기본 생성자
    }

    public PartyRoom(String title, String location, long timestamp, String creatorUid) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.location = location;
        this.timestamp = timestamp;
        this.creatorUid = creatorUid;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getCreatorUid() { return creatorUid; }
    public void setCreatorUid(String creatorUid) { this.creatorUid = creatorUid; }

    public Map<String, String> getUsers() { return users; }
    public void setUsers(Map<String, String> users) { this.users = users; }
}
