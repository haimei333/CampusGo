package com.campusgo.data.mock;

import androidx.annotation.NonNull;

import com.campusgo.domain.model.AppNotification;
import com.campusgo.domain.model.ChatThread;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;

import java.util.ArrayList;
import java.util.List;

/**
 * M03 演示数据：聊天列表按当前身份展示不同会话
 */
public final class MockMessageRepository {

    private static final List<AppNotification> NOTIFICATIONS = new ArrayList<>();

    static {
        NOTIFICATIONS.add(new AppNotification("n1", "您的任务已被接单",
                "小明接取了您的快递代取任务", "10分钟前", true,
                AppNotification.LinkType.TASK, "p2", TaskStatus.ACCEPTED, TaskMode.NORMAL,
                null, null));
        NOTIFICATIONS.add(new AppNotification("n2", "订单已完成",
                "您的奶茶代购任务已完成，请确认收货", "30分钟前", true,
                AppNotification.LinkType.TASK, "done1", TaskStatus.COMPLETED, TaskMode.NORMAL,
                null, null));
        NOTIFICATIONS.add(new AppNotification("n3", "新消息提醒",
                "小红给您发来了一条新消息", "1小时前", true,
                AppNotification.LinkType.CHAT, "c1", null, null,
                "小明", "取快递 - 中通"));
        NOTIFICATIONS.add(new AppNotification("n4", "拼单即将满员",
                "代买奶茶拼单还差 1 人，快去看看", "2小时前", true,
                AppNotification.LinkType.GROUP, "pool1", null, null,
                null, null));
        NOTIFICATIONS.add(new AppNotification("n5", "系统更新通知",
                "CampusGo 已更新到 v2.1，新增校园互助功能", "昨天", false,
                AppNotification.LinkType.HELP, null, null, null,
                null, null));
    }

    private MockMessageRepository() {
    }

    /** 发布者视角：主要与接单跑腿员沟通 */
    @NonNull
    public static List<ChatThread> chatsForPublisher() {
        List<ChatThread> list = new ArrayList<>();
        list.add(new ChatThread("c1", "小明", UserRole.RUNNER,
                "好的，我马上到菜鸟驿站取件", "刚刚", 2, "取快递 - 中通"));
        list.add(new ChatThread("c2", "小李", UserRole.RUNNER,
                "文件已经送到行政楼3楼了", "30分钟前", 1, "送文件 - 行政楼"));
        list.add(new ChatThread("c3", "系统通知", null,
                "您的任务已被接单", "10分钟前", 0, null));
        list.add(new ChatThread("c6", "跑腿小助手", null,
                "欢迎使用 CampusGo！查看新手教程", "昨天", 0, null));
        return list;
    }

    /** 跑腿员视角：主要与发单发布者沟通 */
    @NonNull
    public static List<ChatThread> chatsForRunner() {
        List<ChatThread> list = new ArrayList<>();
        list.add(new ChatThread("c2", "小红", UserRole.PUBLISHER,
                "奶茶已经买好了，你在哪个宿舍？", "5分钟前", 0, "代买奶茶 - 食堂"));
        list.add(new ChatThread("c5", "小张", UserRole.PUBLISHER,
                "能帮我顺便带个快递吗？", "1小时前", 0, "取快递 - 菜鸟驿站"));
        list.add(new ChatThread("c4", "小林", UserRole.PUBLISHER,
                "麻烦尽快送到，谢谢", "2小时前", 1, "打印开题报告"));
        list.add(new ChatThread("c3", "系统通知", null,
                "您有新的可接任务推荐", "10分钟前", 0, null));
        list.add(new ChatThread("c6", "跑腿小助手", null,
                "接单前请确认已完成校园卡认证", "昨天", 0, null));
        return list;
    }

    @NonNull
    public static List<ChatThread> chats(@NonNull UserRole viewerRole) {
        return viewerRole == UserRole.PUBLISHER ? chatsForPublisher() : chatsForRunner();
    }

    @NonNull
    public static List<AppNotification> notifications() {
        return new ArrayList<>(NOTIFICATIONS);
    }

    public static int chatUnreadTotal(@NonNull UserRole viewerRole) {
        int total = 0;
        for (ChatThread c : chats(viewerRole)) {
            total += c.unreadCount;
        }
        return total;
    }

    public static int notifyUnreadTotal() {
        int total = 0;
        for (AppNotification n : NOTIFICATIONS) {
            if (n.unread) {
                total++;
            }
        }
        return total;
    }

    public static void markAllNotificationsRead() {
        for (AppNotification n : NOTIFICATIONS) {
            n.unread = false;
        }
    }
}
