package com.campusgo.api.config;

import com.campusgo.application.wallet.WalletService;
import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.TaskRepository;
import com.campusgo.domain.repository.UserRepository;
import com.campusgo.domain.repository.WalletLedgerRepository;
import com.campusgo.domain.repository.WalletRepository;
import com.campusgo.infrastructure.persistence.jpa.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campusgo.task.mock", havingValue = "false")
public class TaskDataSeeder implements ApplicationRunner {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletLedgerRepository walletLedgerRepository;
    private final WalletService walletService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (taskJpaRepository.count() > 0) {
            return;
        }
        UserProfile publisher = userRepository.findByPhone("13800138000")
                .orElseGet(() -> userRepository.createUser("13800138000", "用户8000", "123456"));
        UserProfile otherPublisher = userRepository.findByPhone("13900139000")
                .orElseGet(() -> userRepository.createUser("13900139000", "同学9000", "123456"));
        walletRepository.initWallet(publisher.getId());
        walletRepository.initWallet(otherPublisher.getId());
        long uid = publisher.getId();
        long otherUid = otherPublisher.getId();

        Task h1 = taskRepository.save(Task.builder()
                .taskNo("CG20240803001")
                .publisherId(otherUid)
                .mode(TaskMode.NORMAL)
                .category(TaskCategory.EXPRESS)
                .title("取快递 - 菜鸟驿站")
                .description("请帮忙取一个中通快递，小件")
                .status(TaskStatus.PENDING)
                .pickupName("菜鸟驿站")
                .dropoffName("6号楼下")
                .timeLabel("今天 18:00 前")
                .rewardCent(500)
                .baseRewardCent(500)
                .escrowCent(500)
                .build());

        Task h3 = taskRepository.save(Task.builder()
                .taskNo("CG20240803002")
                .publisherId(otherUid)
                .mode(TaskMode.EMERGENCY)
                .category(TaskCategory.ERRAND)
                .title("送文件到行政楼")
                .description("一份密封文件，需当面交接")
                .status(TaskStatus.PENDING)
                .pickupName("图书馆")
                .dropoffName("行政楼")
                .timeLabel("尽快")
                .rewardCent(2250)
                .baseRewardCent(1500)
                .escrowCent(2250)
                .build());

        Task h5 = taskRepository.save(Task.builder()
                .taskNo("CG20240803003")
                .publisherId(otherUid)
                .mode(TaskMode.GROUP)
                .category(TaskCategory.BUY)
                .title("拼单满员待抢")
                .description("已满 3/3，跑腿员可抢单")
                .status(TaskStatus.PENDING)
                .pickupName("蜜雪冰城")
                .dropoffName("各宿舍")
                .timeLabel("尽快")
                .rewardCent(1800)
                .baseRewardCent(1800)
                .escrowCent(1800)
                .groupTargetCount(3)
                .groupJoinedCount(3)
                .build());

        Task pool1 = taskRepository.save(Task.builder()
                .taskNo("CG20240803004")
                .publisherId(otherUid)
                .mode(TaskMode.GROUP)
                .category(TaskCategory.BUY)
                .title("代买奶茶拼单")
                .description("均摊 ¥10/人")
                .status(TaskStatus.GROUPING)
                .pickupName("蜜雪冰城")
                .dropoffName("各宿舍")
                .timeLabel("今晚")
                .rewardCent(3000)
                .baseRewardCent(3000)
                .escrowCent(3000)
                .groupTargetCount(3)
                .groupJoinedCount(2)
                .build());

        Task h2 = taskRepository.save(Task.builder()
                .taskNo("CG20240803005")
                .publisherId(uid)
                .mode(TaskMode.GROUP)
                .category(TaskCategory.BUY)
                .title("代买奶茶+零食")
                .description("蜜雪冰城 · 未满员，均摊拼单")
                .status(TaskStatus.GROUPING)
                .pickupName("蜜雪冰城")
                .dropoffName("各宿舍")
                .timeLabel("1 小时内")
                .rewardCent(3000)
                .baseRewardCent(3000)
                .escrowCent(3000)
                .groupTargetCount(3)
                .groupJoinedCount(2)
                .build());

        Task p2 = taskRepository.save(Task.builder()
                .taskNo("CG20240803006")
                .publisherId(uid)
                .mode(TaskMode.NORMAL)
                .category(TaskCategory.EXPRESS)
                .title("取快递 - 中通")
                .description("已发布 30 分钟无人接 · 可加价")
                .status(TaskStatus.PENDING)
                .pickupName("中通快递点")
                .dropoffName("6号楼")
                .timeLabel("今天")
                .rewardCent(1500)
                .baseRewardCent(1500)
                .escrowCent(1500)
                .build());

        // 拼单发起人用 otherUid，演示账号 13800138000 可加入拼单
        seedGroup(pool1.getId(), otherUid);
        seedGroup(h2.getId(), otherUid);
        seedGroupFull(h5.getId(), otherUid);

        holdSeedEscrow(h1);
        holdSeedEscrow(h3);
        holdSeedEscrow(h5);
        holdSeedEscrow(pool1);
        holdSeedEscrow(h2);
        holdSeedEscrow(p2);

        int bal = (int) walletRepository.getByUserId(uid).getBalanceCent();
        walletLedgerRepository.append(uid, "INCOME", 1500, "IN", bal, null, "任务完成 - 取快递");
        log.info("Seeded persistent demo tasks: hall={},{},{} pool={},{} mine={}",
                h1.getId(), h3.getId(), h5.getId(), pool1.getId(), h2.getId(), p2.getId());
    }

    private void holdSeedEscrow(Task task) {
        if (task.getEscrowCent() <= 0) {
            return;
        }
        walletService.hold(task.getPublisherId(), task.getEscrowCent(), task.getId(),
                "演示任务托管 - " + task.getTitle());
    }

    private void seedGroup(long taskId, long creatorId) {
        taskRepository.replaceGroupMembers(taskId, java.util.List.of(
                GroupMember.builder().userId(creatorId).name("李同学").role("CREATOR")
                        .addressSummary("宿舍楼 3 栋").shareCent(1000).payStatus("PAID").build(),
                GroupMember.builder().userId(null).name("王同学").role("MEMBER")
                        .addressSummary("宿舍楼 5 栋").shareCent(1000).payStatus("PAID").build()
        ));
    }

    private void seedGroupFull(long taskId, long creatorId) {
        taskRepository.replaceGroupMembers(taskId, java.util.List.of(
                GroupMember.builder().userId(creatorId).name("李同学").role("CREATOR")
                        .addressSummary("宿舍楼 3 栋").shareCent(600).payStatus("PAID").build(),
                GroupMember.builder().userId(null).name("王同学").role("MEMBER")
                        .addressSummary("宿舍楼 5 栋").shareCent(600).payStatus("PAID").build(),
                GroupMember.builder().userId(null).name("赵同学").role("MEMBER")
                        .addressSummary("宿舍楼 8 栋").shareCent(600).payStatus("PAID").build()
        ));
    }
}
