package com.team5.taketac.model;

import java.util.List;

public class ChatRoomInfo {
    private String id; // 파티 문서 ID = 채팅방 ID
    private List<String> nicknames;

    public ChatRoomInfo(String id, List<String> nicknames) {
        this.id = id;
        this.nicknames = nicknames;
    }

    public String getId() { return id; }
    public List<String> getNicknames() { return nicknames; }
}
