package com.campusgo.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.api.TaskApi;
import com.campusgo.data.remote.dto.common.PageResponse;
import com.campusgo.data.remote.dto.task.CancelTaskRequest;
import com.campusgo.data.remote.dto.task.DeliveryPhotoRequest;
import com.campusgo.data.remote.dto.task.GroupOrderDetailDto;
import com.campusgo.data.remote.dto.task.JoinGroupRequest;
import com.campusgo.data.remote.dto.task.PublishTaskRequest;
import com.campusgo.data.remote.dto.task.PublishTaskResponse;
import com.campusgo.data.remote.dto.task.RaisePriceRequest;
import com.campusgo.data.remote.dto.task.TaskDetailDto;
import com.campusgo.data.remote.dto.task.TaskListItemDto;
import com.campusgo.data.remote.dto.task.SaveDraftRequest;
import com.campusgo.data.remote.dto.task.TaskDraftDto;
import com.campusgo.data.remote.mapper.TaskDraftMapper;
import com.campusgo.data.remote.mapper.TaskDtoMapper;
import com.campusgo.data.remote.mapper.GroupDtoMapper;
import com.campusgo.domain.model.GroupOrderDetail;
import com.campusgo.domain.model.PublishDraft;
import com.campusgo.domain.model.TaskDetail;
import com.campusgo.domain.model.TaskListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 任务模块远程数据源（Mock API / 后续真实 TaskService）。
 */
public class TaskRemoteDataSource {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final TaskApi taskApi;

    public TaskRemoteDataSource(@NonNull TaskApi taskApi) {
        this.taskApi = taskApi;
    }

    public void loadHall(int page, @NonNull ApiCallback<List<TaskListItem>> callback) {
        loadPage(taskApi.hall(page, DEFAULT_PAGE_SIZE), TaskListItem.Tab.HALL, callback);
    }

    public void loadPool(int page, @NonNull ApiCallback<List<TaskListItem>> callback) {
        loadPage(taskApi.pool(page, DEFAULT_PAGE_SIZE), TaskListItem.Tab.POOL, callback);
    }

    public void loadMinePublished(int page, @NonNull ApiCallback<List<TaskListItem>> callback) {
        loadPage(taskApi.minePublished(page, DEFAULT_PAGE_SIZE), TaskListItem.Tab.MINE_PUBLISH, callback);
    }

    /** 我的发布 = 草稿 + 已发布任务 */
    public void loadMinePublishedWithDrafts(int page, @NonNull ApiCallback<List<TaskListItem>> callback) {
        ApiExecutor.enqueue(taskApi.listDrafts(), new ApiCallback<List<TaskDraftDto>>() {
            @Override
            public void onSuccess(@NonNull List<TaskDraftDto> drafts) {
                loadPage(taskApi.minePublished(page, DEFAULT_PAGE_SIZE), TaskListItem.Tab.MINE_PUBLISH,
                        new ApiCallback<List<TaskListItem>>() {
                            @Override
                            public void onSuccess(@NonNull List<TaskListItem> published) {
                                List<TaskListItem> merged = new ArrayList<>();
                                merged.addAll(TaskDraftMapper.toDraftListItems(drafts));
                                merged.addAll(published);
                                callback.onSuccess(merged);
                            }

                            @Override
                            public void onError(@NonNull ApiException error) {
                                callback.onSuccess(TaskDraftMapper.toDraftListItems(drafts));
                            }
                        });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                loadMinePublished(page, callback);
            }
        });
    }

    public void findDraft(@NonNull String draftId, @NonNull ApiCallback<PublishDraft> callback) {
        ApiExecutor.enqueue(taskApi.listDrafts(), new ApiCallback<List<TaskDraftDto>>() {
            @Override
            public void onSuccess(@NonNull List<TaskDraftDto> data) {
                for (TaskDraftDto dto : data) {
                    if (draftId.equals(dto.id)) {
                        PublishDraft draft = TaskDraftMapper.toPublishDraft(dto);
                        if (draft != null) {
                            callback.onSuccess(draft);
                            return;
                        }
                    }
                }
                callback.onError(new ApiException(-1, "草稿不存在"));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void saveDraft(@NonNull PublishDraft draft, boolean createNew,
                          @NonNull ApiCallback<PublishDraft> callback) {
        SaveDraftRequest request = TaskDraftMapper.toSaveRequest(draft);
        if (createNew) {
            ApiExecutor.enqueue(taskApi.createDraft(request), draftSavedCallback(callback));
        } else {
            ApiExecutor.enqueue(taskApi.updateDraft(draft.id, request), draftSavedCallback(callback));
        }
    }

    public void deleteDraft(@NonNull String draftId, @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(taskApi.deleteDraft(draftId), callback);
    }

    @NonNull
    private ApiCallback<TaskDraftDto> draftSavedCallback(@NonNull ApiCallback<PublishDraft> callback) {
        return new ApiCallback<TaskDraftDto>() {
            @Override
            public void onSuccess(@NonNull TaskDraftDto data) {
                PublishDraft draft = TaskDraftMapper.toPublishDraft(data);
                if (draft != null) {
                    callback.onSuccess(draft);
                } else {
                    callback.onError(new ApiException(-1, "草稿保存失败"));
                }
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        };
    }

    public void loadMineAccepted(int page, @NonNull ApiCallback<List<TaskListItem>> callback) {
        loadPage(taskApi.mineAccepted(page, DEFAULT_PAGE_SIZE), TaskListItem.Tab.MINE_TAKE, callback);
    }

    public void loadReservations(int page, @NonNull ApiCallback<List<TaskListItem>> callback) {
        loadPage(taskApi.mineReservations(page, DEFAULT_PAGE_SIZE), TaskListItem.Tab.RESERVE, callback);
    }

    public void loadDetail(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        ApiExecutor.enqueue(taskApi.detail(taskId), new ApiCallback<TaskDetailDto>() {
            @Override
            public void onSuccess(@NonNull TaskDetailDto data) {
                TaskDetail detail = TaskDtoMapper.toDetail(data);
                if (detail != null) {
                    callback.onSuccess(detail);
                } else {
                    callback.onError(new ApiException(-1, "任务详情为空"));
                }
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void grab(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.grab(taskId), callback);
    }

    public void holdReserve(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.holdReserve(taskId), callback);
    }

    public void releaseReserve(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.releaseReserve(taskId), callback);
    }

    public void confirmReserve(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.confirmReserve(taskId), callback);
    }

    public void publish(@NonNull PublishTaskRequest request, @NonNull ApiCallback<PublishTaskResponse> callback) {
        ApiExecutor.enqueue(taskApi.publish(request), callback);
    }

    public void startDeliver(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.startDeliver(taskId), callback);
    }

    public void uploadPhoto(@NonNull String taskId, @Nullable String photoUrl,
                            @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.uploadPhoto(taskId, new DeliveryPhotoRequest(photoUrl)), callback);
    }

    public void confirm(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.confirm(taskId), callback);
    }

    public void raisePrice(@NonNull String taskId, int addCent, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.raisePrice(taskId, new RaisePriceRequest(addCent)), callback);
    }

    public void emergency(@NonNull String taskId, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.emergency(taskId), callback);
    }

