package com.suryakusuma.novelin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelViewHolder> {

    private List<Novel> novelList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Novel novel);
    }

    // Constructor dengan 2 argumen untuk memperbaiki error "Expected 1 argument but found 2"
    public NovelAdapter(List<Novel> novelList, OnItemClickListener listener) {
        this.novelList = novelList;
        this.listener = listener;
    }

    // Method filterList untuk memperbaiki error "Cannot resolve method 'filterList'"
    public void filterList(List<Novel> filteredList) {
        this.novelList = filteredList;
        notifyDataSetChanged();
    }

    // Method untuk update data dari ViewModel nantinya
    public void setNovelList(List<Novel> newList) {
        this.novelList = newList;
        notifyDataSetChanged();
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

        // Memuat gambar: Prioritaskan coverUrl (API), jika kosong pakai coverResourceId (Lokal)
        if (novel.getCoverUrl() != null && !novel.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(novel.getCoverUrl())
                    .placeholder(R.drawable.novel1) // Placeholder saat loading
                    .error(R.drawable.novel1)       // Gambar jika gagal
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(novel.getCoverResourceId());
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(novel));
    }

    @Override
    public int getItemCount() {
        return novelList != null ? novelList.size() : 0;
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
