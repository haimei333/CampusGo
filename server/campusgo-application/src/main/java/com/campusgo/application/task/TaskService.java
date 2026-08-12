package com.campusgo.application.task;

import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.GroupOrder;
import com.campusgo.domain.model.Task;

import java.util.List;

public interface TaskService {

    List<Task> listDrafts(long userId);

    Task createDraft(long userId, DraftCommand command);

    Task updateDraft(long userId, long draftId, DraftCommand command);

    void deleteDraft(long userId, long draftId);

    Task publish(long userId, PublishCommand command);

    List<Task> listHall();

    List<Task> listPool();

    List<Task> listMinePublished(long userId);

    List<Task> listMineAccepted(long userId);

    List<ReservationEntry> listReservations(long userId);

    TaskDetailView getDetail(long taskId, Long viewerUserId);

    Task grab(long userId, long taskId);

    Task holdReserve(long userId, long taskId);

    Task releaseReserve(long userId, long taskId);

    Task confirmReserve(long userId, long taskId);

    Task startDeliver(long userId, long taskId);

    Task uploadPhoto(long userId, long taskId, String photoUrl);

    Task confirm(long userId, long taskId);

    Task raisePrice(long userId, long taskId, int addCent);

    Task toEmergency(long userId, long taskId);

    Task cancel(long userId, long taskId, String reason);

    GroupOrder getGroupDetail(long taskId, long userId);

    GroupOrder joinGroup(long taskId, long userId, String address);

    GroupOrder leaveGroup(long taskId, long userId);

    record ReservationEntry(Task task, UserRole reserveForRole) {
    }

    record TaskDetailView(Task task, boolean reserveSlotHeld, int reserveHoldCount) {
    }

    record DraftCommand(
            String title,
            String description,
            TaskMode mode,
            TaskCategory category,
            String pickupAddress,
            String deliveryAddress,
            String timeLabel,
            int rewardCent
    ) {
    }

    record PublishCommand(
            Long draftId,
            String title,
            String description,
            TaskMode mode,
            TaskCategory category,
            String pickupAddress,
            String deliveryAddress,
            String timeLabel,
            int rewardCent,
            Integer groupTargetCount
    ) {
    }
}
