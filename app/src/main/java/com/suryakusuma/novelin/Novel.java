package com.suryakusuma.novelin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Novel implements Serializable {
    private String title;
    private String author;
    private String description;
    private int coverResourceId;
    private String coverUrl;
    private String novelUrl; // URL ke halaman detail novel
    private List<Chapter> chapters;

    // Constructor untuk data lokal
    public Novel(String title, String author, String description, int coverResourceId, List<Chapter> chapters) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverResourceId = coverResourceId;
        this.chapters = chapters;
    }

    // Constructor untuk data API/Scraping
    public Novel(String title, String author, String description, String coverUrl, String novelUrl) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverUrl = coverUrl;
        this.novelUrl = novelUrl;
        this.chapters = new ArrayList<>();
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public int getCoverResourceId() { return coverResourceId; }
    public String getCoverUrl() { return coverUrl; }
    public String getNovelUrl() { return novelUrl; }
    public List<Chapter> getChapters() { return chapters; }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }

    public static class Chapter implements Serializable {
        private String title;
        private String url; // URL ke isi chapter atau file name lokal

        public Chapter(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }
}
