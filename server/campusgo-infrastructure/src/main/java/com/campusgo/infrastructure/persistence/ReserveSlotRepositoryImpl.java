package com.campusgo.infrastructure.persistence;

import com.campusgo.domain.enums.ReserveSlotStatus;
import com.campusgo.domain.model.ReserveSlot;
import com.campusgo.domain.repository.ReserveSlotRepository;
import com.campusgo.infrastructure.persistence.entity.TaskReserveSlotEntity;
import com.campusgo.infrastructure.persistence.jpa.TaskReserveSlotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReserveSlotRepositoryImpl implements ReserveSlotRepository {

    private final TaskReserveSlotJpaRepository jpaRepository;

    @Override
    public Optional<ReserveSlot> findByTaskIdAndRunnerId(long taskId, long runnerId) {
        return jpaRepository.findByTaskIdAndRunnerId(taskId, runnerId).map(this::toModel);
    }

    @Override
    public List<ReserveSlot> findByTaskIdAndStatus(long taskId, ReserveSlotStatus status) {
        return jpaRepository.findByTaskIdAndStatus(taskId, status).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<ReserveSlot> findByRunnerIdAndStatus(long runnerId, ReserveSlotStatus status) {
        return jpaRepository.findByRunnerIdAndStatus(runnerId, status).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public long countByTaskIdAndStatus(long taskId, ReserveSlotStatus status) {
        return jpaRepository.countByTaskIdAndStatus(taskId, status);
    }

    @Override
    public ReserveSlot save(ReserveSlot slot) {
        TaskReserveSlotEntity entity;
        if (slot.getId() > 0) {
            entity = jpaRepository.findById(slot.getId()).orElseGet(TaskReserveSlotEntity::new);
        } else {
            entity = new TaskReserveSlotEntity();
        }
        entity.setTaskId(slot.getTaskId());
        entity.setRunnerId(slot.getRunnerId());
        entity.setStatus(slot.getStatus());
        entity.setHoldAt(slot.getHoldAt());
        entity.setConfirmDeadline(slot.getConfirmDeadline());
        return toModel(jpaRepository.save(entity));
    }

    @Override
    public void updateStatus(long id, ReserveSlotStatus status) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(status);
            jpaRepository.save(entity);
        });
    }

    @Override
    public void cancelAllForTask(long taskId) {
        jpaRepository.findByTaskIdAndStatus(taskId, ReserveSlotStatus.HOLDING).forEach(entity -> {
            entity.setStatus(ReserveSlotStatus.CANCELLED);
            jpaRepository.save(entity);
        });
    }

    private ReserveSlot toModel(TaskReserveSlotEntity entity) {
        return ReserveSlot.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .runnerId(entity.getRunnerId())
                .status(entity.getStatus())
                .holdAt(entity.getHoldAt())
                .confirmDeadline(entity.getConfirmDeadline())
                .build();
    }
}
