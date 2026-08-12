package com.campusgo.ui.address;

import android.os.Bundle;
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

import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.data.mock.MockAddressRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.ActivityAddressEditBinding;
import com.campusgo.domain.model.SavedAddress;

/**
 * T04 新增 / 编辑地址
 */
public class AddressEditActivity extends AppCompatActivity {

    private static final SavedAddress.Type[] TYPES = {
            SavedAddress.Type.DORM,
            SavedAddress.Type.BUILDING,
            SavedAddress.Type.LIBRARY,
            SavedAddress.Type.CANTEEN,
            SavedAddress.Type.OTHER
    };

    private ActivityAddressEditBinding binding;
    @Nullable
    private String editId;
    private SavedAddress.Type selectedType = SavedAddress.Type.DORM;
    private TextView[] typeChips;
    private boolean saving;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editId = getIntent().getStringExtra(AddressNavigator.EXTRA_EDIT_ID);
        boolean editing = editId != null;

        binding.tvTitleBar.setText(editing ? R.string.address_edit : R.string.address_add);
        binding.btnDelete.setVisibility(editing ? View.VISIBLE : View.GONE);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveAddress());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());

        setupTypeGrid();
        if (editing && editId != null) {
            loadExisting(editId);
        }
    }

    private void loadExisting(@NonNull String id) {
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().addressRemote().get(id, new ApiCallback<SavedAddress>() {
                @Override
                public void onSuccess(@NonNull SavedAddress existing) {
                    runOnUiThread(() -> applyExisting(existing));
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddressEditActivity.this,
                                error.getMessage() != null ? error.getMessage() : "加载失败",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        } else {
            SavedAddress existing = MockAddressRepository.findById(this, id);
            if (existing != null) {
                applyExisting(existing);
            }
        }
    }

    private void applyExisting(@NonNull SavedAddress existing) {
        selectedType = existing.type;
        binding.etTitle.setText(existing.title);
        binding.etDetail.setText(existing.detail);
        binding.switchDefault.setChecked(existing.isDefault);
        styleTypeChips();
    }

    private void setupTypeGrid() {
        typeChips = new TextView[TYPES.length];
        for (int i = 0; i < TYPES.length; i++) {
            final SavedAddress.Type type = TYPES[i];
            TextView chip = new TextView(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(i % 3, 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            chip.setLayoutParams(lp);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(0, dp(10), 0, dp(10));
            chip.setText(typeLabel(type));
            chip.setTextSize(13);
            chip.setOnClickListener(v -> {
                selectedType = type;
                styleTypeChips();
            });
            typeChips[i] = chip;
            binding.typeGrid.addView(chip);
        }
        styleTypeChips();
    }

    private void styleTypeChips() {
        for (int i = 0; i < TYPES.length; i++) {
            boolean active = selectedType == TYPES[i];
            typeChips[i].setBackgroundResource(active
                    ? R.drawable.bg_amount_chip_selected
                    : R.drawable.bg_amount_chip);
            typeChips[i].setTextColor(ContextCompat.getColor(this,
                    active ? R.color.cg_brand : R.color.cg_text_primary));
        }
    }

    private void saveAddress() {
        if (saving) {
            return;
        }
        String title = binding.etTitle.getText().toString().trim();
        String detail = binding.etDetail.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.address_title_required, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isDefault = binding.switchDefault.isChecked();
        SavedAddress address = new SavedAddress(
                editId != null ? editId : "",
                selectedType,
                title,
                detail,
                isDefault);

        if (!FeatureFlags.USE_REMOTE_API) {
            if (editId != null) {
                MockAddressRepository.update(this, address);
            } else {
                MockAddressRepository.add(this, new SavedAddress(
                        "a" + System.currentTimeMillis(), selectedType, title, detail, isDefault));
            }
            Toast.makeText(this, R.string.address_saved, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        saving = true;
        binding.btnSave.setEnabled(false);
        ApiCallback<SavedAddress> callback = new ApiCallback<SavedAddress>() {
            @Override
            public void onSuccess(@NonNull SavedAddress data) {
                runOnUiThread(() -> {
                    saving = false;
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(AddressEditActivity.this, R.string.address_saved, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() -> {
                    saving = false;
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(AddressEditActivity.this,
                            error.getMessage() != null ? error.getMessage() : "保存失败",
                            Toast.LENGTH_SHORT).show();
                });
            }
        };
        if (editId != null) {
            RetrofitClient.get().addressRemote().update(address, callback);
        } else {
            RetrofitClient.get().addressRemote().create(address, callback);
        }
    }

    private void confirmDelete() {
        if (editId == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.address_delete_title)
                .setMessage(R.string.address_delete_confirm)
                .setPositiveButton(R.string.address_delete, (d, w) -> deleteAddress())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteAddress() {
        if (editId == null) {
            return;
        }
        if (!FeatureFlags.USE_REMOTE_API) {
            MockAddressRepository.delete(this, editId);
            Toast.makeText(this, R.string.address_deleted, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        RetrofitClient.get().addressRemote().delete(editId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(@NonNull Void data) {
                runOnUiThread(() -> {
                    Toast.makeText(AddressEditActivity.this, R.string.address_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(@NonNull ApiException error) {
                runOnUiThread(() -> Toast.makeText(AddressEditActivity.this,
                        error.getMessage() != null ? error.getMessage() : "删除失败",
                        Toast.LENGTH_SHORT).show());
            }
        });
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
