package com.campusgo.data.remote;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * 与后端 {@code ApiResponse<T>} 对齐。
 */
public class ApiResponse<T> {

    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    @Nullable
    public T data;

    @SerializedName("traceId")
    @Nullable
    public String traceId;

    public boolean isSuccess() {
        return code == 0;
    }
}
