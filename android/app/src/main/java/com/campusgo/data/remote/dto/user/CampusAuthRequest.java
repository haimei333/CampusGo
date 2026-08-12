package com.campusgo.data.remote.dto.user;

import com.google.gson.annotations.SerializedName;

public class CampusAuthRequest {

    @SerializedName("realName")
    public String realName;

    @SerializedName("studentId")
    public String studentId;

    public CampusAuthRequest(String realName, String studentId) {
        this.realName = realName;
        this.studentId = studentId;
    }
}
