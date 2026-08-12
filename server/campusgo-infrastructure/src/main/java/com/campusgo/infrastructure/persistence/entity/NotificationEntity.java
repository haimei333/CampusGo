package com.campusgo.infrastructure.persistence.entity;

import com.campusgo.domain.enums.NotificationBizType;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "notification")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 512)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "biz_type", nullable = false, length = 32)
    private NotificationBizType bizType;

    @Column(name = "biz_id", length = 64)
    private String bizId;

    @Column(name = "task_id")
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", length = 16)
    private TaskStatus taskStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_mode", length = 16)
    private TaskMode taskMode;

    @Column(name = "chat_peer_name", length = 64)
    private String chatPeerName;

    @Column(name = "chat_task_title", length = 128)
    private String chatTaskTitle;

    @Column(name = "read_flag", nullable = false)
    private short readFlag;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
