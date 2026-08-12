package com.campusgo.data.remote.dto.chat;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {

    @SerializedName("msgType")
    public String msgType;

    @SerializedName("content")
    public String content;

    public SendMessageRequest(String content) {
        this.msgType = "TEXT";
        this.content = content;
    }
}
