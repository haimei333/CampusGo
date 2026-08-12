package com.campusgo.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class JoinGroupRequest {

    @SerializedName("address")
    public String address;

    public JoinGroupRequest(String address) {
        this.address = address;
    }
}
