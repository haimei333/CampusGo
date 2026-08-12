package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.infrastructure.persistence.entity.TaskGroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskGroupMemberJpaRepository extends JpaRepository<TaskGroupMemberEntity, Long> {

    List<TaskGroupMemberEntity> findByTaskIdOrderByIdAsc(Long taskId);

    Optional<TaskGroupMemberEntity> findByTaskIdAndUserId(Long taskId, Long userId);

    void deleteByTaskIdAndUserId(Long taskId, Long userId);

    long countByTaskIdAndRoleNot(Long taskId, String role);
}
