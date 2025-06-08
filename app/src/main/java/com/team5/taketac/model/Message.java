package com.team5.taketac.model;

public class Message {
    private String text;
    private boolean sentByUser;
    private String senderNickname;  // 닉네임 필드 추가

    public Message() {
        // Firebase나 다른 곳에서 데이터 바인딩할 때 필요할 수 있음
    }

    public Message(String text, boolean sentByUser, String senderNickname) {
        this.text = text;
        this.sentByUser = sentByUser;
        this.senderNickname = senderNickname;
    }

    public String getText() {
        return text;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }

    public String getSenderNickname() {
        return senderNickname;
    }


    public void setText(String text) {
        this.text = text;
    }

    public void setSentByUser(boolean sentByUser) {
        this.sentByUser = sentByUser;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

}
