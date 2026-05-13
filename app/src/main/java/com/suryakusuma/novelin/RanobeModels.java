package com.suryakusuma.novelin;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RanobeModels {

    public static class SeriesResponse {
        @SerializedName("series")
        public List<SeriesItem> series;
        public int currentPage;
        public int totalPages;
    }

    public static class SeriesItem {
        public int id;
        public String title;
        public String romaji;
        public String olang;
        public BookCover book;
    }

    public static class BookCover {
        public int id;
        public ImageData image;
    }

    public static class ImageData {
        public int id;
        public String filename;
    }

    public static class SeriesDetail {
        public int id;
        public String title;

        @SerializedName("description_ja")
        public String descriptionJa;

        @SerializedName("description_en")
        public String descriptionEn;
        
        @SerializedName("description")
        public String descriptionGeneric;

        @SerializedName("original_language")
        public String olang;

        public List<StaffItem> staff;
        public List<TagItem> tags;
        public List<BookItem> books;
    }

    public static class StaffItem {
        public String name;
        @SerializedName("role_type")
        public String roleType;
    }

    public static class TagItem {
        public String name;
    }

    public static class BookItem {
        public int id;
        public String title;
        public ImageData image;
        @SerializedName("book_type")
        public String bType;
    }
}
