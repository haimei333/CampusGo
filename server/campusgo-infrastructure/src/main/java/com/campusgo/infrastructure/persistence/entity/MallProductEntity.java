package com.campusgo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mall_product")
public class MallProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "subtitle", length = 200)
    private String subtitle;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "points_cost", nullable = false)
    private int pointsCost;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "emoji", length = 10)
    private String emoji;

    @Column(name = "flash_sale", nullable = false)
    private boolean flashSale;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    private Integer version;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}