    public void cancel(@NonNull String taskId, @Nullable String reason, @NonNull ApiCallback<TaskDetail> callback) {
        enqueueDetail(taskApi.cancel(taskId, new CancelTaskRequest(reason)), callback);
    }

    public void submitReview(@NonNull String taskId, int score,
                             @NonNull java.util.List<String> tags, @Nullable String content,
                             @NonNull ApiCallback<Void> callback) {
        ApiExecutor.enqueue(taskApi.submitReview(taskId,
                        new com.campusgo.data.remote.dto.review.SubmitReviewRequest(score, tags, content)),
                callback);
    }

    public void loadGroupDetail(@NonNull String taskId, @NonNull ApiCallback<GroupOrderDetail> callback) {
        ApiExecutor.enqueue(taskApi.groupDetail(taskId), new ApiCallback<GroupOrderDetailDto>() {
            @Override
            public void onSuccess(@NonNull GroupOrderDetailDto data) {
                GroupOrderDetail detail = GroupDtoMapper.toDetail(data);
                if (detail != null) {
                    callback.onSuccess(detail);
                } else {
                    callback.onError(new ApiException(-1, "拼单详情为空"));
                }
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    public void joinGroup(@NonNull String taskId, @NonNull String address,
                          @NonNull ApiCallback<GroupOrderDetail> callback) {
        ApiExecutor.enqueue(taskApi.joinGroup(taskId, new JoinGroupRequest(address)),
                groupDetailCallback(callback));
    }

    public void leaveGroup(@NonNull String taskId, @NonNull ApiCallback<GroupOrderDetail> callback) {
        ApiExecutor.enqueue(taskApi.leaveGroup(taskId), groupDetailCallback(callback));
    }

    @NonNull
    private ApiCallback<GroupOrderDetailDto> groupDetailCallback(@NonNull ApiCallback<GroupOrderDetail> callback) {
        return new ApiCallback<GroupOrderDetailDto>() {
            @Override
            public void onSuccess(@NonNull GroupOrderDetailDto data) {
                GroupOrderDetail detail = GroupDtoMapper.toDetail(data);
                if (detail != null) {
                    callback.onSuccess(detail);
                } else {
                    callback.onError(new ApiException(-1, "拼单数据为空"));
                }
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        };
    }

    private void enqueueDetail(@NonNull retrofit2.Call<ApiResponse<TaskDetailDto>> call,
                               @NonNull ApiCallback<TaskDetail> callback) {
        ApiExecutor.enqueue(call, new ApiCallback<TaskDetailDto>() {
            @Override
            public void onSuccess(@NonNull TaskDetailDto data) {
                TaskDetail detail = TaskDtoMapper.toDetail(data);
                if (detail != null) {
                    callback.onSuccess(detail);
                } else {
                    callback.onError(new ApiException(-1, "任务数据为空"));
                }
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }

    private void loadPage(
            @NonNull retrofit2.Call<ApiResponse<PageResponse<TaskListItemDto>>> call,
            @NonNull TaskListItem.Tab tab,
            @NonNull ApiCallback<List<TaskListItem>> callback) {
        ApiExecutor.enqueue(call, new ApiCallback<PageResponse<TaskListItemDto>>() {
            @Override
            public void onSuccess(@NonNull PageResponse<TaskListItemDto> data) {
                List<TaskListItemDto> list = data.list != null ? data.list : Collections.emptyList();
                callback.onSuccess(TaskDtoMapper.toListItems(list, tab));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                callback.onError(error);
            }
        });
    }
}
