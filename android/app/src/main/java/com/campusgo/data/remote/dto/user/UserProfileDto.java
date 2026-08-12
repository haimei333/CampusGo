package com.campusgo.data.remote.dto.user;

import com.campusgo.domain.model.UserRole;
import com.google.gson.annotations.SerializedName;

public class UserProfileDto {

    @SerializedName("id")
    public Long id;

    @SerializedName("phone")
    public String phone;

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("avatarUrl")
    public String avatarUrl;

    @SerializedName("creditScore")
    public int creditScore;

    @SerializedName("activeRole")
    public UserRole activeRole;

    @SerializedName("campusStatus")
    public String campusStatus;
}
