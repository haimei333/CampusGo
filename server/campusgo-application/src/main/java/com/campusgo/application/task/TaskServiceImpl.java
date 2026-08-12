package com.campusgo.application.task;

import com.campusgo.domain.enums.ReserveSlotStatus;
import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.GroupOrder;
import com.campusgo.domain.model.ReserveSlot;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.repository.ReserveSlotRepository;
import com.campusgo.domain.repository.TaskRepository;
import com.campusgo.domain.repository.UserRepository;
import com.campusgo.application.chat.ChatService;
import com.campusgo.application.notification.NotificationService;
import com.campusgo.application.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final int DEFAULT_GROUP_TARGET = 3;
    private static final int DEFAULT_GROUP_SHARE_CENT = 1000;
    private static final int MAX_RESERVE_HOLDERS = 5;

    private final TaskRepository taskRepository;
    private final ReserveSlotRepository reserveSlotRepository;
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;

    @Override
    public List<Task> listDrafts(long userId) {
        return taskRepository.findDrafts(userId);
    }

    @Override
    @Transactional
    public Task createDraft(long userId, DraftCommand command) {
        Task task = baseFromDraft(userId, command).toBuilder()
                .taskNo("DR" + taskRepository.nextTaskNoSeq())
                .status(TaskStatus.DRAFT)
                .build();
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task updateDraft(long userId, long draftId, DraftCommand command) {
        Task existing = requireOwnedDraft(userId, draftId);
        Task updated = existing.toBuilder()
                .title(trim(command.title(), 60))
                .description(nullToEmpty(command.description()))
                .mode(command.mode() == null ? TaskMode.NORMAL : command.mode())
                .category(command.category() == null ? TaskCategory.OTHER : command.category())
                .pickupName(nullToEmpty(command.pickupAddress()))
                .dropoffName(nullToEmpty(command.deliveryAddress()))
                .timeLabel(nullToEmpty(command.timeLabel()))
                .rewardCent(Math.max(command.rewardCent(), 0))
                .baseRewardCent(Math.max(command.rewardCent(), 0))
                .build();
        return taskRepository.save(updated);
    }

    @Override
    @Transactional
    public void deleteDraft(long userId, long draftId) {
        requireOwnedDraft(userId, draftId);
        taskRepository.delete(draftId);
    }

    @Override
    @Transactional
    public Task publish(long userId, PublishCommand command) {
        if (command.draftId() != null) {
            Task draft = requireOwnedDraft(userId, command.draftId());
            taskRepository.delete(draft.getId());
        }
        TaskStatus status = initialStatus(command.mode());
        int reward = Math.max(command.rewardCent(), 100);
        int groupTarget = command.groupTargetCount() == null ? DEFAULT_GROUP_TARGET : command.groupTargetCount();
        TaskMode mode = command.mode() == null ? TaskMode.NORMAL : command.mode();
        // 拼单：发起人先托管本人份额；普通/紧急/预约：托管全额酬劳
        int holdCent = mode == TaskMode.GROUP
                ? Math.max(reward / Math.max(groupTarget, 1), 100)
                : reward;

        Task task = Task.builder()
                .taskNo("CG" + taskRepository.nextTaskNoSeq())
                .publisherId(userId)
                .mode(mode)
                .category(command.category() == null ? TaskCategory.OTHER : command.category())
                .title(trim(command.title(), 60))
                .description(nullToEmpty(command.description()))
                .status(status)
                .pickupName(nullToEmpty(command.pickupAddress()))
                .dropoffName(nullToEmpty(command.deliveryAddress()))
                .timeLabel(nullToEmpty(command.timeLabel()))
                .rewardCent(reward)
                .baseRewardCent(reward)
                .escrowCent(holdCent)
                .groupTargetCount(mode == TaskMode.GROUP ? groupTarget : null)
                .groupJoinedCount(mode == TaskMode.GROUP ? 1 : null)
                .build();
        Task saved = taskRepository.save(task);
        walletService.hold(userId, holdCent, saved.getId(), "发布任务托管 - " + saved.getTitle());
        taskRepository.appendStatusLog(saved.getId(), null, status, userId, "发布");

        if (saved.getMode() == TaskMode.GROUP) {
            String nickname = userRepository.findById(userId).map(u -> u.getNickname()).orElse("发起人");
            taskRepository.replaceGroupMembers(saved.getId(), List.of(
                    GroupMember.builder()
                            .userId(userId)
                            .name(nickname)
                            .role("CREATOR")
                            .addressSummary(saved.getDropoffName())
                            .shareCent(holdCent)
                            .payStatus("PAID")
                            .build()
            ));
        }
        return saved;
    }

    @Override
    public List<Task> listHall() {
        return taskRepository.findHall();
    }

    @Override
    public List<Task> listPool() {
        return taskRepository.findPool();
    }

    @Override
    public List<Task> listMinePublished(long userId) {
        return taskRepository.findPublishedByPublisher(userId);
    }

    @Override
    public List<Task> listMineAccepted(long userId) {
        return taskRepository.findAcceptedByRunner(userId);
    }

    @Override
    public List<ReservationEntry> listReservations(long userId) {
        LinkedHashMap<Long, ReservationEntry> map = new LinkedHashMap<>();
        taskRepository.findByModeAndStatus(TaskMode.RESERVE, TaskStatus.RESERVING).stream()
                .filter(t -> t.getPublisherId() == userId)
                .forEach(t -> map.put(t.getId(), new ReservationEntry(t, UserRole.PUBLISHER)));
        reserveSlotRepository.findByRunnerIdAndStatus(userId, ReserveSlotStatus.HOLDING).forEach(slot ->
                taskRepository.findById(slot.getTaskId()).ifPresent(t -> {
                    if (t.getMode() == TaskMode.RESERVE && t.getStatus() == TaskStatus.RESERVING) {
                        map.put(t.getId(), new ReservationEntry(t, UserRole.RUNNER));
                    }
                }));
        return new ArrayList<>(map.values());
    }

    @Override
    public TaskDetailView getDetail(long taskId, Long viewerUserId) {
        Task task = enrichRunner(requireTask(taskId));
        boolean held = viewerUserId != null
                && reserveSlotRepository.findByTaskIdAndRunnerId(taskId, viewerUserId)
                .map(slot -> slot.getStatus() == ReserveSlotStatus.HOLDING)
                .orElse(false);
        int holdCount = (int) reserveSlotRepository.countByTaskIdAndStatus(taskId, ReserveSlotStatus.HOLDING);
        return new TaskDetailView(task, held, holdCount);
    }

    @Override
    @Transactional
    public Task grab(long userId, long taskId) {
        Task task = requireTask(taskId);
        if (task.getMode() == TaskMode.RESERVE) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "预约任务请先占位，到时间后确认接单");
        }
        if (task.getStatus() != TaskStatus.PENDING || task.getRunnerId() != null) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "手慢无，任务已被抢");
        }
        Task updated = task.toBuilder()
                .runnerId(userId)
                .status(TaskStatus.ACCEPTED)
                .acceptedAt(Instant.now())
                .build();
        Task saved = taskRepository.save(updated);
        taskRepository.appendStatusLog(taskId, TaskStatus.PENDING, TaskStatus.ACCEPTED, userId, "抢单");
        chatService.ensureForAcceptedTask(taskId, saved.getPublisherId(), userId);
        String runnerName = userRepository.findById(userId).map(u -> u.getNickname()).orElse("跑腿员");
        notificationService.notifyTaskAccepted(saved, runnerName);
        return enrichRunner(saved);
    }

    @Override
    @Transactional
    public Task holdReserve(long userId, long taskId) {
        Task task = requireReserveTask(taskId);
        if (task.getPublisherId() == userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "不能占位自己发布的预约任务");
        }
        if (reserveSlotRepository.findByTaskIdAndRunnerId(taskId, userId)
                .filter(slot -> slot.getStatus() == ReserveSlotStatus.HOLDING)
                .isPresent()) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "您已占位该预约任务");
        }
        long holding = reserveSlotRepository.countByTaskIdAndStatus(taskId, ReserveSlotStatus.HOLDING);
        if (holding >= MAX_RESERVE_HOLDERS) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "预约占位已满（最多 " + MAX_RESERVE_HOLDERS + " 人）");
        }
        reserveSlotRepository.save(ReserveSlot.builder()
                .taskId(taskId)
                .runnerId(userId)
                .status(ReserveSlotStatus.HOLDING)
                .holdAt(Instant.now())
                .build());
        taskRepository.appendStatusLog(taskId, TaskStatus.RESERVING, TaskStatus.RESERVING, userId, "预约占位");
        return enrichRunner(task);
    }

    @Override
    @Transactional
    public Task releaseReserve(long userId, long taskId) {
        requireReserveTask(taskId);
        ReserveSlot slot = reserveSlotRepository.findByTaskIdAndRunnerId(taskId, userId)
                .filter(s -> s.getStatus() == ReserveSlotStatus.HOLDING)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "您尚未占位该预约任务"));
        reserveSlotRepository.updateStatus(slot.getId(), ReserveSlotStatus.RELEASED);
        taskRepository.appendStatusLog(taskId, TaskStatus.RESERVING, TaskStatus.RESERVING, userId, "取消占位");
        return enrichRunner(requireTask(taskId));
    }

    @Override
    @Transactional
    public Task confirmReserve(long userId, long taskId) {
        Task task = requireReserveTask(taskId);
        ReserveSlot slot = reserveSlotRepository.findByTaskIdAndRunnerId(taskId, userId)
                .filter(s -> s.getStatus() == ReserveSlotStatus.HOLDING)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.FORBIDDEN, "请先占位后再确认接单"));
        reserveSlotRepository.findByTaskIdAndStatus(taskId, ReserveSlotStatus.HOLDING).forEach(other -> {
            if (other.getRunnerId() != userId) {
                reserveSlotRepository.updateStatus(other.getId(), ReserveSlotStatus.RELEASED);
            }
        });
        reserveSlotRepository.updateStatus(slot.getId(), ReserveSlotStatus.CONFIRMED);
        Task saved = taskRepository.save(task.toBuilder()
                .runnerId(userId)
                .status(TaskStatus.ACCEPTED)
                .acceptedAt(Instant.now())
                .build());
        taskRepository.appendStatusLog(taskId, TaskStatus.RESERVING, TaskStatus.ACCEPTED, userId, "确认预约接单");
        chatService.ensureForAcceptedTask(taskId, saved.getPublisherId(), userId);
        String runnerName = userRepository.findById(userId).map(u -> u.getNickname()).orElse("跑腿员");
        notificationService.notifyTaskAccepted(saved, runnerName);
        return enrichRunner(saved);
    }

    @Override
    @Transactional
    public Task startDeliver(long userId, long taskId) {
        Task task = requireRunner(taskId, userId);
        Task saved = taskRepository.save(task.toBuilder().status(TaskStatus.DELIVERING).build());
        taskRepository.appendStatusLog(taskId, TaskStatus.ACCEPTED, TaskStatus.DELIVERING, userId, "开始配送");
        return saved;
    }

    @Override
    @Transactional
    public Task uploadPhoto(long userId, long taskId, String photoUrl) {
        Task task = requireRunner(taskId, userId);
        Task saved = taskRepository.save(task.toBuilder()
                .status(TaskStatus.CONFIRMING)
                .deliveryPhotoUrl(photoUrl)
                .build());
        taskRepository.appendStatusLog(taskId, TaskStatus.DELIVERING, TaskStatus.CONFIRMING, userId, "上传送达照");
        return saved;
    }

    @Override
    @Transactional
    public Task confirm(long userId, long taskId) {
        Task task = requireTask(taskId);
        if (task.getPublisherId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "仅发布者可确认");
        }
        if (task.getRunnerId() == null) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "任务尚未接单");
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "当前状态不可确认");
        }
        releaseTaskEscrow(task);
        int payout = Math.max(task.getRewardCent(), 0);
        if (payout > 0) {
            walletService.creditIncome(task.getRunnerId(), payout, taskId,
                    "任务完成 - " + task.getTitle());
        }
        Task saved = taskRepository.save(task.toBuilder()
                .status(TaskStatus.COMPLETED)
                .escrowCent(0)
                .completedAt(Instant.now())
                .build());
        taskRepository.appendStatusLog(taskId, task.getStatus(), TaskStatus.COMPLETED, userId, "确认收货");
        notificationService.notifyTaskCompleted(saved);
        return saved;
    }

    @Override
    @Transactional
    public Task raisePrice(long userId, long taskId, int addCent) {
        Task task = requirePublisher(taskId, userId);
        if (addCent <= 0) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "加价金额无效");
        }
        walletService.holdRaise(userId, addCent, taskId, "任务加价");
        return taskRepository.save(task.toBuilder()
                .rewardCent(task.getRewardCent() + addCent)
                .escrowCent(task.getEscrowCent() + addCent)
                .build());
    }

    @Override
    @Transactional
    public Task toEmergency(long userId, long taskId) {
        Task task = requirePublisher(taskId, userId);
        int nextReward = (int) Math.round(task.getRewardCent() * 1.5);
        int delta = nextReward - task.getRewardCent();
        if (delta > 0) {
            walletService.holdRaise(userId, delta, taskId, "转紧急加价");
        }
        return taskRepository.save(task.toBuilder()
                .mode(TaskMode.EMERGENCY)
                .rewardCent(nextReward)
                .escrowCent(task.getEscrowCent() + Math.max(delta, 0))
                .build());
    }

    @Override
    @Transactional
    public Task cancel(long userId, long taskId, String reason) {
        Task task = requireTask(taskId);
        boolean publisher = task.getPublisherId() == userId;
        boolean runner = task.getRunnerId() != null && task.getRunnerId() == userId;
        if (!publisher && !runner) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权取消");
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "当前状态不可取消");
        }
        refundTaskEscrow(task);
        Task saved = taskRepository.save(task.toBuilder()
                .status(TaskStatus.CANCELLED)
                .escrowCent(0)
                .cancelReason(reason)
                .build());
        if (task.getMode() == TaskMode.RESERVE) {
            reserveSlotRepository.cancelAllForTask(taskId);
        }
        taskRepository.appendStatusLog(taskId, task.getStatus(), TaskStatus.CANCELLED, userId, reason);
        if (publisher) {
            if (task.getRunnerId() != null) {
                notificationService.notifyTaskCancelled(saved, task.getRunnerId(), reason);
            }
        } else if (runner) {
            notificationService.notifyTaskCancelled(saved, task.getPublisherId(), reason);
        }
        return saved;
    }

    @Override
    public GroupOrder getGroupDetail(long taskId, long userId) {
        return toGroupOrder(requireGroupTask(taskId), userId);
    }

    @Override
    @Transactional
    public GroupOrder joinGroup(long taskId, long userId, String address) {
        Task task = requireGroupTask(taskId);
        GroupOrder preview = toGroupOrder(task, userId);
        if (preview.isFull()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "拼单已满员");
        }
        if (preview.isViewerJoined()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "您已加入该拼单");
        }
        if (preview.isViewerIsCreator()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "发起人无需加入");
        }
        int share = shareCent(task);
        walletService.hold(userId, share, taskId, "拼单支付 - " + task.getTitle());
        String nickname = userRepository.findById(userId).map(u -> u.getNickname()).orElse("用户");
        taskRepository.saveGroupMember(taskId, GroupMember.builder()
                .userId(userId)
                .name(nickname)
                .role("MEMBER")
                .addressSummary(nullToEmpty(address))
                .shareCent(share)
                .payStatus("PAID")
                .build());
        int joined = preview.getJoinedCount() + 1;
        Task.TaskBuilder builder = task.toBuilder()
                .groupJoinedCount(joined)
                .escrowCent(task.getEscrowCent() + share);
        if (joined >= preview.getMaxMembers()) {
            builder.status(TaskStatus.PENDING);
            taskRepository.appendStatusLog(taskId, TaskStatus.GROUPING, TaskStatus.PENDING, userId, "拼单满员");
        }
        Task saved = taskRepository.save(builder.build());
        return toGroupOrder(saved, userId);
    }

    @Override
    @Transactional
    public GroupOrder leaveGroup(long taskId, long userId) {
        Task task = requireGroupTask(taskId);
        GroupOrder preview = toGroupOrder(task, userId);
        if (!preview.isViewerJoined() || preview.isViewerIsCreator()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无法退出拼单");
        }
        int share = shareCent(task);
        walletService.refundEscrow(userId, share, taskId, "拼单退款 - " + task.getTitle());
        taskRepository.removeGroupMember(taskId, userId);
        Task saved = taskRepository.save(task.toBuilder()
                .groupJoinedCount(Math.max(0, preview.getJoinedCount() - 1))
                .escrowCent(Math.max(0, task.getEscrowCent() - share))
                .status(TaskStatus.GROUPING)
                .build());
        return toGroupOrder(saved, userId);
    }

    private GroupOrder toGroupOrder(Task task, long userId) {
        List<GroupMember> stored = taskRepository.findGroupMembers(task.getId());
        int max = task.getGroupTargetCount() == null ? DEFAULT_GROUP_TARGET : task.getGroupTargetCount();
        int joined = (int) stored.stream().filter(m -> !"EMPTY_SLOT".equals(m.getRole())).count();
        List<GroupMember> display = new ArrayList<>(stored);
        while (display.size() < max) {
            display.add(GroupMember.builder()
                    .name("")
                    .role("EMPTY_SLOT")
                    .addressSummary("")
                    .shareCent(0)
                    .payStatus("UNPAID")
                    .build());
        }
        boolean viewerJoined = stored.stream().anyMatch(m -> m.getUserId() != null && m.getUserId() == userId);
        return GroupOrder.builder()
                .task(task)
                .totalReward(task.getRewardCent() / 100.0)
                .sharePerPerson(shareCent(task) / 100.0)
                .maxMembers(max)
                .joinedCount(joined)
                .viewerJoined(viewerJoined)
                .viewerIsCreator(task.getPublisherId() == userId)
                .full(joined >= max)
                .members(display)
                .build();
    }

    private Task baseFromDraft(long userId, DraftCommand command) {
        int reward = Math.max(command.rewardCent(), 0);
        return Task.builder()
                .publisherId(userId)
                .mode(command.mode() == null ? TaskMode.NORMAL : command.mode())
                .category(command.category() == null ? TaskCategory.OTHER : command.category())
                .title(trim(command.title(), 60))
                .description(nullToEmpty(command.description()))
                .pickupName(nullToEmpty(command.pickupAddress()))
                .dropoffName(nullToEmpty(command.deliveryAddress()))
                .timeLabel(nullToEmpty(command.timeLabel()))
                .rewardCent(reward)
                .baseRewardCent(reward)
                .escrowCent(0)
                .build();
    }

    private void releaseTaskEscrow(Task task) {
        long taskId = task.getId();
        if (task.getMode() == TaskMode.GROUP) {
            int released = 0;
            for (GroupMember member : taskRepository.findGroupMembers(taskId)) {
                if (member.getUserId() == null || member.getShareCent() <= 0) {
                    continue;
                }
                if ("EMPTY_SLOT".equals(member.getRole())) {
                    continue;
                }
                walletService.releaseEscrow(member.getUserId(), member.getShareCent(), taskId,
                        "拼单托管释放 - " + task.getTitle());
                released += member.getShareCent();
            }
            int extra = task.getEscrowCent() - released;
            if (extra > 0) {
                walletService.releaseEscrow(task.getPublisherId(), extra, taskId,
                        "托管释放（加价） - " + task.getTitle());
            }
            return;
        }
        int escrow = task.getEscrowCent() > 0 ? task.getEscrowCent() : task.getRewardCent();
        if (escrow > 0) {
            walletService.releaseEscrow(task.getPublisherId(), escrow, taskId,
                    "托管释放结算 - " + task.getTitle());
        }
    }

    private void refundTaskEscrow(Task task) {
        long taskId = task.getId();
        if (task.getEscrowCent() <= 0 && task.getMode() != TaskMode.GROUP) {
            return;
        }
        if (task.getMode() == TaskMode.GROUP) {
            int refunded = 0;
            for (GroupMember member : taskRepository.findGroupMembers(taskId)) {
                if (member.getUserId() == null || member.getShareCent() <= 0) {
                    continue;
                }
                if ("EMPTY_SLOT".equals(member.getRole())) {
                    continue;
                }
                walletService.refundEscrow(member.getUserId(), member.getShareCent(), taskId,
                        "取消退款 - " + task.getTitle());
                refunded += member.getShareCent();
            }
            int extra = task.getEscrowCent() - refunded;
            if (extra > 0) {
                walletService.refundEscrow(task.getPublisherId(), extra, taskId,
                        "取消退款（加价） - " + task.getTitle());
            }
            return;
        }
        if (task.getEscrowCent() > 0) {
            walletService.refundEscrow(task.getPublisherId(), task.getEscrowCent(), taskId,
                    "取消退款 - " + task.getTitle());
        }
    }

    private Task requireTask(long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCodes.NOT_FOUND, "任务不存在"));
    }

    private Task enrichRunner(Task task) {
        if (task.getRunnerId() == null) {
            return task;
        }
        String name = userRepository.findById(task.getRunnerId())
                .map(u -> u.getNickname())
                .orElse("跑腿员");
        return task.toBuilder()
                .runnerName(name)
                .runnerRating(4.8f)
                .runnerCredit(720)
                .runnerCompletedOrders(42)
                .build();
    }

    private Task requireReserveTask(long id) {
        Task task = requireTask(id);
        if (task.getMode() != TaskMode.RESERVE || task.getStatus() != TaskStatus.RESERVING) {
            throw BusinessException.of(ErrorCodes.INVALID_STATE, "该任务不是可占位的预约任务");
        }
        return task;
    }

    private Task requireGroupTask(long id) {
        Task task = requireTask(id);
        if (task.getMode() != TaskMode.GROUP) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "该任务不是拼单");
        }
        return task;
    }

    private Task requireOwnedDraft(long userId, long draftId) {
        Task task = requireTask(draftId);
        if (task.getStatus() != TaskStatus.DRAFT || task.getPublisherId() != userId) {
            throw BusinessException.of(ErrorCodes.NOT_FOUND, "草稿不存在");
        }
        return task;
    }

    private Task requirePublisher(long taskId, long userId) {
        Task task = requireTask(taskId);
        if (task.getPublisherId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "仅发布者可操作");
        }
        return task;
    }

    private Task requireRunner(long taskId, long userId) {
        Task task = requireTask(taskId);
        if (task.getRunnerId() == null || task.getRunnerId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "仅接单跑腿员可操作");
        }
        return task;
    }

    private static TaskStatus initialStatus(TaskMode mode) {
        if (mode == TaskMode.GROUP) {
            return TaskStatus.GROUPING;
        }
        if (mode == TaskMode.RESERVE) {
            return TaskStatus.RESERVING;
        }
        return TaskStatus.PENDING;
    }

    private static int shareCent(Task task) {
        int target = task.getGroupTargetCount() == null ? DEFAULT_GROUP_TARGET : task.getGroupTargetCount();
        if (target <= 0) {
            return DEFAULT_GROUP_SHARE_CENT;
        }
        return Math.max(task.getRewardCent() / target, 100);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String trim(String value, int max) {
        String v = nullToEmpty(value).trim();
        if (v.isEmpty()) {
            return "未命名任务";
        }
        return v.length() > max ? v.substring(0, max) : v;
    }
}
