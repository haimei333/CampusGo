package com.campusgo.data.remote.dto.wallet;

import com.google.gson.annotations.SerializedName;

public class WalletTransactionDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("timeLabel")
    public String timeLabel;

    @SerializedName("amount")
    public double amount;

    @SerializedName("type")
    public Type type;

    public enum Type {
        INCOME, EXPENSE
    }
}
