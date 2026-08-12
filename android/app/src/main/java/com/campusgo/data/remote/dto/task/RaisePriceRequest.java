package com.campusgo.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class RaisePriceRequest {

    @SerializedName("addCent")
    public int addCent;

    public RaisePriceRequest(int addCent) {
        this.addCent = addCent;
    }
}
