package com.campusgo.ui.profile;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.data.mock.MockComplaintRepository;
import com.campusgo.databinding.ActivityComplaintDetailBinding;
import com.campusgo.domain.model.ComplaintRecord;

/**
 * S03 投诉详情
 */
public class ComplaintDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECORD_ID = ProfileNavigator.EXTRA_COMPLAINT_ID;

    private ActivityComplaintDetailBinding binding;
    private ComplaintRecord record;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComplaintDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String id = getIntent().getStringExtra(EXTRA_RECORD_ID);
        if (id == null) {
            finish();
            return;
        }
        record = MockComplaintRepository.findById(this, id);
        if (record == null) {
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnContact.setOnClickListener(v ->
                Toast.makeText(this, R.string.help_online_service_toast, Toast.LENGTH_SHORT).show());
        renderAll();
    }

    private void renderAll() {
        binding.tvTaskTitle.setText(record.taskTitle);
        binding.tvType.setText(getString(R.string.complaint_detail_type, record.type));
        binding.tvDescription.setText(record.description);
        binding.tvTime.setText(getString(R.string.complaint_detail_time, record.timeLabel));
        renderStatusBanner();
        renderTimeline();
        renderResult();
        binding.btnContact.setVisibility(record.status == ComplaintRecord.Status.PENDING
                ? View.VISIBLE : View.GONE);
    }

    private void renderStatusBanner() {
        switch (record.status) {
            case DONE:
                binding.tvStatusBanner.setText(R.string.complaint_records_status_done);
                binding.tvStatusBanner.setBackgroundResource(R.drawable.bg_tag_group);
                binding.tvStatusBanner.setTextColor(ContextCompat.getColor(this, R.color.cg_success));
                break;
            case REJECTED:
                binding.tvStatusBanner.setText(R.string.complaint_records_status_rejected);
                binding.tvStatusBanner.setBackgroundResource(R.drawable.bg_tag_emergency);
                binding.tvStatusBanner.setTextColor(ContextCompat.getColor(this, R.color.cg_danger));
                break;
            case PENDING:
            default:
                binding.tvStatusBanner.setText(R.string.complaint_records_status_pending);
                binding.tvStatusBanner.setBackgroundResource(R.drawable.bg_tag_reserve);
                binding.tvStatusBanner.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_reserve_text));
                break;
        }
    }

    private void renderTimeline() {
        binding.timelineList.removeAllViews();
        addTimelineStep(getString(R.string.complaint_detail_step_submit), record.timeLabel, true);
        switch (record.status) {
            case DONE:
                addTimelineStep(getString(R.string.complaint_detail_step_processing),
                        getString(R.string.complaint_detail_step_processing_time), true);
                addTimelineStep(getString(R.string.complaint_detail_step_done),
                        getString(R.string.complaint_detail_step_done_time), true);
                break;
            case REJECTED:
                addTimelineStep(getString(R.string.complaint_detail_step_processing),
                        getString(R.string.complaint_detail_step_processing_time), true);
                addTimelineStep(getString(R.string.complaint_detail_step_rejected),
                        getString(R.string.complaint_detail_step_rejected_time), true);
                break;
            case PENDING:
            default:
                addTimelineStep(getString(R.string.complaint_detail_step_processing),
                        getString(R.string.complaint_detail_step_wait), false);
                addTimelineStep(getString(R.string.complaint_detail_step_result),
                        getString(R.string.complaint_detail_step_pending), false);
                break;
        }
    }

    private void addTimelineStep(@NonNull String title, @NonNull String time, boolean done) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView dot = new TextView(this);
        dot.setWidth(dp(8));
        dot.setHeight(dp(8));
        dot.setBackgroundResource(done
                ? R.drawable.bg_heat_dot_hot
                : R.drawable.bg_heat_dot);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(8), dp(8));
        dotLp.topMargin = dp(6);
        dot.setLayoutParams(dotLp);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.setMarginStart(dp(12));
        info.setLayoutParams(infoLp);

        TextView titleTv = new TextView(this);
        titleTv.setText(title);
        titleTv.setTextColor(ContextCompat.getColor(this,
                done ? R.color.cg_text_primary : R.color.cg_text_tertiary));
        titleTv.setTextSize(14);
        titleTv.setTypeface(null, done ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        TextView timeTv = new TextView(this);
        timeTv.setText(time);
        timeTv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
        timeTv.setTextSize(12);

        info.addView(titleTv);
        info.addView(timeTv);
        row.addView(dot);
        row.addView(info);
        binding.timelineList.addView(row);
    }

    private void renderResult() {
        if (record.status == ComplaintRecord.Status.PENDING) {
            binding.panelResult.setVisibility(View.GONE);
            return;
        }
        binding.panelResult.setVisibility(View.VISIBLE);
        int msgRes;
        switch (record.status) {
            case DONE:
                msgRes = R.string.complaint_detail_result_done;
                break;
            case REJECTED:
                msgRes = R.string.complaint_detail_result_rejected;
                break;
            default:
                msgRes = R.string.complaint_detail_result_pending;
                break;
        }
        binding.tvResult.setText(msgRes);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
