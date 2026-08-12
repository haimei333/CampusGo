package com.campusgo.data.remote.dto.user;

import com.google.gson.annotations.SerializedName;

public class UpdateNicknameRequest {

    @SerializedName("nickname")
    public String nickname;

    public UpdateNicknameRequest(String nickname) {
        this.nickname = nickname;
    }
}
