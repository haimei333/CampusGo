package com.campusgo.data.remote.dto.ai;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("reply")
    private String reply;

    public String getSessionId() {
        return sessionId;
    }

    public String getReply() {
        return reply;
    }
}
