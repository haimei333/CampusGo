package com.campusgo.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityReviewBinding;
import com.campusgo.domain.model.TaskStatus;
import com.campusgo.domain.model.UserRole;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * T10 评价
 */
public class ReviewActivity extends AppCompatActivity {

    private ActivityReviewBinding binding;
    private int rating = 5;
    private UserRole viewerRole;
    private String taskId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewerRole = parseRole(getIntent().getStringExtra(TaskNavigator.EXTRA_VIEWER_ROLE));
        taskId = getIntent().getStringExtra(TaskNavigator.EXTRA_TASK_ID);
        String title = getIntent().getStringExtra(TaskNavigator.EXTRA_PUBLISH_TITLE);
        String sub = getIntent().getStringExtra(TaskNavigator.EXTRA_REVIEW_SUB);
        double reward = getIntent().getDoubleExtra(TaskNavigator.EXTRA_PUBLISH_REWARD, 15);

        binding.tvTaskTitle.setText(title != null ? title : "取快递");
        binding.tvTaskSub.setText(sub != null ? sub : "");
        binding.tvReward.setText(String.format("¥%.2f", reward));

        binding.btnBack.setOnClickListener(v -> finish());
        setupStars();
        setupTags();
        updateStarLabel();

        binding.etReview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvCharCount.setText(s.length() + "/100");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void setupStars() {
        binding.starBar.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            ImageButton btn = new ImageButton(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(40), dp(40));
            lp.setMarginEnd(dp(4));
            btn.setLayoutParams(lp);
            btn.setBackground(null);
            btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            btn.setImageResource(star <= rating ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            btn.setOnClickListener(v -> {
                rating = star;
                refreshStars();
                updateStarLabel();
            });
            binding.starBar.addView(btn);
        }
    }

    private void refreshStars() {
        for (int i = 0; i < binding.starBar.getChildCount(); i++) {
            View child = binding.starBar.getChildAt(i);
            if (child instanceof ImageButton) {
                ((ImageButton) child).setImageResource(i < rating
                        ? R.drawable.ic_star_filled
                        : R.drawable.ic_star_outline);
            }
        }
    }

    private void updateStarLabel() {
        String[] labels = getResources().getStringArray(R.array.review_star_labels);
        int index = Math.max(0, Math.min(rating - 1, labels.length - 1));
        binding.tvStarLabel.setText(labels[index]);
    }

    private void setupTags() {
        int arrayRes = viewerRole == UserRole.PUBLISHER
                ? R.array.review_tags_publisher
                : R.array.review_tags_runner;
        String[] tags = getResources().getStringArray(arrayRes);
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.cg_bg_card);
            chip.setChipStrokeWidth(1.5f);
            chip.setChipStrokeColorResource(R.color.cg_divider);
            chip.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_normal_text));
            chip.setOnCheckedChangeListener((button, checked) -> {
                if (checked) {
                    chip.setChipBackgroundColorResource(R.color.cg_brand);
                    chip.setChipStrokeColorResource(R.color.cg_brand);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.cg_text_on_brand));
                } else {
                    chip.setChipBackgroundColorResource(R.color.cg_bg_card);
                    chip.setChipStrokeColorResource(R.color.cg_divider);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_normal_text));
                }
            });
            binding.chipTags.addView(chip);
        }
    }

    private void submitReview() {
        if (rating < 1) {
            Toast.makeText(this, R.string.review_need_star, Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> tags = collectTags();
        String content = binding.etReview.getText().toString().trim();

        if (FeatureFlags.USE_REMOTE_API && taskId != null && !taskId.isEmpty()) {
            binding.btnSubmit.setEnabled(false);
            RetrofitClient.get().taskRemote().submitReview(taskId, rating, tags, content,
                    new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            if (FeatureFlags.USE_REMOTE_API) {
                                RetrofitClient.get().userRemote().loadProfile(new ApiCallback<com.campusgo.data.remote.dto.user.UserProfileDto>() {
                                    @Override
                                    public void onSuccess(@NonNull com.campusgo.data.remote.dto.user.UserProfileDto profile) {
                                        finishReviewSuccess();
                                    }

                                    @Override
                                    public void onError(@NonNull ApiException error) {
                                        finishReviewSuccess();
                                    }
                                });
                            } else {
                                finishReviewSuccess();
                            }
                        }

                        @Override
                        public void onError(@NonNull ApiException error) {
                            binding.btnSubmit.setEnabled(true);
                            Toast.makeText(ReviewActivity.this,
                                    error.getMessage() != null ? error.getMessage() : getString(R.string.review_need_star),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            return;
        }
        finishReviewSuccess();
    }

    private List<String> collectTags() {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < binding.chipTags.getChildCount(); i++) {
            View child = binding.chipTags.getChildAt(i);
            if (child instanceof Chip && ((Chip) child).isChecked()) {
                tags.add(((Chip) child).getText().toString());
            }
        }
        return tags;
    }

    private void finishReviewSuccess() {
        Intent data = new Intent()
                .putExtra(TaskNavigator.EXTRA_TASK_ID, taskId)
                .putExtra(TaskNavigator.EXTRA_STATUS, TaskStatus.REVIEWED.name());
        setResult(RESULT_OK, data);
        Toast.makeText(this, R.string.review_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    @NonNull
    private UserRole parseRole(@Nullable String raw) {
        if (raw == null) {
            return UserRole.PUBLISHER;
        }
        try {
            return UserRole.valueOf(raw);
        } catch (Exception e) {
            return UserRole.PUBLISHER;
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
