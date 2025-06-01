package com.team5.taketac; // 1단계에서 확인한 본인의 패키지명으로 수정하세요

public class ScheduleEntry {
    String day;        // 요일 (예: "MON", "TUE")
    int startTime;     // 시작 시간 (예: 9, 10) - 24시간 기준 시간
    int endTime;       // 종료 시간 (예: 11, 12) - 24시간 기준 시간 (해당 시간 전까지)
    String subjectName; // 과목명
    String classroom;   // 강의실

    // 생성자
    public ScheduleEntry(String day, int startTime, int endTime, String subjectName, String classroom) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectName = subjectName;
        this.classroom = classroom;
    }

    // Getter 메소드 (필요에 따라 Setter도 추가할 수 있습니다)
    public String getDay() {
        return day;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getClassroom() {
        return classroom;
    }

    // (선택 사항) Setter 메소드들
    public void setDay(String day) {
        this.day = day;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
}
