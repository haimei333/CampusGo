package com.campusgo.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.points.UserVoucherDto;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyVouchersActivity extends AppCompatActivity {

    private RecyclerView recyclerVouchers;
    private TextView tvEmpty;
    private TextView tabAll, tabUnused, tabUsed;
    private VoucherAdapter adapter;
    private List<UserVoucherDto> allVouchers = new ArrayList<>();
    private String currentFilter = null; // null=all, UNUSED, USED

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, MyVouchersActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_vouchers);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerVouchers = findViewById(R.id.recyclerVouchers);
        tvEmpty = findViewById(R.id.tvEmpty);
        tabAll = findViewById(R.id.tabAll);
        tabUnused = findViewById(R.id.tabUnused);
        tabUsed = findViewById(R.id.tabUsed);

        adapter = new VoucherAdapter();
        recyclerVouchers.setLayoutManager(new LinearLayoutManager(this));
        recyclerVouchers.setAdapter(adapter);

        setupTabs();
        loadVouchers();
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> {
            currentFilter = null;
            updateTabStyles();
            filterAndRender();
        });

        tabUnused.setOnClickListener(v -> {
            currentFilter = "UNUSED";
            updateTabStyles();
            filterAndRender();
        });

        tabUsed.setOnClickListener(v -> {
            currentFilter = "USED";
            updateTabStyles();
            filterAndRender();
        });
    }

    private void updateTabStyles() {
        int brandColor = getResources().getColor(R.color.cg_brand);
        int secondaryColor = getResources().getColor(R.color.cg_text_secondary);

        tabAll.setTextColor(currentFilter == null ? brandColor : secondaryColor);
        tabUnused.setTextColor("UNUSED".equals(currentFilter) ? brandColor : secondaryColor);
        tabUsed.setTextColor("USED".equals(currentFilter) ? brandColor : secondaryColor);
    }

    private static final String TAG = "MyVouchers";

    private void loadVouchers() {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().voucherRemote().loadVouchers(currentFilter, new ApiCallback<List<UserVoucherDto>>() {
                @Override
                public void onSuccess(@NonNull List<UserVoucherDto> data) {
                    Log.d(TAG, "loadVouchers success, size=" + (data != null ? data.size() : "null"));
                    runOnUiThread(() -> {
                        allVouchers = data != null ? data : new ArrayList<>();
                        filterAndRender();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    Log.e(TAG, "loadVouchers error: " + error.getMessage());
                    runOnUiThread(() -> {
                        showToast("加载失败：" + error.getMessage());
                        allVouchers.clear();
                        filterAndRender();
                    });
                }
            });
        } else {
            Log.d(TAG, "USE_REMOTE_API is false, showing empty");
            allVouchers.clear();
            filterAndRender();
        }
    }

    private void filterAndRender() {
        List<UserVoucherDto> filtered = allVouchers;
        if (currentFilter != null) {
            filtered = new ArrayList<>();
            for (UserVoucherDto v : allVouchers) {
                if (currentFilter.equals(v.status)) {
                    filtered.add(v);
                }
            }
        }

        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerVouchers.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        adapter.setData(filtered);
    }

    private void useVoucher(@NonNull UserVoucherDto voucher) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.voucher_use_confirm_title)
                .setMessage(getString(R.string.voucher_use_confirm_msg, voucher.productName))
                .setPositiveButton(R.string.voucher_use_confirm_ok, (dialog, which) -> {
                    if (FeatureFlags.USE_REMOTE_API) {
                        RetrofitClient.get().voucherRemote().useVoucher(voucher.voucherCode,
                                new ApiCallback<UserVoucherDto>() {
                                    @Override
                                    public void onSuccess(@NonNull UserVoucherDto data) {
                                        runOnUiThread(() -> {
                                            showToast(getString(R.string.voucher_use_success));
                                            loadVouchers();
                                        });
                                    }

                                    @Override
                                    public void onError(@NonNull com.campusgo.data.remote.ApiException error) {
                                        runOnUiThread(() -> showToast(error.getMessage()));
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {
        private List<UserVoucherDto> data = new ArrayList<>();

        public void setData(List<UserVoucherDto> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserVoucherDto voucher = data.get(position);
            holder.tvProductName.setText(voucher.productName);
            holder.tvVoucherCode.setText(voucher.voucherCode);
            holder.tvExpireAt.setText(voucher.expireAt);

            if ("UNUSED".equals(voucher.status)) {
                holder.tvStatus.setText(R.string.voucher_status_unused);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.cg_brand));
                holder.btnUse.setVisibility(View.VISIBLE);
                holder.btnUse.setOnClickListener(v -> useVoucher(voucher));
            } else if ("USED".equals(voucher.status)) {
                holder.tvStatus.setText(R.string.voucher_status_used);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.cg_text_tertiary));
                holder.btnUse.setVisibility(View.GONE);
            } else {
                holder.tvStatus.setText(R.string.voucher_status_expired);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.cg_text_tertiary));
                holder.btnUse.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProductName, tvVoucherCode, tvStatus, tvExpireAt, btnUse;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvExpireAt = itemView.findViewById(R.id.tvExpireAt);
                btnUse = itemView.findViewById(R.id.btnUse);
            }
        }
    }
}
