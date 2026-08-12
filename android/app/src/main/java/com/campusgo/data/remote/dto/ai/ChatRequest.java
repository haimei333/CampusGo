package com.campusgo.data.remote.dto.ai;

import com.google.gson.annotations.SerializedName;

public class ChatRequest {
    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("message")
    private String message;

    public ChatRequest(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }
}
