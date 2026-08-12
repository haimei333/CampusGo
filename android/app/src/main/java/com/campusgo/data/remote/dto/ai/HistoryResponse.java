package com.campusgo.data.remote.dto.ai;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryResponse {
    @SerializedName("sessions")
    private List<String> sessions;

    @SerializedName("messages")
    private List<Message> messages;

    public List<String> getSessions() {
        return sessions;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        @SerializedName("timestamp")
        private String timestamp;

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}
