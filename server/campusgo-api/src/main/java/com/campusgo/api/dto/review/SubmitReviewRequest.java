package com.campusgo.api.dto.review;

import com.campusgo.domain.model.ReviewRecord;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SubmitReviewRequest {

    @NotNull(message = "请评分")
    @Min(1)
    @Max(5)
    private Integer score;

    private List<String> tags;

    @Size(max = 400, message = "评价内容不能超过 400 字")
    private String content;
}
