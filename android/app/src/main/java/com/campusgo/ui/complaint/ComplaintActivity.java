package com.campusgo.ui.complaint;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.R;
import com.campusgo.data.mock.MockComplaintRepository;
import com.campusgo.databinding.ActivityComplaintBinding;
import com.campusgo.ui.task.TaskNavigator;

/**
 * S02 投诉 / 申诉
 */
public class ComplaintActivity extends AppCompatActivity {

    public static final String MODE_COMPLAINT = "complaint";
    public static final String MODE_APPEAL = "appeal";

    private ActivityComplaintBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComplaintBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String mode = getIntent().getStringExtra(TaskNavigator.EXTRA_COMPLAINT_MODE);
        if (mode == null) {
            mode = MODE_COMPLAINT;
        }
        boolean appeal = MODE_APPEAL.equals(mode);

        binding.tvTitle.setText(appeal ? R.string.appeal_title : R.string.complaint_title);
        binding.btnSubmit.setText(appeal ? R.string.appeal_submit : R.string.complaint_submit);

        String title = getIntent().getStringExtra(TaskNavigator.EXTRA_PUBLISH_TITLE);
        double reward = getIntent().getDoubleExtra(TaskNavigator.EXTRA_PUBLISH_REWARD, 15);
        binding.tvTaskTitle.setText(title != null ? title : "取快递 - 中通快递");
        binding.tvReward.setText(String.format("¥%.2f", reward));

        setupTypes();
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> submit(appeal));
    }

    private void setupTypes() {
        String[] types = getResources().getStringArray(R.array.complaint_types);
        for (int i = 0; i < types.length; i++) {
            RadioButton radio = new RadioButton(this);
            radio.setId(View.generateViewId());
            radio.setText(types[i]);
            radio.setTextColor(getColor(R.color.cg_text_primary));
            radio.setPadding(0, dp(10), 0, dp(10));
            binding.radioTypes.addView(radio);
            if (i == 0) {
                radio.setChecked(true);
            }
        }
    }

    private void submit(boolean appeal) {
        String desc = binding.etDescription.getText().toString().trim();
        if (desc.isEmpty()) {
            Toast.makeText(this, R.string.complaint_need_desc, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!appeal) {
            String type = selectedType();
            MockComplaintRepository.addFromSubmit(this, type,
                    binding.tvTaskTitle.getText().toString(), desc);
        }
        Toast.makeText(this,
                appeal ? R.string.appeal_success : R.string.complaint_success,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    @NonNull
    private String selectedType() {
        int id = binding.radioTypes.getCheckedRadioButtonId();
        if (id != View.NO_ID) {
            android.widget.RadioButton radio = findViewById(id);
            if (radio != null && radio.getText() != null) {
                return radio.getText().toString();
            }
        }
        String[] types = getResources().getStringArray(R.array.complaint_types);
        return types.length > 0 ? types[0] : "";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
