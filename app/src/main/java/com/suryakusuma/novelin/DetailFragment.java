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
    private TextView tvLang;
    private TextView tvTags;
    private ImageView ivCover; // Dipindah ke field agar bisa diakses refreshDetailUI
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
        ivCover             = view.findViewById(R.id.ivDetailCover);
        TextView tvTitle    = view.findViewById(R.id.tvDetailTitle);
        tvAuthor            = view.findViewById(R.id.tvDetailAuthor);
        tvDesc              = view.findViewById(R.id.tvDetailDesc);
        tvLang              = view.findViewById(R.id.tvDetailLang);
        tvTags              = view.findViewById(R.id.tvDetailTags);
        btnSave             = view.findViewById(R.id.btnSave);
        RecyclerView rvChapters = view.findViewById(R.id.rvChapters);
        progressBar         = view.findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        if (novel == null) return view;

        tvTitle.setText(novel.getTitle());
        refreshDetailUI();

        // Save button
        updateSaveButton();
        btnSave.setOnClickListener(v -> toggleSave());

        // Chapters RecyclerView
        rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));
        chapterAdapter = new ChapterAdapter(novel.getChapters(), chapter -> {
            // Animasi klik
            if (getView() != null) {
                getView().animate()
                        .alpha(0.7f)
                        .setDuration(100)
                        .withEndAction(() ->
                                getView().animate().alpha(1f).setDuration(100).start())
                        .start();
            }

            ReadingFragment readingFragment = ReadingFragment.newInstance(chapter.getUrl(), novel.getSource());
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, readingFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvChapters.setAdapter(chapterAdapter);

        // Fetch Detail from RanobeDB API
        loadDetailFromApi();

        return view;
    }

    // ✨ ANIMASI MASUK FRAGMENT
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setAlpha(0f);
        view.setTranslationY(50f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .start();
    }

    private void loadDetailFromApi() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        scraper.getNovelDetail(novel, new NovelScraper.ScrapeListener<Novel>() {
            @Override
            public void onResult(Novel result) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    refreshDetailUI();
                    if (novel.getNovelUrl() != null && !novel.getNovelUrl().isEmpty()) {
                        loadChapters();
                    } else {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Gagal memuat detail: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void refreshDetailUI() {
        tvAuthor.setText(novel.getAuthor().isEmpty() ? "Penulis: -" : "Penulis: " + novel.getAuthor());
        tvDesc.setText(novel.getDescription().isEmpty() ? "Sinopsis tidak tersedia." : novel.getDescription());
        tvLang.setText("Bahasa Asli: " + (novel.getLanguage().isEmpty() ? "-" : novel.getLanguage()));

        // Memuat/Update Cover
        if (novel.getCoverUrl() != null && !novel.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(novel.getCoverUrl())
                    .placeholder(R.drawable.novel1)
                    .error(R.drawable.novel1)
                    .into(ivCover);
        }

        if (novel.getTags() != null && !novel.getTags().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < novel.getTags().size(); i++) {
                sb.append(novel.getTags().get(i));
                if (i < novel.getTags().size() - 1) sb.append(", ");
            }
            tvTags.setText(sb.toString());
            tvTags.setVisibility(View.VISIBLE);
        } else {
            tvTags.setVisibility(View.GONE);
        }
    }

    private void loadChapters() {
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
            btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF4444));
        } else {
            btnSave.setText("SIMPAN KE LIBRARY");
            btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF0099CC));
        }
    }
}
