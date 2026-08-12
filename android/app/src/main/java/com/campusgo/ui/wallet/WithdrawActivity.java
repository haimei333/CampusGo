package com.campusgo.ui.wallet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockWithdrawRepository;
import com.campusgo.databinding.ActivityWithdrawBinding;

/**
 * W02 提现申请
 */
public class WithdrawActivity extends AppCompatActivity {

    private static final double MIN_AMOUNT = 10;
    private static final double MAX_AMOUNT = 500;

    private ActivityWithdrawBinding binding;
    private SessionManager sessionManager;
    private double selectedAmount = 0;
    private TextView[] amountChips;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithdrawBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRecords.setOnClickListener(v ->
                startActivity(WalletNavigator.withdrawRecords(this)));
        binding.btnChangeAccount.setOnClickListener(v -> openAccountPage());
        binding.btnSubmit.setOnClickListener(v -> submitWithdraw());

        setupAmountGrid();
        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    selectedAmount = 0;
                    styleAmountChips();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        renderAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderAll();
    }

    private void renderAll() {
        binding.tvBalance.setText(sessionManager.formatWalletBalance());
        renderAccountCard();
    }

    private void renderAccountCard() {
        if (sessionManager.isWithdrawAccountBound()) {
            String type = sessionManager.getWithdrawAccountType();
            binding.tvAccountTitle.setText(sessionManager.withdrawTypeLabel(
                    type != null ? type : SessionManager.WITHDRAW_WECHAT));
            binding.tvAccountDesc.setText(sessionManager.getWithdrawAccountMask());
            binding.tvAccountIcon.setText(accountIcon(type));
            binding.btnChangeAccount.setText(R.string.withdraw_change_account);
        } else {
            binding.tvAccountTitle.setText(R.string.withdraw_account_unbound);
            binding.tvAccountDesc.setText(R.string.withdraw_account_unbound_hint);
            binding.tvAccountIcon.setText("!");
            binding.btnChangeAccount.setText(R.string.withdraw_bind_now);
        }
    }

    @NonNull
    private String accountIcon(@Nullable String type) {
        if (SessionManager.WITHDRAW_ALIPAY.equals(type)) {
            return "支";
        }
        if (SessionManager.WITHDRAW_BANK.equals(type)) {
            return "卡";
        }
        return "微";
    }

    private void setupAmountGrid() {
        String[] labels = {
                getString(R.string.withdraw_chip_all),
                "¥50", "¥100", "¥200"
        };
        amountChips = new TextView[labels.length];
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TextView chip = new TextView(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(i, 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            chip.setLayoutParams(lp);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(0, dp(12), 0, dp(12));
            chip.setText(labels[i]);
            chip.setTextSize(14);
            chip.setOnClickListener(v -> {
                if (index == 0) {
                    selectedAmount = sessionManager.getWalletBalance();
                    binding.etAmount.setText(formatAmountInput(selectedAmount));
                } else {
                    double[] presets = {50, 100, 200};
                    selectedAmount = presets[index - 1];
                    binding.etAmount.setText(formatAmountInput(selectedAmount));
                }
                styleAmountChips();
            });
            amountChips[i] = chip;
            binding.amountGrid.addView(chip);
        }
        styleAmountChips();
    }

    private void styleAmountChips() {
        double input = resolveAmount();
        for (int i = 0; i < amountChips.length; i++) {
            boolean active = false;
            if (i == 0) {
                active = input > 0 && Math.abs(input - sessionManager.getWalletBalance()) < 0.01;
            } else {
                double[] presets = {50, 100, 200};
                active = Math.abs(input - presets[i - 1]) < 0.01;
            }
            amountChips[i].setBackgroundResource(active
                    ? R.drawable.bg_amount_chip_selected
                    : R.drawable.bg_amount_chip);
            amountChips[i].setTextColor(ContextCompat.getColor(this,
                    active ? R.color.cg_brand : R.color.cg_text_primary));
        }
    }

    private void openAccountPage() {
        startActivity(WalletNavigator.withdrawAccount(this));
    }

    private void submitWithdraw() {
        if (!sessionManager.isWithdrawAccountBound()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.withdraw_need_bind_title)
                    .setMessage(R.string.withdraw_need_bind_msg)
                    .setPositiveButton(R.string.withdraw_bind_now, (d, w) -> openAccountPage())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }

        double amount = resolveAmount();
        if (amount < MIN_AMOUNT) {
            Toast.makeText(this, R.string.withdraw_min_amount, Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount > MAX_AMOUNT) {
            Toast.makeText(this, R.string.withdraw_max_amount, Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount > sessionManager.getWalletBalance()) {
            Toast.makeText(this, R.string.withdraw_insufficient, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.deductWalletBalance(amount)) {
            Toast.makeText(this, R.string.withdraw_insufficient, Toast.LENGTH_SHORT).show();
            return;
        }

        String accountLabel = sessionManager.getWithdrawAccountDisplay();
        MockWithdrawRepository.addFromSubmit(this, amount, accountLabel);
        Toast.makeText(this, R.string.withdraw_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private double resolveAmount() {
        String raw = binding.etAmount.getText().toString().trim();
        if (raw.isEmpty()) {
            return selectedAmount;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @NonNull
    private String formatAmountInput(double amount) {
        if (Math.abs(amount - Math.rint(amount)) < 0.001) {
            return String.valueOf((long) amount);
        }
        return String.format("%.2f", amount);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
