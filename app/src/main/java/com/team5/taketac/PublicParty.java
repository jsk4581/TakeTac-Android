package com.team5.taketac;

public class PublicParty {
    private String id;          // Firestore 문서 ID
    private String title;       // 파티 제목
    private String location;    // 장소
    private String date;        // 표시용 날짜 (ex. "2025-06-04")
    private String time;        // 표시용 시간 (ex. "15:00")
    private String creatorId;   // 생성자 UID
    private long timestamp;     // 정렬 및 포맷을 위한 Unix 시간

    public PublicParty() {}  // Firebase 역직렬화용

    public PublicParty(String title, String location, String date, String time, String creatorId, long timestamp) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.time = time;
        this.creatorId = creatorId;
        this.timestamp = timestamp;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
