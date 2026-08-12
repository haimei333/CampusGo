package com.campusgo.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockPointsRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityPointsHistoryBinding;
import com.campusgo.databinding.ItemPointsRecordBinding;
import com.campusgo.domain.model.PointsTransaction;

import java.util.List;

/**
 * 积分记录
 */
public class PointsHistoryActivity extends AppCompatActivity {

    private ActivityPointsHistoryBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPointsHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();
        binding.btnBack.setOnClickListener(v -> finish());
        renderAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderAll();
    }

    private void renderAll() {
        binding.tvBalance.setText(String.format("%,d", sessionManager.getPoints()));
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().pointsRemote().loadTransactions(new ApiCallback<List<PointsTransaction>>() {
                @Override
                public void onSuccess(@NonNull List<PointsTransaction> data) {
                    runOnUiThread(() -> renderRecordList(data));
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> renderRecordList(MockPointsRepository.all(PointsHistoryActivity.this)));
                }
            });
        } else {
            renderRecordList(MockPointsRepository.all(this));
        }
    }

    private void renderRecordList(@NonNull List<PointsTransaction> records) {
        binding.recordList.removeAllViews();
        boolean empty = records.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recordList.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        for (int i = 0; i < records.size(); i++) {
            if (i > 0) {
                View divider = new View(this);
                divider.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_divider));
                binding.recordList.addView(divider);
            }
            bindRecord(records.get(i));
        }
    }

    private void bindRecord(@NonNull PointsTransaction txn) {
        ItemPointsRecordBinding row = ItemPointsRecordBinding.inflate(
                getLayoutInflater(), binding.recordList, false);
        row.tvTitle.setText(txn.title);
        row.tvTime.setText(txn.timeLabel);
        boolean earn = txn.type == PointsTransaction.Type.EARN;
        row.tvIcon.setText(earn ? "+" : "-");
        row.tvIcon.setBackgroundResource(earn
                ? R.drawable.bg_txn_income
                : R.drawable.bg_txn_expense);
        row.tvIcon.setTextColor(ContextCompat.getColor(this,
                earn ? R.color.cg_success : R.color.cg_danger));
        String prefix = earn ? "+" : "-";
        row.tvPoints.setText(prefix + txn.points);
        row.tvPoints.setTextColor(ContextCompat.getColor(this,
                earn ? R.color.cg_success : R.color.cg_danger));
        binding.recordList.addView(row.getRoot());
    }
}
