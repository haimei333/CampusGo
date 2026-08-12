package com.campusgo.api.config;

import com.campusgo.application.wallet.WalletService;
import com.campusgo.domain.enums.TaskCategory;
import com.campusgo.domain.enums.TaskMode;
import com.campusgo.domain.enums.TaskStatus;
import com.campusgo.domain.model.Task;
import com.campusgo.domain.model.UserProfile;
import com.campusgo.domain.repository.TaskRepository;
import com.campusgo.domain.repository.UserRepository;
import com.campusgo.infrastructure.persistence.jpa.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 若无预约演示任务则写入一条 RESERVE 任务（由 13900139000 发布，13800138000 可占位）。
 */
@Slf4j
@Component
@Order(110)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campusgo.task.mock", havingValue = "false")
public class ReserveTaskSeeder implements ApplicationRunner {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (taskJpaRepository.findByModeAndStatusOrderByCreatedAtDesc(TaskMode.RESERVE, TaskStatus.RESERVING)
                .stream().findAny().isPresent()) {
            return;
        }
        UserProfile publisher = userRepository.findByPhone("13900139000").orElse(null);
        if (publisher == null) {
            return;
        }
        Task reserve = taskRepository.save(Task.builder()
                .taskNo("CG" + taskRepository.nextTaskNoSeq())
                .publisherId(publisher.getId())
                .mode(TaskMode.RESERVE)
                .category(TaskCategory.BUY)
                .title("代购生活用品")
                .description("洗衣液、纸巾 · 预约任务演示")
                .status(TaskStatus.RESERVING)
                .pickupName("校园超市")
                .dropoffName("6号楼")
                .timeLabel("明日 18:00")
                .rewardCent(1500)
                .baseRewardCent(1500)
                .escrowCent(1500)
                .build());
        walletService.hold(publisher.getId(), 1500, reserve.getId(), "演示预约托管 - " + reserve.getTitle());
        log.info("Seeded demo reserve task id={} publisher={}", reserve.getId(), publisher.getPhone());
    }
}
