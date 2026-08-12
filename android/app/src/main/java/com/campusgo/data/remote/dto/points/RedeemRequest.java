package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class RedeemRequest {

    @SerializedName("productId")
    public long productId;

    @SerializedName("address")
    public String address;

    public RedeemRequest(long productId, String address) {
        this.productId = productId;
        this.address = address;
    }
}