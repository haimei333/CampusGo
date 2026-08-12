package com.campusgo.api.mock.task;

import com.campusgo.api.dto.common.PageResponse;
import com.campusgo.api.dto.task.GroupMemberDto;
import com.campusgo.api.dto.task.GroupOrderDetailDto;
import com.campusgo.api.dto.task.JoinGroupRequest;
import com.campusgo.api.dto.task.PublishTaskRequest;
import com.campusgo.api.dto.task.PublishTaskResponse;
import com.campusgo.api.dto.task.RunnerSummaryDto;
import com.campusgo.api.dto.task.SaveDraftRequest;
import com.campusgo.api.dto.task.TaskDetailDto;
import com.campusgo.api.dto.task.TaskDraftDto;
import com.campusgo.api.dto.task.TaskListItemDto;
import com.campusgo.api.mock.wallet.WalletMockSupport;
import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import com.campusgo.domain.repository.UserRepository;
import com.campusgo.domain.repository.WalletRepository;
import com.campusgo.domain.util.MoneyUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Task 模块 Mock 实现，数据对齐 Android {@code MockTaskRepository}。
 */
@Service
public class TaskMockService {

    private static final int GROUP_MAX_MEMBERS = 3;
    private static final double GROUP_TOTAL_REWARD = 30.0;
    private static final double GROUP_SHARE = 10.0;

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    private final Map<String, TaskMockRecord> tasks = new ConcurrentHashMap<>();
    private final Map<String, TaskDraftDto> drafts = new ConcurrentHashMap<>();
    private final Map<String, List<GroupMemberEntry>> groupMembers = new ConcurrentHashMap<>();
    private final AtomicLong taskSeq = new AtomicLong(100);
    private final AtomicLong draftSeq = new AtomicLong(1);
    private final AtomicLong orderSeq = new AtomicLong(20240803001L);

