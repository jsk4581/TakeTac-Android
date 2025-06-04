package com.team5.taketac;

import java.util.UUID;

public class PartyRoom {
    private String id;
    private String title;       // 파티 제목
    private String location;    // 장소
    private long timestamp;     // 출발 시간 등
    private String creatorUid;  // 생성자 UID

    public PartyRoom() {}  // Firebase 역직렬화용

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
}
