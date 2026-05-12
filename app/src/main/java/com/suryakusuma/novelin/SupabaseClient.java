package com.suryakusuma.novelin;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    private static final String SUPABASE_URL = "https://ubdqjyojnzzdqhlmmkil.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InViZHFqeW9qbnp6ZHFobG1ta2lsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg0NzgyNzksImV4cCI6MjA5NDA1NDI3OX0.jp9Tj-hfQBTpzUtq8RFzhzUr4My73EHw-VY6HhYSKyk";

    private static Retrofit retrofit;
    private static OkHttpClient client;

    public static Retrofit getClient() {
        if (retrofit == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.Request request = original.newBuilder()
                                .header("apikey", SUPABASE_ANON_KEY)
                                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SUPABASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static String getSupabaseUrl() {
        return SUPABASE_URL;
    }

    public static String getSupabaseAnonKey() {
        return SUPABASE_ANON_KEY;
    }
}