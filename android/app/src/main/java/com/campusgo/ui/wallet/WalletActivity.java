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

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockWalletRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.wallet.WalletResponse;
import com.campusgo.databinding.ActivityWalletBinding;
import com.campusgo.domain.model.WalletTransaction;

import java.util.Collections;
import java.util.List;

/**
 * W01 我的钱包
 */
public class WalletActivity extends AppCompatActivity {

    private ActivityWalletBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnTopUp.setOnClickListener(v ->
                startActivity(WalletNavigator.topUp(this)));
        binding.btnWithdraw.setOnClickListener(v ->
                startActivity(WalletNavigator.withdraw(this)));
        binding.btnWithdrawRecords.setOnClickListener(v ->
                startActivity(WalletNavigator.withdrawRecords(this)));

        renderBalance();
        loadTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().walletRemote().loadWallet(new ApiCallback<WalletResponse>() {
                @Override
                public void onSuccess(@NonNull WalletResponse data) {
                    runOnUiThread(WalletActivity.this::renderBalance);
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(WalletActivity.this::renderBalance);
                }
            });
            loadTransactions();
        } else {
            renderBalance();
            renderTransactions(MockWalletRepository.recentTransactions());
        }
    }

    private void renderBalance() {
        binding.tvBalance.setText(sessionManager.formatWalletBalance());
        binding.tvTotalIncome.setText(getString(R.string.wallet_total_income,
                String.format("¥%,.2f", sessionManager.getTotalIncome())));
        double frozen = sessionManager.getFrozenBalance();
        if (frozen > 0.001) {
            binding.tvFrozen.setVisibility(View.VISIBLE);
            binding.tvFrozen.setText(getString(R.string.wallet_frozen,
                    String.format("¥%,.2f", frozen)));
        } else {
            binding.tvFrozen.setVisibility(View.GONE);
        }
    }

    private void loadTransactions() {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().walletRemote().loadTransactions(new ApiCallback<List<WalletTransaction>>() {
                @Override
                public void onSuccess(@NonNull List<WalletTransaction> data) {
                    runOnUiThread(() -> renderTransactions(data));
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> renderTransactions(Collections.emptyList()));
                }
            });
            return;
        }
        renderTransactions(MockWalletRepository.recentTransactions());
    }

    private void renderTransactions(@NonNull List<WalletTransaction> transactions) {
        binding.txnList.removeAllViews();
        for (WalletTransaction txn : transactions) {
            binding.txnList.addView(buildTxnRow(txn));
        }
    }

    @NonNull
    private View buildTxnRow(@NonNull WalletTransaction txn) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView icon = new TextView(this);
        icon.setWidth(dp(36));
        icon.setHeight(dp(36));
        icon.setGravity(Gravity.CENTER);
        icon.setText(txn.type == WalletTransaction.Type.INCOME ? "↓" : "↑");
        icon.setTextColor(ContextCompat.getColor(this,
                txn.type == WalletTransaction.Type.INCOME
                        ? R.color.cg_success
                        : R.color.cg_danger));
        icon.setBackgroundResource(txn.type == WalletTransaction.Type.INCOME
                ? R.drawable.bg_txn_income
                : R.drawable.bg_txn_expense);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        infoLp.setMarginStart(dp(12));
        info.setLayoutParams(infoLp);

        TextView title = new TextView(this);
        title.setText(txn.title);
        title.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        title.setTextSize(14);

        TextView time = new TextView(this);
        time.setText(txn.timeLabel);
        time.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
        time.setTextSize(12);

        info.addView(title);
        info.addView(time);

        TextView amount = new TextView(this);
        amount.setText(txn.formatAmount());
        amount.setTextColor(ContextCompat.getColor(this,
                txn.type == WalletTransaction.Type.INCOME
                        ? R.color.cg_success
                        : R.color.cg_text_primary));
        amount.setTextSize(14);
        amount.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(icon);
        row.addView(info);
        row.addView(amount);
        return row;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
