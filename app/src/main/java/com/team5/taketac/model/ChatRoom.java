package com.team5.taketac.model;

import java.util.UUID;

public class ChatRoom {
    private String id;
    private String name;

    public ChatRoom(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}