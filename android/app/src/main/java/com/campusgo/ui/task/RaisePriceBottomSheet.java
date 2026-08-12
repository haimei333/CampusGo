package com.campusgo.ui.task;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.databinding.BottomSheetRaisePriceBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * T06 加价半屏
 */
public class RaisePriceBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onRaiseConfirmed(int amount);
    }

    private static final String ARG_REWARD = "reward";

    @Nullable
    private Listener listener;
    private int selectedAmount = 3;

    public static RaisePriceBottomSheet newInstance(double currentReward) {
        RaisePriceBottomSheet sheet = new RaisePriceBottomSheet();
        Bundle args = new Bundle();
        args.putDouble(ARG_REWARD, currentReward);
        sheet.setArguments(args);
        return sheet;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        BottomSheetRaisePriceBinding binding =
                BottomSheetRaisePriceBinding.inflate(inflater, container, false);

        double reward = getArguments() != null
                ? getArguments().getDouble(ARG_REWARD, 15)
                : 15;
        binding.tvRaiseHint.setText(getString(R.string.raise_current_reward,
                String.format("¥%.2f", reward)));

        TextView[] chips = {binding.chipRaise3, binding.chipRaise5, binding.chipRaise10};
        int[] amounts = {3, 5, 10};
        for (int i = 0; i < chips.length; i++) {
            final int amount = amounts[i];
            chips[i].setOnClickListener(v -> {
                selectedAmount = amount;
                styleChips(chips, amount);
            });
        }
        styleChips(chips, selectedAmount);

        binding.btnDismiss.setOnClickListener(v -> dismiss());
        binding.btnConfirmRaise.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRaiseConfirmed(selectedAmount);
            }
            dismiss();
        });
        return binding.getRoot();
    }

    private void styleChips(@NonNull TextView[] chips, int selected) {
        int[] amounts = {3, 5, 10};
        for (int i = 0; i < chips.length; i++) {
            boolean active = amounts[i] == selected;
            chips[i].setBackgroundResource(active
                    ? R.drawable.bg_raise_chip_selected
                    : R.drawable.bg_raise_chip);
            chips[i].setTextColor(ContextCompat.getColor(requireContext(),
                    active ? R.color.cg_brand : R.color.cg_text_secondary));
            chips[i].setTypeface(null, active
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }
    }
}
