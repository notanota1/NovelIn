package com.suryakusuma.novelin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    private SharedPreferences googlePrefs;
    private SharedPreferences manualPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googlePrefs = getSharedPreferences("google_session", MODE_PRIVATE);
        manualPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_search) {
                selectedFragment = new LibraryFragment();
            } else if (id == R.id.nav_account) {
                // Kirim data user ke AccountFragment via Bundle
                selectedFragment = new AccountFragment();
                Bundle bundle = new Bundle();

                String userName = googlePrefs.getString("name", manualPrefs.getString("username", "User"));
                String userEmail = googlePrefs.getString("email", "");
                String loginType = googlePrefs.getString("login_type", manualPrefs.getString("login_type", "manual"));

                bundle.putString("user_name", userName);
                bundle.putString("user_email", userEmail);
                bundle.putString("login_type", loginType);
                selectedFragment.setArguments(bundle);
            }

            if (selectedFragment != null) {

                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateBottomNavVisibility();
        });
    }

    private void updateBottomNavVisibility() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ReadingFragment || currentFragment instanceof DetailFragment) {
            bottomNav.setVisibility(View.GONE);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavVisibility();
    }

    public String getCurrentUserName() {
        String googleName = googlePrefs.getString("name", "");
        if (!googleName.isEmpty()) {
            return googleName;
        }
        return manualPrefs.getString("username", "User");
    }

    public void logout() {
        googlePrefs.edit().clear().apply();
        manualPrefs.edit().clear().apply();
    }
}
