package com.team5.taketac;

import com.google.firebase.firestore.Exclude;

public class ScheduleEntry {
    @Exclude
    private String id;

    private String day;
    private int startTime;
    private int endTime;
    private String subjectName;
    private String classroom;

    public ScheduleEntry() {} // 🔥 반드시 있어야 Firestore가 객체로 변환 가능

    public ScheduleEntry(String day, int startTime, int endTime, String subjectName, String classroom) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectName = subjectName;
        this.classroom = classroom;
    }

    // ✅ 모든 필드에 대해 getter/setter 필요
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public int getStartTime() { return startTime; }
    public void setStartTime(int startTime) { this.startTime = startTime; }

    public int getEndTime() { return endTime; }
    public void setEndTime(int endTime) { this.endTime = endTime; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean overlapsWith(ScheduleEntry other) {
        return this.day.equals(other.day) &&
                !(this.endTime <= other.startTime || this.startTime >= other.endTime);
    }
}

