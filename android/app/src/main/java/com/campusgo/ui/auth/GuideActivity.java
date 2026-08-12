package com.campusgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.databinding.ActivityGuideBinding;
import com.campusgo.ui.main.MainActivity;

/**
 * A02 新手引导（3 步）
 */
public class GuideActivity extends AppCompatActivity {

    private static final int STEP_COUNT = 3;

    private ActivityGuideBinding binding;
    private SessionManager sessionManager;
    private int stepIndex;

    private final int[] emojiRes = {
            R.string.guide_step1_emoji,
            R.string.guide_step2_emoji,
            R.string.guide_step3_emoji
    };
    private final int[] titleRes = {
            R.string.guide_step1_title,
            R.string.guide_step2_title,
            R.string.guide_step3_title
    };
    private final int[] descRes = {
            R.string.guide_step1_desc,
            R.string.guide_step2_desc,
            R.string.guide_step3_desc
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnSkip.setOnClickListener(v -> finishGuide());
        binding.btnNext.setOnClickListener(v -> onNext());
        renderStep();
    }

    private void onNext() {
        if (stepIndex >= STEP_COUNT - 1) {
            finishGuide();
            return;
        }
        stepIndex++;
        renderStep();
    }

    private void renderStep() {
        binding.tvStepEmoji.setText(getString(emojiRes[stepIndex]));
        binding.tvStepTitle.setText(titleRes[stepIndex]);
        binding.tvStepDesc.setText(descRes[stepIndex]);
        binding.btnNext.setText(stepIndex >= STEP_COUNT - 1
                ? R.string.guide_start
                : R.string.guide_next);
        updateDots();
    }

    private void updateDots() {
        View[] dots = {binding.dot0, binding.dot1, binding.dot2};
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i == stepIndex
                    ? R.drawable.bg_guide_dot_active
                    : R.drawable.bg_guide_dot_inactive);
        }
    }

    private void finishGuide() {
        sessionManager.setGuideShown(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
