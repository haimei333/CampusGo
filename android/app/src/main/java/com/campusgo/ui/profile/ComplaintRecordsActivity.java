package com.campusgo.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.data.mock.MockComplaintRepository;
import com.campusgo.databinding.ActivityComplaintRecordsBinding;
import com.campusgo.databinding.ItemComplaintRecordBinding;
import com.campusgo.domain.model.ComplaintRecord;

import java.util.List;

/**
 * S03 投诉记录
 */
public class ComplaintRecordsActivity extends AppCompatActivity {

    private ActivityComplaintRecordsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComplaintRecordsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());
        renderRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderRecords();
    }

    private void renderRecords() {
        List<ComplaintRecord> records = MockComplaintRepository.all(this);
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
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
                binding.recordList.addView(spacer);
            }
            ComplaintRecord record = records.get(i);
            ItemComplaintRecordBinding row = ItemComplaintRecordBinding.inflate(
                    getLayoutInflater(), binding.recordList, false);
            bindRecord(row, record);
            row.getRoot().setOnClickListener(v ->
                    startActivity(ProfileNavigator.complaintDetail(this, record.id)));
            binding.recordList.addView(row.getRoot());
        }
    }

    private void bindRecord(@NonNull ItemComplaintRecordBinding row, @NonNull ComplaintRecord record) {
        row.tvType.setText(record.type);
        row.tvTask.setText(getString(R.string.complaint_records_task, record.taskTitle));
        row.tvDesc.setText(record.description);
        row.tvTime.setText(record.timeLabel);
        styleStatus(row.tvStatus, record.status);
    }

    private void styleStatus(@NonNull TextView tv, @NonNull ComplaintRecord.Status status) {
        switch (status) {
            case DONE:
                tv.setText(R.string.complaint_records_status_done);
                tv.setBackgroundResource(R.drawable.bg_tag_group);
                tv.setTextColor(ContextCompat.getColor(this, R.color.cg_success));
                break;
            case REJECTED:
                tv.setText(R.string.complaint_records_status_rejected);
                tv.setBackgroundResource(R.drawable.bg_tag_emergency);
                tv.setTextColor(ContextCompat.getColor(this, R.color.cg_danger));
                break;
            case PENDING:
            default:
                tv.setText(R.string.complaint_records_status_pending);
                tv.setBackgroundResource(R.drawable.bg_tag_reserve);
                tv.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_reserve_text));
                break;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
