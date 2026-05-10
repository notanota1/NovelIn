package com.suryakusuma.novelin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Pakai Glide
import com.suryakusuma.novelin.model.ItemsItem; // Sesuaikan dengan nama class Model API kamu

import java.util.ArrayList;
import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelViewHolder> {

    private List<ItemsItem> novelList = new ArrayList<>(); // Pakai model API
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemsItem novel);
    }

    public NovelAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setNovelList(List<ItemsItem> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NovelDiffCallback(this.novelList, newList));
        this.novelList.clear();
        this.novelList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public NovelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_novel, parent, false);
        return new NovelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelViewHolder holder, int position) {
        ItemsItem novel = novelList.get(position);

        // Ambil data dari volumeInfo (struktur Google Books API)
        if (novel.getVolumeInfo() != null) {
            holder.tvTitle.setText(novel.getVolumeInfo().getTitle());

            // Menggunakan Glide untuk load gambar dari URL
            if (novel.getVolumeInfo().getImageLinks() != null) {
                String imageUrl = novel.getVolumeInfo().getImageLinks().getThumbnail();
                // Google API biasanya pakai http, ganti ke https agar aman di Android
                if (imageUrl != null) {
                    imageUrl = imageUrl.replace("http://", "https://");
                }

                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_book) // gambar saat loading
                        .error(R.drawable.error_image)           // gambar jika gagal
                        .into(holder.ivCover);
            }
        }

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

    private static class NovelDiffCallback extends DiffUtil.Callback {
        private final List<ItemsItem> oldList;
        private final List<ItemsItem> newList;

        public NovelDiffCallback(List<ItemsItem> oldList, List<ItemsItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            // Pakai ID unik dari API Google Books
            return oldList.get(oldPos).getId().equals(newList.get(newPos).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).equals(newList.get(newPos));
        }
    }
}