package com.campusgo.infrastructure.persistence.entity;

import com.campusgo.domain.enums.CampusVerifyStatus;
import com.campusgo.domain.enums.UserRole;
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
@Table(name = "app_user")
public class AppUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 32)
    private String nickname;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "credit_score", nullable = false)
    private int creditScore = 500;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_role", nullable = false, length = 16)
    private UserRole activeRole = UserRole.PUBLISHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "campus_status", nullable = false, length = 16)
    private CampusVerifyStatus campusStatus = CampusVerifyStatus.NONE;

    @Column(nullable = false)
    private short status = 1;

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
