package com.campusgo.ui.wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.databinding.ActivityWithdrawAccountBinding;

/**
 * W05 绑定提现账户
 */
public class WithdrawAccountActivity extends AppCompatActivity {

    private ActivityWithdrawAccountBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithdrawAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.rowWechat.setOnClickListener(v -> confirmBind(SessionManager.WITHDRAW_WECHAT));
        binding.rowAlipay.setOnClickListener(v -> confirmBind(SessionManager.WITHDRAW_ALIPAY));
        binding.rowBank.setOnClickListener(v -> confirmBind(SessionManager.WITHDRAW_BANK));
        binding.btnUnbind.setOnClickListener(v -> confirmUnbind());
        renderBoundCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderBoundCard();
    }

    private void renderBoundCard() {
        if (sessionManager.isWithdrawAccountBound()) {
            binding.boundCard.setVisibility(View.VISIBLE);
            String type = sessionManager.getWithdrawAccountType();
            binding.tvBoundTitle.setText(sessionManager.withdrawTypeLabel(
                    type != null ? type : SessionManager.WITHDRAW_WECHAT));
            binding.tvBoundMask.setText(sessionManager.getWithdrawAccountMask());
            binding.tvBoundIcon.setText(iconFor(type));
        } else {
            binding.boundCard.setVisibility(View.GONE);
        }
    }

    @NonNull
    private String iconFor(@Nullable String type) {
        if (SessionManager.WITHDRAW_ALIPAY.equals(type)) {
            return "支";
        }
        if (SessionManager.WITHDRAW_BANK.equals(type)) {
            return "卡";
        }
        return "微";
    }

    private void confirmBind(@NonNull String type) {
        String label = sessionManager.withdrawTypeLabel(type);
        new AlertDialog.Builder(this)
                .setTitle(R.string.withdraw_bind_confirm_title)
                .setMessage(getString(R.string.withdraw_bind_confirm_msg, label,
                        sessionManager.defaultWithdrawMask()))
                .setPositiveButton(R.string.withdraw_bind_confirm, (d, w) -> {
                    sessionManager.bindWithdrawAccount(type, sessionManager.defaultWithdrawMask());
                    Toast.makeText(this, R.string.withdraw_bind_success, Toast.LENGTH_SHORT).show();
                    renderBoundCard();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmUnbind() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.withdraw_unbind_title)
                .setMessage(R.string.withdraw_unbind_msg)
                .setPositiveButton(R.string.withdraw_unbind, (d, w) -> {
                    sessionManager.unbindWithdrawAccount();
                    Toast.makeText(this, R.string.withdraw_unbind_success, Toast.LENGTH_SHORT).show();
                    renderBoundCard();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
