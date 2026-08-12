package com.campusgo.ui.wallet;

import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityTopupBinding;

/**
 * W04 充值
 */
public class TopUpActivity extends AppCompatActivity {

    private static final int[] PRESET_AMOUNTS = {10, 20, 50, 100, 200, 500};

    private ActivityTopupBinding binding;
    private SessionManager sessionManager;
    private int selectedAmount = 100;
    private TextView[] amountChips;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTopupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        double suggest = getIntent().getDoubleExtra(WalletNavigator.EXTRA_SUGGEST_AMOUNT, 0);
        if (suggest > 0) {
            selectedAmount = pickPreset((int) Math.ceil(suggest));
        }

        binding.btnBack.setOnClickListener(v -> finish());
        refreshBalanceLabel();
        setupAmountGrid();
        styleAmountChips();

        binding.etCustomAmount.addTextChangedListener(new TextWatcher() {
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

        binding.btnSubmit.setOnClickListener(v -> submitTopUp());
    }

    private void refreshBalanceLabel() {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().walletRemote().loadWallet(new ApiCallback<com.campusgo.data.remote.dto.wallet.WalletResponse>() {
                @Override
                public void onSuccess(@NonNull com.campusgo.data.remote.dto.wallet.WalletResponse data) {
                    binding.tvBalance.setText(sessionManager.formatWalletBalance());
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    binding.tvBalance.setText(sessionManager.formatWalletBalance());
                }
            });
        } else {
            binding.tvBalance.setText(sessionManager.formatWalletBalance());
        }
    }

    private int pickPreset(int min) {
        for (int amount : PRESET_AMOUNTS) {
            if (amount >= min) {
                return amount;
            }
        }
        return PRESET_AMOUNTS[PRESET_AMOUNTS.length - 1];
    }

    private void setupAmountGrid() {
        amountChips = new TextView[PRESET_AMOUNTS.length];
        for (int i = 0; i < PRESET_AMOUNTS.length; i++) {
            final int amount = PRESET_AMOUNTS[i];
            TextView chip = new TextView(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(i % 3, 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            chip.setLayoutParams(lp);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(0, dp(16), 0, dp(16));
            chip.setText(getString(R.string.topup_amount_value, amount));
            chip.setTextSize(16);
            chip.setOnClickListener(v -> {
                selectedAmount = amount;
                binding.etCustomAmount.setText("");
                styleAmountChips();
            });
            amountChips[i] = chip;
            binding.amountGrid.addView(chip);
        }
    }

    private void styleAmountChips() {
        for (int i = 0; i < PRESET_AMOUNTS.length; i++) {
            boolean active = selectedAmount == PRESET_AMOUNTS[i];
            amountChips[i].setBackgroundResource(active
                    ? R.drawable.bg_amount_chip_selected
                    : R.drawable.bg_amount_chip);
            amountChips[i].setTextColor(ContextCompat.getColor(this,
                    active ? R.color.cg_brand : R.color.cg_text_primary));
            amountChips[i].setTypeface(null, active
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }
    }

    private void submitTopUp() {
        double amount = resolveAmount();
        if (amount <= 0) {
            Toast.makeText(this, R.string.topup_invalid_amount, Toast.LENGTH_SHORT).show();
            return;
        }
        if (FeatureFlags.USE_REMOTE_API) {
            binding.btnSubmit.setEnabled(false);
            RetrofitClient.get().walletRemote().topup(amount, new ApiCallback<com.campusgo.data.remote.dto.wallet.WalletResponse>() {
                @Override
                public void onSuccess(@NonNull com.campusgo.data.remote.dto.wallet.WalletResponse data) {
                    binding.btnSubmit.setEnabled(true);
                    finishTopUp(amount);
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    binding.btnSubmit.setEnabled(true);
                    Toast.makeText(TopUpActivity.this,
                            error.getMessage() != null ? error.getMessage() : getString(R.string.topup_invalid_amount),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        sessionManager.addWalletBalance(amount);
        finishTopUp(amount);
    }

    private void finishTopUp(double amount) {
        Intent data = new Intent().putExtra(WalletNavigator.EXTRA_TOPUP_RESULT, amount);
        setResult(RESULT_OK, data);
        Toast.makeText(this, getString(R.string.topup_success, amount), Toast.LENGTH_SHORT).show();
        finish();
    }

    private double resolveAmount() {
        String custom = binding.etCustomAmount.getText().toString().trim();
        if (!custom.isEmpty()) {
            try {
                return Double.parseDouble(custom);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return selectedAmount;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
