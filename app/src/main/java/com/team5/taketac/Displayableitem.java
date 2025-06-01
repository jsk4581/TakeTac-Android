package com.team5.taketac;

// 시간표의 각 셀에 표시될 아이템의 타입을 나타내는 enum 또는 상수 집합
enum ItemType {
    TIME_LABEL, // 시간 표시 (09:00, 10:00 등)
    SCHEDULE_ENTRY, // 강의 정보
    EMPTY_SLOT // 빈 시간표 칸
}

// RecyclerView에 표시될 각 아이템의 정보를 담는 클래스
class DisplayableItem {
    ItemType type;
    String text; // 시간 레이블 또는 과목명
    String subText; // 강의실
    ScheduleEntry originalEntry; // 실제 ScheduleEntry 객체 (SCHEDULE_ENTRY 타입일 경우)
    boolean isContinuation; // 여러 시간 걸친 강의의 연속 부분인지 여부

    // 생성자들
    public DisplayableItem(ItemType type, String text) { // TIME_LABEL, EMPTY_SLOT (text는 시간)
        this.type = type;
        this.text = text;
    }

    public DisplayableItem(ItemType type, ScheduleEntry entry, boolean isContinuation) { // SCHEDULE_ENTRY
        this.type = type;
        this.originalEntry = entry;
        this.text = entry.getSubjectName();
        this.subText = entry.getClassroom();
        this.isContinuation = isContinuation;
    }
}
