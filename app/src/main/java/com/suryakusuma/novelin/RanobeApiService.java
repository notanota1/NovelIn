package com.suryakusuma.novelin;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RanobeApiService {


    @GET("series")
    Call<RanobeModels.SeriesResponse> searchSeries(
            @Query("q") String query,
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("series/{id}")
    Call<RanobeModels.SeriesDetail> getSeriesDetail(
            @Path("id") int id
    );
}
