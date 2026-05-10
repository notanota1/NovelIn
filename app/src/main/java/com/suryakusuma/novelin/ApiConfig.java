package com.suryakusuma.novelin;

import com.suryakusuma.novelin.BuildConfig;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiConfig {

    public static ApiService getApiService() {

        // 1. Logging Interceptor (Opsional: Untuk melihat log request & response di Logcat)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

        // 2. Auth Interceptor: Otomatis menambahkan API Key ke setiap request
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            // Menambahkan query parameter "key" secara otomatis
            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("key", BuildConfig.BOOKS_API_KEY)
                    .build();

            Request request = original.newBuilder()
                    .url(url)
                    .build();
            return chain.proceed(request);
        };

        // 3. OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build();

        // 4. Konfigurasi Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/") // Base URL Google Books API
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(ApiService.class);
    }
}