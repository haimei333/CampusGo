package com.campusgo.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockTaskDetailRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.TaskRemoteDataSource;
import com.campusgo.databinding.ActivityTaskDetailBinding;
import com.campusgo.databinding.ItemTimelineStepBinding;
import com.campusgo.domain.model.TaskDetail;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.chat.ChatNavigator;
import com.campusgo.ui.wallet.WalletNavigator;

/**
 * T06 任务详情 — 多状态底栏（MVP 核心状态）
 */
public class TaskDetailActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private SessionManager sessionManager;
    private TaskDetail task;
    @Nullable
    private String taskId;
    private UserRole viewerRole;
    private boolean grabbing;
    private boolean reserveBusy;
    private boolean loadingRemote;

    private final ActivityResultLauncher<Intent> photoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (FeatureFlags.USE_REMOTE_API && taskId != null) {
                        remoteAction(remote -> remote.uploadPhoto(taskId, "mock://photo", new ApiCallback<TaskDetail>() {
                            @Override
                            public void onSuccess(@NonNull TaskDetail data) {
                                runOnUiThread(() -> applyTask(data));
                            }

                            @Override
                            public void onError(@NonNull ApiException error) {
                                runOnUiThread(() ->
                                        Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }));
                    } else {
                        task.status = TaskStatus.CONFIRMING;
                        renderAll();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> reviewLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String statusRaw = result.getData().getStringExtra(TaskNavigator.EXTRA_STATUS);
                    if (statusRaw != null) {
                        try {
                            task.status = TaskStatus.valueOf(statusRaw);
                        } catch (Exception ignored) {
                        }
                    }
                    renderAll();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnShare.setOnClickListener(v -> TaskShareHelper.share(this, task));
        binding.btnChat.setOnClickListener(v -> openChat());

        taskId = getIntent().getStringExtra(TaskNavigator.EXTRA_TASK_ID);
        viewerRole = parseViewerRole();

        if (FeatureFlags.USE_REMOTE_API && taskId != null && !taskId.startsWith("new")) {
            task = placeholderTask(taskId);
            loadRemoteDetail();
        } else {
            task = loadTask();
            renderAll();
        }
    }

    @NonNull
    private TaskDetail placeholderTask(@NonNull String id) {
        return new TaskDetail(
                id, "…", "…", TaskMode.NORMAL, TaskStatus.PENDING,
                "…", "…", "…", "", 0, "…", "", 0f, 0, 0);
    }

    private void loadRemoteDetail() {
        loadingRemote = true;
        binding.bottomBar.setVisibility(View.GONE);
        renderAll();
        RetrofitClient.get().taskRemote().loadDetail(taskId, new ApiCallback<TaskDetail>() {
            @Override
            public void onSuccess(@NonNull TaskDetail data) {
                runOnUiThread(() -> {
                    loadingRemote = false;
                    applyTask(data);
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() -> {
                    loadingRemote = false;
                    Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void applyTask(@NonNull TaskDetail detail) {
        task = detail;
        renderAll();
    }

    @NonNull
    private TaskRemoteDataSource remote() {
        return RetrofitClient.get().taskRemote();
    }

    private void remoteAction(@NonNull RemoteAction action) {
        if (taskId == null) {
            return;
        }
        action.run(remote());
    }

    private interface RemoteAction {
        void run(@NonNull TaskRemoteDataSource remote);
    }

    @NonNull
    private TaskDetail loadTask() {
        String taskId = getIntent().getStringExtra(TaskNavigator.EXTRA_TASK_ID);
        if (taskId != null) {
            TaskDetail found = MockTaskDetailRepository.findById(this, taskId);
            if (found != null) {
                applyStatusOverride(found);
                return found;
            }
        }
        return buildFromPublishIntent();
    }

    @NonNull
    private TaskDetail buildFromPublishIntent() {
        String title = getIntent().getStringExtra(TaskNavigator.EXTRA_PUBLISH_TITLE);
        if (title == null) {
            return MockTaskDetailRepository.defaultTask();
        }
        TaskMode mode = TaskMode.NORMAL;
        try {
            String modeRaw = getIntent().getStringExtra(TaskNavigator.EXTRA_MODE);
            if (modeRaw != null) {
                mode = TaskMode.valueOf(modeRaw);
            }
        } catch (Exception ignored) {
        }
        double reward = getIntent().getDoubleExtra(TaskNavigator.EXTRA_PUBLISH_REWARD, 15);
        return new TaskDetail(
                "new",
                title,
                "代取快递",
                mode,
                TaskStatus.PENDING,
                "菜鸟驿站 · 东门",
                "宿舍楼 5栋",
                getString(R.string.publish_time_asap),
                "",
                reward,
                "20240725NEW",
                "张同学",
                4.8f,
                720,
                32);
    }

    private void applyStatusOverride(@NonNull TaskDetail detail) {
        String raw = getIntent().getStringExtra(TaskNavigator.EXTRA_STATUS);
        if (raw == null) {
            return;
        }
        try {
            detail.status = TaskStatus.valueOf(raw);
        } catch (Exception ignored) {
            detail.status = mapLegacyStatus(raw);
        }
        String modeRaw = getIntent().getStringExtra(TaskNavigator.EXTRA_MODE);
        if (modeRaw != null) {
            try {
                TaskMode mode = TaskMode.valueOf(modeRaw);
                // mode is final - detail already has mode from list item
            } catch (Exception ignored) {
            }
        }
    }

    @NonNull
    private TaskStatus mapLegacyStatus(@NonNull String raw) {
        switch (raw) {
            case "confirm":
                return TaskStatus.CONFIRMING;
            case "accepted":
                return TaskStatus.ACCEPTED;
            case "delivering":
                return TaskStatus.DELIVERING;
            default:
                return TaskStatus.PENDING;
        }
    }

    @NonNull
    private UserRole parseViewerRole() {
        String raw = getIntent().getStringExtra(TaskNavigator.EXTRA_VIEWER_ROLE);
        if (raw != null) {
            try {
                return UserRole.valueOf(raw);
            } catch (Exception ignored) {
            }
        }
        return sessionManager.getActiveRole();
    }

    private void renderAll() {
        renderTopBar();
        renderHeader();
        renderStatusBanner();
        renderRunnerPanel();
        renderPhotoPanel();
        renderTimeline();
        renderHint();
        renderBottomBar();
    }

    private void renderTopBar() {
        boolean canShare = viewerRole == UserRole.PUBLISHER && task.status == TaskStatus.PENDING;
        binding.btnShare.setVisibility(canShare ? View.VISIBLE : View.INVISIBLE);
    }

    private void renderHeader() {
        binding.tvCategoryTag.setText(task.categoryLabel);
        binding.tvEmergencyTag.setVisibility(task.isEmergency() ? View.VISIBLE : View.GONE);
        binding.tvOrderNo.setText(getString(R.string.task_detail_order, task.orderNo));
        binding.tvTaskTitle.setText(task.title);
        binding.tvPickup.setText(task.pickupAddress);
        binding.tvDelivery.setText(task.deliveryAddress);
        binding.tvReward.setText(task.formatReward());
        binding.tvTime.setText(task.timeLabel);
        binding.tvDescription.setText(task.description.isEmpty()
                ? getString(R.string.task_detail_no_desc) : task.description);
    }

    private void renderStatusBanner() {
        StatusUi ui = statusUi(task.status, viewerRole);
        binding.statusBanner.setBackgroundResource(ui.bannerBg);
        binding.tvStatusTitle.setText(ui.title + (task.isEmergency() && task.status == TaskStatus.PENDING
                ? " · " + getString(R.string.tag_emergency) : ""));
        binding.tvStatusDesc.setText(getString(ui.descRes));
        binding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, ui.titleColor));
        binding.tvStatusDesc.setTextColor(ContextCompat.getColor(this, ui.descColor));
    }

    private void renderRunnerPanel() {
        boolean show = task.status != TaskStatus.PENDING
                && task.status != TaskStatus.DRAFT
                && task.status != TaskStatus.CANCELLED;
        binding.panelRunner.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.tvRunnerName.setText(task.runnerName);
        binding.tvRunnerMeta.setText(getString(R.string.task_detail_runner_meta,
                task.runnerCompletedOrders, task.runnerCredit));
    }

    private void renderPhotoPanel() {
        binding.panelPhoto.setVisibility(task.status == TaskStatus.CONFIRMING ? View.VISIBLE : View.GONE);
    }

    private void renderTimeline() {
        binding.timelineSteps.removeAllViews();
        int currentStep = timelineStep(task.status);
        String[] labels = getResources().getStringArray(R.array.task_timeline_steps);
        LayoutInflater inflater = getLayoutInflater();
        int brand = ContextCompat.getColor(this, R.color.cg_brand);
        int primary = ContextCompat.getColor(this, R.color.cg_text_primary);
        int tertiary = ContextCompat.getColor(this, R.color.cg_text_tertiary);
        int lineDone = brand;
        int linePending = ContextCompat.getColor(this, R.color.cg_divider);

        for (int i = 0; i < labels.length; i++) {
            ItemTimelineStepBinding step = ItemTimelineStepBinding.inflate(
                    inflater, binding.timelineSteps, true);
            boolean done = i < currentStep;
            boolean current = i == currentStep;
            int dotSize = current ? dp(20) : dp(10);

            ViewGroup.LayoutParams dotLp = step.dot.getLayoutParams();
            dotLp.width = dotSize;
            dotLp.height = dotSize;
            step.dot.setLayoutParams(dotLp);

            if (current) {
                step.dot.setBackgroundResource(R.drawable.bg_timeline_dot_current);
            } else if (done) {
                step.dot.setBackgroundResource(R.drawable.bg_timeline_dot_done);
            } else {
                step.dot.setBackgroundResource(R.drawable.bg_timeline_dot_pending);
            }

            if (i == labels.length - 1) {
                step.lineBelow.setVisibility(View.GONE);
            } else {
                step.lineBelow.setVisibility(View.VISIBLE);
                step.lineBelow.setBackgroundColor(i < currentStep ? lineDone : linePending);
            }

            step.tvLabel.setText(labels[i]);
            if (current) {
                step.tvLabel.setTextColor(brand);
                step.tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (done) {
                step.tvLabel.setTextColor(primary);
                step.tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                step.tvLabel.setTextColor(tertiary);
                step.tvLabel.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            String sub = timelineSubtitle(i, done, current);
            if (sub != null && !sub.isEmpty()) {
                step.tvSub.setVisibility(View.VISIBLE);
                step.tvSub.setText(sub);
            } else {
                step.tvSub.setVisibility(View.GONE);
            }
        }
    }

    @Nullable
    private String timelineSubtitle(int index, boolean done, boolean current) {
        if (current && task.status == TaskStatus.DELIVERING && index == 2) {
            return getString(R.string.task_timeline_sub_delivering);
        }
        if (!done) {
            return null;
        }
        switch (index) {
            case 0:
                return getString(R.string.task_timeline_sub_published);
            case 1:
                return getString(R.string.task_timeline_sub_accepted);
            case 2:
                return task.status == TaskStatus.CONFIRMING
                        || task.status == TaskStatus.COMPLETED
                        || task.status == TaskStatus.REVIEWED
                        ? getString(R.string.task_timeline_sub_started)
                        : null;
            case 3:
                return task.status == TaskStatus.CONFIRMING
                        ? getString(R.string.task_timeline_sub_confirming)
                        : null;
            default:
                return null;
        }
    }

    private void renderHint() {
        int hintRes = hintRes(task.status, viewerRole);
        if (hintRes == 0) {
            binding.tvTimelineHint.setVisibility(View.GONE);
        } else {
            binding.tvTimelineHint.setVisibility(View.VISIBLE);
            binding.tvTimelineHint.setText(hintRes);
        }
    }

    @StringRes
    private int hintRes(@NonNull TaskStatus status, @NonNull UserRole role) {
        boolean pub = role == UserRole.PUBLISHER;
        switch (status) {
            case PENDING:
                return pub ? R.string.task_detail_hint_pending_pub : R.string.task_detail_hint_pending_run;
            case ACCEPTED:
                return pub ? R.string.task_detail_hint_accepted_pub : R.string.task_detail_hint_accepted_run;
            case DELIVERING:
                return pub ? R.string.task_detail_hint_delivering_pub : R.string.task_detail_hint_delivering_run;
            case CONFIRMING:
                return pub ? R.string.task_detail_hint_confirm_pub : R.string.task_detail_hint_confirm_run;
            default:
                return 0;
        }
    }

    private void renderBottomBar() {
        binding.bottomBar.removeAllViews();
        if (loadingRemote) {
            binding.bottomBar.setVisibility(View.GONE);
            return;
        }
        binding.bottomBar.setVisibility(View.VISIBLE);
        BottomSpec spec = bottomSpec(task.status, viewerRole);

        if (spec.message != null) {
            TextView msg = new TextView(this);
            msg.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            msg.setText(spec.message);
            msg.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
            msg.setTextSize(14);
            msg.setGravity(Gravity.CENTER);
            msg.setPadding(0, dp(8), 0, dp(8));
            binding.bottomBar.addView(msg);
            return;
        }

        java.util.List<BottomAction> left = new java.util.ArrayList<>();
        java.util.List<BottomAction> right = new java.util.ArrayList<>();
        for (BottomAction action : spec.actions) {
            if (action.style == ActionStyle.DANGER_TEXT) {
                left.add(action);
            } else {
                right.add(action);
            }
        }

        for (BottomAction action : left) {
            binding.bottomBar.addView(buildActionButton(action));
        }

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1f));
        binding.bottomBar.addView(spacer);

        if (right.isEmpty()) {
            return;
        }
        if (right.size() == 1) {
            binding.bottomBar.addView(buildActionButton(right.get(0)));
            return;
        }

        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.HORIZONTAL);
        group.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < right.size(); i++) {
            if (i > 0) {
                LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(dp(8), 1);
                group.addView(new View(this), gap);
            }
            group.addView(buildActionButton(right.get(i)));
        }
        binding.bottomBar.addView(group);
    }

    @NonNull
    private View buildActionButton(@NonNull BottomAction action) {
        if (action.style == ActionStyle.DANGER_TEXT) {
            TextView btn = new TextView(this);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            btn.setText(action.label);
            btn.setTextColor(ContextCompat.getColor(this, R.color.cg_danger));
            btn.setTextSize(14);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
            btn.setPadding(dp(4), dp(8), dp(12), dp(8));
            btn.setOnClickListener(v -> handleAction(action.id));
            return btn;
        }

        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER);
        int height = dp(44);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, height);
        btn.setLayoutParams(lp);
        btn.setMinimumWidth(action.style == ActionStyle.PRIMARY ? dp(128) : dp(80));
        int padH = action.style == ActionStyle.PRIMARY ? dp(14) : dp(12);
        btn.setPadding(padH, 0, padH, 0);

        if (action.iconRes != 0) {
            android.widget.ImageView icon = new android.widget.ImageView(this);
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(16), dp(16));
            icon.setLayoutParams(iconLp);
            icon.setImageResource(action.iconRes);
            int iconColor = action.style == ActionStyle.PRIMARY
                    ? ContextCompat.getColor(this, R.color.cg_text_on_brand)
                    : ContextCompat.getColor(this, R.color.cg_brand);
            icon.setColorFilter(iconColor);
            btn.addView(icon);
            View gap = new View(this);
            gap.setLayoutParams(new LinearLayout.LayoutParams(dp(6), 1));
            btn.addView(gap);
        }

        TextView label = new TextView(this);
        label.setText(action.label);
        label.setTextSize(14);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        if (action.style == ActionStyle.PRIMARY) {
            label.setTextColor(ContextCompat.getColor(this, R.color.cg_text_on_brand));
        } else {
            label.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));
        }
        btn.addView(label);

        if (action.style == ActionStyle.PRIMARY) {
            btn.setBackgroundResource(R.drawable.bg_btn_primary_brand);
        } else {
            btn.setBackgroundResource(R.drawable.bg_btn_outline_brand);
        }
        btn.setClickable(true);
        btn.setFocusable(true);
        btn.setOnClickListener(v -> handleAction(action.id));
        return btn;
    }

    private void handleAction(@NonNull String actionId) {
        switch (actionId) {
            case "grab":
                tryGrab();
                break;
            case "start_delivery":
                if (FeatureFlags.USE_REMOTE_API && taskId != null) {
                    remoteStartDeliver();
                } else {
                    task.status = TaskStatus.DELIVERING;
                    renderAll();
                }
                break;
            case "delivered":
                photoLauncher.launch(TaskNavigator.photoConfirm(this, task.id, viewerRole));
                break;
            case "confirm":
                if (FeatureFlags.USE_REMOTE_API && taskId != null) {
                    remoteConfirm();
                } else {
                    Toast.makeText(this, R.string.task_detail_confirmed, Toast.LENGTH_SHORT).show();
                    task.status = TaskStatus.COMPLETED;
                    renderAll();
                }
                break;
            case "raise":
                showRaiseSheet();
                break;
            case "emergency":
                confirmEmergency();
                break;
            case "cancel":
                showCancelSheet();
                break;
            case "track":
                openTracking();
                break;
            case "chat":
                openChat();
                break;
            case "review":
                openReview();
                break;
            case "complaint":
                openComplaint();
                break;
            case "reserve_hold":
                tryHoldReserve();
                break;
            case "reserve_release":
                tryReleaseReserve();
                break;
            case "reserve_confirm":
                tryConfirmReserve();
                break;
            default:
                break;
        }
    }

    private void tryGrab() {
        if (grabbing) {
            return;
        }
        if (task.mode == TaskMode.RESERVE && task.status == TaskStatus.RESERVING) {
            Toast.makeText(this, R.string.task_status_reserving_desc, Toast.LENGTH_SHORT).show();
            return;
        }
        SessionManager.SwitchRoleResult gate = sessionManager.canSwitchToRunner();
        if (gate == SessionManager.SwitchRoleResult.NEED_VERIFY) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.task_detail_need_verify_title)
                    .setMessage(R.string.task_detail_need_verify_msg)
                    .setPositiveButton(R.string.task_detail_go_verify, (d, w) ->
                            startActivity(WalletNavigator.verify(this)))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }
        if (gate == SessionManager.SwitchRoleResult.CREDIT_BLOCKED) {
            Toast.makeText(this, R.string.task_detail_credit_blocked, Toast.LENGTH_SHORT).show();
            return;
        }
        grabbing = true;
        renderBottomBar();
        if (FeatureFlags.USE_REMOTE_API && taskId != null) {
            remote().grab(taskId, new ApiCallback<TaskDetail>() {
                @Override
                public void onSuccess(@NonNull TaskDetail data) {
                    runOnUiThread(() -> {
                        grabbing = false;
                        applyTask(data);
                        Toast.makeText(TaskDetailActivity.this, R.string.task_detail_grab_success, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        grabbing = false;
                        renderBottomBar();
                        Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return;
        }
        binding.bottomBar.postDelayed(() -> {
            grabbing = false;
            task.status = TaskStatus.ACCEPTED;
            renderAll();
            Toast.makeText(this, R.string.task_detail_grab_success, Toast.LENGTH_SHORT).show();
        }, 600);
    }

    private void tryHoldReserve() {
        if (reserveBusy || taskId == null) {
            return;
        }
        SessionManager.SwitchRoleResult gate = sessionManager.canSwitchToRunner();
        if (gate == SessionManager.SwitchRoleResult.NEED_VERIFY) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.task_detail_need_verify_title)
                    .setMessage(R.string.task_detail_need_verify_msg)
                    .setPositiveButton(R.string.task_detail_go_verify, (d, w) ->
                            startActivity(WalletNavigator.verify(this)))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }
        if (gate == SessionManager.SwitchRoleResult.CREDIT_BLOCKED) {
            Toast.makeText(this, R.string.task_detail_credit_blocked, Toast.LENGTH_SHORT).show();
            return;
        }
        reserveBusy = true;
        renderBottomBar();
        if (FeatureFlags.USE_REMOTE_API) {
            remote().holdReserve(taskId, new ApiCallback<TaskDetail>() {
                @Override
                public void onSuccess(@NonNull TaskDetail data) {
                    runOnUiThread(() -> {
                        reserveBusy = false;
                        applyTask(data);
                        Toast.makeText(TaskDetailActivity.this,
                                R.string.task_detail_reserve_hold_success, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        reserveBusy = false;
                        renderBottomBar();
                        Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            task.reserveSlotHeld = true;
            reserveBusy = false;
            renderAll();
            Toast.makeText(this, R.string.task_detail_reserve_hold_success, Toast.LENGTH_SHORT).show();
        }
    }

    private void tryReleaseReserve() {
        if (reserveBusy || taskId == null) {
            return;
        }
        reserveBusy = true;
        renderBottomBar();
        if (FeatureFlags.USE_REMOTE_API) {
            remote().releaseReserve(taskId, new ApiCallback<TaskDetail>() {
                @Override
                public void onSuccess(@NonNull TaskDetail data) {
                    runOnUiThread(() -> {
                        reserveBusy = false;
                        applyTask(data);
                        Toast.makeText(TaskDetailActivity.this,
                                R.string.task_detail_reserve_release_success, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        reserveBusy = false;
                        renderBottomBar();
                        Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            task.reserveSlotHeld = false;
            reserveBusy = false;
            renderAll();
            Toast.makeText(this, R.string.task_detail_reserve_release_success, Toast.LENGTH_SHORT).show();
        }
    }

    private void tryConfirmReserve() {
        if (reserveBusy || taskId == null) {
            return;
        }
        reserveBusy = true;
        renderBottomBar();
        if (FeatureFlags.USE_REMOTE_API) {
            remote().confirmReserve(taskId, new ApiCallback<TaskDetail>() {
                @Override
                public void onSuccess(@NonNull TaskDetail data) {
                    runOnUiThread(() -> {
                        reserveBusy = false;
                        applyTask(data);
                        Toast.makeText(TaskDetailActivity.this,
                                R.string.task_detail_reserve_confirm_success, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        reserveBusy = false;
                        renderBottomBar();
                        Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            task.status = TaskStatus.ACCEPTED;
            task.reserveSlotHeld = false;
            reserveBusy = false;
            renderAll();
            Toast.makeText(this, R.string.task_detail_reserve_confirm_success, Toast.LENGTH_SHORT).show();
        }
    }

    private void remoteStartDeliver() {
        remoteAction(remote -> remote.startDeliver(taskId, detailCallback()));
    }

    private void remoteConfirm() {
        remoteAction(remote -> remote.confirm(taskId, new ApiCallback<TaskDetail>() {
            @Override
            public void onSuccess(@NonNull TaskDetail data) {
                runOnUiThread(() -> {
                    applyTask(data);
                    Toast.makeText(TaskDetailActivity.this,
                            R.string.task_detail_confirmed, Toast.LENGTH_SHORT).show();
                    refreshWalletIfRemote();
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() ->
                        Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }));
    }

    private void refreshWalletIfRemote() {
        if (!FeatureFlags.USE_REMOTE_API) {
            return;
        }
        RetrofitClient.get().walletRemote().loadWallet(new ApiCallback<com.campusgo.data.remote.dto.wallet.WalletResponse>() {
            @Override
            public void onSuccess(@NonNull com.campusgo.data.remote.dto.wallet.WalletResponse data) {
            }

            @Override
            public void onError(@NonNull ApiException error) {
            }
        });
    }

    @NonNull
    private ApiCallback<TaskDetail> detailCallback() {
        return detailCallback(0);
    }

    @NonNull
    private ApiCallback<TaskDetail> detailCallback(@StringRes int toastRes) {
        return new ApiCallback<TaskDetail>() {
            @Override
            public void onSuccess(@NonNull TaskDetail data) {
                runOnUiThread(() -> {
                    applyTask(data);
                    if (toastRes != 0) {
                        Toast.makeText(TaskDetailActivity.this, toastRes, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() ->
                        Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show());
            }
        };
    }

    private int timelineStep(@NonNull TaskStatus status) {
        switch (status) {
            case ACCEPTED:
                return 1;
            case DELIVERING:
                return 2;
            case CONFIRMING:
            case COMPLETED:
            case REVIEWED:
                return 3;
            default:
                return 0;
        }
    }

    @NonNull
    private StatusUi statusUi(@NonNull TaskStatus status, @NonNull UserRole role) {
        boolean publisher = role == UserRole.PUBLISHER;
        switch (status) {
            case ACCEPTED:
                return new StatusUi(
                        R.drawable.bg_status_banner_accepted,
                        getString(R.string.task_status_accepted),
                        publisher ? R.string.task_status_accepted_desc_pub : R.string.task_status_accepted_desc_run,
                        R.color.cg_brand, R.color.cg_brand);
            case DELIVERING:
                return new StatusUi(
                        R.drawable.bg_status_banner_delivering,
                        getString(R.string.task_status_delivering),
                        publisher ? R.string.task_status_delivering_desc_pub : R.string.task_status_delivering_desc_run,
                        R.color.cg_tag_reserve_text, R.color.cg_tag_reserve_text);
            case CONFIRMING:
                return new StatusUi(
                        R.drawable.bg_status_banner_confirming,
                        getString(R.string.task_status_confirming),
                        publisher ? R.string.task_status_confirming_desc_pub : R.string.task_status_confirming_desc_run,
                        R.color.cg_tag_emergency_text, R.color.cg_tag_emergency_text);
            case COMPLETED:
            case REVIEWED:
                return new StatusUi(
                        R.drawable.bg_status_banner_accepted,
                        getString(R.string.task_status_completed),
                        R.string.task_status_completed_desc,
                        R.color.cg_brand, R.color.cg_text_secondary);
            case CANCELLED:
                return new StatusUi(
                        R.drawable.bg_status_banner_pending,
                        getString(R.string.task_status_cancelled),
                        R.string.task_status_cancelled_desc,
                        R.color.cg_text_secondary, R.color.cg_text_tertiary);
            case RESERVING:
                return new StatusUi(
                        R.drawable.bg_tag_reserve,
                        getString(R.string.tasks_filter_reserve),
                        R.string.task_status_reserving_desc,
                        R.color.cg_tag_reserve_text, R.color.cg_text_secondary);
            case PENDING:
            default:
                return new StatusUi(
                        R.drawable.bg_status_banner_pending,
                        getString(R.string.task_status_pending),
                        publisher ? R.string.task_status_pending_desc_pub : R.string.task_status_pending_desc_run,
                        R.color.cg_text_primary, R.color.cg_text_secondary);
        }
    }

    @NonNull
    private BottomSpec bottomSpec(@NonNull TaskStatus status, @NonNull UserRole role) {
        BottomSpec spec = new BottomSpec();
        boolean pub = role == UserRole.PUBLISHER;
        switch (status) {
            case PENDING:
                if (pub) {
                    spec.actions.add(action("cancel", R.string.task_action_cancel, ActionStyle.DANGER_TEXT, 0));
                    spec.actions.add(action("raise", R.string.task_action_raise, ActionStyle.OUTLINE, 0));
                    spec.actions.add(action("emergency", R.string.task_action_emergency, ActionStyle.PRIMARY, 0));
                } else {
                    spec.actions.add(action("grab", grabbing
                            ? R.string.task_action_grabbing
                            : R.string.task_action_grab, ActionStyle.PRIMARY, 0));
                }
                break;
            case ACCEPTED:
                if (pub) {
                    spec.actions.add(action("cancel", R.string.task_action_cancel, ActionStyle.DANGER_TEXT, 0));
                    spec.actions.add(action("chat", R.string.task_action_contact_runner, ActionStyle.OUTLINE,
                            R.drawable.ic_message));
                } else {
                    spec.actions.add(action("cancel", R.string.task_action_cancel, ActionStyle.DANGER_TEXT, 0));
                    spec.actions.add(action("chat", R.string.task_action_contact_publisher, ActionStyle.OUTLINE,
                            R.drawable.ic_message));
                    spec.actions.add(action("start_delivery", R.string.task_action_start_delivery,
                            ActionStyle.PRIMARY, 0));
                }
                break;
            case DELIVERING:
                if (pub) {
                    spec.actions.add(action("complaint", R.string.task_action_complaint, ActionStyle.DANGER_TEXT, 0));
                    spec.actions.add(action("chat", R.string.task_action_contact, ActionStyle.OUTLINE,
                            R.drawable.ic_message));
                    spec.actions.add(action("track", R.string.task_action_track_location, ActionStyle.PRIMARY,
                            R.drawable.ic_map_pin));
                } else {
                    spec.actions.add(action("chat", R.string.task_action_contact_publisher, ActionStyle.OUTLINE,
                            R.drawable.ic_message));
                    spec.actions.add(action("delivered", R.string.task_action_delivered, ActionStyle.PRIMARY, 0));
                }
                break;
            case CONFIRMING:
                if (pub) {
                    spec.actions.add(action("complaint", R.string.task_action_dispute, ActionStyle.DANGER_TEXT, 0));
                    spec.actions.add(action("confirm", R.string.task_action_confirm, ActionStyle.PRIMARY, 0));
                } else {
                    spec.message = getString(R.string.task_status_waiting_confirm);
                }
                break;
            case COMPLETED:
                spec.actions.add(action("review", R.string.task_action_review, ActionStyle.PRIMARY, 0));
                break;
            case REVIEWED:
                spec.message = getString(R.string.task_status_reviewed_msg);
                break;
            case RESERVING:
                if (pub) {
                    spec.actions.add(action("cancel", R.string.task_action_cancel, ActionStyle.DANGER_TEXT, 0));
                } else if (task.reserveSlotHeld) {
                    spec.actions.add(action("reserve_release", R.string.task_action_reserve_release,
                            ActionStyle.DANGER_TEXT, 0));
                    spec.actions.add(action("reserve_confirm", reserveBusy
                            ? R.string.task_action_reserve_holding
                            : R.string.task_action_reserve_confirm, ActionStyle.PRIMARY, 0));
                } else {
                    spec.actions.add(action("reserve_hold", reserveBusy
                            ? R.string.task_action_reserve_holding
                            : R.string.task_action_reserve_hold, ActionStyle.PRIMARY, 0));
                }
                break;
            case CANCELLED:
                spec.message = getString(R.string.task_status_cancelled_msg);
                break;
            default:
                break;
        }
        return spec;
    }

    @NonNull
    private BottomAction action(@NonNull String id, @StringRes int labelRes,
                                @NonNull ActionStyle style, @DrawableRes int iconRes) {
        return new BottomAction(id, getString(labelRes), style, iconRes);
    }

    private void openChat() {
        String peerName = viewerRole == UserRole.PUBLISHER ? task.runnerName : "发布者";
        startActivity(ChatNavigator.fromTask(this, task.id, peerName, task.title, task.reward, viewerRole));
    }

    private void openReview() {
        String subtitle = task.categoryLabel != null && !task.categoryLabel.isEmpty()
                ? task.categoryLabel
                : task.pickupAddress;
        reviewLauncher.launch(TaskNavigator.review(
                this, task.id, task.title, subtitle, task.reward, viewerRole));
    }

    private void openTracking() {
        startActivity(TaskNavigator.tracking(
                this, task.id, task.title, task.reward, task.runnerName, viewerRole));
    }

    private void openComplaint() {
        startActivity(TaskNavigator.complaint(
                this,
                task.id,
                task.title,
                task.reward,
                com.campusgo.ui.complaint.ComplaintActivity.MODE_COMPLAINT));
    }

    private void showRaiseSheet() {
        RaisePriceBottomSheet sheet = RaisePriceBottomSheet.newInstance(task.reward);
        sheet.setListener(amount -> {
            if (FeatureFlags.USE_REMOTE_API && taskId != null) {
                int addCent = (int) Math.round(amount * 100);
                remoteAction(remote -> remote.raisePrice(taskId, addCent, new ApiCallback<TaskDetail>() {
                    @Override
                    public void onSuccess(@NonNull TaskDetail data) {
                        runOnUiThread(() -> {
                            applyTask(data);
                            Toast.makeText(TaskDetailActivity.this,
                                    getString(R.string.raise_success, amount), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        runOnUiThread(() ->
                                Toast.makeText(TaskDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }));
            } else {
                task.reward = Math.round((task.reward + amount) * 100) / 100.0;
                renderAll();
                Toast.makeText(this, getString(R.string.raise_success, amount), Toast.LENGTH_SHORT).show();
            }
        });
        sheet.show(getSupportFragmentManager(), "raise_price");
    }

    private void confirmEmergency() {
        if (task.isEmergency()) {
            Toast.makeText(this, R.string.emergency_already, Toast.LENGTH_SHORT).show();
            return;
        }
        double newReward = Math.round(task.reward * 1.5 * 100) / 100.0;
        new AlertDialog.Builder(this)
                .setTitle(R.string.emergency_confirm_title)
                .setMessage(getString(R.string.emergency_confirm_msg, task.formatReward(),
                        String.format("¥%.2f", newReward)))
                .setPositiveButton(R.string.emergency_confirm_ok, (d, w) -> {
                    if (FeatureFlags.USE_REMOTE_API && taskId != null) {
                        remoteAction(remote -> remote.emergency(taskId, detailCallback(R.string.emergency_success)));
                    } else {
                        task.mode = TaskMode.EMERGENCY;
                        task.reward = newReward;
                        renderAll();
                        Toast.makeText(this, R.string.emergency_success, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCancelSheet() {
        CancelTaskBottomSheet sheet = CancelTaskBottomSheet.newInstance(task.status);
        sheet.setListener(reason -> {
            if (FeatureFlags.USE_REMOTE_API && taskId != null) {
                remoteAction(remote -> remote.cancel(taskId, reason, detailCallback(R.string.task_detail_cancelled)));
            } else {
                task.status = TaskStatus.CANCELLED;
                Toast.makeText(this, R.string.task_detail_cancelled, Toast.LENGTH_SHORT).show();
                renderAll();
            }
        });
        sheet.show(getSupportFragmentManager(), "cancel_task");
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static final class StatusUi {
        final int bannerBg;
        final String title;
        final int descRes;
        final int titleColor;
        final int descColor;

        StatusUi(int bannerBg, String title, int descRes, int titleColor, int descColor) {
            this.bannerBg = bannerBg;
            this.title = title;
            this.descRes = descRes;
            this.titleColor = titleColor;
            this.descColor = descColor;
        }
    }

    private enum ActionStyle { PRIMARY, OUTLINE, DANGER_TEXT }

    private static final class BottomAction {
        final String id;
        final String label;
        final ActionStyle style;
        final int iconRes;

        BottomAction(String id, String label, ActionStyle style, int iconRes) {
            this.id = id;
            this.label = label;
            this.style = style;
            this.iconRes = iconRes;
        }
    }

    private static final class BottomSpec {
        String message;
        final java.util.List<BottomAction> actions = new java.util.ArrayList<>();
    }
}
