package com.suryakusuma.novelin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Novel implements Serializable {
    private int id; // Diubah menjadi int agar sesuai dengan API RanobeDB
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private int coverResourceId;
    private String novelUrl;
    private String source;
    private String language;
    private List<String> tags;
    private List<Chapter> chapters;

    // Tambahkan Constructor Kosong ini agar 'new Novel()' di Scraper tidak error
    public Novel() {
        this.chapters = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Novel(String title, String author, String description, String coverUrl, String novelUrl) {
        this.title = title;
        this.author = author != null ? author : "";
        this.description = description != null ? description : "";
        this.coverUrl = coverUrl != null ? coverUrl : "";
        this.novelUrl = novelUrl != null ? novelUrl : "";
        this.chapters = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // Constructor baru untuk mendukung resource ID dan daftar chapter (digunakan di DatabaseHelper dan NovelData)
    public Novel(String title, String author, String description, int coverResourceId, List<Chapter> chapters) {
        this.title = title;
        this.author = author != null ? author : "";
        this.description = description != null ? description : "";
        this.coverResourceId = coverResourceId;
        this.chapters = chapters != null ? chapters : new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // Getters and Setters
    // setId sekarang menerima int sesuai kebutuhan di NovelScraper
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author != null ? author : ""; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl != null ? coverUrl : ""; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public int getCoverResourceId() { return coverResourceId; }
    public void setCoverResourceId(int coverResourceId) { this.coverResourceId = coverResourceId; }

    public String getNovelUrl() { return novelUrl != null ? novelUrl : ""; }
    public void setNovelUrl(String novelUrl) { this.novelUrl = novelUrl; }

    public String getSource() { return source != null ? source : ""; }
    public void setSource(String source) { this.source = source; }

    public String getLanguage() { return language != null ? language : ""; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<Chapter> getChapters() { return chapters != null ? chapters : new ArrayList<>(); }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }

    public static class Chapter implements Serializable {
        private final String title;
        private final String url;

        public Chapter(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }
}