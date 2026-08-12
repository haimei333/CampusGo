package com.campusgo.data.remote.dto.address;

import com.google.gson.annotations.SerializedName;

public class AddressDto {

    @SerializedName("id")
    public String id;

    @SerializedName("type")
    public String type;

    @SerializedName("title")
    public String title;

    @SerializedName("detail")
    public String detail;

    @SerializedName("isDefault")
    public boolean isDefault;
}
