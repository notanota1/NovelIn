package com.suryakusuma.novelin;

import com.google.gson.annotations.SerializedName;

public class NovelSearchResponse {
    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @SerializedName("link")
    private String link;

    public String getTitle() { return title; }
    public String getImage() { return image; }
    public String getLink() { return link; }
}