    public TaskMockService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        seedTasks();
        seedDraft();
        seedGroups();
    }

    public GroupOrderDetailDto getGroupDetail(String taskId, long userId) {
        TaskMockRecord task = requireGroupTask(taskId);
        List<GroupMemberEntry> members = groupMembers.computeIfAbsent(taskId, id -> defaultGroupMembers());
        return toGroupDetail(task, members, userId);
    }

    public GroupOrderDetailDto joinGroup(String taskId, long userId, JoinGroupRequest request) {
        TaskMockRecord task = requireGroupTask(taskId);
        List<GroupMemberEntry> members = new ArrayList<>(
                groupMembers.computeIfAbsent(taskId, id -> defaultGroupMembers()));
        GroupOrderDetailDto preview = toGroupDetail(task, members, userId);
        if (preview.isFull()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "拼单已满员");
        }
        if (preview.isViewerJoined()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "您已加入该拼单");
        }
        if (preview.isViewerIsCreator()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "发起人无需加入");
        }
        String nickname = userRepository.findById(userId)
                .map(u -> u.getNickname())
                .orElse("用户");
        int shareCent = (int) Math.round(GROUP_SHARE * 100);
        walletRepository.adjustBalance(userId, -shareCent);

        boolean filled = false;
        List<GroupMemberEntry> next = new ArrayList<>();
        for (GroupMemberEntry member : members) {
            if (!filled && member.getRole() == GroupMemberDto.Role.EMPTY_SLOT) {
                next.add(new GroupMemberEntry(
                        "m-" + userId,
                        userId,
                        nickname,
                        GroupMemberDto.Role.MEMBER,
                        nullToEmpty(request.getAddress()),
                        GROUP_SHARE));
                filled = true;
            } else if (member.getRole() != GroupMemberDto.Role.EMPTY_SLOT) {
                next.add(member);
            }
        }
        if (!filled) {
            walletRepository.adjustBalance(userId, shareCent);
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "拼单已满员");
        }
        while (next.size() < GROUP_MAX_MEMBERS) {
            next.add(GroupMemberEntry.emptySlot());
        }
        groupMembers.put(taskId, next);
        WalletMockSupport.recordExpense(userId, "拼单支付 - " + task.getTitle(), GROUP_SHARE);
        // 满员后进入任务大厅，供跑腿员抢单
        int joined = (int) next.stream()
                .filter(m -> m.getRole() != GroupMemberDto.Role.EMPTY_SLOT)
                .count();
        if (joined >= GROUP_MAX_MEMBERS) {
            task.setStatus(TaskStatus.PENDING);
            task.setStatusLabel("拼单·待抢");
            task.setNavTarget(TaskListItemDto.NavTarget.T06);
            task.setListScope(TaskMockRecord.TaskListScope.HALL);
        }
        return toGroupDetail(task, next, userId);
    }

    public GroupOrderDetailDto leaveGroup(String taskId, long userId) {
        TaskMockRecord task = requireGroupTask(taskId);
        List<GroupMemberEntry> members = new ArrayList<>(
                groupMembers.computeIfAbsent(taskId, id -> defaultGroupMembers()));
        GroupOrderDetailDto preview = toGroupDetail(task, members, userId);
        if (!preview.isViewerJoined() || preview.isViewerIsCreator()) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无法退出拼单");
        }
        int shareCent = (int) Math.round(GROUP_SHARE * 100);
        walletRepository.adjustBalance(userId, shareCent);

        List<GroupMemberEntry> next = new ArrayList<>();
        for (GroupMemberEntry member : members) {
            if (member.getUserId() != null && member.getUserId() == userId) {
                continue;
            }
            if (member.getRole() != GroupMemberDto.Role.EMPTY_SLOT) {
                next.add(member);
            }
        }
        next.add(GroupMemberEntry.emptySlot());
        while (next.size() < GROUP_MAX_MEMBERS) {
            next.add(GroupMemberEntry.emptySlot());
        }
        groupMembers.put(taskId, next);
        WalletMockSupport.recordIncome(userId, "拼单退款 - " + task.getTitle(), GROUP_SHARE);
        return toGroupDetail(task, next, userId);
    }

    public List<TaskDraftDto> listDrafts(long userId) {
        return drafts.values().stream()
                .sorted(Comparator.comparing(TaskDraftDto::getUpdatedAt).reversed())
                .toList();
    }

    public TaskDraftDto createDraft(long userId, SaveDraftRequest request) {
        String id = "d" + draftSeq.incrementAndGet();
        TaskDraftDto draft = toDraft(id, request);
        drafts.put(id, draft);
        return draft;
    }

    public TaskDraftDto updateDraft(long userId, String id, SaveDraftRequest request) {
        if (!drafts.containsKey(id)) {
            throw BusinessException.of(ErrorCodes.NOT_FOUND, "草稿不存在");
        }
        TaskDraftDto draft = toDraft(id, request);
        drafts.put(id, draft);
        return draft;
    }

    public void deleteDraft(long userId, String id) {
        if (drafts.remove(id) == null) {
            throw BusinessException.of(ErrorCodes.NOT_FOUND, "草稿不存在");
        }
    }

    public PublishTaskResponse publish(long userId, PublishTaskRequest request) {
        if (request.getDraftId() != null) {
            drafts.remove(request.getDraftId());
        }
        String id = "p" + taskSeq.incrementAndGet();
        TaskStatus status = initialStatus(request.getMode());
        TaskMockRecord record = TaskMockRecord.builder()
                .id(id)
                .listScope(TaskMockRecord.TaskListScope.MINE_PUBLISH)
                .publisherId(userId)
                .title(request.getTitle())
                .statusLabel(statusLabel(status, request.getMode()))
                .description(nullToEmpty(request.getDescription()))
                .rewardCent(request.getRewardCent())
                .category(request.getCategory())
                .mode(request.getMode())
                .status(status)
                .navTarget(request.getMode() == TaskMode.GROUP ? TaskListItemDto.NavTarget.T07 : TaskListItemDto.NavTarget.T06)
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .timeLabel(nullToEmpty(request.getTimeLabel()))
                .orderNo("CG" + orderSeq.incrementAndGet())
                .build();
        tasks.put(id, record);
        mirrorForHall(record);
        return PublishTaskResponse.builder()
                .taskId(id)
                .taskNo(record.getOrderNo())
                .status(status)
                .build();
    }

    public PageResponse<TaskListItemDto> listHall(int page, int pageSize) {
        return page(filterScope(TaskMockRecord.TaskListScope.HALL), page, pageSize);
    }

    public PageResponse<TaskListItemDto> listPool(int page, int pageSize) {
        return page(filterScope(TaskMockRecord.TaskListScope.POOL), page, pageSize);
    }

    public PageResponse<TaskListItemDto> listMinePublished(long userId, int page, int pageSize) {
        List<TaskListItemDto> all = tasks.values().stream()
                .filter(t -> t.getPublisherId() == userId)
                .filter(t -> t.getListScope() == TaskMockRecord.TaskListScope.MINE_PUBLISH
                        || t.getListScope() == TaskMockRecord.TaskListScope.HALL)
                .map(this::toListItem)
                .distinct()
                .toList();
        return paginate(all, page, pageSize);
    }

    public PageResponse<TaskListItemDto> listMineAccepted(long userId, int page, int pageSize) {
        List<TaskListItemDto> all = tasks.values().stream()
                .filter(t -> t.getRunnerId() != null && t.getRunnerId() == userId)
                .map(this::toListItem)
                .toList();
        return paginate(all, page, pageSize);
    }

    public PageResponse<TaskListItemDto> listMineReservations(long userId, int page, int pageSize) {
        List<TaskListItemDto> all = tasks.values().stream()
                .filter(t -> t.getListScope() == TaskMockRecord.TaskListScope.RESERVE)
                .map(this::toListItem)
                .toList();
        return paginate(all, page, pageSize);
    }

    public TaskDetailDto getDetail(String id) {
        TaskMockRecord record = requireTask(id);
        return toDetail(record);
    }

    public TaskDetailDto grab(long userId, String id) {
        TaskMockRecord record = requireTask(id);
        if (record.getStatus() != TaskStatus.PENDING) {
            throw BusinessException.of(ErrorCodes.CONFLICT, "手慢无，任务已被抢");
        }
        record.setRunnerId(userId);
        record.setStatus(TaskStatus.ACCEPTED);
        record.setStatusLabel("已接单");
        record.setRunnerName("跑腿员" + userId);
        record.setRunnerRating(4.8f);
        record.setRunnerCredit(720);
        record.setRunnerCompletedOrders(42);
        return toDetail(record);
    }

    public TaskDetailDto startDeliver(long userId, String id) {
        TaskMockRecord record = requireTask(id);
        assertRunner(record, userId);
        record.setStatus(TaskStatus.DELIVERING);
        record.setStatusLabel("配送中");
        return toDetail(record);
    }

    public TaskDetailDto uploadPhoto(long userId, String id, String photoUrl) {
        TaskMockRecord record = requireTask(id);
        assertRunner(record, userId);
        record.setStatus(TaskStatus.CONFIRMING);
        record.setStatusLabel("待确认");
        return toDetail(record);
    }

    public TaskDetailDto confirm(long userId, String id) {
        TaskMockRecord record = requireTask(id);
        if (record.getPublisherId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "仅发布者可确认");
        }
        record.setStatus(TaskStatus.COMPLETED);
        record.setStatusLabel("已完成");
        return toDetail(record);
    }

    public TaskDetailDto raisePrice(long userId, String id, int addCent) {
        TaskMockRecord record = requireTask(id);
        if (record.getPublisherId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "仅发布者可加价");
        }
        record.setRewardCent(record.getRewardCent() + addCent);
        return toDetail(record);
    }

    public TaskDetailDto toEmergency(long userId, String id) {
        TaskMockRecord record = requireTask(id);
        if (record.getPublisherId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "仅发布者可转紧急");
        }
        record.setMode(TaskMode.EMERGENCY);
        record.setRewardCent((int) (record.getRewardCent() * 1.5));
        record.setStatusLabel("紧急");
        return toDetail(record);
    }

    public TaskDetailDto cancel(long userId, String id, String reason) {
        TaskMockRecord record = requireTask(id);
        if (record.getPublisherId() != userId && (record.getRunnerId() == null || record.getRunnerId() != userId)) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "无权取消");
        }
        record.setStatus(TaskStatus.CANCELLED);
        record.setStatusLabel("已取消");
        record.setDescription(record.getDescription() + (reason != null ? " · " + reason : ""));
        return toDetail(record);
    }

    private void seedDraft() {
        SaveDraftRequest seed = new SaveDraftRequest();
        seed.setTitle("代取快递草稿");
        seed.setDescription("菜鸟驿站，小件");
        seed.setMode(TaskMode.NORMAL);
        seed.setCategory(TaskCategory.EXPRESS);
        seed.setPickupAddress("菜鸟驿站");
        seed.setDeliveryAddress("6号楼");
        seed.setTimeLabel("今天内");
        seed.setRewardCent(800);
        drafts.put("p1", toDraft("p1", seed));
    }

    private void seedTasks() {
        addSeed("h1", TaskMockRecord.TaskListScope.HALL, null, 0L, null,
                "取快递 - 菜鸟驿站", "普通", "请帮忙取一个中通快递，小件", 500,
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.PENDING, TaskListItemDto.NavTarget.T06,
                "菜鸟驿站", "6号楼下", "今天 18:00 前");
        // 拼单招募中 → 只进拼单池，不进任务大厅（大厅仅可抢单任务）
        addSeed("h2", TaskMockRecord.TaskListScope.POOL, null, 0L, null,
                "代买奶茶+零食", "差1人", "蜜雪冰城 · 未满员，均摊拼单", 1200,
                TaskCategory.BUY, TaskMode.GROUP, TaskStatus.GROUPING, TaskListItemDto.NavTarget.T07,
                "蜜雪冰城", "各宿舍", "1 小时内");
        addSeed("h3", TaskMockRecord.TaskListScope.HALL, null, 0L, null,
                "送文件到行政楼", "紧急", "一份密封文件，需当面交接", 2250,
                TaskCategory.ERRAND, TaskMode.EMERGENCY, TaskStatus.PENDING, TaskListItemDto.NavTarget.T06,
                "图书馆", "行政楼", "尽快");
        // 拼单已满员 → 进入大厅供跑腿员抢单
        addSeed("h5", TaskMockRecord.TaskListScope.HALL, null, 0L, null,
                "拼单满员待抢", "拼单·待抢", "已满 3/3，跑腿员可抢单", 1800,
                TaskCategory.BUY, TaskMode.GROUP, TaskStatus.PENDING, TaskListItemDto.NavTarget.T06,
                "蜜雪冰城", "各宿舍", "尽快");
        addSeed("p2", TaskMockRecord.TaskListScope.MINE_PUBLISH, null, 1L, null,
                "取快递 - 中通", "待接单", "已发布 30 分钟无人接 · 可加价", 1500,
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.PENDING, TaskListItemDto.NavTarget.T06,
                "中通快递点", "6号楼", "今天");
        addSeed("t1", TaskMockRecord.TaskListScope.MINE_ACCEPTED, null, 2L, 3L,
                "取快递 - 中通", "配送中", "正在前往送达点", 1500,
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.DELIVERING, TaskListItemDto.NavTarget.T06,
                "中通快递点", "6号楼", "今天");
        TaskMockRecord t1 = tasks.get("t1");
        t1.setRunnerName("小王");
        t1.setRunnerRating(4.9f);
        t1.setRunnerCredit(760);
        t1.setRunnerCompletedOrders(58);
        addSeed("pool1", TaskMockRecord.TaskListScope.POOL, null, 0L, null,
                "代买奶茶拼单", "差1人", "均摊 ¥10/人", 1000,
                TaskCategory.BUY, TaskMode.GROUP, TaskStatus.GROUPING, TaskListItemDto.NavTarget.T07,
                "蜜雪冰城", "各宿舍", "今晚");
        addSeed("r1", TaskMockRecord.TaskListScope.RESERVE, UserRole.PUBLISHER, 1L, null,
                "代购日用品", "预约中", "我发起的预约", 1500,
                TaskCategory.BUY, TaskMode.RESERVE, TaskStatus.RESERVING, TaskListItemDto.NavTarget.T06,
                "超市", "6号楼", "明日 18:00");
    }

    private void addSeed(String id, TaskMockRecord.TaskListScope scope, UserRole reserveRole,
                         long publisherId, Long runnerId,
                         String title, String statusLabel, String desc, int cent,
                         TaskCategory category, TaskMode mode, TaskStatus status,
                         TaskListItemDto.NavTarget nav, String pickup, String dropoff, String time) {
        TaskMockRecord record = TaskMockRecord.builder()
                .id(id)
                .listScope(scope)
                .reserveForRole(reserveRole)
                .publisherId(publisherId)
                .runnerId(runnerId)
                .title(title)
                .statusLabel(statusLabel)
                .description(desc)
                .rewardCent(cent)
                .category(category)
                .mode(mode)
                .status(status)
                .navTarget(nav)
                .pickupAddress(pickup)
                .deliveryAddress(dropoff)
                .timeLabel(time)
                .orderNo("CG" + orderSeq.getAndIncrement())
                .build();
        tasks.put(id, record);
    }

    private void mirrorForHall(TaskMockRecord published) {
        // 未满员拼单进拼单池；待抢单（含拼单满员后）进大厅
        if (published.getStatus() == TaskStatus.GROUPING
                || (published.getMode() == TaskMode.GROUP && published.getStatus() != TaskStatus.PENDING)) {
            published.setListScope(TaskMockRecord.TaskListScope.POOL);
            return;
        }
        if (published.getStatus() == TaskStatus.PENDING) {
            published.setListScope(TaskMockRecord.TaskListScope.HALL);
        }
    }

    private List<TaskListItemDto> filterScope(TaskMockRecord.TaskListScope scope) {
        return tasks.values().stream()
                .filter(t -> matchesScope(t, scope))
                .map(this::toListItem)
                .toList();
    }

    private boolean matchesScope(TaskMockRecord t, TaskMockRecord.TaskListScope scope) {
        if (scope == TaskMockRecord.TaskListScope.HALL) {
            // 大厅：可抢单任务；招募中的拼单不出现
            if (t.getStatus() == TaskStatus.GROUPING || t.getNavTarget() == TaskListItemDto.NavTarget.T07) {
                return false;
            }
            return t.getListScope() == TaskMockRecord.TaskListScope.HALL
                    || (t.getStatus() == TaskStatus.PENDING
                    && t.getListScope() == TaskMockRecord.TaskListScope.MINE_PUBLISH);
        }
        if (scope == TaskMockRecord.TaskListScope.POOL) {
            return t.getListScope() == TaskMockRecord.TaskListScope.POOL
                    || (t.getMode() == TaskMode.GROUP && t.getStatus() == TaskStatus.GROUPING);
        }
        return t.getListScope() == scope;
    }

    private PageResponse<TaskListItemDto> page(List<TaskListItemDto> all, int page, int pageSize) {
        return paginate(all, page, pageSize);
    }

    private PageResponse<TaskListItemDto> paginate(List<TaskListItemDto> all, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        int from = (safePage - 1) * safeSize;
        if (from >= all.size()) {
            return PageResponse.<TaskListItemDto>builder()
                    .list(List.of())
                    .page(safePage)
                    .pageSize(safeSize)
                    .total(all.size())
                    .build();
        }
        int to = Math.min(from + safeSize, all.size());
        return PageResponse.<TaskListItemDto>builder()
                .list(new ArrayList<>(all.subList(from, to)))
                .page(safePage)
                .pageSize(safeSize)
                .total(all.size())
                .build();
    }

    private TaskMockRecord requireTask(String id) {
        TaskMockRecord record = tasks.get(id);
        if (record == null) {
            throw BusinessException.of(ErrorCodes.NOT_FOUND, "任务不存在");
        }
        return record;
    }

    private void assertRunner(TaskMockRecord record, long userId) {
        if (record.getRunnerId() == null || record.getRunnerId() != userId) {
            throw BusinessException.of(ErrorCodes.FORBIDDEN, "仅接单跑腿员可操作");
        }
    }

    private TaskDraftDto toDraft(String id, SaveDraftRequest request) {
        return TaskDraftDto.builder()
                .id(id)
                .title(nullToEmpty(request.getTitle()))
                .description(nullToEmpty(request.getDescription()))
                .mode(request.getMode())
                .category(request.getCategory())
                .pickupAddress(nullToEmpty(request.getPickupAddress()))
                .deliveryAddress(nullToEmpty(request.getDeliveryAddress()))
                .timeLabel(nullToEmpty(request.getTimeLabel()))
                .rewardCent(request.getRewardCent())
                .rewardYuan(MoneyUtils.formatYuan(request.getRewardCent()))
                .updatedAt(Instant.now())
                .build();
    }

    private TaskListItemDto toListItem(TaskMockRecord record) {
        return TaskListItemDto.builder()
                .id(record.getId())
                .title(record.getTitle())
                .statusLabel(record.getStatusLabel())
                .description(record.getDescription())
                .priceCent(record.getRewardCent())
                .priceYuan(MoneyUtils.formatYuan(record.getRewardCent()))
                .category(record.getCategory())
                .mode(record.getMode())
                .status(record.getStatus())
                .navTarget(record.getNavTarget())
                .reserveForRole(record.getReserveForRole())
                .build();
    }

    private TaskDetailDto toDetail(TaskMockRecord record) {
        RunnerSummaryDto runner = null;
        if (record.getRunnerId() != null) {
            runner = RunnerSummaryDto.builder()
                    .name(record.getRunnerName())
                    .rating(record.getRunnerRating())
                    .creditScore(record.getRunnerCredit())
                    .completedOrders(record.getRunnerCompletedOrders())
                    .build();
        }
        return TaskDetailDto.builder()
                .id(record.getId())
                .title(record.getTitle())
                .categoryLabel(categoryLabel(record.getCategory()))
                .mode(record.getMode())
                .status(record.getStatus())
                .pickupAddress(record.getPickupAddress())
                .deliveryAddress(record.getDeliveryAddress())
                .timeLabel(record.getTimeLabel())
                .description(record.getDescription())
                .rewardCent(record.getRewardCent())
                .rewardYuan(MoneyUtils.formatYuan(record.getRewardCent()))
                .orderNo(record.getOrderNo())
                .runner(runner)
                .build();
    }

    private static TaskStatus initialStatus(TaskMode mode) {
        return switch (mode) {
            case GROUP -> TaskStatus.GROUPING;
            case RESERVE -> TaskStatus.RESERVING;
            default -> TaskStatus.PENDING;
        };
    }

    private static String statusLabel(TaskStatus status, TaskMode mode) {
        if (mode == TaskMode.EMERGENCY && status == TaskStatus.PENDING) {
            return "紧急";
        }
        return switch (status) {
            case GROUPING -> "拼单中";
            case RESERVING -> "预约中";
            case PENDING -> "待接单";
            default -> status.name();
        };
    }

    private static String categoryLabel(TaskCategory category) {
        return switch (category) {
            case EXPRESS -> "代取快递";
            case BUY -> "代买物品";
            case ERRAND -> "代办事务";
            case OTHER -> "其他";
        };
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void seedGroups() {
        groupMembers.put("h2", defaultGroupMembers());
        groupMembers.put("pool1", defaultGroupMembers());
    }

    private List<GroupMemberEntry> defaultGroupMembers() {
        return new ArrayList<>(List.of(
                new GroupMemberEntry("m1", null, "李同学", GroupMemberDto.Role.CREATOR,
                        "宿舍楼 3 栋", GROUP_SHARE),
                new GroupMemberEntry("m2", null, "王同学", GroupMemberDto.Role.MEMBER,
                        "宿舍楼 5 栋", GROUP_SHARE),
                GroupMemberEntry.emptySlot()
        ));
    }

    private TaskMockRecord requireGroupTask(String taskId) {
        TaskMockRecord task = requireTask(taskId);
        if (task.getMode() != TaskMode.GROUP) {
            throw BusinessException.of(ErrorCodes.VALIDATION, "该任务不是拼单");
        }
        return task;
    }

    private GroupOrderDetailDto toGroupDetail(TaskMockRecord task,
                                              List<GroupMemberEntry> members,
                                              long userId) {
        int joined = (int) members.stream()
                .filter(m -> m.getRole() != GroupMemberDto.Role.EMPTY_SLOT)
                .count();
        boolean full = joined >= GROUP_MAX_MEMBERS;
        boolean viewerJoined = members.stream()
                .anyMatch(m -> m.getUserId() != null && m.getUserId() == userId);
        boolean viewerIsCreator = task.getPublisherId() == userId;
        List<GroupMemberDto> dtoMembers = members.stream()
                .map(m -> GroupMemberDto.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .role(m.getRole())
                        .addressSummary(m.getAddressSummary())
                        .paidAmount(m.getPaidAmount())
                        .joined(m.getRole() != GroupMemberDto.Role.EMPTY_SLOT)
                        .build())
                .toList();
        return GroupOrderDetailDto.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .categoryLabel(categoryLabel(task.getCategory()))
                .pickupAddress(nullToEmpty(task.getPickupAddress()))
                .deliverySummary(nullToEmpty(task.getDeliveryAddress()))
                .totalReward(GROUP_TOTAL_REWARD)
                .sharePerPerson(GROUP_SHARE)
                .maxMembers(GROUP_MAX_MEMBERS)
                .joinedCount(joined)
                .timeLabel(nullToEmpty(task.getTimeLabel()))
                .viewerJoined(viewerJoined)
                .viewerIsCreator(viewerIsCreator)
                .full(full)
                .members(dtoMembers)
                .build();
    }
}
