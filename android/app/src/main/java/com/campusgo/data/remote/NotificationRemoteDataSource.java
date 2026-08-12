package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.api.NotificationApi;
import com.campusgo.data.remote.dto.notification.NotificationDto;
import com.campusgo.domain.model.AppNotification;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationRemoteDataSource {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA);

    private final NotificationApi notificationApi;

    public NotificationRemoteDataSource(@NonNull NotificationApi notificationApi) {
        this.notificationApi = notificationApi;
    }

    public void list(@NonNull ApiCallback<List<AppNotification>> callback) {
        ApiExecutor.enqueue(notificationApi.list(), new ApiCallback<List<NotificationDto>>() {
            @Override
            public void onSuccess(List<NotificationDto> data) {
                List<AppNotification> list = new ArrayList<>();
                if (data != null) {
                    for (NotificationDto dto : data) {
                        list.add(toModel(dto));
                    }
                }
                callback.onSuccess(list);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void markAllRead(@NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(notificationApi.markAllRead(), callback);
    }

    public void markRead(@NonNull String id, @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(notificationApi.markRead(id), callback);
    }

    @NonNull
    private static AppNotification toModel(@NonNull NotificationDto dto) {
        AppNotification.LinkType linkType = AppNotification.LinkType.NONE;
        if (dto.linkType != null) {
            try {
                linkType = AppNotification.LinkType.valueOf(dto.linkType);
            } catch (IllegalArgumentException ignored) {
            }
        }
        TaskStatus taskStatus = null;
        if (dto.taskStatus != null) {
            try {
                taskStatus = TaskStatus.valueOf(dto.taskStatus);
            } catch (IllegalArgumentException ignored) {
            }
        }
        TaskMode taskMode = null;
        if (dto.taskMode != null) {
            try {
                taskMode = TaskMode.valueOf(dto.taskMode);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new AppNotification(
                dto.id != null ? dto.id : "",
                dto.title != null ? dto.title : "",
                dto.body != null ? dto.body : "",
                formatRelative(dto.createdAt),
                dto.unread,
                linkType,
                dto.linkTargetId,
                taskStatus,
                taskMode,
                dto.chatPeerName,
                dto.chatTaskTitle,
                dto.chatTaskId,
                dto.bizType);
    }

    @NonNull
    private static String formatRelative(@androidx.annotation.Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        try {
            Instant instant = Instant.parse(raw);
            long minutes = ChronoUnit.MINUTES.between(instant, Instant.now());
            if (minutes < 1) {
                return "刚刚";
            }
            if (minutes < 60) {
                return minutes + "分钟前";
            }
            long hours = minutes / 60;
            if (hours < 24) {
                return hours + "小时前";
            }
            long days = hours / 24;
            if (days == 1) {
                return "昨天";
            }
            if (days < 7) {
                return days + "天前";
            }
            return TIME_FMT.format(instant.atZone(ZoneId.systemDefault()));
        } catch (Exception e) {
            return "";
        }
    }
}
