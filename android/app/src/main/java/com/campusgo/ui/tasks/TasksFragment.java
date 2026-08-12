package com.campusgo.ui.tasks;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.mock.MockTaskRepository;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.FragmentTasksBinding;
import com.campusgo.domain.model.TaskCategory;
import com.campusgo.domain.model.TaskListItem;
import com.campusgo.domain.model.TaskMode;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.task.TaskNavigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.campusgo.data.remote.TaskRemoteDataSource;

/**
 * M02 任务 Tab：按身份裁剪 Tab + 大厅筛选 + TaskCard 列表
 */
public class TasksFragment extends Fragment {

    private enum HallFilter {
        ALL, EXPRESS, BUY, ERRAND, GROUP, EMERGENCY, RESERVE
    }

    private static final class TabDef {
        final TaskListItem.Tab tab;
        final int labelRes;

        TabDef(TaskListItem.Tab tab, int labelRes) {
            this.tab = tab;
            this.labelRes = labelRes;
        }
    }

    private FragmentTasksBinding binding;
    private SessionManager sessionManager;
    private TaskListAdapter adapter;
    private UserRole lastRole;
    private TaskListItem.Tab activeTab;
    private HallFilter activeFilter = HallFilter.ALL;
    private List<TabDef> currentTabs = new ArrayList<>();
    private boolean loading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = ((CampusGoApp) requireActivity().getApplication()).getSessionManager();

