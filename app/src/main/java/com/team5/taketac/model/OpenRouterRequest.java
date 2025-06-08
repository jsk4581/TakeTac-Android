package com.team5.taketac.model;

import java.util.List;

public class OpenRouterRequest {
    private String model;
    private List<MessageData> messages;

    public OpenRouterRequest(String model, List<MessageData> messages) {
        this.model = model;
        this.messages = messages;
    }

    public static class MessageData {
        private String role;
        private String content;

        public MessageData(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
