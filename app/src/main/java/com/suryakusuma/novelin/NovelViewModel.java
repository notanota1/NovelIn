package com.suryakusuma.novelin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NovelViewModel extends ViewModel {

    private final MutableLiveData<List<Novel>> _novels = new MutableLiveData<>();
    public LiveData<List<Novel>> getNovels() {
        return _novels;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public void searchNovels(String query) {
        _isLoading.setValue(true);
        ApiService apiService = ApiConfig.getApiService();
        
        // Menggunakan query default jika kosong
        String searchQuery = (query == null || query.isEmpty()) ? "novel" : query;

        apiService.searchNovels(searchQuery, BuildConfig.BOOKS_API_KEY).enqueue(new Callback<NovelResponse>() {
            @Override
            public void onResponse(Call<NovelResponse> call, Response<NovelResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Novel> list = new ArrayList<>();
                    if (response.body().getItems() != null) {
                        for (NovelResponse.NovelItem item : response.body().getItems()) {
                            NovelResponse.VolumeInfo info = item.getVolumeInfo();
                            if (info == null) continue;

                            String title = info.getTitle();
                            String author = (info.getAuthors() != null && !info.getAuthors().isEmpty()) 
                                    ? info.getAuthors().get(0) : "Unknown Author";
                            String desc = info.getDescription();
                            String thumbnail = (info.getImageLinks() != null) 
                                    ? info.getImageLinks().getThumbnail() : null;

                            // Map ke model Novel yang mendukung URL
                            list.add(new Novel(title, author, desc, thumbnail, new ArrayList<>()));
                        }
                    }
                    _novels.setValue(list);
                } else {
                    _errorMessage.setValue("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NovelResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Gagal terhubung: " + t.getMessage());
            }
        });
    }
}
