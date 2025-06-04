package com.team5.taketac.model;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    private String id;
    private String name;
    private String createrUid; // 생성자 ID
    private Map<String, Boolean> users;

    // 기본 생성자 (필수)
    public ChatRoom() {
    }

    // 생성자 (id 자동생성, users 초기화 및 creatorUid 추가)
    public ChatRoom(String name, String createrUid) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.createrUid = createrUid;
        this.users = new HashMap<>();
        this.users.put(createrUid, true);  // 생성자를 users에 포함
    }

    // 생성자 (UUID 자동 생성)
    public ChatRoom(String name) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
    }

    // getter, setter
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

    public String getCreaterUid() {
        return createrUid;
    }
    public void setCreaterUid(String createrUid) {
        this.createrUid = createrUid;
    }

    public Map<String, Boolean> getUsers() { return users; }
    public void setUsers(Map<String, Boolean> users) { this.users = users; }
}
