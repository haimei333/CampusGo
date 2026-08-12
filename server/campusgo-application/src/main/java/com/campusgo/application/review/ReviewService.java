package com.campusgo.application.review;

import com.campusgo.domain.model.ReviewRecord;
import com.campusgo.domain.model.Task;

import java.util.List;

public interface ReviewService {

    ReviewRecord submit(long userId, long taskId, int score, List<String> tags, String content);

    List<ReviewRecord> listByTask(long userId, long taskId);
}
