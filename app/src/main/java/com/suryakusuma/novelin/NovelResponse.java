package com.suryakusuma.novelin;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NovelResponse {

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("items")
    private List<NovelItem> items;

    public int getTotalItems() {
        return totalItems;
    }

    public List<NovelItem> getItems() {
        return items;
    }

    // Class penampung tiap entri buku di dalam array "items"
    public static class NovelItem {
        @SerializedName("id")
        private String id;

        @SerializedName("volumeInfo")
        private VolumeInfo volumeInfo;

        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
    }

    public static class VolumeInfo {
        @SerializedName("title")
        private String title;

        @SerializedName("authors")
        private List<String> authors;

        @SerializedName("description")
        private String description;

        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        public String getTitle() { return title; }
        public List<String> getAuthors() { return authors; }
        public String getDescription() { return description; }
        public ImageLinks getImageLinks() { return imageLinks; }
    }

    // Class penampung URL gambar
    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;

        public String getThumbnail() {
            // Mengubah http menjadi https agar aman bagi Android modern
            if (thumbnail != null && thumbnail.startsWith("http://")) {
                return thumbnail.replace("http://", "https://");
            }
            return thumbnail;
        }
    }
}