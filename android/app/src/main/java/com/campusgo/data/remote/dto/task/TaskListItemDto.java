package com.campusgo.data.remote.dto.task;

import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;
import com.google.gson.annotations.SerializedName;

public class TaskListItemDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("statusLabel")
    public String statusLabel;

    @SerializedName("description")
    public String description;

    @SerializedName("priceCent")
    public int priceCent;

    @SerializedName("priceYuan")
    public String priceYuan;

    @SerializedName("category")
    public TaskCategory category;

    @SerializedName("mode")
    public TaskMode mode;

    @SerializedName("status")
    public TaskStatus status;

    @SerializedName("navTarget")
    public String navTarget;

    @SerializedName("reserveForRole")
    public UserRole reserveForRole;
}
