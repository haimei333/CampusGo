package com.campusgo.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockTaskRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.points.CheckInStatusDto;
import com.campusgo.data.remote.mapper.TaskDtoMapper;
import com.campusgo.databinding.FragmentHomeBinding;
import com.campusgo.databinding.ItemHomeRunnerTaskBinding;
import com.campusgo.domain.model.RecommendTask;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.main.MainActivity;
import com.campusgo.ui.task.TaskNavigator;
import com.campusgo.ui.profile.ProfileNavigator;
import com.campusgo.ui.wallet.WalletNavigator;
import com.campusgo.ui.ai.AiChatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * M01 首页：发布者 / 跑腿员双变体（对齐原型 home-publisher / home-runner）
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager sessionManager;
    private final List<RecommendTask> recommendTasks = new ArrayList<>();
    private final Map<String, TaskListItem> hallTaskItems = new HashMap<>();
    private boolean loadingRecommend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = ((CampusGoApp) requireActivity().getApplication()).getSessionManager();

        binding.btnRolePublisher.setOnClickListener(v -> switchRole(UserRole.PUBLISHER));
        binding.btnRoleRunner.setOnClickListener(v -> trySwitchToRunner());
        binding.cardCheckIn.setOnClickListener(v -> startActivity(WalletNavigator.checkIn(requireContext())));
        binding.cardAiAssistant.setOnClickListener(v -> openAiAssistant());
        binding.btnAvatar.setOnClickListener(v -> goProfileTab());
        binding.bannerAuth.setOnClickListener(v -> openVerify());
        binding.cardOngoing.setOnClickListener(v -> openOngoingTask());

        binding.quickExpress.setOnClickListener(v -> openPublish(TaskCategory.EXPRESS));
        binding.quickFood.setOnClickListener(v -> openPublish(TaskCategory.BUY));
        binding.quickFile.setOnClickListener(v -> openPublish(TaskCategory.ERRAND));
        binding.quickOther.setOnClickListener(v -> startActivity(TaskNavigator.publish(requireContext())));

        binding.cardHeat.setOnClickListener(v -> openHeatmap());
        binding.btnHeatMapAll.setOnClickListener(v -> openHeatmap());
        binding.btnRecommendMore.setOnClickListener(v -> goTasksTab());
        binding.btnGoHall.setOnClickListener(v -> goTasksTab());

        renderRole(sessionManager.getActiveRole());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        refreshHomeState();
    }

    private void refreshHomeState() {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().pointsRemote().loadCheckInStatus(new ApiCallback<CheckInStatusDto>() {
                @Override
                public void onSuccess(@NonNull CheckInStatusDto data) {
                    if (binding == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        updateCheckInUi();
                        renderRole(sessionManager.getActiveRole());
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    if (binding == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        updateCheckInUi();
                        renderRole(sessionManager.getActiveRole());
                    });
                }
            });
        } else {
            updateCheckInUi();
            renderRole(sessionManager.getActiveRole());
        }
    }

    private void updateCheckInUi() {
        int streak = sessionManager.getCheckInStreak();
        if (sessionManager.isCheckedInToday()) {
            binding.tvCheckInSub.setText(R.string.checkin_done);
            binding.tvCheckInReward.setText(R.string.home_checkin_reward_done);
        } else {
            binding.tvCheckInSub.setText(getString(R.string.home_checkin_sub_dynamic, streak));
            binding.tvCheckInReward.setText(R.string.home_checkin_reward);
        }
    }

    private void seedMockTasks() {
        recommendTasks.clear();
        hallTaskItems.clear();
        recommendTasks.add(new RecommendTask(
                "h1", "菜鸟驿站取件", "取快递", false,
                "¥5.00", 500, "今天 18:00", 50, 60,
                "10分钟前", "菜鸟驿站 → 宿舍楼 5栋 | 约500m", "小件",
                false, RecommendTask.CardStyle.EXPRESS));
        recommendTasks.add(new RecommendTask(
                "h2", "代买奶茶 - 食堂", "代买", false,
                "¥8.00", 300, "今天 12:30", 80, 70,
                "刚刚", "二食堂 → 教学楼 A座 | 约300m", "轻件",
                true, RecommendTask.CardStyle.BUY));
        recommendTasks.add(new RecommendTask(
                "h3", "送文件 - 行政楼", "配送", false,
                "¥6.00", 800, "今天 16:00", 60, 50,
                "30分钟前", "图书馆 → 行政楼 3层 | 约800m", "文件",
                false, RecommendTask.CardStyle.FILE));
    }

    private void trySwitchToRunner() {
        SessionManager.SwitchRoleResult result = sessionManager.canSwitchToRunner();
        switch (result) {
            case NEED_VERIFY:
                new AlertDialog.Builder(requireContext())
                        .setTitle("需要校园卡认证")
                        .setMessage("完成校园卡认证后即可切换为跑腿员接单。")
                        .setPositiveButton("去认证", (d, w) -> openVerify())
                        .setNegativeButton("取消", null)
                        .show();
                break;
            case CREDIT_BLOCKED:
                new AlertDialog.Builder(requireContext())
                        .setTitle("信用分不足")
                        .setMessage("信用分低于 400，暂不可接单。可在帮助中心了解信用规则。")
                        .setPositiveButton(R.string.help_view,
                                (d, w) -> startActivity(ProfileNavigator.help(requireContext())))
                        .setNegativeButton("取消", null)
                        .show();
                break;
            case ALLOWED:
                switchRole(UserRole.RUNNER);
                break;
        }
    }

    private void openVerify() {
        startActivity(WalletNavigator.verify(requireContext()));
    }

    private void switchRole(@NonNull UserRole role) {
        if (FeatureFlags.USE_REMOTE_API) {
            binding.btnRolePublisher.setEnabled(false);
            binding.btnRoleRunner.setEnabled(false);
            RetrofitClient.get().userRemote().switchRole(role, new ApiCallback<>() {
                @Override
                public void onSuccess(@NonNull Void data) {
                    if (binding == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        binding.btnRolePublisher.setEnabled(true);
                        binding.btnRoleRunner.setEnabled(true);
                        renderRole(sessionManager.getActiveRole());
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    if (binding == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        binding.btnRolePublisher.setEnabled(true);
                        binding.btnRoleRunner.setEnabled(true);
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        renderRole(sessionManager.getActiveRole());
                    });
                }
            });
            return;
        }
        sessionManager.setActiveRole(role);
        renderRole(role);
    }

    private void renderRole(@NonNull UserRole role) {
        boolean publisher = role == UserRole.PUBLISHER;
        String nick = sessionManager.getNickname();
        String initial = nick.isEmpty() ? "跑" : nick.substring(0, 1).toUpperCase();

        binding.tvGreeting.setText(publisher
                ? getString(R.string.home_greeting_publisher)
                : getString(R.string.home_greeting_runner_simple));
        binding.tvGreetingSub.setText(publisher
                ? R.string.home_sub_publisher
                : R.string.home_sub_runner);

        binding.tvRunnerAvatar.setVisibility(publisher ? View.GONE : View.VISIBLE);
        binding.tvRunnerAvatar.setText(initial);
        binding.panelEarnings.setVisibility(publisher ? View.GONE : View.VISIBLE);
        binding.btnAvatar.setVisibility(publisher ? View.VISIBLE : View.GONE);

        binding.panelPublisher.setVisibility(publisher ? View.VISIBLE : View.GONE);
        binding.panelRunner.setVisibility(publisher ? View.GONE : View.VISIBLE);
        binding.bannerAuth.setVisibility(sessionManager.isCampusVerified() ? View.GONE : View.VISIBLE);

        styleRoleSwitch(publisher);

        if (!publisher) {
            loadRecommendTasks();
        }
    }

    private void loadRecommendTasks() {
        if (FeatureFlags.USE_REMOTE_API) {
            if (loadingRecommend) {
                return;
            }
            loadingRecommend = true;
            RetrofitClient.get().taskRemote().loadHall(1, new ApiCallback<List<TaskListItem>>() {
                @Override
                public void onSuccess(@NonNull List<TaskListItem> data) {
                    if (binding == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        loadingRecommend = false;
                        hallTaskItems.clear();
                        for (TaskListItem item : data) {
                            hallTaskItems.put(item.id, item);
                        }
                        recommendTasks.clear();
                        recommendTasks.addAll(TaskDtoMapper.toRecommendTasks(data, 3));
                        renderTaskList();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    if (binding == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        loadingRecommend = false;
                        renderTaskList();
                    });
                }
            });
            return;
        }
        seedMockTasks();
        renderTaskList();
    }

    private void styleRoleSwitch(boolean publisher) {
        int brand = ContextCompat.getColor(requireContext(), R.color.cg_brand);
        int onBrand = ContextCompat.getColor(requireContext(), R.color.cg_text_on_brand);
        int secondary = ContextCompat.getColor(requireContext(), R.color.cg_text_secondary);
        int input = ContextCompat.getColor(requireContext(), R.color.cg_bg_input);

        binding.btnRolePublisher.setSelected(publisher);
        binding.btnRoleRunner.setSelected(!publisher);
        binding.btnRolePublisher.setTextColor(publisher ? onBrand : secondary);
        binding.btnRoleRunner.setTextColor(publisher ? secondary : onBrand);
        binding.btnRolePublisher.setBackgroundColor(publisher ? brand : input);
        binding.btnRoleRunner.setBackgroundColor(publisher ? input : brand);
    }

    private void renderTaskList() {
        binding.taskList.removeAllViews();
        boolean empty = recommendTasks.isEmpty();
        binding.emptyRecommend.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.taskList.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        for (RecommendTask task : recommendTasks) {
            ItemHomeRunnerTaskBinding card = ItemHomeRunnerTaskBinding.inflate(
                    inflater, binding.taskList, false);
            bindRunnerCard(card, task);
            card.getRoot().setOnClickListener(v -> openRecommendTask(task));
            binding.taskList.addView(card.getRoot());
        }
    }

    private void bindRunnerCard(@NonNull ItemHomeRunnerTaskBinding card, @NonNull RecommendTask task) {
        card.tvTitle.setText(task.title);
        card.tvPrice.setText(task.priceLabel);
        card.tvCategory.setText(task.category);
        card.tvTimeAgo.setText(task.timeAgo);
        card.tvRoute.setText(task.routeSummary);
        card.tvSizeLabel.setText(task.sizeLabel);

        int iconBg;
        int iconColor;
        int tagBg;
        int tagColor;
        String iconText;
        switch (task.cardStyle) {
            case BUY:
                iconBg = R.drawable.bg_icon_accent;
                iconColor = R.color.cg_accent;
                tagBg = R.drawable.bg_tag_buy;
                tagColor = R.color.cg_accent;
                iconText = "买";
                break;
            case FILE:
                iconBg = R.drawable.bg_icon_blue;
                iconColor = R.color.cg_blue;
                tagBg = R.drawable.bg_tag_delivery;
                tagColor = R.color.cg_tag_reserve_text;
                iconText = "文";
                break;
            case EXPRESS:
            default:
                iconBg = R.drawable.bg_icon_soft;
                iconColor = R.color.cg_brand;
                tagBg = R.drawable.bg_tag_group;
                tagColor = R.color.cg_tag_group_text;
                iconText = "取";
                break;
        }
        card.tvIcon.setBackgroundResource(iconBg);
        card.tvIcon.setTextColor(ContextCompat.getColor(requireContext(), iconColor));
        card.tvIcon.setText(iconText);
        card.tvCategory.setBackgroundResource(tagBg);
        card.tvCategory.setTextColor(ContextCompat.getColor(requireContext(), tagColor));
    }

    private void openRecommendTask(@NonNull RecommendTask task) {
        if (task.groupOrder) {
            startActivity(TaskNavigator.groupDetail(requireContext(), task.id));
            return;
        }
        startActivity(TaskNavigator.taskDetailFromItem(requireContext(),
                findListItem(task.id), UserRole.RUNNER));
    }

    @NonNull
    private TaskListItem findListItem(@NonNull String id) {
        TaskListItem cached = hallTaskItems.get(id);
        if (cached != null) {
            return cached;
        }
        for (TaskListItem item : MockTaskRepository.all(requireContext())) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return MockTaskRepository.all(requireContext()).get(0);
    }

    private void openPublish(@NonNull TaskCategory category) {
        startActivity(TaskNavigator.publishWithCategory(requireContext(), category));
    }

    private void openOngoingTask() {
        startActivity(TaskNavigator.taskDetail(requireContext(), "t1", UserRole.PUBLISHER)
                .putExtra(TaskNavigator.EXTRA_STATUS, TaskStatus.DELIVERING.name())
                .putExtra(TaskNavigator.EXTRA_MODE, TaskMode.NORMAL.name()));
    }

    private void goProfileTab() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).selectTab(R.id.profileFragment);
        }
    }

    private void goTasksTab() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).selectTab(R.id.tasksFragment);
        }
    }

    private void openHeatmap() {
        startActivity(TaskNavigator.heatmap(requireContext()));
    }

    private void openAiAssistant() {
        startActivity(new Intent(requireContext(), AiChatActivity.class));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
