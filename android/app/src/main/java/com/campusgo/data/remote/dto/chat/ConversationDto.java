package com.campusgo.data.remote.dto.chat;

import com.google.gson.annotations.SerializedName;

public class ConversationDto {

    @SerializedName("id")
    public String id;

    @SerializedName("taskId")
    public String taskId;

    @SerializedName("peerName")
    public String peerName;

    @SerializedName("peerRole")
    public String peerRole;

    @SerializedName("preview")
    public String preview;

    @SerializedName("lastMsgAt")
    public String lastMsgAt;

    @SerializedName("unreadCount")
    public int unreadCount;

    @SerializedName("taskTitle")
    public String taskTitle;

    @SerializedName("taskReward")
    public double taskReward;

    @SerializedName("archived")
    public boolean archived;
}
