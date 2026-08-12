package com.campusgo.data.remote.dto.auth;

import com.campusgo.data.remote.dto.user.UserProfileDto;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("accessToken")
    public String accessToken;

    @SerializedName("refreshToken")
    public String refreshToken;

    @SerializedName("expiresIn")
    public long expiresIn;

    @SerializedName("userProfile")
    public UserProfileDto userProfile;
}
