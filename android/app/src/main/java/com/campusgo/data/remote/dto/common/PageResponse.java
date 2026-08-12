package com.campusgo.data.remote.dto.common;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PageResponse<T> {

    @SerializedName("list")
    public List<T> list;

    @SerializedName("page")
    public int page;

    @SerializedName("pageSize")
    public int pageSize;

    @SerializedName("total")
    public long total;
}
