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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class DetailFragment extends Fragment {

    private static final String ARG_NOVEL = "novel";
    private Novel novel;
    private DatabaseHelper db;
    private String username;
    private Button btnSave;
    private boolean isSaved;

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

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        if (novel != null) {
            // Load Cover Image using Glide (API URL or local resource)
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
                    // Update: passing the whole novel object to save method
                    if (db.saveNovel(username, novel)) {
                        Toast.makeText(getContext(), "Saved to Library", Toast.LENGTH_SHORT).show();
                        isSaved = true;
                        updateSaveButton();
                    }
                }
            });

            // Set up Chapters RecyclerView
            rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));
            ChapterAdapter chapterAdapter = new ChapterAdapter(novel.getChapters(), chapter -> {
                ReadingFragment readingFragment = ReadingFragment.newInstance(chapter.getFileName());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, readingFragment)
                        .addToBackStack(null)
                        .commit();
            });
            rvChapters.setAdapter(chapterAdapter);
        }

        return view;
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
