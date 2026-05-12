package com.suryakusuma.novelin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class NovelViewModel extends ViewModel {

    private final MutableLiveData<List<Novel>> _novels = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Novel>> getNovels() { return _novels; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private final NovelScraper scraper = new NovelScraper();
    private int page = 1;

    public void loadBrowseNovels() {
        if (Boolean.TRUE.equals(_isLoading.getValue())) return;
        
        _isLoading.setValue(true);
        page = 1;
        
        scraper.browseNovels(NovelScraper.SOURCE_MEIONOVEL, page, new NovelScraper.ScrapeListener<List<Novel>>() {
            @Override
            public void onResult(List<Novel> result) {
                _novels.postValue(result);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void loadMoreNovels() {
        if (Boolean.TRUE.equals(_isLoading.getValue())) return;
        
        _isLoading.setValue(true);
        page++;
        final int p = page;
        
        scraper.browseNovels(NovelScraper.SOURCE_MEIONOVEL, p, new NovelScraper.ScrapeListener<List<Novel>>() {
            @Override
            public void onResult(List<Novel> result) {
                List<Novel> current = _novels.getValue();
                if (current == null) current = new ArrayList<>();
                List<Novel> updated = new ArrayList<>(current);
                updated.addAll(result);
                _novels.postValue(updated);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void searchNovels(String query) {
        if (query == null || query.trim().isEmpty()) return;
        
        _isLoading.setValue(true);
        scraper.searchNovels(query.trim(), new NovelScraper.ScrapeListener<List<Novel>>() {
            @Override
            public void onResult(List<Novel> result) {
                _novels.postValue(result);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }
}
