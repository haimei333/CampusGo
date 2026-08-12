package com.campusgo.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.R;
import com.campusgo.databinding.BottomSheetCancelTaskBinding;
import com.campusgo.domain.model.TaskStatus;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * T11 取消任务半屏
 */
public class CancelTaskBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onCancelled(@NonNull String reason);
    }

    private static final String ARG_STATUS = "status";

    @Nullable
    private Listener listener;

    public static CancelTaskBottomSheet newInstance(@NonNull TaskStatus status) {
        CancelTaskBottomSheet sheet = new CancelTaskBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status.name());
        sheet.setArguments(args);
        return sheet;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        BottomSheetCancelTaskBinding binding =
                BottomSheetCancelTaskBinding.inflate(inflater, container, false);
        TaskStatus status = parseStatus();
        binding.tvRefundHint.setText(status == TaskStatus.PENDING
                ? getString(R.string.cancel_refund_pending)
                : getString(R.string.cancel_refund_accepted));

        String[] reasons = getResources().getStringArray(R.array.cancel_reasons);
        for (int i = 0; i < reasons.length; i++) {
            RadioButton radio = new RadioButton(requireContext());
            radio.setId(View.generateViewId());
            radio.setText(reasons[i]);
            radio.setTextColor(getResources().getColor(R.color.cg_text_primary, null));
            radio.setPadding(0, dp(12), 0, dp(12));
            binding.radioReasons.addView(radio);
            if (i == 0) {
                radio.setChecked(true);
            }
        }

        binding.radioReasons.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = group.findViewById(checkedId);
            boolean other = selected != null
                    && getString(R.string.cancel_reason_other).contentEquals(selected.getText());
            binding.etOtherReason.setVisibility(other ? View.VISIBLE : View.GONE);
        });

        binding.btnDismiss.setOnClickListener(v -> dismiss());
        binding.btnConfirmCancel.setOnClickListener(v -> {
            String reason = selectedReason(binding.radioReasons, binding.etOtherReason);
            if (reason.isEmpty()) {
                Toast.makeText(requireContext(), R.string.cancel_need_reason, Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) {
                listener.onCancelled(reason);
            }
            dismiss();
        });
        return binding.getRoot();
    }

    @NonNull
    private String selectedReason(@NonNull RadioGroup group, @NonNull EditText otherInput) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) {
            return "";
        }
        RadioButton selected = group.findViewById(id);
        if (selected == null) {
            return "";
        }
        if (getString(R.string.cancel_reason_other).contentEquals(selected.getText())) {
            return otherInput.getText().toString().trim();
        }
        return selected.getText().toString();
    }

    @NonNull
    private TaskStatus parseStatus() {
        Bundle args = getArguments();
        if (args == null) {
            return TaskStatus.PENDING;
        }
        try {
            return TaskStatus.valueOf(args.getString(ARG_STATUS, TaskStatus.PENDING.name()));
        } catch (Exception e) {
            return TaskStatus.PENDING;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
