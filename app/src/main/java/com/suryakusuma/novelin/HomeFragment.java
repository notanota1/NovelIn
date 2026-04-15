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

import java.util.ArrayList;
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
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");

        loadSavedNovels();

        adapter = new NovelAdapter(savedNovelList, novel -> {
            DetailFragment detailFragment = DetailFragment.newInstance(novel);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvSavedNovels.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvSavedNovels.setHasFixedSize(true);
        rvSavedNovels.setAdapter(adapter);

        return view;
    }

    private void loadSavedNovels() {
        savedNovelList = new ArrayList<>();
        List<String> savedTitles = db.getSavedNovels(username);
        
        List<Novel> allNovels = NovelData.getAllNovels();
        for (Novel n : allNovels) {
            if (savedTitles.contains(n.getTitle())) {
                savedNovelList.add(n);
            }
        }
    }
}
