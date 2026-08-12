package com.campusgo.api.dto.review;

import com.campusgo.domain.model.ReviewRecord;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ReviewDto {

    private String id;
    private String taskId;
    private String fromUserId;
    private String toUserId;
    private int score;
    private List<String> tags;
    private String content;
    private Instant createdAt;

    public static ReviewDto from(ReviewRecord record) {
        return ReviewDto.builder()
                .id(String.valueOf(record.getId()))
                .taskId(String.valueOf(record.getTaskId()))
                .fromUserId(String.valueOf(record.getFromUserId()))
                .toUserId(String.valueOf(record.getToUserId()))
                .score(record.getScore())
                .tags(record.getTags())
                .content(record.getContent())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
