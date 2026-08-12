package com.campusgo.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.campusgo.R;
import com.campusgo.databinding.ActivityMainBinding;
import com.campusgo.ui.task.TaskNavigator;

/**
 * 主壳：4 Tab + FAB（对齐界面拆分 M01–M04）
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostMain);
        if (navHost != null) {
            NavController navController = navHost.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);
        }

        binding.fabPublish.setOnClickListener(v ->
                startActivity(TaskNavigator.publish(this)));
    }

    /** 供首页等模块切换底部 Tab */
    public void selectTab(int menuItemId) {
        binding.bottomNav.setSelectedItemId(menuItemId);
    }
}
