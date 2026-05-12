package com.suryakusuma.novelin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

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

    private final NovelScraper scraper = new NovelScraper();

    public void searchNovels(String query) {
        if (query == null || query.trim().isEmpty()) return;

        _isLoading.setValue(true);
        String searchQuery = query.trim();

        // Hanya menggunakan Scraping Website (WuxiaWorld)
        scraper.searchNovels(searchQuery, new NovelScraper.ScrapeListener<List<Novel>>() {
            @Override
            public void onResult(List<Novel> result) {
                _isLoading.postValue(false);
                _novels.postValue(result);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue("Gagal mengambil data dari WuxiaWorld: " + e.getMessage());
            }
        });
    }
}
