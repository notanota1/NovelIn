package com.suryakusuma.novelin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView rvNovels;
    private NovelAdapter adapter;
    private List<Novel> localNovelList;
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private NovelViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        
        rvNovels = view.findViewById(R.id.rvNovels);
        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.progressBar);

        // 1. Ambil data lokal dari NovelData
        localNovelList = NovelData.getAllNovels();
        
        // 2. Setup Adapter dengan data lokal sebagai awalan
        adapter = new NovelAdapter(new ArrayList<>(localNovelList), novel -> {
            DetailFragment detailFragment = DetailFragment.newInstance(novel);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvNovels.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvNovels.setHasFixedSize(true);
        rvNovels.setAdapter(adapter);

        // 3. Inisialisasi ViewModel
        viewModel = new ViewModelProvider(this).get(NovelViewModel.class);

        // 4. Observasi data dari API
        viewModel.getNovels().observe(getViewLifecycleOwner(), apiNovels -> {
            if (apiNovels != null) {
                // Gabungkan data lokal dan data API untuk hasil pencarian
                List<Novel> combinedList = new ArrayList<>(localNovelList);
                // Filter local list based on query if needed, 
                // but usually API results are enough for a global search.
                // For now, let's just combine them or show API results if searching.
                
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    List<Novel> filteredLocal = getFilteredLocal(query);
                    List<Novel> results = new ArrayList<>(filteredLocal);
                    results.addAll(apiNovels);
                    adapter.setNovelList(results);
                }
            }
        });

        // Observasi status loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observasi error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Listener Pencarian
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Jika kosong, tampilkan kembali data lokal saja
                    adapter.setNovelList(localNovelList);
                } else {
                    // Filter lokal dulu agar responsif
                    adapter.setNovelList(getFilteredLocal(query));
                    
                    // Jika lebih dari 2 karakter, cari di API untuk hasil lebih luas
                    if (query.length() > 2) {
                        viewModel.searchNovels(query);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private List<Novel> getFilteredLocal(String query) {
        List<Novel> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Novel item : localNovelList) {
            if (item.getTitle().toLowerCase().contains(lowerQuery) ||
                item.getAuthor().toLowerCase().contains(lowerQuery)) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }
}
