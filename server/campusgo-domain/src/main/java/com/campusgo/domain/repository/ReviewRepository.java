package com.campusgo.domain.repository;

import com.campusgo.domain.model.ReviewRecord;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    ReviewRecord save(ReviewRecord review);

    Optional<ReviewRecord> findByTaskAndFromUser(long taskId, long fromUserId);

    List<ReviewRecord> listByTask(long taskId);

    int countByTask(long taskId);
}
