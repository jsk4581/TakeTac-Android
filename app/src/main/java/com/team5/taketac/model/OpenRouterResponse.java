package com.team5.taketac.model;

import java.util.List;

public class OpenRouterResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public static class Message {
            private String role;
            private String content;

            public String getContent() {
                return content;
            }
        }
    }
}
