package com.campusgo.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusgo.core.config.FeatureFlags;
import com.campusgo.CampusGoApp;
import com.campusgo.R;
import com.campusgo.core.session.SessionManager;
import com.campusgo.data.remote.ApiCallback;
import com.campusgo.data.remote.ApiException;
import com.campusgo.data.remote.RetrofitClient;
import com.campusgo.databinding.FragmentProfileBinding;
import com.campusgo.databinding.ItemProfileMenuBinding;
import com.campusgo.domain.model.UserRole;
import com.campusgo.ui.auth.AuthNavigator;
import com.campusgo.ui.settings.SettingsActivity;
import com.campusgo.ui.wallet.WalletNavigator;

/**
 * M04 我的
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = ((CampusGoApp) requireActivity().getApplication()).getSessionManager();
        binding.headerProfile.setOnClickListener(v ->
                startActivity(AuthNavigator.editProfile(requireContext())));
        buildMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FeatureFlags.USE_REMOTE_API) {
            // Load points balance from server
            RetrofitClient.get().pointsRemote().loadBalance(new ApiCallback<com.campusgo.data.remote.dto.points.PointsBalanceDto>() {
                @Override
                public void onSuccess(@NonNull com.campusgo.data.remote.dto.points.PointsBalanceDto data) {
                    if (binding != null) {
                        requireActivity().runOnUiThread(() -> {
                            renderProfile();
                            buildMenu();
                        });
                    }
                }

                @Override
                public void onError(@NonNull ApiException error) {
                    if (binding != null) {
                        requireActivity().runOnUiThread(() -> {
                            renderProfile();
                            buildMenu();
                        });
                    }
                }
            });
        } else {
            renderProfile();
            buildMenu();
        }
    }

    private void renderProfile() {
        String nickname = sessionManager.getNickname();
        binding.tvAvatar.setText(sessionManager.getAvatarInitial());
        binding.tvNickname.setText(nickname);
        binding.tvPhone.setText(maskPhone(sessionManager.getPhone()));

        int credit = sessionManager.getCreditScore();
        binding.tvCreditScore.setText(String.valueOf(credit));
        binding.progressCredit.setProgress(credit);
        binding.tvCreditLevel.setText(creditLevelLabel(credit));

        boolean publisher = sessionManager.getActiveRole() == UserRole.PUBLISHER;
        if (publisher) {
            binding.tvStat1Value.setText("12");
            binding.tvStat1Label.setText(R.string.profile_stat_publish_count);
            binding.tvStat2Value.setText("92%");
            binding.tvStat2Label.setText(R.string.profile_stat_complete_rate);
        } else {
            binding.tvStat2Value.setText(String.format("¥%.0f", sessionManager.getTotalIncome()));
            binding.tvStat2Label.setText(R.string.profile_stat_total_income);
            binding.tvStat1Value.setText("8");
            binding.tvStat1Label.setText(R.string.profile_stat_take_count);
        }
    }

    private void buildMenu() {
        binding.menuList.removeAllViews();
        MenuItem[] items = new MenuItem[]{
                new MenuItem("看", R.string.profile_menu_dashboard, null, () ->
                        startActivity(ProfileNavigator.dashboard(requireContext()))),
                new MenuItem("钱", R.string.profile_menu_wallet, sessionManager.formatWalletBalance(), () ->
                        startActivity(WalletNavigator.wallet(requireContext()))),
                new MenuItem("积", R.string.profile_menu_mall,
                        getString(R.string.profile_points_value, sessionManager.getPoints()), () ->
                        startActivity(ProfileNavigator.mall(requireContext()))),
                new MenuItem("券", R.string.voucher_profile_entry, null, () ->
                        startActivity(MyVouchersActivity.newIntent(requireContext()))),
                new MenuItem("证", R.string.profile_menu_verify, verifyStatusLabel(), () ->
                        startActivity(WalletNavigator.verify(requireContext()))),
                new MenuItem("诉", R.string.profile_menu_complaint, null, () ->
                        startActivity(ProfileNavigator.complaintRecords(requireContext()))),
                new MenuItem("AI", R.string.profile_menu_ai_assistant, null, () ->
                        startActivity(ProfileNavigator.aiAssistant(requireContext()))),
                new MenuItem("帮", R.string.profile_menu_help, null, () ->
                        startActivity(ProfileNavigator.help(requireContext()))),
                new MenuItem("设", R.string.profile_menu_settings, null, () ->
                        startActivity(new Intent(requireContext(), SettingsActivity.class)))
        };
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                View divider = new View(requireContext());
                divider.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(requireContext().getColor(R.color.cg_divider));
                binding.menuList.addView(divider);
            }
            MenuItem item = items[i];
            ItemProfileMenuBinding row = ItemProfileMenuBinding.inflate(
                    LayoutInflater.from(requireContext()), binding.menuList, false);
            row.tvIcon.setText(item.icon);
            row.tvLabel.setText(item.labelRes);
            if (item.extra != null) {
                row.tvExtra.setVisibility(View.VISIBLE);
                row.tvExtra.setText(item.extra);
            }
            row.getRoot().setOnClickListener(v -> item.action.run());
            binding.menuList.addView(row.getRoot());
        }
    }

    private static final class MenuItem {
        final String icon;
        final int labelRes;
        @Nullable
        final String extra;
        final Runnable action;

        MenuItem(String icon, int labelRes, @Nullable String extra, Runnable action) {
            this.icon = icon;
            this.labelRes = labelRes;
            this.extra = extra;
            this.action = action;
        }
    }

    @NonNull
    private String verifyStatusLabel() {
        if (sessionManager.isCampusVerified()) {
            return getString(R.string.profile_verify_done);
        }
        return getString(R.string.profile_verify_pending);
    }

    @NonNull
    private String creditLevelLabel(int credit) {
        if (credit >= 600) {
            return getString(R.string.profile_credit_good);
        }
        if (credit >= 400) {
            return getString(R.string.profile_credit_normal);
        }
        return getString(R.string.profile_credit_low);
    }

    @NonNull
    private String maskPhone(@Nullable String phone) {
        if (phone == null || phone.length() < 11) {
            return "138****5678";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
