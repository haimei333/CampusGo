package com.campusgo.data.mock;

import android.content.Context;

import androidx.annotation.NonNull;

import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;

import java.util.ArrayList;
import java.util.List;

/**
 * M02 演示数据（对齐原型 tasks.html）
 */
public final class MockTaskRepository {

    private MockTaskRepository() {
    }

    @NonNull
    public static List<TaskListItem> all(@NonNull Context context) {
        List<TaskListItem> items = new ArrayList<>();

        // 任务大厅
        items.add(new TaskListItem("h1", TaskListItem.Tab.HALL, null,
                "取快递 - 菜鸟驿站", "普通", "请帮忙取一个中通快递，小件", "¥5.00",
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.PENDING, TaskListItem.NavTarget.T06));
        items.add(new TaskListItem("h3", TaskListItem.Tab.HALL, null,
                "送文件到行政楼", "紧急", "一份密封文件，需当面交接", "¥22.50",
                TaskCategory.ERRAND, TaskMode.EMERGENCY, TaskStatus.PENDING, TaskListItem.NavTarget.T06));
        items.add(new TaskListItem("h4", TaskListItem.Tab.HALL, null,
                "代购生活用品", "预约", "洗衣液、纸巾 · 明日 18:00", "¥15.00",
                TaskCategory.BUY, TaskMode.RESERVE, TaskStatus.RESERVING, TaskListItem.NavTarget.T06));
        items.add(new TaskListItem("h5", TaskListItem.Tab.HALL, null,
                "拼单满员待抢", "拼单·待抢", "已满 3/3，跑腿员可抢单", "¥18.00",
                TaskCategory.BUY, TaskMode.GROUP, TaskStatus.PENDING, TaskListItem.NavTarget.T06));

        // 我的发布（草稿来自本地持久化）
        items.addAll(MockPublishDraftRepository.draftListItems(context));
        items.add(new TaskListItem("p2", TaskListItem.Tab.MINE_PUBLISH, null,
                "取快递 - 中通", "待接单", "已发布 30 分钟无人接 · 可加价", "¥15.00",
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.PENDING, TaskListItem.NavTarget.T06));
        items.add(new TaskListItem("p3", TaskListItem.Tab.MINE_PUBLISH, null,
                "拼单奶茶", "拼单中", "2/3 已加入", "¥30.00",
                TaskCategory.BUY, TaskMode.GROUP, TaskStatus.GROUPING, TaskListItem.NavTarget.T07));
        items.add(new TaskListItem("done1", TaskListItem.Tab.MINE_PUBLISH, null,
                "取快递 - 中通", "已完成", "配送已完成，点击去评价", "¥15.00",
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.COMPLETED, TaskListItem.NavTarget.T06));

        // 我的接单
        items.add(new TaskListItem("t1", TaskListItem.Tab.MINE_TAKE, null,
                "取快递 - 中通", "配送中", "正在前往送达点", "¥15.00",
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.DELIVERING, TaskListItem.NavTarget.T06));
        items.add(new TaskListItem("t2", TaskListItem.Tab.MINE_TAKE, null,
                "送文件", "待确认", "已拍照，等待发布者确认", "¥22.50",
                TaskCategory.ERRAND, TaskMode.EMERGENCY, TaskStatus.CONFIRMING, TaskListItem.NavTarget.T06));
        items.add(new TaskListItem("done1", TaskListItem.Tab.MINE_TAKE, null,
                "取快递 - 中通", "已完成", "配送已完成，点击去评价", "¥15.00",
                TaskCategory.EXPRESS, TaskMode.NORMAL, TaskStatus.COMPLETED, TaskListItem.NavTarget.T06));

        // 拼单池
        items.add(new TaskListItem("h2", TaskListItem.Tab.POOL, null,
                "代买奶茶+零食", "差1人", "蜜雪冰城 · 未满员，均摊拼单", "¥12.00",
                TaskCategory.BUY, TaskMode.GROUP, TaskStatus.GROUPING, TaskListItem.NavTarget.T07));
        items.add(new TaskListItem("pool1", TaskListItem.Tab.POOL, null,
                "代买奶茶拼单", "差1人", "均摊 ¥10/人", "¥10.00",
                TaskCategory.BUY, TaskMode.GROUP, TaskStatus.GROUPING, TaskListItem.NavTarget.T07));

        // 我的预约
        items.add(new TaskListItem("r1", TaskListItem.Tab.RESERVE, UserRole.PUBLISHER,
                "代购日用品", "预约中", "我发起的预约", "¥15.00",
                TaskCategory.BUY, TaskMode.RESERVE, TaskStatus.RESERVING, TaskListItem.NavTarget.T06));
        items.add(new TaskListItem("r2", TaskListItem.Tab.RESERVE, UserRole.RUNNER,
                "代购日用品", "已占位", "我占位的预约", "¥15.00",
                TaskCategory.BUY, TaskMode.RESERVE, TaskStatus.RESERVING, TaskListItem.NavTarget.T06));

        return items;
    }

    @NonNull
    public static List<TaskListItem> forTab(@NonNull Context context,
                                            @NonNull TaskListItem.Tab tab,
                                            @NonNull UserRole role) {
        List<TaskListItem> result = new ArrayList<>();
        for (TaskListItem item : all(context)) {
            if (item.tab != tab) {
                continue;
            }
            if (tab == TaskListItem.Tab.RESERVE
                    && item.reserveForRole != null
                    && item.reserveForRole != role) {
                continue;
            }
            result.add(item);
        }
        return result;
    }
}
