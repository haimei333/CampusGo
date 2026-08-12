package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.domain.enums.ReserveSlotStatus;
import com.campusgo.infrastructure.persistence.entity.TaskReserveSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskReserveSlotJpaRepository extends JpaRepository<TaskReserveSlotEntity, Long> {

    Optional<TaskReserveSlotEntity> findByTaskIdAndRunnerId(Long taskId, Long runnerId);

    List<TaskReserveSlotEntity> findByTaskIdAndStatus(Long taskId, ReserveSlotStatus status);

    List<TaskReserveSlotEntity> findByRunnerIdAndStatus(Long runnerId, ReserveSlotStatus status);

    long countByTaskIdAndStatus(Long taskId, ReserveSlotStatus status);
}
