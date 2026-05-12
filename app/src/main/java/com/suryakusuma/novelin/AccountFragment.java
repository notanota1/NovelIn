package com.suryakusuma.novelin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class AccountFragment extends Fragment {

    private TextView tvUsername, tvEmail;
    private ImageView ivProfile;
    private LinearLayout menuDarkMode, menuAbout;
    private Button btnLogout;
    private GoogleSignInHelper googleSignInHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gunakan layout fragment_account yang sudah Anda edit
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Inisialisasi View yang masih ada di XML
        tvUsername = view.findViewById(R.id.tvAccountUsername);
        tvEmail = view.findViewById(R.id.tvAccountEmail);
        ivProfile = view.findViewById(R.id.ivProfilePicture);
        menuDarkMode = view.findViewById(R.id.menuDarkMode);
        menuAbout = view.findViewById(R.id.menuAbout);
        btnLogout = view.findViewById(R.id.btnLogout);

        googleSignInHelper = new GoogleSignInHelper(getActivity());

        // Memuat data user ke UI
        loadUserData();

        // Listener untuk ganti tema
        if (menuDarkMode != null) {
            menuDarkMode.setOnClickListener(v -> showAppearanceDialog());
        }

        // Listener untuk About
        if (menuAbout != null) {
            menuAbout.setOnClickListener(v -> showAboutDialog());
        }

        // Listener Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (googleSignInHelper != null) {
                    googleSignInHelper.signOut();
                }

                // Hapus semua session login
                SharedPreferences googlePrefs = getActivity().getSharedPreferences("google_session", Context.MODE_PRIVATE);
                SharedPreferences manualPrefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                googlePrefs.edit().clear().apply();
                manualPrefs.edit().clear().apply();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void showAppearanceDialog() {
        String[] themes = {"Light Mode", "Dark Mode", "System Default"};
        SharedPreferences prefs = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        int currentTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int checkedItem = 2;
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) checkedItem = 0;
        else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 1;

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Appearance")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int mode;
                    switch (which) {
                        case 0: mode = AppCompatDelegate.MODE_NIGHT_NO; break;
                        case 1: mode = AppCompatDelegate.MODE_NIGHT_YES; break;
                        default: mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; break;
                    }
                    AppCompatDelegate.setDefaultNightMode(mode);
                    prefs.edit().putInt("theme_mode", mode).apply();
                    dialog.dismiss();
                })
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About NovelIn")
                .setMessage("NovelIn v1.0.0\n\nAplikasi pembaca novel digital.\n\nDeveloped by Surya Kusuma.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadUserData() {
        if (!isAdded() || getActivity() == null) return;

        SharedPreferences googlePrefs = getActivity().getSharedPreferences("google_session", Context.MODE_PRIVATE);
        SharedPreferences manualPrefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);

        String name, email, photoUrl;

        if (googlePrefs.getBoolean("is_logged_in", false)) {
            name = googlePrefs.getString("name", "User");
            email = googlePrefs.getString("email", "-");
            photoUrl = googlePrefs.getString("photo_url", null);
        } else {
            name = manualPrefs.getString("username", "User");
            email = "Local Account";
            photoUrl = null;
        }

        if (tvUsername != null) tvUsername.setText(name);
        if (tvEmail != null) tvEmail.setText(email);

        if (ivProfile != null) {
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.profile)
                        .circleCrop()
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.profile);
            }
        }
    }
}