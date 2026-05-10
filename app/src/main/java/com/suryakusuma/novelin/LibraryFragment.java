package com.suryakusuma.novelin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView rvNovels;
    private NovelAdapter adapter;
    private List<Novel> novelList;
    private TextInputEditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        rvNovels = view.findViewById(R.id.rvNovels);
        etSearch = view.findViewById(R.id.etSearch);

        novelList = NovelData.getAllNovels();
        
        adapter = new NovelAdapter(novelList, novel -> {
            DetailFragment detailFragment = DetailFragment.newInstance(novel);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvNovels.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvNovels.setHasFixedSize(true); // Optimasi: Ukuran item tetap
        rvNovels.setItemViewCacheSize(20); // Optimasi: Simpan lebih banyak view di cache
        rvNovels.setDrawingCacheEnabled(true);
        rvNovels.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rvNovels.setAdapter(adapter);

        // Menambahkan listener untuk fitur pencarian
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void filter(String text) {
        List<Novel> filteredList = new ArrayList<>();
        String query = text.toLowerCase().trim();

        if (query.isEmpty()) {
            filteredList.addAll(novelList);
        } else {
            for (Novel item : novelList) {
                if (item.getTitle().toLowerCase().contains(query) ||
                    item.getAuthor().toLowerCase().contains(query)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.filterList(filteredList);
    }
}
