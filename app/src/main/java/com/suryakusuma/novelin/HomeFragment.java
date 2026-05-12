package com.suryakusuma.novelin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvSavedNovels;
    private NovelAdapter adapter;
    private List<Novel> savedNovelList;
    private DatabaseHelper db;
    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rvSavedNovels = view.findViewById(R.id.rvNovels);

        db = new DatabaseHelper(getContext());
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");

        loadSavedNovels();

        adapter = new NovelAdapter(savedNovelList, novel -> {

            // ✨ Animasi klik item
            if (getView() != null) {
                getView().animate()
                        .alpha(0.8f)
                        .setDuration(100)
                        .withEndAction(() ->
                                getView().animate().alpha(1f).setDuration(100).start())
                        .start();
            }

            DetailFragment detailFragment = DetailFragment.newInstance(novel);

            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvSavedNovels.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvSavedNovels.setHasFixedSize(true);
        rvSavedNovels.setAdapter(adapter);

        return view;
    }

    // ✨ Animasi saat fragment muncul
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setAlpha(0f);
        view.setTranslationY(40f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .start();
    }

    private void loadSavedNovels() {
        savedNovelList = db.getFullSavedNovels(username);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadSavedNovels();
        if (adapter != null) {
            adapter.setNovelList(savedNovelList);
        }
    }
}