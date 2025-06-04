package com.team5.taketac;

public class DisplayableItem {
    public ItemType type;
    public String text;
    public ScheduleEntry originalEntry;
    public boolean isContinuation;

    public DisplayableItem(ItemType type, String text) {
        this.type = type;
        this.text = text;
    }

    public DisplayableItem(ItemType type, ScheduleEntry originalEntry, boolean isContinuation) {
        this.type = type;
        this.originalEntry = originalEntry;
        this.isContinuation = isContinuation;
    }
}


