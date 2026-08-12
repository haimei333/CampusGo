package com.campusgo.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.campusgo.databinding.ActivityPublishBinding;
import com.campusgo.data.mock.MockPublishDraftRepository;
import com.campusgo.data.mock.MockTemplateRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.task.PublishTaskRequest;
import com.campusgo.data.remote.dto.task.PublishTaskResponse;
import com.campusgo.domain.model.PublishDraft;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskTemplate;
import com.campusgo.ui.address.AddressNavigator;
import com.campusgo.ui.wallet.WalletNavigator;

/**
 * T01 发布任务 — 三步向导（类型 / 填写 / 确认）
 */
public class PublishActivity extends AppCompatActivity {

    private ActivityPublishBinding binding;
    private SessionManager sessionManager;
    private int currentStep = 1;
    private TaskMode selectedMode = TaskMode.NORMAL;
    private TaskCategory selectedCategory = TaskCategory.EXPRESS;
    private String pickupAddress;
    private String deliveryAddress;
    private String timeLabel;
    private boolean pickingPickup;
    @Nullable
    private String draftId;

    private final ActivityResultLauncher<Intent> topUpLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && currentStep == 3) {
                    buildSummary();
                    Toast.makeText(this, R.string.publish_topup_return, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> addressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                String selected = result.getData().getStringExtra(AddressNavigator.EXTRA_RESULT_DISPLAY);
                if (selected == null || selected.isEmpty()) {
                    return;
                }
                if (pickingPickup) {
                    pickupAddress = selected;
                    binding.tvPickup.setText(pickupAddress);
                    binding.tvPickup.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
                } else {
                    deliveryAddress = selected;
                    binding.tvDelivery.setText(deliveryAddress);
                    binding.tvDelivery.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
                }
            });

    private final ActivityResultLauncher<Intent> templateLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                String templateId = result.getData().getStringExtra(TemplateActivity.EXTRA_RESULT_TEMPLATE_ID);
                if (templateId != null) {
                    applyTemplate(templateId);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublishBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        pickupAddress = getString(R.string.publish_address_unset);
        deliveryAddress = getString(R.string.publish_address_unset);
        timeLabel = getString(R.string.publish_time_asap);

        TaskCategory preset = TaskNavigator.parseCategory(getIntent());
        if (preset != null) {
            selectedCategory = preset;
        }

        draftId = TaskNavigator.parseDraftId(getIntent());

        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnTemplate.setOnClickListener(v ->
                templateLauncher.launch(TaskNavigator.templates(this)));
        binding.btnSaveDraft.setOnClickListener(v -> saveDraft());
        binding.btnPrev.setOnClickListener(v -> goStep(currentStep - 1));
        binding.btnNext.setOnClickListener(v -> onNext());

        binding.cardNormal.setOnClickListener(v -> selectMode(TaskMode.NORMAL));
        binding.cardGroup.setOnClickListener(v -> selectMode(TaskMode.GROUP));
        binding.cardEmergency.setOnClickListener(v -> selectMode(TaskMode.EMERGENCY));
        binding.cardReserve.setOnClickListener(v -> selectMode(TaskMode.RESERVE));

        binding.chipExpress.setOnClickListener(v -> selectCategory(TaskCategory.EXPRESS));
        binding.chipBuy.setOnClickListener(v -> selectCategory(TaskCategory.BUY));
        binding.chipErrand.setOnClickListener(v -> selectCategory(TaskCategory.ERRAND));

        binding.rowPickup.setOnClickListener(v -> pickAddress(true));
        binding.rowDelivery.setOnClickListener(v -> pickAddress(false));
        binding.tvTime.setOnClickListener(v -> pickTime());

        binding.etReward.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEmergencyHint();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        selectMode(TaskMode.NORMAL);
        selectCategory(selectedCategory);
        if (draftId != null) {
            loadDraft(draftId);
        }
        goStep(1);
    }

    private void loadDraft(@NonNull String id) {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().taskRemote().findDraft(id, new ApiCallback<PublishDraft>() {
                @Override
                public void onSuccess(@NonNull PublishDraft draft) {
                    runOnUiThread(() -> applyDraft(draft));
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() ->
                            Toast.makeText(PublishActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
            return;
        }
        PublishDraft draft = MockPublishDraftRepository.findById(this, id);
        if (draft != null) {
            applyDraft(draft);
        }
    }

    private void applyDraft(@NonNull PublishDraft draft) {
        draftId = draft.id;
        selectedMode = draft.mode;
        selectedCategory = draft.category;
        pickupAddress = draft.pickupAddress;
        deliveryAddress = draft.deliveryAddress;
        timeLabel = draft.timeLabel;
        binding.etTitle.setText(draft.title);
        binding.etDesc.setText(draft.description);
        if (draft.reward > 0) {
            binding.etReward.setText(String.valueOf(draft.reward));
        }
        selectMode(selectedMode);
        selectCategory(selectedCategory);
        applyAddressUi(binding.tvPickup, pickupAddress);
        applyAddressUi(binding.tvDelivery, deliveryAddress);
        binding.tvTime.setText(timeLabel);
        goStep(2);
    }

    private void applyTemplate(@NonNull String templateId) {
        TaskTemplate template = MockTemplateRepository.findById(this, templateId);
        if (template == null) {
            return;
        }
        selectedMode = template.mode;
        selectedCategory = template.category;
        pickupAddress = template.pickupAddress;
        deliveryAddress = template.deliveryAddress;
        timeLabel = template.timeLabel;
        binding.etTitle.setText(template.title);
        binding.etDesc.setText(template.description);
        binding.etReward.setText(String.valueOf(template.reward));
        selectMode(selectedMode);
        selectCategory(selectedCategory);
        applyAddressUi(binding.tvPickup, pickupAddress);
        applyAddressUi(binding.tvDelivery, deliveryAddress);
        binding.tvTime.setText(timeLabel);
        Toast.makeText(this, R.string.template_applied, Toast.LENGTH_SHORT).show();
        goStep(2);
    }

    private void applyAddressUi(@NonNull TextView view, @NonNull String address) {
        view.setText(address);
        boolean set = !address.equals(getString(R.string.publish_address_unset));
        view.setTextColor(ContextCompat.getColor(this,
                set ? R.color.cg_text_primary : R.color.cg_text_tertiary));
    }

    private void saveDraft() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.length() < 2) {
            Toast.makeText(this, R.string.publish_draft_need_title, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean createNew = draftId == null;
        if (draftId == null) {
            draftId = "d" + System.currentTimeMillis();
        }
        PublishDraft draft = new PublishDraft(
                draftId,
                title,
                binding.etDesc.getText().toString().trim(),
                selectedMode,
                selectedCategory,
                pickupAddress,
                deliveryAddress,
                timeLabel,
                parseReward(),
                System.currentTimeMillis());
        if (FeatureFlags.USE_REMOTE_API) {
            binding.btnSaveDraft.setEnabled(false);
            RetrofitClient.get().taskRemote().saveDraft(draft, createNew, new ApiCallback<PublishDraft>() {
                @Override
                public void onSuccess(@NonNull PublishDraft data) {
                    runOnUiThread(() -> {
                        Toast.makeText(PublishActivity.this, R.string.publish_draft_saved, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        binding.btnSaveDraft.setEnabled(true);
                        Toast.makeText(PublishActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return;
        }
        MockPublishDraftRepository.save(this, draft);
        Toast.makeText(this, R.string.publish_draft_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void selectMode(@NonNull TaskMode mode) {
        selectedMode = mode;
        styleModeCard(binding.cardNormal, mode == TaskMode.NORMAL);
        styleModeCard(binding.cardGroup, mode == TaskMode.GROUP);
        styleModeCard(binding.cardEmergency, mode == TaskMode.EMERGENCY);
        styleModeCard(binding.cardReserve, mode == TaskMode.RESERVE);
        binding.panelGroupSettings.setVisibility(mode == TaskMode.GROUP ? View.VISIBLE : View.GONE);
        binding.tvEmergencyHint.setVisibility(mode == TaskMode.EMERGENCY ? View.VISIBLE : View.GONE);
        binding.tvReserveHint.setVisibility(mode == TaskMode.RESERVE ? View.VISIBLE : View.GONE);
        if (mode == TaskMode.RESERVE && getString(R.string.publish_time_asap).equals(timeLabel)) {
            timeLabel = getString(R.string.publish_time_tomorrow_18);
            binding.tvTime.setText(timeLabel);
        }
        updateEmergencyHint();
    }

    private void styleModeCard(@NonNull View card, boolean selected) {
        card.setAlpha(selected ? 1f : 0.72f);
        card.setBackgroundResource(selected
                ? R.drawable.bg_banner_auth
                : R.drawable.bg_card);
    }

    private void selectCategory(@NonNull TaskCategory category) {
        selectedCategory = category;
        styleCategoryChip(binding.chipExpress, category == TaskCategory.EXPRESS);
        styleCategoryChip(binding.chipBuy, category == TaskCategory.BUY);
        styleCategoryChip(binding.chipErrand, category == TaskCategory.ERRAND);
    }

    private void styleCategoryChip(@NonNull TextView chip, boolean selected) {
        chip.setSelected(selected);
        chip.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.cg_text_on_brand : R.color.cg_tag_normal_text));
    }

    private void pickAddress(boolean pickup) {
        pickingPickup = pickup;
        addressLauncher.launch(AddressNavigator.pick(this, pickup));
    }

    private void pickTime() {
        String[] options;
        if (selectedMode == TaskMode.RESERVE) {
            options = new String[]{
                    getString(R.string.publish_time_tomorrow_12),
                    getString(R.string.publish_time_tomorrow_18),
                    getString(R.string.publish_time_day_after_18)
            };
        } else {
            options = new String[]{
                    getString(R.string.publish_time_asap),
                    getString(R.string.publish_time_1h),
                    getString(R.string.publish_time_2h)
            };
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.publish_field_time)
                .setItems(options, (d, which) -> {
                    timeLabel = options[which];
                    binding.tvTime.setText(timeLabel);
                })
                .show();
    }

    private void onNext() {
        if (currentStep == 1) {
            goStep(2);
            return;
        }
        if (currentStep == 2) {
            if (!validateStep2()) {
                return;
            }
            buildSummary();
            goStep(3);
            return;
        }
        submitPublish();
    }

    private boolean validateStep2() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.length() < 2) {
            Toast.makeText(this, R.string.publish_error_title, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pickupAddress.equals(getString(R.string.publish_address_unset))
                || deliveryAddress.equals(getString(R.string.publish_address_unset))) {
            Toast.makeText(this, R.string.publish_error_address, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedMode == TaskMode.RESERVE
                && getString(R.string.publish_time_asap).equals(timeLabel)) {
            Toast.makeText(this, R.string.publish_error_reserve_time, Toast.LENGTH_SHORT).show();
            return false;
        }
        double reward = parseReward();
        if (reward <= 0) {
            Toast.makeText(this, R.string.publish_error_reward, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private double parseReward() {
        try {
            return Double.parseDouble(binding.etReward.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double payAmount() {
        double base = parseReward();
        if (selectedMode == TaskMode.EMERGENCY) {
            base = Math.round(base * 1.5 * 100) / 100.0;
        }
        return base;
    }

    private void updateEmergencyHint() {
        if (selectedMode != TaskMode.EMERGENCY) {
            return;
        }
        binding.tvEmergencyHint.setText(getString(
                R.string.publish_emergency_hint,
                String.format("¥%.2f", payAmount())));
    }

    private void buildSummary() {
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDesc.getText().toString().trim();
        String modeLabel = modeLabel(selectedMode);
        String categoryLabel = categoryLabel(selectedCategory);
        String summary = getString(R.string.publish_summary_template,
                title,
                modeLabel,
                categoryLabel,
                pickupAddress,
                deliveryAddress,
                timeLabel,
                TextUtils.isEmpty(desc) ? "—" : desc,
                String.format("¥%.2f", payAmount()));
        binding.tvSummary.setText(summary);
        binding.tvPayTotal.setText(String.format("¥%.2f", payAmount()));
        updateEmergencyHint();
    }

    private void submitPublish() {
        double amount = payAmount();
        if (FeatureFlags.USE_REMOTE_API) {
            double balance = sessionManager.getWalletBalance();
            if (balance + 0.001 < amount) {
                showInsufficientDialog(amount, balance);
                return;
            }
            remotePublish(amount);
            return;
        }
        double balance = sessionManager.getWalletBalance();
        if (balance + 0.001 < amount) {
            showInsufficientDialog(amount, balance);
            return;
        }
        if (!sessionManager.deductWalletBalance(amount)) {
            Toast.makeText(this, R.string.publish_insufficient_title, Toast.LENGTH_SHORT).show();
            return;
        }
        finishPublishLocal(amount);
    }

    private void remotePublish(double amount) {
        binding.btnNext.setEnabled(false);
        PublishTaskRequest request = new PublishTaskRequest();
        request.draftId = draftId;
        request.title = binding.etTitle.getText().toString().trim();
        request.description = binding.etDesc.getText().toString().trim();
        request.mode = selectedMode;
        request.category = selectedCategory;
        request.pickupAddress = pickupAddress;
        request.deliveryAddress = deliveryAddress;
        request.timeLabel = timeLabel;
        request.rewardCent = (int) Math.round(amount * 100);

        RetrofitClient.get().taskRemote().publish(request, new ApiCallback<PublishTaskResponse>() {
            @Override
            public void onSuccess(@NonNull PublishTaskResponse data) {
                RetrofitClient.get().walletRemote().loadWallet(new ApiCallback<com.campusgo.data.remote.dto.wallet.WalletResponse>() {
                    @Override
                    public void onSuccess(@NonNull com.campusgo.data.remote.dto.wallet.WalletResponse wallet) {
                        runOnUiThread(() -> finishRemotePublish(request, data, amount));
                    }

                    @Override
                    public void onError(@NonNull ApiException error) {
                        runOnUiThread(() -> finishRemotePublish(request, data, amount));
                    }
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() -> {
                    binding.btnNext.setEnabled(true);
                    Toast.makeText(PublishActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void finishRemotePublish(@NonNull PublishTaskRequest request,
                                     @NonNull PublishTaskResponse data,
                                     double amount) {
        binding.btnNext.setEnabled(true);
        String taskId = data.taskId != null ? data.taskId : "new";
        startActivity(TaskNavigator.publishSuccess(PublishActivity.this, request.title,
                selectedMode, amount, pickupAddress, deliveryAddress, taskId));
        finish();
    }

    private void showInsufficientDialog(double amount, double balance) {
        double shortfall = Math.round((amount - balance) * 100) / 100.0;
        new AlertDialog.Builder(this)
                .setTitle(R.string.publish_insufficient_title)
                .setMessage(getString(R.string.publish_insufficient_msg, shortfall))
                .setPositiveButton(R.string.publish_go_topup, (d, w) ->
                        topUpLauncher.launch(WalletNavigator.topUp(this, shortfall)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void finishPublishLocal(double amount) {
        String title = binding.etTitle.getText().toString().trim();
        if (draftId != null) {
            MockPublishDraftRepository.delete(this, draftId);
        }
        startActivity(TaskNavigator.publishSuccess(this, title, selectedMode, amount,
                pickupAddress, deliveryAddress, null));
        finish();
    }

    private void goStep(int step) {
        currentStep = Math.max(1, Math.min(3, step));
        binding.panelStep1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        binding.panelStep2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        binding.panelStep3.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);
        binding.btnPrev.setVisibility(currentStep > 1 ? View.VISIBLE : View.GONE);
        binding.btnNext.setText(currentStep == 3
                ? R.string.publish_submit
                : R.string.publish_next);
        styleStepIndicator(binding.step1Indicator, currentStep >= 1, currentStep == 1);
        styleStepIndicator(binding.step2Indicator, currentStep >= 2, currentStep == 2);
        styleStepIndicator(binding.step3Indicator, currentStep >= 3, currentStep == 3);
    }

    private void styleStepIndicator(@NonNull TextView tv, boolean reached, boolean active) {
        tv.setTextColor(ContextCompat.getColor(this, active || reached
                ? R.color.cg_brand
                : R.color.cg_text_tertiary));
        tv.setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            goStep(currentStep - 1);
        } else {
            super.onBackPressed();
        }
    }

    @NonNull
    private String modeLabel(@NonNull TaskMode mode) {
        switch (mode) {
            case GROUP:
                return getString(R.string.publish_mode_group_title);
            case EMERGENCY:
                return getString(R.string.publish_mode_emergency_title);
            case RESERVE:
                return getString(R.string.publish_mode_reserve_title);
            default:
                return getString(R.string.publish_mode_normal_title);
        }
    }

    @NonNull
    private String categoryLabel(@NonNull TaskCategory category) {
        switch (category) {
            case BUY:
                return getString(R.string.home_quick_buy);
            case ERRAND:
                return getString(R.string.home_quick_errand);
            default:
                return getString(R.string.home_quick_express);
        }
    }
}
