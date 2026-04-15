package com.suryakusuma.novelin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadingFragment extends Fragment {

    private static final String ARG_FILE_NAME = "file_name";
    private String fileName;

    public static ReadingFragment newInstance(String fileName) {
        ReadingFragment fragment = new ReadingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_NAME, fileName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fileName = getArguments().getString(ARG_FILE_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);
        TextView tvContent = view.findViewById(R.id.tvReadingContent);

        if (fileName != null) {
            tvContent.setText(loadAssetFile(fileName));
        }

        return view;
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
