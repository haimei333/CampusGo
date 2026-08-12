package com.campusgo.data.remote.dto.points;

import com.google.gson.annotations.SerializedName;

public class MallProductDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("category")
    public String category;

    @SerializedName("pointsCost")
    public int pointsCost;

    @SerializedName("stock")
    public int stock;

    @SerializedName("emoji")
    public String emoji;

    @SerializedName("flashSale")
    public boolean flashSale;
}