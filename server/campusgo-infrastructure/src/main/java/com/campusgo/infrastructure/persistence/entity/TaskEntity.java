package com.campusgo.infrastructure.persistence.entity;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "task")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_no", nullable = false, unique = true, length = 32)
    private String taskNo;

    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    @Column(name = "runner_id")
    private Long runnerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 16)
    private TaskMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 16)
    private TaskCategory category;

    @Column(name = "title", nullable = false, length = 60)
    private String title;

    @Column(name = "description", length = 400)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TaskStatus status;

    @Column(name = "pickup_name", nullable = false, length = 128)
    private String pickupName;

    @Column(name = "pickup_detail", length = 256)
    private String pickupDetail;

    @Column(name = "pickup_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLng = new BigDecimal("116.397428");

    @Column(name = "pickup_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLat = new BigDecimal("39.90923");

    @Column(name = "dropoff_name", nullable = false, length = 128)
    private String dropoffName;

    @Column(name = "dropoff_detail", length = 256)
    private String dropoffDetail;

    @Column(name = "dropoff_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLng = new BigDecimal("116.397428");

    @Column(name = "dropoff_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLat = new BigDecimal("39.90923");

    @Column(name = "expect_finish_at")
    private Instant expectFinishAt;

    @Column(name = "reserve_at")
    private Instant reserveAt;

    @Column(name = "time_label", length = 64)
    private String timeLabel;

    @Column(name = "reward_cent", nullable = false)
    private int rewardCent;

    @Column(name = "base_reward_cent", nullable = false)
    private int baseRewardCent;

    @Column(name = "emergency_rate")
    private Integer emergencyRate;

    @Column(name = "escrow_cent", nullable = false)
    private int escrowCent;

    @Column(name = "group_target_count")
    private Integer groupTargetCount;

    @Column(name = "group_joined_count")
    private Integer groupJoinedCount;

    @Column(name = "group_split_type", length = 16)
    private String groupSplitType;

    @Column(name = "delivery_photo_url", length = 512)
    private String deliveryPhotoUrl;

    @Column(name = "cancel_reason", length = 256)
    private String cancelReason;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "delivering_at")
    private Instant deliveringAt;

    @Column(name = "confirming_at")
    private Instant confirmingAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
