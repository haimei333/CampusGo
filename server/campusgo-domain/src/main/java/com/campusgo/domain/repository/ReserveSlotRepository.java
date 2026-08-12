package com.campusgo.domain.repository;

import com.campusgo.domain.enums.ReserveSlotStatus;
import com.campusgo.domain.model.ReserveSlot;

import java.util.List;
import java.util.Optional;

public interface ReserveSlotRepository {

    Optional<ReserveSlot> findByTaskIdAndRunnerId(long taskId, long runnerId);

    List<ReserveSlot> findByTaskIdAndStatus(long taskId, ReserveSlotStatus status);

    List<ReserveSlot> findByRunnerIdAndStatus(long runnerId, ReserveSlotStatus status);

    long countByTaskIdAndStatus(long taskId, ReserveSlotStatus status);

    ReserveSlot save(ReserveSlot slot);

    void updateStatus(long id, ReserveSlotStatus status);

    void cancelAllForTask(long taskId);
}
