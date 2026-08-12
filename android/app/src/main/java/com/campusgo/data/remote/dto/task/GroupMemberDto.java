package com.campusgo.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class GroupMemberDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("role")
    public Role role;

    @SerializedName("addressSummary")
    public String addressSummary;

    @SerializedName("paidAmount")
    public double paidAmount;

    @SerializedName("joined")
    public boolean joined;

    public enum Role {
        CREATOR, MEMBER, EMPTY_SLOT
    }
}
