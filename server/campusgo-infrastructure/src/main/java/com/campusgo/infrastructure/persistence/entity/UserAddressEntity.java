package com.campusgo.infrastructure.persistence.entity;

import com.campusgo.domain.enums.AddressTag;
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

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_address")
public class UserAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, length = 256)
    private String detail = "";

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng = new BigDecimal("116.310003");

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat = new BigDecimal("39.992801");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AddressTag tag = AddressTag.OTHER;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "use_count", nullable = false)
    private int useCount;

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
