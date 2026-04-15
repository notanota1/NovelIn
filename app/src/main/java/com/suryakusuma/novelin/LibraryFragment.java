package com.suryakusuma.novelin;

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

public class LibraryFragment extends Fragment {

    private RecyclerView rvNovels;
    private NovelAdapter adapter;
    private List<Novel> novelList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        rvNovels = view.findViewById(R.id.rvNovels);

        // Mengambil data terpusat dari NovelData
        novelList = NovelData.getAllNovels();
        
        adapter = new NovelAdapter(novelList, novel -> {
            DetailFragment detailFragment = DetailFragment.newInstance(novel);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvNovels.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvNovels.setAdapter(adapter);

        return view;
    }
}
