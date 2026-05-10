package com.suryakusuma.novelin;

import java.io.Serializable;
import java.util.List;

public class Novel implements Serializable {
    private String title;           // Judul Novel
    private String author;          // Nama Penulis
    private String description;     // Sinopsis/Deskripsi Novel
    private int coverResourceId;    // ID resource gambar cover (dari R.drawable)
    private String coverUrl;        // URL untuk cover dari API
    private List<Chapter> chapters; // Daftar chapter yang tersedia untuk novel ini

    // Constructor untuk data lokal
    public Novel(String title, String author, String description, int coverResourceId, List<Chapter> chapters) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverResourceId = coverResourceId;
        this.chapters = chapters;
    }

    // Constructor untuk data API
    public Novel(String title, String author, String description, String coverUrl, List<Chapter> chapters) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverUrl = coverUrl;
        this.chapters = chapters;
    }

    // Getter methods untuk mengakses properti novel
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public int getCoverResourceId() { return coverResourceId; }
    public String getCoverUrl() { return coverUrl; }
    public List<Chapter> getChapters() { return chapters; }

    //Inner class untuk merepresentasikan data Chapter.
    public static class Chapter implements Serializable {
        private String title;    // Judul Chapter (misal: "Chapter 1")
        private String fileName;

        public Chapter(String title, String fileName) {
            this.title = title;
            this.fileName = fileName;
        }

        public String getTitle() { return title; }
        public String getFileName() { return fileName; }
    }
}