        adapter = new TaskListAdapter();
        adapter.setOnTaskClickListener(this::onTaskClick);
        binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTasks.setAdapter(adapter);
        binding.recyclerTasks.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = dp(12);
            }
        });

        binding.btnEmptyAction.setOnClickListener(v -> {
            if (sessionManager.getActiveRole() == UserRole.RUNNER) {
                selectTab(TaskListItem.Tab.HALL);
            }
        });

        refreshForRole(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        UserRole role = sessionManager.getActiveRole();
        if (lastRole != role) {
            refreshForRole(true);
        } else {
            renderList();
        }
    }

    private void refreshForRole(boolean resetTab) {
        UserRole role = sessionManager.getActiveRole();
        lastRole = role;
        currentTabs = tabsForRole(role);
        if (resetTab || !containsTab(activeTab)) {
            activeTab = currentTabs.get(0).tab;
            activeFilter = HallFilter.ALL;
        }
        renderTabs();
        renderFilters();
        renderList();
    }

    private boolean containsTab(@Nullable TaskListItem.Tab tab) {
        if (tab == null) {
            return false;
        }
        for (TabDef def : currentTabs) {
            if (def.tab == tab) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private List<TabDef> tabsForRole(@NonNull UserRole role) {
        if (role == UserRole.PUBLISHER) {
            return Arrays.asList(
                    new TabDef(TaskListItem.Tab.MINE_PUBLISH, R.string.tasks_tab_mine_publish),
                    new TabDef(TaskListItem.Tab.POOL, R.string.tasks_tab_pool),
                    new TabDef(TaskListItem.Tab.RESERVE, R.string.tasks_tab_reserve));
        }
        return Arrays.asList(
                new TabDef(TaskListItem.Tab.HALL, R.string.tasks_tab_hall),
                new TabDef(TaskListItem.Tab.POOL, R.string.tasks_tab_pool),
                new TabDef(TaskListItem.Tab.MINE_TAKE, R.string.tasks_tab_mine_take),
                new TabDef(TaskListItem.Tab.RESERVE, R.string.tasks_tab_reserve));
    }

    private void renderTabs() {
        binding.tabRow.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (TabDef def : currentTabs) {
            TextView tabView = (TextView) inflater.inflate(R.layout.item_task_tab, binding.tabRow, false);
            tabView.setText(def.labelRes);
            boolean selected = def.tab == activeTab;
            styleTab(tabView, selected);
            tabView.setOnClickListener(v -> selectTab(def.tab));
            binding.tabRow.addView(tabView);
        }
    }

    private void selectTab(@NonNull TaskListItem.Tab tab) {
        activeTab = tab;
        activeFilter = HallFilter.ALL;
        renderTabs();
        renderFilters();
        renderList();
    }

    private void styleTab(@NonNull TextView tabView, boolean selected) {
        tabView.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.cg_brand : R.color.cg_text_tertiary));
        tabView.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabView.setBackgroundResource(selected ? R.drawable.bg_tab_selected : android.R.color.transparent);
    }

    private void renderFilters() {
        boolean showFilters = activeTab == TaskListItem.Tab.HALL
                && sessionManager.getActiveRole() == UserRole.RUNNER;
        binding.filterScroll.setVisibility(showFilters ? View.VISIBLE : View.GONE);
        if (!showFilters) {
            binding.filterRow.removeAllViews();
            return;
        }

        binding.filterRow.removeAllViews();
        HallFilter[] filters = HallFilter.values();
        int[] labels = {
                R.string.tasks_filter_all,
                R.string.tasks_filter_express,
                R.string.tasks_filter_buy,
                R.string.tasks_filter_errand,
                R.string.tasks_filter_group,
                R.string.tasks_filter_emergency,
                R.string.tasks_filter_reserve
        };
        for (int i = 0; i < filters.length; i++) {
            HallFilter filter = filters[i];
            TextView chip = new TextView(requireContext());
            chip.setText(labels[i]);
            chip.setTextSize(12);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(14), dp(6), dp(14), dp(6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                lp.setMarginStart(dp(8));
            }
            chip.setLayoutParams(lp);
            styleFilterChip(chip, filter == activeFilter);
            chip.setOnClickListener(v -> {
                activeFilter = filter;
                renderFilters();
                renderList();
            });
            binding.filterRow.addView(chip);
        }
    }

    private void styleFilterChip(@NonNull TextView chip, boolean selected) {
        chip.setBackgroundResource(R.drawable.bg_chip_filter_solid);
        chip.setSelected(selected);
        chip.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.cg_text_on_brand : R.color.cg_tag_normal_text));
    }

    private void renderList() {
        if (FeatureFlags.USE_REMOTE_API) {
            loadRemoteList();
            return;
        }
        showMockList();
    }

    private void loadRemoteList() {
        if (binding == null || loading) {
            return;
        }
        loading = true;
        binding.recyclerTasks.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.btnEmptyAction.setVisibility(View.GONE);
        binding.tvEmptyMessage.setText(R.string.common_loading);

        ApiCallback<List<TaskListItem>> callback = new ApiCallback<List<TaskListItem>>() {
            @Override
            public void onSuccess(@NonNull List<TaskListItem> data) {
                if (binding == null) {
                    return;
                }
                loading = false;
                List<TaskListItem> items = data;
                if (activeTab == TaskListItem.Tab.HALL) {
                    items = applyHallFilter(items);
                }
                showList(items);
            }

            @Override
            public void onError(@NonNull ApiException error) {
                if (binding == null) {
                    return;
                }
                loading = false;
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                showList(Collections.emptyList());
            }
        };

        TaskRemoteDataSource remote = RetrofitClient.get().taskRemote();
        switch (activeTab) {
            case HALL:
                remote.loadHall(1, callback);
                break;
            case POOL:
                remote.loadPool(1, callback);
                break;
            case MINE_PUBLISH:
                remote.loadMinePublishedWithDrafts(1, callback);
                break;
            case MINE_TAKE:
                remote.loadMineAccepted(1, callback);
                break;
            case RESERVE:
                remote.loadReservations(1, callback);
                break;
            default:
                loading = false;
                showMockList();
                break;
        }
    }

    private void showMockList() {
        UserRole role = sessionManager.getActiveRole();
        List<TaskListItem> items = MockTaskRepository.forTab(requireContext(), activeTab, role);
        if (activeTab == TaskListItem.Tab.HALL) {
            items = applyHallFilter(items);
        }
        showList(items);
    }

    private void showList(@NonNull List<TaskListItem> items) {
        adapter.setItems(items);
        boolean empty = items.isEmpty();
        binding.recyclerTasks.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (!empty) {
            return;
        }

        binding.btnEmptyAction.setVisibility(View.GONE);
        if (activeTab == TaskListItem.Tab.HALL) {
            binding.tvEmptyMessage.setText(R.string.tasks_empty_hall);
        } else if (activeTab == TaskListItem.Tab.MINE_TAKE) {
            binding.tvEmptyMessage.setText(R.string.tasks_empty_mine_take);
            binding.btnEmptyAction.setText(R.string.tasks_empty_go_hall);
            binding.btnEmptyAction.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyMessage.setText(R.string.tasks_empty_default);
        }
    }

    @NonNull
    private List<TaskListItem> applyHallFilter(@NonNull List<TaskListItem> items) {
        if (activeFilter == HallFilter.ALL) {
            return items;
        }
        List<TaskListItem> filtered = new ArrayList<>();
        for (TaskListItem item : items) {
            if (matchesFilter(item, activeFilter)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private boolean matchesFilter(@NonNull TaskListItem item, @NonNull HallFilter filter) {
        switch (filter) {
            case EXPRESS:
                return item.category == TaskCategory.EXPRESS;
            case BUY:
                return item.category == TaskCategory.BUY;
            case ERRAND:
                return item.category == TaskCategory.ERRAND;
            case GROUP:
                return item.mode == TaskMode.GROUP;
            case EMERGENCY:
                return item.mode == TaskMode.EMERGENCY;
            case RESERVE:
                return item.mode == TaskMode.RESERVE;
            case ALL:
            default:
                return true;
        }
    }

    private void onTaskClick(@NonNull TaskListItem item) {
        UserRole role = sessionManager.getActiveRole();
        if (item.navTarget == TaskListItem.NavTarget.T01) {
            startActivity(TaskNavigator.publishWithDraft(requireContext(), item.id));
            return;
        }
        if (item.navTarget == TaskListItem.NavTarget.T07) {
            startActivity(TaskNavigator.groupDetail(requireContext(), item.id));
            return;
        }
        startActivity(TaskNavigator.taskDetailFromItem(requireContext(), item, role));
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
