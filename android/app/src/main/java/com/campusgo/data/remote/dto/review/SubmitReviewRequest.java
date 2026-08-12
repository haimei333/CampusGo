package com.campusgo.data.remote.dto.review;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubmitReviewRequest {

    @SerializedName("score")
    public int score;

    @SerializedName("tags")
    public List<String> tags;

    @SerializedName("content")
    public String content;

    public SubmitReviewRequest(int score, List<String> tags, String content) {
        this.score = score;
        this.tags = tags;
        this.content = content;
    }
}
