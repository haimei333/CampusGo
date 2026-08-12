package com.campusgo.ui.wallet;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.data.mock.MockWithdrawRepository;
import com.campusgo.databinding.ActivityWithdrawRecordsBinding;
import com.campusgo.databinding.ItemWithdrawRecordBinding;
import com.campusgo.domain.model.WithdrawRecord;

import java.util.List;

/**
 * W03 提现记录
 */
public class WithdrawRecordsActivity extends AppCompatActivity {

    private ActivityWithdrawRecordsBinding binding;
    @Nullable
    private WithdrawRecord.Status activeFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithdrawRecordsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());
        setupFilters();
        renderRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderRecords();
    }

    private void setupFilters() {
        FilterTab[] tabs = new FilterTab[]{
                new FilterTab(R.string.withdraw_filter_all, null),
                new FilterTab(R.string.withdraw_filter_processing, WithdrawRecord.Status.PROCESSING),
                new FilterTab(R.string.withdraw_filter_completed, WithdrawRecord.Status.COMPLETED),
                new FilterTab(R.string.withdraw_filter_failed, WithdrawRecord.Status.FAILED)
        };
        binding.filterTabs.removeAllViews();
        for (FilterTab tab : tabs) {
            TextView chip = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(14), 0, dp(14), 0);
            chip.setText(tab.labelRes);
            chip.setTextSize(13);
            boolean active = tab.status == activeFilter
                    || (tab.status == null && activeFilter == null);
            styleFilterChip(chip, active);
            chip.setOnClickListener(v -> {
                activeFilter = tab.status;
                setupFilters();
                renderRecords();
            });
            binding.filterTabs.addView(chip);
        }
    }

    private void styleFilterChip(@NonNull TextView chip, boolean active) {
        chip.setBackgroundResource(active
                ? R.drawable.bg_amount_chip_selected
                : R.drawable.bg_amount_chip);
        chip.setTextColor(ContextCompat.getColor(this,
                active ? R.color.cg_brand : R.color.cg_text_secondary));
        chip.setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void renderRecords() {
        List<WithdrawRecord> records = MockWithdrawRepository.filter(this, activeFilter);
        binding.recordList.removeAllViews();
        boolean empty = records.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        for (int i = 0; i < records.size(); i++) {
            if (i > 0) {
                View spacer = new View(this);
                spacer.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(10)));
                binding.recordList.addView(spacer);
            }
            bindRecord(records.get(i));
        }
    }

    private void bindRecord(@NonNull WithdrawRecord record) {
        ItemWithdrawRecordBinding row = ItemWithdrawRecordBinding.inflate(
                getLayoutInflater(), binding.recordList, false);
        row.tvTitle.setText(record.title);
        row.tvTime.setText(record.timeLabel);
        row.tvAmount.setText(String.format("-¥%.2f", record.amount));
        styleStatus(row.tvStatus, record.status);
        binding.recordList.addView(row.getRoot());
    }

    private void styleStatus(@NonNull TextView tv, @NonNull WithdrawRecord.Status status) {
        switch (status) {
            case COMPLETED:
                tv.setText(R.string.withdraw_status_completed);
                tv.setBackgroundResource(R.drawable.bg_tag_group);
                tv.setTextColor(ContextCompat.getColor(this, R.color.cg_success));
                break;
            case FAILED:
                tv.setText(R.string.withdraw_status_failed);
                tv.setBackgroundResource(R.drawable.bg_tag_emergency);
                tv.setTextColor(ContextCompat.getColor(this, R.color.cg_danger));
                break;
            case PROCESSING:
            default:
                tv.setText(R.string.withdraw_status_processing);
                tv.setBackgroundResource(R.drawable.bg_tag_reserve);
                tv.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_reserve_text));
                break;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class FilterTab {
        final int labelRes;
        @Nullable
        final WithdrawRecord.Status status;

        FilterTab(int labelRes, @Nullable WithdrawRecord.Status status) {
            this.labelRes = labelRes;
            this.status = status;
        }
    }
}
