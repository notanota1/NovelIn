package com.suryakusuma.novelin;

import java.io.Serializable;
import java.util.List;

public class Novel implements Serializable {
    private String title;
    private String author;
    private String description;
    private int coverResourceId;
    private List<Chapter> chapters;

    public Novel(String title, String author, String description, int coverResourceId, List<Chapter> chapters) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverResourceId = coverResourceId;
        this.chapters = chapters;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public int getCoverResourceId() { return coverResourceId; }
    public List<Chapter> getChapters() { return chapters; }

    public static class Chapter implements Serializable {
        private String title;
        private String fileName;

        public Chapter(String title, String fileName) {
            this.title = title;
            this.fileName = fileName;
        }

        public String getTitle() { return title; }
        public String getFileName() { return fileName; }
    }
}
