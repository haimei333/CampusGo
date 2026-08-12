package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {

    Optional<ReviewEntity> findByTaskIdAndFromUserId(Long taskId, Long fromUserId);

    List<ReviewEntity> findByTaskId(Long taskId);

    int countByTaskId(Long taskId);
}
