package com.campusgo.data.remote.dto.address;

import com.google.gson.annotations.SerializedName;

public class AddressUpsertRequest {

    @SerializedName("title")
    public String title;

    @SerializedName("detail")
    public String detail;

    @SerializedName("type")
    public String type;

    @SerializedName("isDefault")
    public boolean isDefault;

    public AddressUpsertRequest(String title, String detail, String type, boolean isDefault) {
        this.title = title;
        this.detail = detail;
        this.type = type;
        this.isDefault = isDefault;
    }
}
