package com.campusgo.data.remote.dto.chat;

import com.google.gson.annotations.SerializedName;

public class ChatMessageDto {

    @SerializedName("id")
    public String id;

    @SerializedName("conversationId")
    public String conversationId;

    @SerializedName("taskId")
    public String taskId;

    @SerializedName("senderId")
    public String senderId;

    @SerializedName("msgType")
    public String msgType;

    @SerializedName("content")
    public String content;

    @SerializedName("mine")
    public boolean mine;

    @SerializedName("read")
    public boolean read;

    @SerializedName("createdAt")
    public String createdAt;
}
