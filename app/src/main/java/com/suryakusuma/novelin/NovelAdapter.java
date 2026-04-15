package com.suryakusuma.novelin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelViewHolder> {

    private List<Novel> novelList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Novel novel);
    }

    public NovelAdapter(List<Novel> novelList, OnItemClickListener listener) {
        this.novelList = novelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NovelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_novel, parent, false);
        return new NovelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelViewHolder holder, int position) {
        Novel novel = novelList.get(position);
        holder.tvTitle.setText(novel.getTitle());
        holder.ivCover.setImageResource(novel.getCoverResourceId());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(novel));
    }

    @Override
    public int getItemCount() {
        return novelList.size();
    }

    static class NovelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;

        public NovelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
