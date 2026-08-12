package com.campusgo.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class CancelTaskRequest {

    @SerializedName("reason")
    public String reason;

    public CancelTaskRequest(String reason) {
        this.reason = reason;
    }
}
