package com.campusgo.ui.location;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.data.mock.MockHeatmapRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.heatmap.HeatmapDataDto;
import com.campusgo.databinding.ActivityHeatmapBinding;
import com.campusgo.databinding.IncludeHeatmapStatCardBinding;

/**
 * L02 任务热力图
 */
public class HeatmapActivity extends AppCompatActivity {

    private ActivityHeatmapBinding binding;
    private boolean monthFilter;
    private HeatmapDataDto heatmapData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHeatmapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFilterWeek.setOnClickListener(v -> applyFilter(false));
        binding.btnFilterMonth.setOnClickListener(v -> applyFilter(true));

        setupStatCards();
        applyFilter(false);
    }

    private void applyFilter(boolean month) {
        monthFilter = month;
        styleFilterChip(binding.btnFilterWeek, !month);
        styleFilterChip(binding.btnFilterMonth, month);
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().heatmapRemote().loadData(month ? "today" : "1h",
                    new ApiCallback<HeatmapDataDto>() {
                        @Override
                        public void onSuccess(@NonNull HeatmapDataDto data) {
                            runOnUiThread(() -> {
                                heatmapData = data;
                                binding.tvTotalOrders.setText(getString(R.string.heatmap_total_orders,
                                        data.totalOrders));
                                renderGrid(data.grid != null ? data.grid : new int[0][0]);
                            });
                        }

                        @Override
                        public void onError(@NonNull ApiException error) {
                            runOnUiThread(() -> {
                                heatmapData = null;
                                binding.tvTotalOrders.setText(getString(R.string.heatmap_total_orders,
                                        MockHeatmapRepository.totalOrders(month)));
                                renderGrid(MockHeatmapRepository.grid(month));
                            });
                        }
                    });
        } else {
            heatmapData = null;
            binding.tvTotalOrders.setText(getString(R.string.heatmap_total_orders,
                    MockHeatmapRepository.totalOrders(month)));
            renderGrid(MockHeatmapRepository.grid(month));
        }
    }

    private void styleFilterChip(@NonNull TextView chip, boolean selected) {
        chip.setBackgroundResource(selected
                ? R.drawable.bg_filter_chip_selected
                : R.drawable.bg_filter_chip);
        chip.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.cg_text_on_brand : R.color.cg_text_secondary));
        chip.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void renderGrid(@NonNull int[][] grid) {
        binding.heatGrid.removeAllViews();
        String[] days = heatmapData != null && heatmapData.dayLabels != null
                ? heatmapData.dayLabels
                : MockHeatmapRepository.dayLabels();
        String[] times = heatmapData != null && heatmapData.timeLabels != null
                ? heatmapData.timeLabels
                : MockHeatmapRepository.timeLabels();

        addHeaderCell("");
        for (String day : days) {
            addHeaderCell(day);
        }

        for (int row = 0; row < grid.length; row++) {
            addTimeCell(times[row]);
            for (int col = 0; col < grid[row].length; col++) {
                addHeatCell(grid[row][col]);
            }
        }
    }

    private void addHeaderCell(@NonNull String text) {
        TextView tv = new TextView(this);
        GridLayout.LayoutParams lp = cellParams();
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
        tv.setTextSize(11);
        binding.heatGrid.addView(tv);
    }

    private void addTimeCell(@NonNull String text) {
        TextView tv = new TextView(this);
        GridLayout.LayoutParams lp = cellParams();
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
        tv.setTextSize(11);
        binding.heatGrid.addView(tv);
    }

    private void addHeatCell(int level) {
        View cell = new View(this);
        GridLayout.LayoutParams lp = cellParams();
        int size = dp(28);
        lp.width = size;
        lp.height = size;
        cell.setLayoutParams(lp);
        cell.setBackgroundResource(heatDrawable(level));
        binding.heatGrid.addView(cell);
    }

    @NonNull
    private GridLayout.LayoutParams cellParams() {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        return lp;
    }

    private int heatDrawable(int level) {
        switch (Math.max(0, Math.min(level, 4))) {
            case 1:
                return R.drawable.bg_heat_cell_1;
            case 2:
                return R.drawable.bg_heat_cell_2;
            case 3:
                return R.drawable.bg_heat_cell_3;
            case 4:
                return R.drawable.bg_heat_cell_4;
            case 0:
            default:
                return R.drawable.bg_heat_cell_0;
        }
    }

    private void setupStatCards() {
        IncludeHeatmapStatCardBinding times = IncludeHeatmapStatCardBinding.bind(binding.cardHotTimes.getRoot());
        times.tvStatTitle.setText(R.string.heatmap_hot_times);
        addStatRow(times.statList, "1", "12:00-14:00", getString(R.string.heatmap_peak_noon),
                R.drawable.bg_stat_rank_gold);
        addStatRow(times.statList, "2", "16:00-18:00", getString(R.string.heatmap_peak_afternoon),
                R.drawable.bg_stat_rank_silver);
        addStatRow(times.statList, "3", "18:00-20:00", getString(R.string.heatmap_peak_evening),
                R.drawable.bg_stat_rank_bronze);

        IncludeHeatmapStatCardBinding zones = IncludeHeatmapStatCardBinding.bind(binding.cardHotZones.getRoot());
        zones.tvStatTitle.setText(R.string.heatmap_hot_zones);
        addStatRow(zones.statList, "1", getString(R.string.home_heat_row_canteen), "42 单",
                R.drawable.bg_stat_rank_gold);
        addStatRow(zones.statList, "2", getString(R.string.home_heat_row_library), "35 单",
                R.drawable.bg_stat_rank_silver);
        addStatRow(zones.statList, "3", getString(R.string.home_heat_row_dorm), "28 单", R.drawable.bg_stat_rank_bronze);
    }

    private void addStatRow(@NonNull LinearLayout list, @NonNull String rank,
            @NonNull String title, @NonNull String subtitle, int rankBg) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        TextView rankView = new TextView(this);
        rankView.setBackgroundResource(rankBg);
        rankView.setGravity(Gravity.CENTER);
        rankView.setText(rank);
        rankView.setTextSize(11);
        rankView.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        LinearLayout.LayoutParams rankLp = new LinearLayout.LayoutParams(dp(22), dp(22));
        rankView.setLayoutParams(rankLp);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        titleLp.setMarginStart(dp(8));
        titleView.setLayoutParams(titleLp);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        titleView.setTextSize(14);

        TextView subView = new TextView(this);
        subView.setText(subtitle);
        subView.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));
        subView.setTextSize(12);

        row.addView(rankView);
        row.addView(titleView);
        row.addView(subView);
        list.addView(row);

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_divider));
        list.addView(divider);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
