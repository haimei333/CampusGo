package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.repository.TaskRepository;
import com.campusgo.infrastructure.persistence.entity.TaskEntity;
import com.campusgo.infrastructure.persistence.entity.TaskGroupMemberEntity;
import com.campusgo.infrastructure.persistence.entity.TaskStatusLogEntity;
import com.campusgo.infrastructure.persistence.jpa.TaskGroupMemberJpaRepository;
import com.campusgo.infrastructure.persistence.jpa.TaskJpaRepository;
import com.campusgo.infrastructure.persistence.jpa.TaskStatusLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    /** 从库中已有单号续号，避免重启后与 uk_task_no 冲突。 */
    private final AtomicLong taskNoSeq = new AtomicLong(20240803000L);

    private final TaskJpaRepository taskJpa;
    private final TaskGroupMemberJpaRepository memberJpa;
    private final TaskStatusLogJpaRepository statusLogJpa;

    @PostConstruct
    void initTaskNoSequence() {
        taskJpa.findAll().stream()
                .map(TaskEntity::getTaskNo)
                .filter(no -> no != null && no.length() > 2)
                .mapToLong(TaskRepositoryImpl::parseTaskNoSuffix)
                .max()
                .ifPresent(taskNoSeq::set);
    }

    @Override
    public Task save(Task task) {
        TaskEntity entity = task.getId() == null
                ? new TaskEntity()
                : taskJpa.findById(task.getId()).orElse(new TaskEntity());
        apply(entity, task);
        return toModel(taskJpa.save(entity));
    }

    @Override
    public Optional<Task> findById(long id) {
        return taskJpa.findById(id).map(this::toModel);
    }

    @Override
    public List<Task> findHall() {
        List<Task> pending = taskJpa.findByStatusAndRunnerIdIsNullOrderByCreatedAtDesc(TaskStatus.PENDING).stream()
                .map(this::toModel)
                .toList();
        List<Task> reserving = taskJpa.findByModeAndStatusOrderByCreatedAtDesc(TaskMode.RESERVE, TaskStatus.RESERVING).stream()
                .map(this::toModel)
                .toList();
        List<Task> merged = new ArrayList<>(pending.size() + reserving.size());
        merged.addAll(pending);
        merged.addAll(reserving);
        return merged;
    }

    @Override
    public List<Task> findPool() {
        return taskJpa.findByStatusOrderByCreatedAtDesc(TaskStatus.GROUPING).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<Task> findPublishedByPublisher(long publisherId) {
        return taskJpa.findPublishedByPublisher(publisherId).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<Task> findAcceptedByRunner(long runnerId) {
        return taskJpa.findByRunnerIdOrderByUpdatedAtDesc(runnerId).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<Task> findByModeAndStatus(TaskMode mode, TaskStatus status) {
        return taskJpa.findByModeAndStatusOrderByCreatedAtDesc(mode, status).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<Task> findReservations(long userId) {
        // 由 TaskService 结合 reserve_slot 组装；此处保留兼容
        return findByModeAndStatus(TaskMode.RESERVE, TaskStatus.RESERVING);
    }

    @Override
    public List<Task> findDrafts(long publisherId) {
        return taskJpa.findByPublisherIdAndStatusOrderByUpdatedAtDesc(publisherId, TaskStatus.DRAFT).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public void delete(long id) {
        taskJpa.deleteById(id);
    }

    @Override
    public void appendStatusLog(long taskId, TaskStatus from, TaskStatus to, Long operatorId, String remark) {
        TaskStatusLogEntity log = new TaskStatusLogEntity();
        log.setTaskId(taskId);
        log.setFromStatus(from == null ? null : from.name());
        log.setToStatus(to.name());
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        statusLogJpa.save(log);
    }

    @Override
    public List<GroupMember> findGroupMembers(long taskId) {
        return memberJpa.findByTaskIdOrderByIdAsc(taskId).stream()
                .map(this::toMember)
                .toList();
    }

    @Override
    public GroupMember saveGroupMember(long taskId, GroupMember member) {
        TaskGroupMemberEntity entity = new TaskGroupMemberEntity();
        if (member.getId() != null) {
            entity = memberJpa.findById(member.getId()).orElse(entity);
        }
        entity.setTaskId(taskId);
        entity.setUserId(member.getUserId());
        entity.setRole(member.getRole());
        entity.setName(nullToEmpty(member.getName()));
        entity.setAddressSummary(nullToEmpty(member.getAddressSummary()));
        entity.setShareCent(member.getShareCent());
        entity.setPayStatus(member.getPayStatus() == null ? "UNPAID" : member.getPayStatus());
        return toMember(memberJpa.save(entity));
    }

    @Override
    @Transactional
    public void removeGroupMember(long taskId, long userId) {
        memberJpa.deleteByTaskIdAndUserId(taskId, userId);
    }

    @Override
    @Transactional
    public void replaceGroupMembers(long taskId, List<GroupMember> members) {
        List<TaskGroupMemberEntity> existing = memberJpa.findByTaskIdOrderByIdAsc(taskId);
        memberJpa.deleteAll(existing);
        List<TaskGroupMemberEntity> next = new ArrayList<>();
        for (GroupMember member : members) {
            TaskGroupMemberEntity entity = new TaskGroupMemberEntity();
            entity.setTaskId(taskId);
            entity.setUserId(member.getUserId());
            entity.setRole(member.getRole());
            entity.setName(nullToEmpty(member.getName()));
            entity.setAddressSummary(nullToEmpty(member.getAddressSummary()));
            entity.setShareCent(member.getShareCent());
            entity.setPayStatus(member.getPayStatus() == null ? "PAID" : member.getPayStatus());
            next.add(entity);
        }
        memberJpa.saveAll(next);
    }

    @Override
    public long nextTaskNoSeq() {
        return taskNoSeq.incrementAndGet();
    }

    // --- Dashboard statistics ---

    @Override
    public List<Object[]> countByCategoryForPublisher(long uid) {
        return taskJpa.countByCategoryForPublisher(uid);
    }

    @Override
    public List<Object[]> countByCategoryForRunner(long uid) {
        return taskJpa.countByCategoryForRunner(uid);
    }

    @Override
    public List<Object[]> countByStatusForPublisher(long uid) {
        return taskJpa.countByStatusForPublisher(uid);
    }

    @Override
    public List<Object[]> countByStatusForRunner(long uid) {
        return taskJpa.countByStatusForRunner(uid);
    }

    @Override
    public List<Object[]> monthlyTrendForPublisher(long uid) {
        return taskJpa.monthlyTrendForPublisher(uid);
    }

    @Override
    public List<Object[]> monthlyTrendForRunner(long uid) {
        return taskJpa.monthlyTrendForRunner(uid);
    }

    @Override
    public List<Object[]> topRunners() {
        return taskJpa.topRunners();
    }

    // --- Heatmap statistics ---

    @Override
    public List<Object[]> hourlyPickupStats(java.time.Instant since) {
        return taskJpa.hourlyPickupStats(since);
    }

    @Override
    public long countSince(java.time.Instant since) {
        return taskJpa.countSince(since);
    }

    private static long parseTaskNoSuffix(String taskNo) {
        String suffix = taskNo.startsWith("CG") || taskNo.startsWith("DR")
                ? taskNo.substring(2)
                : taskNo;
        try {
            return Long.parseLong(suffix);
        } catch (NumberFormatException e) {
            return 20240803000L;
        }
    }

    private void apply(TaskEntity e, Task t) {
        e.setTaskNo(t.getTaskNo());
        e.setPublisherId(t.getPublisherId());
        e.setRunnerId(t.getRunnerId());
        e.setMode(t.getMode());
        e.setCategory(t.getCategory());
        e.setTitle(t.getTitle());
        e.setDescription(t.getDescription());
        e.setStatus(t.getStatus());
        e.setPickupName(nullToEmpty(t.getPickupName()));
        e.setDropoffName(nullToEmpty(t.getDropoffName()));
        e.setTimeLabel(t.getTimeLabel());
        e.setRewardCent(t.getRewardCent());
        e.setBaseRewardCent(t.getBaseRewardCent());
        e.setEscrowCent(t.getEscrowCent());
        e.setGroupTargetCount(t.getGroupTargetCount());
        e.setGroupJoinedCount(t.getGroupJoinedCount());
        e.setDeliveryPhotoUrl(t.getDeliveryPhotoUrl());
        e.setCancelReason(t.getCancelReason());
        e.setAcceptedAt(t.getAcceptedAt());
        e.setCompletedAt(t.getCompletedAt());
        if (t.getId() != null) {
            e.setId(t.getId());
        }
    }

    private Task toModel(TaskEntity e) {
        return Task.builder()
                .id(e.getId())
                .taskNo(e.getTaskNo())
                .publisherId(e.getPublisherId())
                .runnerId(e.getRunnerId())
                .mode(e.getMode())
                .category(e.getCategory())
                .title(e.getTitle())
                .description(e.getDescription())
                .status(e.getStatus())
                .pickupName(e.getPickupName())
                .dropoffName(e.getDropoffName())
                .timeLabel(e.getTimeLabel())
                .rewardCent(e.getRewardCent())
                .baseRewardCent(e.getBaseRewardCent())
                .escrowCent(e.getEscrowCent())
                .groupTargetCount(e.getGroupTargetCount())
                .groupJoinedCount(e.getGroupJoinedCount())
                .deliveryPhotoUrl(e.getDeliveryPhotoUrl())
                .cancelReason(e.getCancelReason())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .acceptedAt(e.getAcceptedAt())
                .completedAt(e.getCompletedAt())
                .build();
    }

    private GroupMember toMember(TaskGroupMemberEntity e) {
        return GroupMember.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .name(e.getName())
                .role(e.getRole())
                .addressSummary(e.getAddressSummary())
                .shareCent(e.getShareCent())
                .payStatus(e.getPayStatus())
                .build();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public Task require(long id) {
        return findById(id).orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "任务不存在"));
    }
}
