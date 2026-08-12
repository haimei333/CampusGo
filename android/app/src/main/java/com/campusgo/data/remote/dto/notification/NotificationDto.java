package com.campusgo.data.remote.dto.notification;

import com.google.gson.annotations.SerializedName;

public class NotificationDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("body")
    public String body;

    @SerializedName("timeLabel")
    public String timeLabel;

    @SerializedName("unread")
    public boolean unread;

    @SerializedName("linkType")
    public String linkType;

    @SerializedName("bizType")
    public String bizType;

    @SerializedName("linkTargetId")
    public String linkTargetId;

    @SerializedName("taskStatus")
    public String taskStatus;

    @SerializedName("taskMode")
    public String taskMode;

    @SerializedName("chatPeerName")
    public String chatPeerName;

    @SerializedName("chatTaskTitle")
    public String chatTaskTitle;

    @SerializedName("chatTaskId")
    public String chatTaskId;

    @SerializedName("createdAt")
    public String createdAt;
}
