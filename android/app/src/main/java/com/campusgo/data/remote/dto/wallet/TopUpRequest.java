package com.campusgo.data.remote.dto.wallet;

import com.google.gson.annotations.SerializedName;

public class TopUpRequest {

    @SerializedName("amount")
    public double amount;

    public TopUpRequest(double amount) {
        this.amount = amount;
    }
}
