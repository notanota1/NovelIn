package com.suryakusuma.novelin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private List<Novel.Chapter> chapters;
    private final OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(Novel.Chapter chapter);
    }

    public ChapterAdapter(List<Novel.Chapter> chapters, OnChapterClickListener listener) {
        this.chapters = chapters != null ? chapters : new ArrayList<>();
        this.listener = listener;
    }

    public void setChapters(List<Novel.Chapter> chapters) {
        this.chapters = chapters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Novel.Chapter chapter = chapters.get(position);
        holder.tvTitle.setText(chapter.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapter));
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(android.R.id.text1);
        }
    }
}
