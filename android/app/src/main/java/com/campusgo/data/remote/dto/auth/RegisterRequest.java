package com.campusgo.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("phone")
    public String phone;

    @SerializedName("password")
    public String password;

    public RegisterRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}
