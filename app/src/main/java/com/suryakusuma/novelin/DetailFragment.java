package com.suryakusuma.novelin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DetailFragment extends Fragment {

    private static final String ARG_NOVEL = "novel";
    private Novel novel;
    private DatabaseHelper db;
    private String username;

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

        ImageView ivCover = view.findViewById(R.id.ivDetailCover);
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvAuthor = view.findViewById(R.id.tvDetailAuthor);
        TextView tvDesc = view.findViewById(R.id.tvDetailDesc);
        Button btnRead = view.findViewById(R.id.btnRead);
        Button btnSave = view.findViewById(R.id.btnSave);

        if (novel != null) {
            ivCover.setImageResource(novel.getCoverResourceId());
            tvTitle.setText(novel.getTitle());
            tvAuthor.setText("Author: " + novel.getAuthor());
            tvDesc.setText(novel.getDescription());

            btnSave.setOnClickListener(v -> {
                boolean result = db.saveNovel(username, novel.getTitle());
                if (result) {
                    Toast.makeText(getContext(), "Novel saved to Library!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error saving novel", Toast.LENGTH_SHORT).show();
                }
            });

            btnRead.setOnClickListener(v -> {
                ReadingFragment readingFragment = ReadingFragment.newInstance(novel.getContent());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, readingFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }
}
