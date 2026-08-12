package com.campusgo.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockGroupRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.wallet.WalletResponse;
import com.campusgo.databinding.ActivityGroupDetailBinding;
import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.GroupOrderDetail;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.ui.address.AddressNavigator;
import com.campusgo.ui.wallet.WalletNavigator;

/**
 * T07 拼单详情
 */
public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private GroupOrderDetail detail;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> topUpLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    showAddressPicker();
                }
            });

    private final ActivityResultLauncher<Intent> addressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                String selected = result.getData().getStringExtra(AddressNavigator.EXTRA_RESULT_DISPLAY);
                if (selected != null && !selected.isEmpty()) {
                    confirmJoin(selected);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        String taskId = getIntent().getStringExtra(TaskNavigator.EXTRA_TASK_ID);
        if (taskId == null) {
            taskId = "pool1";
        }
        loadDetail(taskId);

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadDetail(@NonNull String taskId) {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().taskRemote().loadGroupDetail(taskId, new ApiCallback<GroupOrderDetail>() {
                @Override
                public void onSuccess(@NonNull GroupOrderDetail data) {
                    runOnUiThread(() -> {
                        detail = data;
                        renderAll();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        Toast.makeText(GroupDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
            return;
        }
        detail = MockGroupRepository.findByTaskId(taskId);
        if (detail == null) {
            Toast.makeText(this, R.string.group_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        renderAll();
    }

    private void renderAll() {
        int progress = detail.maxMembers == 0 ? 0
                : Math.round(100f * detail.joinedCount / detail.maxMembers);
        binding.tvProgressCount.setText(getString(R.string.group_progress_count,
                detail.joinedCount, detail.maxMembers));
        binding.tvProgressHint.setText(detail.full
                ? getString(R.string.group_full_hint)
                : getString(R.string.group_need_more, detail.slotsRemaining()));
        binding.progressGroup.setProgress(progress);

        binding.tvCategoryTag.setText(detail.categoryLabel);
        binding.tvTaskTitle.setText(detail.title);
        binding.tvPickup.setText(detail.pickupAddress);
        binding.tvDelivery.setText(detail.deliverySummary);
        binding.tvReward.setText(getString(R.string.group_reward_value,
                detail.totalReward, detail.sharePerPerson));
        binding.tvTime.setText(detail.timeLabel);

        binding.memberList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (GroupMember member : detail.members) {
            binding.memberList.addView(buildMemberRow(inflater, member));
        }

        renderBottomBar();
    }

    @NonNull
    private View buildMemberRow(@NonNull LayoutInflater inflater, @NonNull GroupMember member) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int padV = dp(10);
        row.setPadding(0, padV, 0, padV);

        TextView avatar = new TextView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        avatar.setGravity(android.view.Gravity.CENTER);
        avatar.setTextSize(14);
        avatar.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView name = new TextView(this);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nameLp.setMarginStart(dp(12));
        name.setLayoutParams(nameLp);
        name.setTextSize(14);

        TextView tag = new TextView(this);
        tag.setTextSize(11);
        tag.setPadding(dp(8), dp(2), dp(8), dp(2));
        tag.setVisibility(View.GONE);

        TextView amount = new TextView(this);
        amount.setTextSize(13);
        amount.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));

        if (member.role == GroupMember.Role.EMPTY_SLOT) {
            avatar.setBackgroundResource(R.drawable.bg_member_slot_empty);
            avatar.setText("+");
            avatar.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
            name.setText(R.string.group_slot_empty);
            name.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
        } else {
            avatar.setBackgroundResource(R.drawable.bg_icon_soft);
            avatar.setText(member.name.isEmpty() ? "?" : member.name.substring(0, 1));
            avatar.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));
            name.setText(member.name);
            name.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
            amount.setText(getString(R.string.group_paid, member.paidAmount));
            if (member.role == GroupMember.Role.CREATOR) {
                tag.setVisibility(View.VISIBLE);
                tag.setText(R.string.group_creator_tag);
                tag.setBackgroundResource(R.drawable.bg_tag_group);
                tag.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_group_text));
            }
        }

        row.addView(avatar);
        row.addView(name);
        if (tag.getVisibility() == View.VISIBLE) {
            LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tagLp.setMarginStart(dp(8));
            row.addView(tag, tagLp);
        }
        if (member.role != GroupMember.Role.EMPTY_SLOT) {
            row.addView(amount);
        }
        return row;
    }

    private void renderBottomBar() {
        binding.btnSecondary.setVisibility(View.GONE);
        if (detail.full) {
            binding.btnPrimary.setText(R.string.group_view_task);
            binding.btnPrimary.setOnClickListener(v ->
                    startActivity(TaskNavigator.taskDetail(this, detail.taskId, sessionManager.getActiveRole())
                            .putExtra(TaskNavigator.EXTRA_STATUS, TaskStatus.PENDING.name())
                            .putExtra(TaskNavigator.EXTRA_MODE, TaskMode.GROUP.name())));
            binding.tvGroupTip.setText(R.string.group_full_runner_tip);
            return;
        }
        if (detail.viewerIsCreator) {
            binding.btnPrimary.setText(R.string.group_cancel);
            binding.btnPrimary.setBackgroundResource(R.drawable.bg_btn_danger);
            binding.btnPrimary.setOnClickListener(v -> showCancelSheet());
            binding.tvGroupTip.setText(R.string.group_creator_tip);
            return;
        }
        if (detail.viewerJoined) {
            binding.btnPrimary.setText(R.string.group_leave);
            binding.btnPrimary.setBackgroundResource(R.drawable.bg_btn_outline_brand);
            binding.btnPrimary.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));
            binding.btnPrimary.setOnClickListener(v -> confirmLeave());
            binding.tvGroupTip.setText(R.string.group_joined_tip);
            return;
        }
        binding.btnPrimary.setText(getString(R.string.group_join, detail.sharePerPerson));
        binding.btnPrimary.setBackgroundResource(R.drawable.bg_btn_primary_brand);
        binding.btnPrimary.setTextColor(ContextCompat.getColor(this, R.color.cg_text_on_brand));
        binding.btnPrimary.setOnClickListener(v -> startJoinFlow());
        binding.tvGroupTip.setText(R.string.group_join_tip);
    }

    private void startJoinFlow() {
        double share = detail.sharePerPerson;
        if (sessionManager.getWalletBalance() + 0.001 < share) {
            double shortfall = Math.round((share - sessionManager.getWalletBalance()) * 100) / 100.0;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.group_join_insufficient_title)
                    .setMessage(getString(R.string.group_join_insufficient_msg, shortfall))
                    .setPositiveButton(R.string.publish_go_topup, (d, w) ->
                            topUpLauncher.launch(WalletNavigator.topUp(this, shortfall)))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }
        showAddressPicker();
    }

    private void showAddressPicker() {
        addressLauncher.launch(AddressNavigator.pick(this, false));
    }

    private void confirmJoin(@NonNull String address) {
        double share = detail.sharePerPerson;
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_join_confirm_title)
                .setMessage(getString(R.string.group_join_confirm_msg,
                        address, String.format("¥%.2f", share)))
                .setPositiveButton(R.string.group_join_confirm, (d, w) -> submitJoin(address))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitJoin(@NonNull String address) {
        double share = detail.sharePerPerson;
        if (FeatureFlags.USE_REMOTE_API) {
            binding.btnPrimary.setEnabled(false);
            RetrofitClient.get().taskRemote().joinGroup(detail.taskId, address, new ApiCallback<GroupOrderDetail>() {
                @Override
                public void onSuccess(@NonNull GroupOrderDetail data) {
                    reloadWalletAndDetail(data, R.string.group_join_success);
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        binding.btnPrimary.setEnabled(true);
                        Toast.makeText(GroupDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return;
        }
        if (!sessionManager.deductWalletBalance(share)) {
            Toast.makeText(this, R.string.group_join_insufficient_title, Toast.LENGTH_SHORT).show();
            return;
        }
        String taskId = detail.taskId;
        if (!MockGroupRepository.join(taskId, sessionManager.getNickname(), address, share)) {
            sessionManager.addWalletBalance(share);
            Toast.makeText(this, R.string.group_join_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.group_join_success, Toast.LENGTH_SHORT).show();
        loadDetail(taskId);
    }

    private void reloadWalletAndDetail(@NonNull GroupOrderDetail data, int successMsgRes) {
        RetrofitClient.get().walletRemote().loadWallet(new ApiCallback<WalletResponse>() {
            @Override
            public void onSuccess(@NonNull WalletResponse wallet) {
                runOnUiThread(() -> applyDetail(data, successMsgRes));
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() -> applyDetail(data, successMsgRes));
            }
        });
    }

    private void applyDetail(@NonNull GroupOrderDetail data, int successMsgRes) {
        detail = data;
        binding.btnPrimary.setEnabled(true);
        Toast.makeText(this, successMsgRes, Toast.LENGTH_SHORT).show();
        renderAll();
    }

    private void confirmLeave() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_leave_title)
                .setMessage(R.string.group_leave_msg)
                .setPositiveButton(R.string.group_leave, (d, w) -> {
                    if (FeatureFlags.USE_REMOTE_API) {
                        RetrofitClient.get().taskRemote().leaveGroup(detail.taskId,
                                new ApiCallback<GroupOrderDetail>() {
                                    @Override
                                    public void onSuccess(@NonNull GroupOrderDetail data) {
                                        reloadWalletAndDetail(data, R.string.group_leave_success);
                                    }

                                    @Override
                                    public void onError(@NonNull ApiException error) {
                                        runOnUiThread(() -> Toast.makeText(GroupDetailActivity.this,
                                                error.getMessage(), Toast.LENGTH_SHORT).show());
                                    }
                                });
                        return;
                    }
                    double refund = detail.sharePerPerson;
                    MockGroupRepository.leave(detail.taskId);
                    sessionManager.addWalletBalance(refund);
                    Toast.makeText(this, R.string.group_leave_success, Toast.LENGTH_SHORT).show();
                    loadDetail(detail.taskId);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCancelSheet() {
        CancelTaskBottomSheet sheet = CancelTaskBottomSheet.newInstance(TaskStatus.GROUPING);
        sheet.setListener(reason -> {
            Toast.makeText(this, R.string.group_cancelled, Toast.LENGTH_SHORT).show();
            finish();
        });
        sheet.show(getSupportFragmentManager(), "cancel_group");
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
