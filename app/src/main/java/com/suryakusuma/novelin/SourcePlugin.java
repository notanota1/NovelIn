package com.suryakusuma.novelin;

import java.util.List;

public interface SourcePlugin {
    String getId();
    String getName();
    String getSite();
    String getLang();

    void searchNovels(String query, int page, Callback<List<Novel>> cb);
    void getPopularNovels(int page, Callback<List<Novel>> cb);
    void getChapters(String novelUrl, Callback<List<Novel.Chapter>> cb);
    void getChapterContent(String chapterUrl, Callback<String> cb);

    interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
