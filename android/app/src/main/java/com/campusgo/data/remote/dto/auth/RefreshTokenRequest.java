package com.campusgo.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest {

    @SerializedName("refreshToken")
    public String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
