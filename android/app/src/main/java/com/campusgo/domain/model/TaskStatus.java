package com.campusgo.domain.model;

/**
 * 全 App 唯一任务状态机（对齐 PRD / 数据表设计）
 */
public enum TaskStatus {
    DRAFT,
    GROUPING,
    RESERVING,
    PENDING,
    ACCEPTED,
    DELIVERING,
    CONFIRMING,
    COMPLETED,
    REVIEWED,
    CANCELLED
}
