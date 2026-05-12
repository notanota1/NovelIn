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

        // Inisialisasi cookie Cloudflare untuk SakuraNovel di background
        NovelScraper.initSakuraCookies(this, null);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_search) {
                selected = new LibraryFragment();
            } else if (id == R.id.nav_account) {
                selected = new AccountFragment();
                Bundle bundle = new Bundle();
                bundle.putString("user_name",
                    googlePrefs.getString("name", manualPrefs.getString("username", "User")));
                bundle.putString("user_email", googlePrefs.getString("email", ""));
                bundle.putString("login_type",
                    googlePrefs.getString("login_type", manualPrefs.getString("login_type", "manual")));
                selected.setArguments(bundle);
            }

            if (selected != null) {
                getSupportFragmentManager()
                    .popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selected)
                    .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateBottomNavVisibility);
    }

    private void updateBottomNavVisibility() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        boolean hide = current instanceof ReadingFragment || current instanceof DetailFragment;
        bottomNav.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavVisibility();
    }

    public String getCurrentUserName() {
        String googleName = googlePrefs.getString("name", "");
        return googleName.isEmpty()
            ? manualPrefs.getString("username", "User")
            : googleName;
    }

    public void logout() {
        googlePrefs.edit().clear().apply();
        manualPrefs.edit().clear().apply();
    }
}
