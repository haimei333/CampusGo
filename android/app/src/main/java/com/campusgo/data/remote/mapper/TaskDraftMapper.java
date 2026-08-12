package com.campusgo.data.remote.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.dto.task.SaveDraftRequest;
import com.campusgo.data.remote.dto.task.TaskDraftDto;
import com.campusgo.domain.model.PublishDraft;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TaskDraftMapper {

    private TaskDraftMapper() {
    }

    @NonNull
    public static SaveDraftRequest toSaveRequest(@NonNull PublishDraft draft) {
        SaveDraftRequest request = new SaveDraftRequest();
        request.title = draft.title;
        request.description = draft.description;
        request.mode = draft.mode;
        request.category = draft.category;
        request.pickupAddress = draft.pickupAddress;
        request.deliveryAddress = draft.deliveryAddress;
        request.timeLabel = draft.timeLabel;
        request.rewardCent = (int) Math.round(draft.reward * 100);
        return request;
    }

    @Nullable
    public static PublishDraft toPublishDraft(@Nullable TaskDraftDto dto) {
        if (dto == null || dto.id == null) {
            return null;
        }
        double reward = dto.rewardCent / 100.0;
        return new PublishDraft(
                dto.id,
                nullToEmpty(dto.title),
                nullToEmpty(dto.description),
                dto.mode != null ? dto.mode : TaskMode.NORMAL,
                dto.category != null ? dto.category : TaskCategory.EXPRESS,
                nullToEmpty(dto.pickupAddress),
                nullToEmpty(dto.deliveryAddress),
                nullToEmpty(dto.timeLabel),
                reward,
                System.currentTimeMillis());
    }

    @NonNull
    public static List<TaskListItem> toDraftListItems(@Nullable List<TaskDraftDto> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return Collections.emptyList();
        }
        List<TaskListItem> items = new ArrayList<>(drafts.size());
        for (TaskDraftDto draft : drafts) {
            TaskListItem item = toDraftListItem(draft);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    @Nullable
    public static TaskListItem toDraftListItem(@Nullable TaskDraftDto draft) {
        if (draft == null || draft.id == null) {
            return null;
        }
        PublishDraft model = toPublishDraft(draft);
        if (model == null) {
            return null;
        }
        String priceLabel = draft.rewardYuan != null ? draft.rewardYuan
                : (draft.rewardCent > 0 ? String.format("¥%.2f", draft.rewardCent / 100.0) : null);
        return new TaskListItem(
                draft.id,
                TaskListItem.Tab.MINE_PUBLISH,
                null,
                "草稿 · " + model.listTitle(),
                "草稿",
                model.listSubtitle(),
                priceLabel,
                model.category,
                model.mode,
                TaskStatus.DRAFT,
                TaskListItem.NavTarget.T01);
    }

    @NonNull
    private static String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }
}
