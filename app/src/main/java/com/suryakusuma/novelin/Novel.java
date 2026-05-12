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
    private String novelUrl;
    private String source;
    private List<Chapter> chapters;

    public Novel(String title, String author, String description, int coverResourceId, List<Chapter> chapters) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverResourceId = coverResourceId;
        this.chapters = chapters;
        this.coverUrl = "";
        this.novelUrl = "";
        this.source = "";
    }

    public Novel(String title, String author, String description, String coverUrl, String novelUrl) {
        this.title = title;
        this.author = author != null ? author : "";
        this.description = description != null ? description : "";
        this.coverUrl = coverUrl != null ? coverUrl : "";
        this.novelUrl = novelUrl != null ? novelUrl : "";
        this.chapters = new ArrayList<>();
        this.source = "";
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author != null ? author : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public int getCoverResourceId() { return coverResourceId; }
    public String getCoverUrl() { return coverUrl != null ? coverUrl : ""; }
    public String getNovelUrl() { return novelUrl != null ? novelUrl : ""; }
    public String getSource() { return source != null ? source : ""; }
    public List<Chapter> getChapters() { return chapters != null ? chapters : new ArrayList<>(); }

    public void setAuthor(String author) { this.author = author; }
    public void setDescription(String description) { this.description = description; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setSource(String source) { this.source = source; }
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
