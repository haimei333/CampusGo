package com.campusgo.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockMallRepository;
import com.campusgo.data.mock.MockPointsRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.points.RedeemRecordDto;
import com.campusgo.databinding.ActivityMallBinding;
import com.campusgo.databinding.ItemMallProductBinding;
import com.campusgo.domain.model.MallProduct;

import java.util.List;

/**
 * G01 积分商城
 */
public class MallActivity extends AppCompatActivity {

    private ActivityMallBinding binding;
    private SessionManager sessionManager;
    private MallProduct.Category selectedCategory = MallProduct.Category.ALL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPointsHistory.setOnClickListener(v -> startActivity(ProfileNavigator.pointsHistory(this)));

        binding.chipAll.setOnClickListener(v -> selectCategory(MallProduct.Category.ALL));
        binding.chipVoucher.setOnClickListener(v -> selectCategory(MallProduct.Category.VOUCHER));
        binding.chipGoods.setOnClickListener(v -> selectCategory(MallProduct.Category.GOODS));
        binding.chipFlash.setOnClickListener(v -> selectCategory(MallProduct.Category.FLASH));

        selectCategory(MallProduct.Category.ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderPoints();
    }

    private void renderPoints() {
        binding.tvPointsBalance.setText(String.format("%,d", sessionManager.getPoints()));
    }

    private void selectCategory(@NonNull MallProduct.Category category) {
        selectedCategory = category;
        styleChip(binding.chipAll, category == MallProduct.Category.ALL);
        styleChip(binding.chipVoucher, category == MallProduct.Category.VOUCHER);
        styleChip(binding.chipGoods, category == MallProduct.Category.GOODS);
        styleChip(binding.chipFlash, category == MallProduct.Category.FLASH);
        renderProducts();
    }

    private void styleChip(@NonNull android.widget.TextView chip, boolean selected) {
        chip.setBackgroundResource(selected
                ? R.drawable.bg_filter_chip_selected
                : R.drawable.bg_filter_chip);
        chip.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.cg_text_on_brand : R.color.cg_text_secondary));
        chip.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void renderProducts() {
        binding.productGrid.removeAllViews();
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().pointsRemote().loadProducts(selectedCategory.name(),
                    new ApiCallback<List<MallProduct>>() {
                        @Override
                        public void onSuccess(@NonNull List<MallProduct> products) {
                            runOnUiThread(() -> renderProductGrid(products));
                        }

                        @Override
                        public void onError(@NonNull ApiException error) {
                            runOnUiThread(() -> Toast.makeText(MallActivity.this,
                                    error.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
        } else {
            renderProductGrid(MockMallRepository.byCategory(selectedCategory));
        }
    }

    private void renderProductGrid(@NonNull List<MallProduct> products) {
        int margin = dp(6);
        for (int i = 0; i < products.size(); i++) {
            MallProduct product = products.get(i);
            ItemMallProductBinding card = ItemMallProductBinding.inflate(
                    getLayoutInflater(), binding.productGrid, false);
            bindProduct(card, product);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(i % 2, 1f);
            lp.setMargins(margin, margin, margin, margin);
            card.getRoot().setLayoutParams(lp);
            binding.productGrid.addView(card.getRoot());
        }
    }

    private void bindProduct(@NonNull ItemMallProductBinding card, @NonNull MallProduct product) {
        card.tvEmoji.setText(product.emoji);
        if (product.imageBgRes != 0) {
            card.tvEmoji.setBackgroundResource(product.imageBgRes);
        }
        card.tvName.setText(product.name);
        card.tvSubtitle.setText(product.subtitle);
        card.tvPoints.setText(getString(R.string.mall_points_cost, product.pointsCost));
        card.tvFlashBadge.setVisibility(product.flashSale ? View.VISIBLE : View.GONE);
        if (product.flashSale) {
            card.btnRedeem.setBackgroundResource(R.drawable.bg_btn_danger);
            card.btnRedeem.setText(R.string.mall_flash_redeem);
            card.tvPoints.setTextColor(ContextCompat.getColor(this, R.color.cg_danger));
        } else {
            card.btnRedeem.setBackgroundResource(R.drawable.bg_btn_primary_brand);
            card.btnRedeem.setText(R.string.mall_redeem);
            card.tvPoints.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));
        }
        card.btnRedeem.setOnClickListener(v -> confirmRedeem(product));
    }

    private void confirmRedeem(@NonNull MallProduct product) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.mall_redeem_title)
                .setMessage(getString(R.string.mall_redeem_msg, product.name, product.pointsCost))
                .setPositiveButton(R.string.mall_redeem, (d, w) -> redeem(product))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void redeem(@NonNull MallProduct product) {
        if (sessionManager.getPoints() < product.pointsCost) {
            Toast.makeText(this, R.string.mall_points_insufficient, Toast.LENGTH_SHORT).show();
            return;
        }
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().pointsRemote().redeem(product.id, null, new ApiCallback<RedeemRecordDto>() {
                @Override
                public void onSuccess(@NonNull RedeemRecordDto data) {
                    runOnUiThread(() -> {
                        // Points already deducted in PointsRemoteDataSource
                        renderPoints();
                        Toast.makeText(MallActivity.this,
                                getString(R.string.mall_redeem_success, product.name), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> Toast.makeText(MallActivity.this,
                            error.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            if (!sessionManager.deductPoints(product.pointsCost)) {
                Toast.makeText(this, R.string.mall_points_insufficient, Toast.LENGTH_SHORT).show();
                return;
            }
            MockPointsRepository.addSpend(this,
                    getString(R.string.points_spend_redeem, product.name), product.pointsCost);
            renderPoints();
            Toast.makeText(this, getString(R.string.mall_redeem_success, product.name), Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
