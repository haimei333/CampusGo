package com.campusgo.data.remote.dto.task;

import com.campusgo.domain.model.TaskStatus;
import com.google.gson.annotations.SerializedName;

public class PublishTaskResponse {

    @SerializedName("taskId")
    public String taskId;

    @SerializedName("taskNo")
    public String taskNo;

    @SerializedName("status")
    public TaskStatus status;
}
