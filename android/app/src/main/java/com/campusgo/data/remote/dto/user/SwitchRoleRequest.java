package com.campusgo.data.remote.dto.user;

import com.campusgo.domain.model.UserRole;
import com.google.gson.annotations.SerializedName;

public class SwitchRoleRequest {

    @SerializedName("activeRole")
    public UserRole activeRole;

    public SwitchRoleRequest(UserRole activeRole) {
        this.activeRole = activeRole;
    }
}
