package com.campusgo.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.campusgo.R;
import com.campusgo.data.mock.MockTemplateRepository;
import com.campusgo.databinding.ActivityTemplatesBinding;
import com.campusgo.databinding.ItemTemplateBinding;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.TaskTemplate;

import java.util.List;

/**
 * T03 任务模板
 */
public class TemplateActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_TEMPLATE_ID = "result_template_id";

    private ActivityTemplatesBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTemplatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddTemplate.setOnClickListener(v -> showAddTemplateDialog());
        renderLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderLists();
    }

    private void renderLists() {
        renderSection(binding.systemList, MockTemplateRepository.systemTemplates(), false);
        renderSection(binding.userList, MockTemplateRepository.userTemplates(this), true);
    }

    private void renderSection(@NonNull LinearLayout container,
                               @NonNull List<TaskTemplate> templates,
                               boolean userSection) {
        container.removeAllViews();
        for (int i = 0; i < templates.size(); i++) {
            if (i > 0) {
                container.addView(divider());
            }
            bindTemplate(container, templates.get(i), userSection);
        }
    }

    private void bindTemplate(@NonNull LinearLayout container,
                              @NonNull TaskTemplate template,
                              boolean userSection) {
        ItemTemplateBinding row = ItemTemplateBinding.inflate(getLayoutInflater(), container, false);
        row.tvIcon.setText(template.iconEmoji);
        row.tvName.setText(template.name);
        row.tvSubtitle.setText(template.subtitle);
        row.btnDelete.setVisibility(userSection ? View.VISIBLE : View.GONE);
        row.btnDelete.setOnClickListener(v -> confirmDelete(template));
        row.getRoot().setOnClickListener(v -> returnTemplate(template));
        container.addView(row.getRoot());
    }

    private void returnTemplate(@NonNull TaskTemplate template) {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT_TEMPLATE_ID, template.id));
        finish();
    }

    private void confirmDelete(@NonNull TaskTemplate template) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.template_delete_title)
                .setMessage(getString(R.string.template_delete_msg, template.name))
                .setPositiveButton(R.string.address_delete, (d, w) -> {
                    MockTemplateRepository.deleteUser(this, template.id);
                    Toast.makeText(this, R.string.template_deleted, Toast.LENGTH_SHORT).show();
                    renderLists();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAddTemplateDialog() {
        View form = getLayoutInflater().inflate(R.layout.dialog_add_template, null);
        android.widget.EditText etName = form.findViewById(R.id.etName);
        android.widget.EditText etSubtitle = form.findViewById(R.id.etSubtitle);
        new AlertDialog.Builder(this)
                .setTitle(R.string.template_add)
                .setView(form)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.template_name_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String subtitle = etSubtitle.getText().toString().trim();
                    TaskTemplate template = new TaskTemplate(
                            "u" + System.currentTimeMillis(),
                            TaskTemplate.Source.USER,
                            name,
                            subtitle.isEmpty() ? getString(R.string.template_default_subtitle) : subtitle,
                            name,
                            subtitle,
                            TaskMode.NORMAL,
                            TaskCategory.EXPRESS,
                            "菜鸟驿站 · 东门",
                            "38号楼 512室 · 海淀区颐和园路5号北京大学",
                            getString(R.string.publish_time_asap),
                            10.0,
                            "📋");
                    MockTemplateRepository.addUser(this, template);
                    Toast.makeText(this, R.string.template_added, Toast.LENGTH_SHORT).show();
                    renderLists();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @NonNull
    private View divider() {
        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        view.setBackgroundColor(getColor(R.color.cg_divider));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.setMarginStart(dp(68));
        view.setLayoutParams(lp);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
