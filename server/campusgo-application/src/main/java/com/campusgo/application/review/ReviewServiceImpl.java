package com.campusgo.application.review;

import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.ReviewRecord;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.repository.ReviewRepository;
import com.campusgo.domain.repository.TaskRepository;
import com.campusgo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewRecord submit(long userId, long taskId, int score, List<String> tags, String content) {
        if (score < 1 || score > 5) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "评分需在 1–5 星");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "任务不存在"));
        if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.REVIEWED) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "任务完成后才可评价");
        }
        if (task.getRunnerId() == null) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "任务尚未有跑腿员");
        }

        long toUserId;
        if (userId == task.getPublisherId()) {
            toUserId = task.getRunnerId();
        } else if (userId == task.getRunnerId()) {
            toUserId = task.getPublisherId();
        } else {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权评价该任务");
        }

        if (reviewRepository.findByTaskAndFromUser(taskId, userId).isPresent()) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "您已评价过该任务");
        }

        String trimmed = content == null ? "" : content.trim();
        if (trimmed.length() > 400) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "评价内容不能超过 400 字");
        }

        ReviewRecord saved = reviewRepository.save(ReviewRecord.builder()
                .taskId(taskId)
                .fromUserId(userId)
                .toUserId(toUserId)
                .score(score)
                .tags(tags == null ? List.of() : tags)
                .content(trimmed)
                .isDefault(false)
                .createdAt(Instant.now())
                .build());

        userRepository.adjustCreditScore(toUserId, creditDelta(score));

        int reviewCount = reviewRepository.countByTask(taskId);
        if (reviewCount >= 2 && task.getStatus() == TaskStatus.COMPLETED) {
            taskRepository.save(task.toBuilder().status(TaskStatus.REVIEWED).build());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRecord> listByTask(long userId, long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "任务不存在"));
        if (userId != task.getPublisherId()
                && (task.getRunnerId() == null || userId != task.getRunnerId())) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权查看评价");
        }
        return reviewRepository.listByTask(taskId);
    }

    private static int creditDelta(int score) {
        return switch (score) {
            case 5 -> 10;
            case 4 -> 5;
            case 3 -> 0;
            case 2 -> -5;
            default -> -10;
        };
    }
}
