package com.suryakusuma.novelin;

import java.util.List;

public class NovelApi {
    private String id;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private List<Novel.Chapter> chapters;

    public NovelApi() {}

    public NovelApi(String id, String title, String author, String description, String coverUrl, List<Novel.Chapter> chapters) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverUrl = coverUrl;
        this.chapters = chapters;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    // Ubah return type menjadi Novel.Chapter
    public List<Novel.Chapter> getChapters() { return chapters; }
    public void setChapters(List<Novel.Chapter> chapters) { this.chapters = chapters; }
}