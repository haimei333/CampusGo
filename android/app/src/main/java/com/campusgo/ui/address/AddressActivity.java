package com.campusgo.ui.address;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.data.mock.MockAddressRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityAddressBinding;
import com.campusgo.databinding.ItemAddressBinding;
import com.campusgo.domain.model.SavedAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * T04 地址管理 / 选择
 */
public class AddressActivity extends AppCompatActivity {

    private ActivityAddressBinding binding;
    private boolean selectMode;
    private boolean pickupSelect;
    private final List<SavedAddress> cached = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectMode = getIntent().getBooleanExtra(AddressNavigator.EXTRA_SELECT_MODE, false);
        pickupSelect = getIntent().getBooleanExtra(AddressNavigator.EXTRA_PICKUP, false);

        binding.btnBack.setOnClickListener(v -> finish());
        if (selectMode) {
            binding.tvTitle.setText(R.string.address_select_title);
            binding.btnAdd.setText(R.string.address_manual_input);
            binding.btnAdd.setOnClickListener(v -> showManualInputDialog());
        } else {
            binding.btnAdd.setOnClickListener(v ->
                    startActivity(AddressNavigator.add(this)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }

    private void loadAddresses() {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().addressRemote().list(new ApiCallback<List<SavedAddress>>() {
                @Override
                public void onSuccess(@NonNull List<SavedAddress> data) {
                    runOnUiThread(() -> {
                        cached.clear();
                        cached.addAll(data);
                        renderList(cached);
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddressActivity.this,
                                error.getMessage() != null ? error.getMessage() : "加载地址失败",
                                Toast.LENGTH_SHORT).show();
                        cached.clear();
                        renderList(cached);
                    });
                }
            });
        } else {
            cached.clear();
            cached.addAll(MockAddressRepository.all(this));
            renderList(cached);
        }
    }

    private void renderList(@NonNull List<SavedAddress> addresses) {
        binding.addressList.removeAllViews();

        if (selectMode) {
            binding.addressList.addView(buildManualInputRow());
            if (!addresses.isEmpty()) {
                View spacer = new View(this);
                spacer.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(10)));
                binding.addressList.addView(spacer);
            }
        }

        boolean empty = addresses.isEmpty();
        if (selectMode) {
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.scrollContent.setVisibility(View.VISIBLE);
            if (empty) {
                binding.emptyState.setText(R.string.address_select_empty);
            }
        } else {
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.scrollContent.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (empty) {
                return;
            }
        }

        for (int i = 0; i < addresses.size(); i++) {
            if (i > 0) {
                View spacer = new View(this);
                spacer.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(10)));
                binding.addressList.addView(spacer);
            }
            bindAddress(addresses.get(i));
        }
    }

    @NonNull
    private View buildManualInputRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int pad = dp(16);
        row.setPadding(pad, pad, pad, pad);
        row.setClickable(true);
        row.setFocusable(true);

        TextView icon = new TextView(this);
        icon.setText("✎");
        icon.setTextSize(18);
        icon.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));

        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        labelLp.setMarginStart(dp(12));
        label.setLayoutParams(labelLp);
        label.setText(R.string.address_manual_input);
        label.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));
        label.setTextSize(15);
        label.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
        arrow.setTextSize(18);

        row.addView(icon);
        row.addView(label);
        row.addView(arrow);
        row.setOnClickListener(v -> showManualInputDialog());
        return row;
    }

    private void showManualInputDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(2);
        input.setHint(selectMode && pickupSelect
                ? getString(R.string.publish_pickup)
                : getString(R.string.address_manual_input_hint));
        int pad = dp(16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle(R.string.address_manual_input_title)
                .setView(input)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, R.string.address_manual_input_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    returnCustomSelection(text);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void bindAddress(@NonNull SavedAddress address) {
        ItemAddressBinding row = ItemAddressBinding.inflate(getLayoutInflater(), binding.addressList, false);
        row.tvTitle.setText(address.title);
        row.tvDetail.setText(address.detail);
        row.tvDefaultTag.setVisibility(address.isDefault ? View.VISIBLE : View.GONE);
        styleTypeTag(row.tvTypeTag, address.type);
        if (selectMode) {
            row.btnEdit.setVisibility(View.GONE);
            row.getRoot().setOnClickListener(v -> returnSelection(address));
        } else {
            row.getRoot().setOnClickListener(null);
            row.btnEdit.setOnClickListener(v ->
                    startActivity(AddressNavigator.edit(this, address.id)));
            row.getRoot().setOnLongClickListener(v -> {
                confirmDelete(address);
                return true;
            });
        }
        binding.addressList.addView(row.getRoot());
    }

    private void returnSelection(@NonNull SavedAddress address) {
        returnCustomSelection(address.formatFull());
    }

    private void returnCustomSelection(@NonNull String display) {
        Intent data = new Intent()
                .putExtra(AddressNavigator.EXTRA_RESULT_DISPLAY, display);
        setResult(RESULT_OK, data);
        finish();
    }

    private void confirmDelete(@NonNull SavedAddress address) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.address_delete_title)
                .setMessage(getString(R.string.address_delete_msg, address.title))
                .setPositiveButton(R.string.address_delete, (d, w) -> deleteAddress(address))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteAddress(@NonNull SavedAddress address) {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().addressRemote().delete(address.id, new ApiCallback<Void>() {
                @Override
                public void onSuccess(@NonNull Void data) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddressActivity.this, R.string.address_deleted, Toast.LENGTH_SHORT).show();
                        loadAddresses();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> Toast.makeText(AddressActivity.this,
                            error.getMessage() != null ? error.getMessage() : "删除失败",
                            Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            MockAddressRepository.delete(this, address.id);
            Toast.makeText(this, R.string.address_deleted, Toast.LENGTH_SHORT).show();
            loadAddresses();
        }
    }

    private void styleTypeTag(@NonNull TextView tag, @NonNull SavedAddress.Type type) {
        tag.setText(typeLabel(type));
        switch (type) {
            case DORM:
                tag.setBackgroundResource(R.drawable.bg_tag_emergency);
                tag.setTextColor(ContextCompat.getColor(this, R.color.cg_accent));
                break;
            case BUILDING:
                tag.setBackgroundResource(R.drawable.bg_tag_reserve);
                tag.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_reserve_text));
                break;
            case LIBRARY:
                tag.setBackgroundResource(R.drawable.bg_tag_group);
                tag.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_group_text));
                break;
            case CANTEEN:
                tag.setBackgroundResource(R.drawable.bg_txn_income);
                tag.setTextColor(ContextCompat.getColor(this, R.color.cg_success));
                break;
            case OTHER:
            default:
                tag.setBackgroundResource(R.drawable.bg_tag);
                tag.setTextColor(ContextCompat.getColor(this, R.color.cg_tag_normal_text));
                break;
        }
    }

    private int typeLabel(@NonNull SavedAddress.Type type) {
        switch (type) {
            case DORM:
                return R.string.address_type_dorm;
            case BUILDING:
                return R.string.address_type_building;
            case LIBRARY:
                return R.string.address_type_library;
            case CANTEEN:
                return R.string.address_type_canteen;
            case OTHER:
            default:
                return R.string.address_type_other;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
