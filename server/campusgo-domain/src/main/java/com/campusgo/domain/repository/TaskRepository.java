package com.campusgo.domain.repository;

import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.Task;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(long id);

    List<Task> findHall();

    List<Task> findPool();

    List<Task> findPublishedByPublisher(long publisherId);

    List<Task> findAcceptedByRunner(long runnerId);

    List<Task> findByModeAndStatus(TaskMode mode, TaskStatus status);

    List<Task> findReservations(long userId);

    List<Task> findDrafts(long publisherId);

    void delete(long id);

    void appendStatusLog(long taskId, TaskStatus from, TaskStatus to, Long operatorId, String remark);

    List<GroupMember> findGroupMembers(long taskId);

    GroupMember saveGroupMember(long taskId, GroupMember member);

    void removeGroupMember(long taskId, long userId);

    void replaceGroupMembers(long taskId, List<GroupMember> members);

    long nextTaskNoSeq();

    // --- Dashboard statistics ---

    List<Object[]> countByCategoryForPublisher(long uid);

    List<Object[]> countByCategoryForRunner(long uid);

    List<Object[]> countByStatusForPublisher(long uid);

    List<Object[]> countByStatusForRunner(long uid);

    List<Object[]> monthlyTrendForPublisher(long uid);

    List<Object[]> monthlyTrendForRunner(long uid);

    List<Object[]> topRunners();

    // --- Heatmap statistics ---

    List<Object[]> hourlyPickupStats(Instant since);

    long countSince(Instant since);
}
