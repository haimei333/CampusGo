package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.model.ReviewRecord;
import com.campusgo.domain.repository.ReviewRepository;
import com.campusgo.infrastructure.persistence.entity.ReviewEntity;
import com.campusgo.infrastructure.persistence.jpa.ReviewJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ReviewJpaRepository jpaRepository;

    @Override
    public ReviewRecord save(ReviewRecord review) {
        ReviewEntity entity = new ReviewEntity();
        entity.setTaskId(review.getTaskId());
        entity.setFromUserId(review.getFromUserId());
        entity.setToUserId(review.getToUserId());
        entity.setScore((short) review.getScore());
        entity.setTagsJson(encodeTags(review.getTags()));
        entity.setContent(review.getContent());
        entity.setIsDefault((short) (review.isDefault() ? 1 : 0));
        if (review.getCreatedAt() != null) {
            entity.setCreatedAt(review.getCreatedAt());
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<ReviewRecord> findByTaskAndFromUser(long taskId, long fromUserId) {
        return jpaRepository.findByTaskIdAndFromUserId(taskId, fromUserId).map(this::toDomain);
    }

    @Override
    public List<ReviewRecord> listByTask(long taskId) {
        return jpaRepository.findByTaskId(taskId).stream().map(this::toDomain).toList();
    }

    @Override
    public int countByTask(long taskId) {
        return jpaRepository.countByTaskId(taskId);
    }

    private ReviewRecord toDomain(ReviewEntity entity) {
        return ReviewRecord.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .fromUserId(entity.getFromUserId())
                .toUserId(entity.getToUserId())
                .score(entity.getScore())
                .tags(decodeTags(entity.getTagsJson()))
                .content(entity.getContent())
                .isDefault(entity.getIsDefault() != 0)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static String encodeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(tags);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> decodeTags(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
