package com.campusgo.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class DeliveryPhotoRequest {

    @SerializedName("photoUrl")
    public String photoUrl;

    public DeliveryPhotoRequest(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
