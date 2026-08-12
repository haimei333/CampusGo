package com.campusgo.infrastructure.persistence.entity;

import com.campusgo.domain.enums.ReserveSlotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "task_reserve_slot")
public class TaskReserveSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "runner_id", nullable = false)
    private Long runnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReserveSlotStatus status = ReserveSlotStatus.HOLDING;

    @Column(name = "hold_at", nullable = false)
    private Instant holdAt;

    @Column(name = "confirm_deadline")
    private Instant confirmDeadline;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (holdAt == null) {
            holdAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
