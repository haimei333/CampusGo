package com.campusgo.data.remote.dto.wallet;

import com.google.gson.annotations.SerializedName;

public class WalletResponse {

    @SerializedName("balanceCent")
    public long balanceCent;

    @SerializedName("frozenCent")
    public long frozenCent;

    @SerializedName("totalIncomeCent")
    public long totalIncomeCent;

    @SerializedName("balanceYuan")
    public String balanceYuan;
}
