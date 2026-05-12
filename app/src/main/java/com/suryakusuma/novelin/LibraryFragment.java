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
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private NovelViewModel viewModel;

    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        rvNovels     = view.findViewById(R.id.rvNovels);
        etSearch     = view.findViewById(R.id.etSearch);
        progressBar  = view.findViewById(R.id.progressBar);

        // Adapter kosong — data akan diisi dari ViewModel
        adapter = new NovelAdapter(new ArrayList<>(), novel -> {
            DetailFragment detailFragment = DetailFragment.newInstance(novel);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Grid 3 kolom untuk tampilan lebih banyak novel
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        rvNovels.setLayoutManager(layoutManager);
        rvNovels.setHasFixedSize(false);
        rvNovels.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(NovelViewModel.class);

        // Observasi daftar novel
        viewModel.getNovels().observe(getViewLifecycleOwner(), novels -> {
            if (novels != null) {
                adapter.setNovelList(novels);
            }
        });

        // Observasi loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            if (progressBar != null) {
                progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
            }
        });

        // Observasi error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Infinite scroll — muat lebih banyak saat sampai bawah
        rvNovels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (isSearching) return;
                if (dy <= 0) return;
                if (!rv.canScrollVertically(1)) {
                    // Sudah di paling bawah — muat halaman berikutnya
                    viewModel.loadMoreNovels();
                }
            }
        });

        // Search listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();
                if (q.isEmpty()) {
                    // Kembali ke mode browse
                    isSearching = false;
                    viewModel.loadBrowseNovels();
                } else if (q.length() >= 3) {
                    // Cari di semua sumber
                    isSearching = true;
                    viewModel.searchNovels(q);
                }
            }
        });

        // Muat novel dari Meionovel saat fragment pertama dibuka
        if (viewModel.getNovels().getValue() == null
                || viewModel.getNovels().getValue().isEmpty()) {
            viewModel.loadBrowseNovels();
        }

        return view;
    }
}
