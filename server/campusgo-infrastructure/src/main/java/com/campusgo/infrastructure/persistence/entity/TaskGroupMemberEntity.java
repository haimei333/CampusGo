package com.campusgo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "task_group_member")
public class TaskGroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role", nullable = false, length = 16)
    private String role;

    @Column(name = "name", nullable = false, length = 32)
    private String name = "";

    @Column(name = "address_summary", nullable = false, length = 256)
    private String addressSummary = "";

    @Column(name = "share_cent", nullable = false)
    private int shareCent;

    @Column(name = "pay_status", nullable = false, length = 16)
    private String payStatus = "UNPAID";

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
