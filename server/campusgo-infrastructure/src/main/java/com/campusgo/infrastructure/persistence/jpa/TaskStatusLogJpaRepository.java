package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.TaskStatusLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStatusLogJpaRepository extends JpaRepository<TaskStatusLogEntity, Long> {
}
