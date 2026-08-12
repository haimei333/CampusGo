package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class UseVoucherRequest {

    @SerializedName("voucherCode")
    public String voucherCode;

    public UseVoucherRequest(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}
