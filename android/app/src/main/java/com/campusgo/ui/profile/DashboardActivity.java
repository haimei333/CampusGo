package com.campusgo.ui.profile;

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

import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.config.FeatureFlags;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockDashboardRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.data.remote.dto.dashboard.DashboardStatsDto;
import com.campusgo.databinding.ActivityDashboardBinding;
import com.campusgo.domain.model.DashboardStat;
import com.campusgo.domain.model.UserRole;

import java.util.List;

/**
 * D01 数据看板
 */
public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private SessionManager sessionManager;
    private UserRole viewRole = UserRole.RUNNER;
    private DashboardStatsDto dashboardStats;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = ((CampusGoApp) getApplication()).getSessionManager();
        viewRole = sessionManager.getActiveRole();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRolePublisher.setOnClickListener(v -> switchRole(UserRole.PUBLISHER));
        binding.btnRoleRunner.setOnClickListener(v -> switchRole(UserRole.RUNNER));
        renderAll();
    }

    private void switchRole(@NonNull UserRole role) {
        viewRole = role;
        renderAll();
    }

    private void renderAll() {
        styleRoleSwitch();
        if (FeatureFlags.USE_REMOTE_API) {
            RetrofitClient.get().dashboardRemote().loadStats(new ApiCallback<DashboardStatsDto>() {
                @Override
                public void onSuccess(@NonNull DashboardStatsDto data) {
                    runOnUiThread(() -> {
                        dashboardStats = data;
                        renderStatGrid();
                        renderTrendCard();
                        renderCategoryCard();
                        renderLeaderboard();
                    });
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    runOnUiThread(() -> {
                        dashboardStats = null;
                        renderStatGrid();
                        renderTrendCard();
                        renderCategoryCard();
                        renderLeaderboard();
                    });
                }
            });
        } else {
            dashboardStats = null;
            renderStatGrid();
            renderTrendCard();
            renderCategoryCard();
            renderLeaderboard();
        }
    }

    private void styleRoleSwitch() {
        boolean publisher = viewRole == UserRole.PUBLISHER;
        int brand = ContextCompat.getColor(this, R.color.cg_brand);
        int onBrand = ContextCompat.getColor(this, R.color.cg_text_on_brand);
        int secondary = ContextCompat.getColor(this, R.color.cg_text_secondary);
        int input = ContextCompat.getColor(this, R.color.cg_bg_input);

        binding.btnRolePublisher.setTextColor(publisher ? onBrand : secondary);
        binding.btnRoleRunner.setTextColor(publisher ? secondary : onBrand);
        binding.btnRolePublisher.setBackgroundColor(publisher ? brand : input);
        binding.btnRoleRunner.setBackgroundColor(publisher ? input : brand);
        binding.btnRolePublisher.setTypeface(null, publisher
                ? android.graphics.Typeface.BOLD
                : android.graphics.Typeface.NORMAL);
        binding.btnRoleRunner.setTypeface(null, publisher
                ? android.graphics.Typeface.NORMAL
                : android.graphics.Typeface.BOLD);
    }

    private void renderStatGrid() {
        binding.statGrid.removeAllViews();
        List<DashboardStat> stats;
        if (dashboardStats != null && dashboardStats.overviewStats != null) {
            stats = new java.util.ArrayList<>();
            for (DashboardStatsDto.StatItemDto s : dashboardStats.overviewStats) {
                stats.add(new DashboardStat(s.label, s.value, s.trend, s.trendPositive));
            }
        } else {
            stats = MockDashboardRepository.overviewStats(viewRole);
        }
        int margin = dp(6);
        for (int i = 0; i < stats.size(); i++) {
            DashboardStat stat = stats.get(i);
            View card = buildStatCard(stat);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(i % 2, 1f);
            lp.setMargins(margin, margin, margin, margin);
            card.setLayoutParams(lp);
            binding.statGrid.addView(card);
        }
    }

    @NonNull
    private View buildStatCard(@NonNull DashboardStat stat) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView label = new TextView(this);
        label.setText(stat.label);
        label.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
        label.setTextSize(12);

        TextView value = new TextView(this);
        value.setText(stat.value);
        value.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        value.setTextSize(20);
        value.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueLp.topMargin = dp(6);
        value.setLayoutParams(valueLp);

        TextView trend = new TextView(this);
        trend.setText(stat.trend);
        trend.setTextSize(11);
        trend.setTextColor(ContextCompat.getColor(this,
                stat.trendPositive ? R.color.cg_success : R.color.cg_danger));
        LinearLayout.LayoutParams trendLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trendLp.topMargin = dp(4);
        trend.setLayoutParams(trendLp);

        card.addView(label);
        card.addView(value);
        card.addView(trend);
        return card;
    }

    private void renderTrendCard() {
        binding.cardTrend.removeAllViews();
        boolean runner = viewRole == UserRole.RUNNER;

        int[] values;
        String[] labels;
        String trendUnit;
        String trendTotal;
        if (dashboardStats != null && dashboardStats.trendValues != null) {
            values = new int[dashboardStats.trendValues.size()];
            for (int i = 0; i < dashboardStats.trendValues.size(); i++) {
                values[i] = dashboardStats.trendValues.get(i);
            }
            labels = dashboardStats.trendLabels != null
                    ? dashboardStats.trendLabels.toArray(new String[0])
                    : new String[0];
            trendUnit = dashboardStats.trendUnit != null ? dashboardStats.trendUnit : "";
            trendTotal = dashboardStats.trendTotal != null ? dashboardStats.trendTotal : "";
        } else {
            values = MockDashboardRepository.trendValues(viewRole);
            labels = MockDashboardRepository.trendLabels();
            trendUnit = MockDashboardRepository.trendUnit(viewRole);
            trendTotal = MockDashboardRepository.trendTotal(viewRole);
        }

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = sectionTitle(runner
                ? R.string.dashboard_trend_income
                : R.string.dashboard_trend_publish);
        TextView unit = caption(getString(R.string.dashboard_trend_unit,
                trendUnit));
        LinearLayout.LayoutParams unitLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        unitLp.weight = 0;
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleLp);
        header.addView(title);
        header.addView(unit);

        LinearLayout chart = buildBarChart(values, labels);

        View divider = divider();
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(0, dp(12), 0, 0);
        TextView totalLabel = caption(runner
                ? getString(R.string.dashboard_week_income)
                : getString(R.string.dashboard_week_publish));
        TextView totalValue = sectionTitleValue(trendTotal);
        LinearLayout.LayoutParams totalLabelLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        totalLabel.setLayoutParams(totalLabelLp);
        footer.addView(totalLabel);
        footer.addView(totalValue);

        binding.cardTrend.addView(header);
        LinearLayout.LayoutParams chartLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chartLp.topMargin = dp(16);
        chart.setLayoutParams(chartLp);
        binding.cardTrend.addView(chart);
        binding.cardTrend.addView(divider);
        binding.cardTrend.addView(footer);
    }

    @NonNull
    private LinearLayout buildBarChart(@NonNull int[] values, @NonNull String[] labels) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.BOTTOM);
        int max = 1;
        for (int v : values) {
            max = Math.max(max, v);
        }
        for (int i = 0; i < values.length; i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            colLp.setMarginStart(dp(2));
            colLp.setMarginEnd(dp(2));
            col.setLayoutParams(colLp);

            TextView val = new TextView(this);
            val.setText(String.valueOf(values[i]));
            val.setTextSize(11);
            val.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
            val.setGravity(Gravity.CENTER);

            View bar = new View(this);
            int barMaxHeight = dp(90);
            int fillHeight = Math.max(dp(8), (int) (barMaxHeight * (values[i] / (float) max)));
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, fillHeight);
            barLp.topMargin = dp(4);
            bar.setLayoutParams(barLp);
            bar.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_brand));

            TextView day = new TextView(this);
            day.setText(labels[i]);
            day.setTextSize(11);
            day.setTextColor(ContextCompat.getColor(this, R.color.cg_text_tertiary));
            day.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams dayLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dayLp.topMargin = dp(4);
            day.setLayoutParams(dayLp);

            col.addView(val);
            col.addView(bar);
            col.addView(day);
            row.addView(col);
        }
        return row;
    }

    private void renderCategoryCard() {
        binding.cardCategory.removeAllViews();
        binding.cardCategory.addView(sectionTitle(R.string.dashboard_category_title));

        int[] colors = {
                R.color.cg_brand,
                R.color.cg_accent,
                R.color.cg_warning,
                R.color.cg_text_tertiary
        };

        String categoryTotal;
        if (dashboardStats != null && dashboardStats.categoryStats != null) {
            for (int i = 0; i < dashboardStats.categoryStats.size(); i++) {
                if (i > 0) {
                    binding.cardCategory.addView(spacer(dp(12)));
                }
                DashboardStatsDto.CategoryItemDto item = dashboardStats.categoryStats.get(i);
                binding.cardCategory.addView(buildCategoryRow(
                        item.name,
                        Math.round(item.percent),
                        String.valueOf(item.count),
                        ContextCompat.getColor(this, colors[i % colors.length])));
            }
            categoryTotal = dashboardStats.categoryTotal != null ? dashboardStats.categoryTotal : "0";
        } else {
            String[][] stats = MockDashboardRepository.categoryStats(viewRole);
            for (int i = 0; i < stats.length; i++) {
                if (i > 0) {
                    binding.cardCategory.addView(spacer(dp(12)));
                }
                binding.cardCategory.addView(buildCategoryRow(
                        stats[i][0],
                        Integer.parseInt(stats[i][1]),
                        stats[i][2],
                        ContextCompat.getColor(this, colors[i])));
            }
            categoryTotal = MockDashboardRepository.categoryTotal(viewRole);
        }

        binding.cardCategory.addView(divider());
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setPadding(0, dp(12), 0, 0);
        TextView label = caption(viewRole == UserRole.RUNNER
                ? getString(R.string.dashboard_month_orders)
                : getString(R.string.dashboard_month_publish));
        TextView value = sectionTitleValue(categoryTotal);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelLp);
        footer.addView(label);
        footer.addView(value);
        binding.cardCategory.addView(footer);
    }

    @NonNull
    private View buildCategoryRow(@NonNull String name, int percent, @NonNull String count,
            int color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        View dot = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(10), dp(10));
        dot.setLayoutParams(dotLp);
        dot.setBackgroundColor(color);
        TextView nameTv = new TextView(this);
        nameTv.setText(name);
        nameTv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        nameTv.setTextSize(14);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameLp.setMarginStart(dp(8));
        nameTv.setLayoutParams(nameLp);
        TextView pct = new TextView(this);
        pct.setText(percent + "%");
        pct.setTextSize(12);
        pct.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
        LinearLayout.LayoutParams pctLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        pct.setGravity(Gravity.END);
        pct.setLayoutParams(pctLp);
        header.addView(dot);
        header.addView(nameTv);
        header.addView(pct);

        View track = new View(this);
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        trackLp.topMargin = dp(6);
        track.setLayoutParams(trackLp);
        track.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_bg_input));

        View fill = new View(this);
        LinearLayout.LayoutParams fillLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        fillLp.topMargin = dp(6);
        fill.setLayoutParams(fillLp);
        fill.setBackgroundColor(color);
        fill.setScaleX(percent / 100f);
        fill.setPivotX(0f);

        TextView countTv = caption(count + (viewRole == UserRole.RUNNER
                ? getString(R.string.dashboard_order_unit)
                : getString(R.string.dashboard_task_unit)));
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countLp.topMargin = dp(4);
        countTv.setLayoutParams(countLp);

        row.addView(header);
        row.addView(fill);
        row.addView(countTv);
        return row;
    }

    private void renderLeaderboard() {
        if (viewRole != UserRole.RUNNER) {
            binding.cardLeaderboard.setVisibility(View.GONE);
            return;
        }
        binding.cardLeaderboard.setVisibility(View.VISIBLE);
        binding.cardLeaderboard.removeAllViews();

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = sectionTitle(R.string.dashboard_leaderboard);
        TextView top = caption("TOP 5");
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleLp);
        header.addView(title);
        header.addView(top);
        binding.cardLeaderboard.addView(header);

        if (dashboardStats != null && dashboardStats.leaderboard != null) {
            for (int i = 0; i < dashboardStats.leaderboard.size(); i++) {
                DashboardStatsDto.LeaderItemDto item = dashboardStats.leaderboard.get(i);
                String[] row = {
                        String.valueOf(item.rank),
                        item.avatarInitial != null ? item.avatarInitial : "",
                        item.name != null ? item.name : "",
                        String.valueOf(item.orderCount),
                        item.badge != null ? item.badge : ""
                };
                binding.cardLeaderboard.addView(spacer(dp(8)));
                binding.cardLeaderboard.addView(buildLeaderRow(row, i < dashboardStats.leaderboard.size() - 1));
            }
        } else {
            String[][] rows = MockDashboardRepository.leaderboard();
            for (int i = 0; i < rows.length; i++) {
                binding.cardLeaderboard.addView(spacer(dp(8)));
                binding.cardLeaderboard.addView(buildLeaderRow(rows[i], i < rows.length - 1));
            }
        }
    }

    @NonNull
    private View buildLeaderRow(@NonNull String[] data, boolean showDivider) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(0, dp(10), 0, dp(10));

        TextView rank = new TextView(this);
        rank.setText(data[0]);
        rank.setGravity(Gravity.CENTER);
        rank.setWidth(dp(28));
        rank.setHeight(dp(28));
        rank.setTextColor(ContextCompat.getColor(this, R.color.cg_text_on_brand));
        rank.setTextSize(13);
        rank.setTypeface(null, android.graphics.Typeface.BOLD);
        int rankBg = R.drawable.bg_stat_rank_gold;
        if ("2".equals(data[0])) {
            rankBg = R.drawable.bg_stat_rank_silver;
        } else if ("3".equals(data[0])) {
            rankBg = R.drawable.bg_stat_rank_bronze;
        } else if (!"1".equals(data[0])) {
            rank.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
            rank.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_bg_input));
        }
        if ("1".equals(data[0]) || "2".equals(data[0]) || "3".equals(data[0])) {
            rank.setBackgroundResource(rankBg);
        }

        TextView avatar = new TextView(this);
        avatar.setText(data[1]);
        avatar.setGravity(Gravity.CENTER);
        avatar.setWidth(dp(40));
        avatar.setHeight(dp(40));
        avatar.setBackgroundResource(R.drawable.bg_icon_soft);
        avatar.setTextColor(ContextCompat.getColor(this, R.color.cg_brand));
        avatar.setTextSize(14);
        avatar.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        avatarLp.setMarginStart(dp(12));
        avatar.setLayoutParams(avatarLp);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.setMarginStart(dp(12));
        info.setLayoutParams(infoLp);
        TextView name = new TextView(this);
        name.setText(data[2]);
        name.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        name.setTextSize(14);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        TextView orders = caption(getString(R.string.dashboard_leader_orders, data[3]));
        info.addView(name);
        info.addView(orders);

        TextView badge = new TextView(this);
        badge.setText(data[4]);
        badge.setTextSize(12);
        badge.setTextColor(ContextCompat.getColor(this, R.color.cg_warning));

        content.addView(rank);
        content.addView(avatar);
        content.addView(info);
        content.addView(badge);
        row.addView(content);
        if (showDivider) {
            row.addView(divider());
        }
        return row;
    }

    @NonNull
    private TextView sectionTitle(int res) {
        TextView tv = new TextView(this);
        tv.setText(res);
        tv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        tv.setTextSize(16);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    @NonNull
    private TextView sectionTitleValue(@NonNull String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_primary));
        tv.setTextSize(16);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    @NonNull
    private TextView caption(@NonNull String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.cg_text_secondary));
        tv.setTextSize(12);
        return tv;
    }

    @NonNull
    private View divider() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_divider));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = dp(12);
        return v;
    }

    @NonNull
    private View spacer(int height) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height));
        return v;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
