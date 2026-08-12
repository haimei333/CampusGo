package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class UserVoucherDto {

    @SerializedName("id")
    public String id;

    @SerializedName("productName")
    public String productName;

    @SerializedName("voucherCode")
    public String voucherCode;

    @SerializedName("status")
    public String status;

    @SerializedName("expireAt")
    public String expireAt;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("usedAt")
    public String usedAt;
}
