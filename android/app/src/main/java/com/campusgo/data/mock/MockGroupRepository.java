package com.campusgo.data.mock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.GroupOrderDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * T07 拼单演示数据
 */
public final class MockGroupRepository {

    private static final Map<String, GroupOrderDetail> JOINED = new HashMap<>();

    private MockGroupRepository() {
    }

    @Nullable
    public static GroupOrderDetail findByTaskId(@NonNull String taskId) {
        GroupOrderDetail joined = JOINED.get(taskId);
        if (joined != null) {
            return joined;
        }
        return baseDetail(taskId);
    }

    public static boolean join(@NonNull String taskId,
                               @NonNull String memberName,
                               @NonNull String address,
                               double shareAmount) {
        GroupOrderDetail base = baseDetail(taskId);
        if (base == null || base.full || base.viewerJoined || base.viewerIsCreator) {
            return false;
        }
        List<GroupMember> members = new ArrayList<>();
        boolean slotFilled = false;
        for (GroupMember member : base.members) {
            if (!slotFilled && member.role == GroupMember.Role.EMPTY_SLOT) {
                members.add(new GroupMember(
                        "m-" + System.currentTimeMillis(),
                        memberName,
                        GroupMember.Role.MEMBER,
                        address,
                        shareAmount,
                        true));
                slotFilled = true;
            } else if (member.role != GroupMember.Role.EMPTY_SLOT) {
                members.add(member);
            }
        }
        if (!slotFilled) {
            return false;
        }
        int joinedCount = base.joinedCount + 1;
        boolean full = joinedCount >= base.maxMembers;
        JOINED.put(taskId, new GroupOrderDetail(
                base.taskId,
                base.title,
                base.categoryLabel,
                base.pickupAddress,
                base.deliverySummary,
                base.totalReward,
                base.sharePerPerson,
                base.maxMembers,
                joinedCount,
                base.timeLabel,
                true,
                base.viewerIsCreator,
                full,
                members));
        return true;
    }

    public static void leave(@NonNull String taskId) {
        JOINED.remove(taskId);
    }

    @Nullable
    private static GroupOrderDetail baseDetail(@NonNull String taskId) {
        switch (taskId) {
            case "h2":
            case "p3":
            case "pool1":
                return milkTeaGroup(taskId);
            default:
                return null;
        }
    }

    @NonNull
    private static GroupOrderDetail milkTeaGroup(@NonNull String taskId) {
        List<GroupMember> members = new ArrayList<>(Arrays.asList(
                new GroupMember("m1", "李同学", GroupMember.Role.CREATOR,
                        "宿舍楼 3 栋", 10.0, true),
                new GroupMember("m2", "王同学", GroupMember.Role.MEMBER,
                        "宿舍楼 5 栋", 10.0, true),
                GroupMember.emptySlot()
        ));
        boolean full = "h5".equals(taskId);
        if (full) {
            members = new ArrayList<>(Arrays.asList(
                    new GroupMember("m1", "李同学", GroupMember.Role.CREATOR,
                            "宿舍楼 3 栋", 10.0, true),
                    new GroupMember("m2", "王同学", GroupMember.Role.MEMBER,
                            "宿舍楼 5 栋", 10.0, true),
                    new GroupMember("m3", "赵同学", GroupMember.Role.MEMBER,
                            "宿舍楼 8 栋", 10.0, true)
            ));
        }
        int joined = full ? 3 : 2;
        return new GroupOrderDetail(
                taskId,
                "代买奶茶拼单",
                "代买奶茶",
                "蜜雪冰城 · 二食堂",
                "各成员地址",
                30.0,
                10.0,
                3,
                joined,
                "尽快送达",
                false,
                "p3".equals(taskId),
                full,
                members
        );
    }
}
