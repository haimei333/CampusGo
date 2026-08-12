package com.campusgo.ui.wallet;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.data.mock.MockPointsRepository;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.points.CheckInResponseDto;
import com.campusgo.data.remote.dto.points.CheckInStatusDto;
import com.campusgo.databinding.ActivityCheckinBinding;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * G02 每日签到
 */
public class CheckInActivity extends AppCompatActivity {

    private static final String[] DAY_LABELS = { "一", "二", "三", "四", "五", "六", "日" };

    private ActivityCheckinBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRules.setOnClickListener(v -> showRules());
        binding.btnCheckIn.setOnClickListener(v -> doCheckIn());

        renderAll();
    }

    private Set<Integer> checkedInDays = new HashSet<>();

    private void renderAll() {
        Calendar cal = Calendar.getInstance();
        binding.tvMonth.setText(getString(R.string.checkin_month,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().pointsRemote().loadCheckInStatus(new ApiCallback<CheckInStatusDto>() {
                @Override
                public void onSuccess(@NonNull CheckInStatusDto data) {
                    runOnUiThread(() -> {
                        // Update local session cache
                        sessionManager.setCheckInStreak(data.streak);
                        sessionManager.setCheckedInToday(data.checkedInToday);

                        binding.tvStreakCount.setText(getString(R.string.checkin_streak_days,
                                data.streak));
                        binding.tvStreakHint.setText(getString(R.string.checkin_streak_days,
                                data.streak));
                        // Parse checked-in dates from API response
                        checkedInDays.clear();
                        if (data.monthDates != null) {
                            for (String dateStr : data.monthDates) {
                                try {
                                    // Format: "2026-08-09"
                                    String[] parts = dateStr.split("-");
                                    if (parts.length == 3) {
                                        checkedInDays.add(Integer.parseInt(parts[2]));
                                    }
                                } catch (Exception e) {
                                    // Ignore invalid dates
                                }
                            }
                        }
                        renderCalendar();
                        renderButton();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        renderCalendar();
                        renderButton();
                    });
                }
            });
        } else {
            binding.tvStreakCount.setText(getString(R.string.checkin_streak_days,
                    sessionManager.getCheckInStreak()));
            binding.tvStreakHint.setText(getString(R.string.checkin_streak_days,
                    sessionManager.getCheckInStreak()));
            renderCalendar();
            renderButton();
        }
    }

    private void renderCalendar() {
        // Render weekday header
        binding.weekdayHeader.removeAllViews();
        for (String label : DAY_LABELS) {
            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setTextSize(11);
            tv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
            tv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tv.setLayoutParams(lp);
            binding.weekdayHeader.addView(tv);
        }

        // Render calendar grid
        binding.calendarGrid.removeAllViews();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int today = cal.get(Calendar.DAY_OF_MONTH);

        // Get first day of month and days in month
        cal.set(year, month, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday, ..., 7=Saturday
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Convert to Monday-based index (0=Monday, 6=Sunday)
        int startOffset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        // Add empty cells for offset
        for (int i = 0; i < startOffset; i++) {
            TextView empty = new TextView(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = dp(40);
            lp.height = dp(40);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            empty.setLayoutParams(lp);
            binding.calendarGrid.addView(empty);
        }

        // Add day cells
        for (int day = 1; day <= daysInMonth; day++) {
            TextView dayCell = new TextView(this);
            dayCell.setText(String.valueOf(day));
            dayCell.setTextSize(13);
            dayCell.setGravity(Gravity.CENTER);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = dp(40);
            lp.height = dp(40);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            dayCell.setLayoutParams(lp);

            if (day == today) {
                // Today - highlight with brand color
                dayCell.setBackgroundResource(R.drawable.bg_checkin_day_done);
                dayCell.setTextColor(ContextCompat.getColor(this, R.color.cg_text_on_brand));
                dayCell.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (checkedInDays.contains(day)) {
                // Checked in day
                dayCell.setBackgroundResource(R.drawable.bg_checkin_day_done);
                dayCell.setTextColor(ContextCompat.getColor(this, R.color.cg_text_on_brand));
            } else if (day < today) {
                // Past day not checked in
                dayCell.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
            } else {
                // Future day
                dayCell.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
            }

            binding.calendarGrid.addView(dayCell);
        }
    }

    private void renderButton() {
        if (sessionManager.isCheckedInToday()) {
            binding.btnCheckIn.setText(R.string.checkin_done);
            binding.btnCheckIn.setAlpha(0.6f);
            binding.btnCheckIn.setEnabled(false);
        } else {
            binding.btnCheckIn.setText(R.string.checkin_submit);
            binding.btnCheckIn.setAlpha(1f);
            binding.btnCheckIn.setEnabled(true);
        }
    }

    private void doCheckIn() {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().pointsRemote().checkIn(new ApiCallback<CheckInResponseDto>() {
                @Override
                public void onSuccess(@NonNull CheckInResponseDto data) {
                    runOnUiThread(() -> {
                        // Update local session cache
                        sessionManager.setPoints(data.newBalance);
                        sessionManager.setCheckInStreak(data.newStreak);
                        sessionManager.setCheckedInToday(true);

                        Toast.makeText(CheckInActivity.this,
                                getString(R.string.checkin_success, data.rewardPoints), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        renderAll();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> Toast.makeText(CheckInActivity.this,
                            error.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            int reward = sessionManager.checkInToday();
            if (reward <= 0) {
                Toast.makeText(this, R.string.checkin_already, Toast.LENGTH_SHORT).show();
                return;
            }
            MockPointsRepository.addEarn(this, getString(R.string.points_earn_checkin), reward);
            Toast.makeText(this, getString(R.string.checkin_success, reward), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            renderAll();
        }
    }

    private void showRules() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.checkin_rules)
                .setMessage(R.string.checkin_rules_body)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
