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

    private static final String ARG_URL     = "url_or_file";
    private static final String ARG_SOURCE  = "source";

    private String urlOrFile;
    private String source;
    private TextView tvContent;
    private ProgressBar progressBar;
    private final NovelScraper scraper = new NovelScraper();

    public static ReadingFragment newInstance(String urlOrFile) {
        return newInstance(urlOrFile, "");
    }

    public static ReadingFragment newInstance(String urlOrFile, String source) {
        ReadingFragment f = new ReadingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, urlOrFile);
        args.putString(ARG_SOURCE, source != null ? source : "");
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            urlOrFile = getArguments().getString(ARG_URL);
            source    = getArguments().getString(ARG_SOURCE, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);
        tvContent   = view.findViewById(R.id.tvReadingContent);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        if (urlOrFile != null) {
            if (urlOrFile.startsWith("http")) {
                loadOnlineContent(urlOrFile);
            } else {
                tvContent.setText(loadFromAssets(urlOrFile));
            }
        }

        return view;
    }

    private void loadOnlineContent(String url) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        scraper.getChapterContent(url, source, new NovelScraper.ScrapeListener<String>() {
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
                    Toast.makeText(getContext(),
                        "Gagal memuat konten: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String loadFromAssets(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = requireContext().getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            reader.close();
        } catch (IOException e) {
            return "Gagal memuat konten: " + e.getMessage();
        }
        return sb.toString();
    }
}
