package com.suryakusuma.novelin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelViewHolder> {

    private List<Novel> novelList;
    private final OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Novel novel);
    }

    public NovelAdapter(List<Novel> novelList, OnItemClickListener listener) {
        // Membuat salinan list agar data asli tetap aman
        this.novelList = new ArrayList<>(novelList);
        this.listener = listener;
    }


    public void filterList(List<Novel> newList) {
        // DiffUtil menghitung perbedaan antara list lama dan baru secara efisien
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NovelDiffCallback(this.novelList, newList));
        this.novelList.clear();
        this.novelList.addAll(newList);
        // Memberitahu RecyclerView hanya bagian mana yang perlu diupdate (mencegah lag)
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public NovelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menghubungkan layout item_novel ke ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_novel, parent, false);
        return new NovelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelViewHolder holder, int position) {
        // Memasang data novel ke komponen UI
        Novel novel = novelList.get(position);
        holder.tvTitle.setText(novel.getTitle());
        holder.ivCover.setImageResource(novel.getCoverResourceId());
        
        // Mengatur listener klik untuk item
        holder.itemView.setOnClickListener(v -> listener.onItemClick(novel));
    }

    @Override
    public int getItemCount() {
        return novelList.size();
    }

    /**
     * ViewHolder sebagai penampung referensi view untuk performa scrolling.
     */
    static class NovelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;

        public NovelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }

    /**
     * Callback untuk membantu DiffUtil mendeteksi perubahan antar item.
     */
    private static class NovelDiffCallback extends DiffUtil.Callback {
        private final List<Novel> oldList;
        private final List<Novel> newList;

        public NovelDiffCallback(List<Novel> oldList, List<Novel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            // Mengecek apakah itemnya sama (berdasarkan judul sebagai ID unik)
            return oldList.get(oldPos).getTitle().equals(newList.get(newPos).getTitle());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            // Mengecek apakah konten di dalam item ada yang berubah
            Novel oldNovel = oldList.get(oldPos);
            Novel newNovel = newList.get(newPos);
            return oldNovel.getTitle().equals(newNovel.getTitle()) &&
                   oldNovel.getCoverResourceId() == newNovel.getCoverResourceId();
        }
    }
}
