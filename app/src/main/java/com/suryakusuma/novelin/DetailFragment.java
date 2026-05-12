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
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackDetail);
        ImageView ivCover = view.findViewById(R.id.ivDetailCover);
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvAuthor = view.findViewById(R.id.tvDetailAuthor);
        TextView tvDesc = view.findViewById(R.id.tvDetailDesc);
        btnSave = view.findViewById(R.id.btnSave);
        RecyclerView rvChapters = view.findViewById(R.id.rvChapters);
        progressBar = view.findViewById(R.id.progressBar); // Pastikan ada ProgressBar di layout

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        if (novel != null) {
            if (novel.getCoverUrl() != null && !novel.getCoverUrl().isEmpty()) {
                Glide.with(this).load(novel.getCoverUrl()).placeholder(R.drawable.novel1).into(ivCover);
            } else {
                ivCover.setImageResource(novel.getCoverResourceId());
            }

            tvTitle.setText(novel.getTitle());
            tvAuthor.setText("Author: " + novel.getAuthor());
            tvDesc.setText(novel.getDescription());

            updateSaveButton();

            btnSave.setOnClickListener(v -> {
                if (isSaved) {
                    if (db.deleteSavedNovel(username, novel.getTitle())) {
                        Toast.makeText(getContext(), "Removed from Library", Toast.LENGTH_SHORT).show();
                        isSaved = false;
                        updateSaveButton();
                    }
                } else {
                    if (db.saveNovel(username, novel)) {
                        Toast.makeText(getContext(), "Saved to Library", Toast.LENGTH_SHORT).show();
                        isSaved = true;
                        updateSaveButton();
                    }
                }
            });

            rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));
            chapterAdapter = new ChapterAdapter(novel.getChapters(), chapter -> {
                // Pass URL atau fileName ke ReadingFragment
                ReadingFragment readingFragment = ReadingFragment.newInstance(chapter.getUrl());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, readingFragment)
                        .addToBackStack(null)
                        .commit();
            });
            rvChapters.setAdapter(chapterAdapter);

            // Jika ada novelUrl (hasil scraping), ambil daftar chapter secara dinamis
            if (novel.getNovelUrl() != null && !novel.getNovelUrl().isEmpty()) {
                loadChapters();
            }
        }

        return view;
    }

    private void loadChapters() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        scraper.getChapters(novel.getNovelUrl(), new NovelScraper.ScrapeListener<List<Novel.Chapter>>() {
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
                    Toast.makeText(getContext(), "Gagal memuat chapter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateSaveButton() {
        isSaved = db.isNovelSaved(username, novel.getTitle());
        if (isSaved) {
            btnSave.setText("REMOVE FROM LIBRARY");
            btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
        } else {
            btnSave.setText("SAVE TO LIBRARY");
            btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_dark)));
        }
    }
}
