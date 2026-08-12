package com.campusgo.ui.help;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.databinding.ActivityHelpBinding;

/**
 * S06 帮助与客服
 */
public class HelpActivity extends AppCompatActivity {

    private ActivityHelpBinding binding;
    private int expandedFaq = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.cardEmergency.setOnClickListener(v -> showEmergencyDialog());
        binding.btnOnlineService.setOnClickListener(v ->
                Toast.makeText(this, R.string.help_online_service_toast, Toast.LENGTH_SHORT).show());

        buildFaqList();
        buildCreditRules();
    }

    private void showEmergencyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.help_emergency_title)
                .setMessage(R.string.help_emergency_dialog)
                .setPositiveButton(R.string.help_emergency_call, (d, w) ->
                        Toast.makeText(this, R.string.help_emergency_call_toast, Toast.LENGTH_SHORT).show())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void buildFaqList() {
        binding.faqList.removeAllViews();
        String[] questions = getResources().getStringArray(R.array.help_faq_questions);
        String[] answers = getResources().getStringArray(R.array.help_faq_answers);
        for (int i = 0; i < questions.length; i++) {
            if (i > 0) {
                binding.faqList.addView(divider());
            }
            binding.faqList.addView(buildFaqItem(i, questions[i], answers[i]));
        }
    }

    @NonNull
    private View buildFaqItem(int index, @NonNull String question, @NonNull String answer) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(16), dp(16), dp(16));
        header.setBackgroundResource(android.R.drawable.list_selector_background);

        TextView title = new TextView(this);
        title.setText(question);
        title.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        title.setTextSize(14);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleLp);

        TextView chevron = new TextView(this);
        chevron.setText(expandedFaq == index ? "⌄" : "›");
        chevron.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
        chevron.setTextSize(16);

        TextView body = new TextView(this);
        body.setText(answer);
        body.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
        body.setTextSize(12);
        body.setLineSpacing(dp(2), 1f);
        body.setPadding(dp(16), 0, dp(16), dp(16));
        body.setVisibility(expandedFaq == index ? View.VISIBLE : View.GONE);

        header.addView(title);
        header.addView(chevron);
        header.setOnClickListener(v -> {
            expandedFaq = expandedFaq == index ? -1 : index;
            buildFaqList();
        });

        item.addView(header);
        item.addView(body);
        return item;
    }

    private void buildCreditRules() {
        binding.creditRuleList.removeAllViews();
        String[] labels = getResources().getStringArray(R.array.help_credit_rules);
        String[] values = getResources().getStringArray(R.array.help_credit_values);
        int[] colors = {
                R.color.cg_success,
                R.color.cg_warning,
                R.color.cg_danger,
                R.color.cg_danger
        };
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                binding.creditRuleList.addView(divider());
            }
            binding.creditRuleList.addView(buildRuleRow(labels[i], values[i], colors[i]));
        }
    }

    @NonNull
    private View buildRuleRow(@NonNull String label, @NonNull String value, int colorRes) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView labelTv = new TextView(this);
        labelTv.setText(label);
        labelTv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        labelTv.setTextSize(14);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelTv.setLayoutParams(labelLp);

        TextView valueTv = new TextView(this);
        valueTv.setText(value);
        valueTv.setTextColor(ContextCompat.getColor(this, colorRes));
        valueTv.setTextSize(14);
        valueTv.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(labelTv);
        row.addView(valueTv);
        return row;
    }

    @NonNull
    private View divider() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_divider));
        return v;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
