package com.team5.taketac.model;

public class Message {
    private String text;
    private String senderUid;           // UID로 보낸 사람을 명시
    private String senderNickname;      // 닉네임

    public Message() {
        // Firebase 등에서 역직렬화할 때 필요
    }

    public Message(String text, String senderUid, String senderNickname) {
        this.text = text;
        this.senderUid = senderUid;
        this.senderNickname = senderNickname;
    }

    public String getText() {
        return text;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    // 메시지를 보낸 사람이 현재 유저인지 판단
    public boolean isSentByUser() {
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        return senderUid != null && senderUid.equals(currentUid);
    }
}
