package com.campusgo.infrastructure.persistence.jpa;

import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findByTaskNo(String taskNo);

    List<TaskEntity> findByPublisherIdAndStatusOrderByUpdatedAtDesc(Long publisherId, TaskStatus status);

    List<TaskEntity> findByPublisherIdAndStatusNotOrderByUpdatedAtDesc(Long publisherId, TaskStatus status);

    List<TaskEntity> findByRunnerIdOrderByUpdatedAtDesc(Long runnerId);

    List<TaskEntity> findByStatusAndRunnerIdIsNullOrderByCreatedAtDesc(TaskStatus status);

    List<TaskEntity> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    List<TaskEntity> findByModeAndStatusOrderByCreatedAtDesc(TaskMode mode, TaskStatus status);

    @Query("select t from TaskEntity t where t.publisherId = :uid and t.status <> com.campusgo.domain.enums.TaskStatus.DRAFT order by t.updatedAt desc")
    List<TaskEntity> findPublishedByPublisher(@Param("uid") Long uid);

    long countByStatus(TaskStatus status);

    // --- Dashboard statistics ---

    @Query("select t.category, count(t) from TaskEntity t where t.publisherId = :uid and t.status <> com.campusgo.domain.enums.TaskStatus.DRAFT group by t.category")
    List<Object[]> countByCategoryForPublisher(@Param("uid") Long uid);

    @Query("select t.category, count(t) from TaskEntity t where t.runnerId = :uid and t.status <> com.campusgo.domain.enums.TaskStatus.DRAFT group by t.category")
    List<Object[]> countByCategoryForRunner(@Param("uid") Long uid);

    @Query("select t.status, count(t) from TaskEntity t where t.publisherId = :uid group by t.status")
    List<Object[]> countByStatusForPublisher(@Param("uid") Long uid);

    @Query("select t.status, count(t) from TaskEntity t where t.runnerId = :uid group by t.status")
    List<Object[]> countByStatusForRunner(@Param("uid") Long uid);

    @Query(value = "select extract(MONTH from t.created_at), count(*) from task t where t.publisher_id = :uid group by extract(MONTH from t.created_at) order by extract(MONTH from t.created_at)", nativeQuery = true)
    List<Object[]> monthlyTrendForPublisher(@Param("uid") Long uid);

    @Query(value = "select extract(MONTH from t.created_at), count(*) from task t where t.runner_id = :uid group by extract(MONTH from t.created_at) order by extract(MONTH from t.created_at)", nativeQuery = true)
    List<Object[]> monthlyTrendForRunner(@Param("uid") Long uid);

    @Query("select t.runnerId, u.nickname, count(t) as cnt from TaskEntity t join AppUserEntity u on t.runnerId = u.id where t.status = com.campusgo.domain.enums.TaskStatus.COMPLETED group by t.runnerId, u.nickname order by cnt desc")
    List<Object[]> topRunners();

    // --- Heatmap statistics ---

    @Query(value = "select extract(HOUR from t.created_at), t.pickup_name, count(*) from task t where t.created_at >= :since group by extract(HOUR from t.created_at), t.pickup_name order by count(*) desc", nativeQuery = true)
    List<Object[]> hourlyPickupStats(@Param("since") java.time.Instant since);

    @Query("select count(t) from TaskEntity t where t.createdAt >= :since")
    long countSince(@Param("since") java.time.Instant since);
}
