package com.suryakusuma.novelin;

import com.suryakusuma.novelin.BuildConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    String myKey = BuildConfig.BOOKS_API_KEY;
    @GET("volumes")
    Call<NovelResponse> searchNovels(
            @Query("q") String query,
            @Query("key") String apiKey
    );

    @GET("volumes")
    Call<NovelResponse> searchNovelsWithLimit(
            @Query("q") String query,
            @Query("startIndex") int startIndex,
            @Query("maxResults") int maxResults,
            @Query("key") String apiKey
    );
}