package com.campusgo.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.R;
import com.campusgo.databinding.ActivityLegalDocumentBinding;

/**
 * 隐私政策 / 用户协议 Mock 页
 */
public class LegalDocumentActivity extends AppCompatActivity {

    public static final String EXTRA_DOC_TYPE = "doc_type";
    public static final String TYPE_PRIVACY = "privacy";
    public static final String TYPE_AGREEMENT = "agreement";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLegalDocumentBinding binding = ActivityLegalDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String type = getIntent().getStringExtra(EXTRA_DOC_TYPE);
        if (TYPE_AGREEMENT.equals(type)) {
            binding.tvTitle.setText(R.string.settings_user_agreement);
            binding.tvContent.setText(R.string.legal_user_agreement_body);
        } else {
            binding.tvTitle.setText(R.string.settings_privacy);
            binding.tvContent.setText(R.string.legal_privacy_body);
        }

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
