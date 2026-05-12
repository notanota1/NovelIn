package com.suryakusuma.novelin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadingFragment extends Fragment {

    private static final String ARG_URL_OR_FILE = "url_or_file";
    private String urlOrFile;
    private TextView tvContent;
    private ProgressBar progressBar;
    private final NovelScraper scraper = new NovelScraper();

    public static ReadingFragment newInstance(String urlOrFile) {
        ReadingFragment fragment = new ReadingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL_OR_FILE, urlOrFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            urlOrFile = getArguments().getString(ARG_URL_OR_FILE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);
        tvContent = view.findViewById(R.id.tvReadingContent);
        progressBar = view.findViewById(R.id.progressBar); // Pastikan ada ProgressBar di fragment_reading.xml
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        if (urlOrFile != null) {
            if (urlOrFile.startsWith("http")) {
                loadOnlineContent(urlOrFile);
            } else {
                tvContent.setText(loadAssetFile(urlOrFile));
            }
        }

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    private void loadOnlineContent(String url) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        scraper.getChapterContent(url, new NovelScraper.ScrapeListener<String>() {
            @Override
            public void onResult(String result) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    tvContent.setText(result);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Gagal memuat konten: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String loadAssetFile(String name) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = getContext().getAssets().open(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error loading content: " + e.getMessage();
        }
        return sb.toString();
    }
}
