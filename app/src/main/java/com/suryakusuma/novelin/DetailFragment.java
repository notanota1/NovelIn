package com.suryakusuma.novelin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DetailFragment extends Fragment {

    private static final String ARG_NOVEL = "novel";
    private Novel novel;
    private DatabaseHelper db;
    private String username;
    private Button btnSave;
    private boolean isSaved;
    private ChapterAdapter chapterAdapter;
    private ProgressBar progressBar;
    private TextView tvAuthor;
    private TextView tvDesc;
    private final NovelScraper scraper = new NovelScraper();

    public static DetailFragment newInstance(Novel novel) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOVEL, novel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            novel = (Novel) getArguments().getSerializable(ARG_NOVEL);
        }
        db = new DatabaseHelper(getContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackDetail);
        ImageView ivCover   = view.findViewById(R.id.ivDetailCover);
        TextView tvTitle    = view.findViewById(R.id.tvDetailTitle);
        tvAuthor            = view.findViewById(R.id.tvDetailAuthor);
        tvDesc              = view.findViewById(R.id.tvDetailDesc);
        btnSave             = view.findViewById(R.id.btnSave);
        RecyclerView rvChapters = view.findViewById(R.id.rvChapters);
        progressBar         = view.findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        if (novel == null) return view;

        // Cover
        if (!novel.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(novel.getCoverUrl())
                    .placeholder(R.drawable.novel1)
                    .error(R.drawable.novel1)
                    .into(ivCover);
        } else {
            ivCover.setImageResource(novel.getCoverResourceId() != 0
                    ? novel.getCoverResourceId() : R.drawable.novel1);
        }

        tvTitle.setText(novel.getTitle());
        refreshAuthorDesc();

        // Save button
        updateSaveButton();
        btnSave.setOnClickListener(v -> toggleSave());

        // Chapters RecyclerView
        rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));
        chapterAdapter = new ChapterAdapter(novel.getChapters(), chapter -> {
            String source = novel.getSource();
            ReadingFragment readingFragment = ReadingFragment.newInstance(chapter.getUrl(), source);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, readingFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvChapters.setAdapter(chapterAdapter);

        // Jika novel dari web, fetch detail & chapters
        if (!novel.getNovelUrl().isEmpty()) {
            loadDetailIfNeeded();
            loadChapters();
        }

        return view;
    }

    /**
     * Muat sinopsis & author dari halaman detail jika belum ada.
     */
    private void loadDetailIfNeeded() {
        boolean needsDetail = novel.getAuthor().isEmpty() || novel.getDescription().isEmpty();
        if (!needsDetail) return;

        scraper.getNovelDetail(novel, new NovelScraper.ScrapeListener<Novel>() {
            @Override
            public void onResult(Novel result) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> refreshAuthorDesc());
            }
            @Override
            public void onError(Exception e) {
                // Gagal fetch detail — tampilkan info kosong, tidak perlu toast
            }
        });
    }

    private void refreshAuthorDesc() {
        String author = novel.getAuthor();
        tvAuthor.setText(author.isEmpty() ? "Penulis: -" : "Penulis: " + author);

        String desc = novel.getDescription();
        tvDesc.setText(desc.isEmpty() ? "Sinopsis tidak tersedia." : desc);
    }

    private void loadChapters() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        scraper.getChapters(novel.getNovelUrl(), novel.getSource(),
            new NovelScraper.ScrapeListener<List<Novel.Chapter>>() {
                @Override
                public void onResult(List<Novel.Chapter> result) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        novel.setChapters(result);
                        chapterAdapter.setChapters(result);
                    });
                }
                @Override
                public void onError(Exception e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                            "Gagal memuat chapter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }

    private void toggleSave() {
        if (isSaved) {
            if (db.deleteSavedNovel(username, novel.getTitle())) {
                Toast.makeText(getContext(), "Dihapus dari Library", Toast.LENGTH_SHORT).show();
                isSaved = false;
                updateSaveButton();
            }
        } else {
            if (db.saveNovel(username, novel)) {
                Toast.makeText(getContext(), "Disimpan ke Library", Toast.LENGTH_SHORT).show();
                isSaved = true;
                updateSaveButton();
            }
        }
    }

    private void updateSaveButton() {
        isSaved = db.isNovelSaved(username, novel.getTitle());
        if (isSaved) {
            btnSave.setText("HAPUS DARI LIBRARY");
            btnSave.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_red_dark)));
        } else {
            btnSave.setText("SIMPAN KE LIBRARY");
            btnSave.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_blue_dark)));
        }
    }
}
