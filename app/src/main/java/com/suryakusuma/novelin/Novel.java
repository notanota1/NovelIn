package com.suryakusuma.novelin;

import java.io.Serializable;

public class Novel implements Serializable {
    private String title;
    private String author;
    private String description;
    private int coverResourceId;
    private String content;

    public Novel(String title, String author, String description, int coverResourceId, String content) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverResourceId = coverResourceId;
        this.content = content;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public int getCoverResourceId() { return coverResourceId; }
    public String getContent() { return content; }
}
