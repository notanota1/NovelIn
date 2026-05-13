package com.suryakusuma.novelin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NovelScraper {

    public static final String SOURCE_RANOBEDB = "RanobeDB";
    public static final String SOURCE_MEIONOVEL  = "MeioNovel";
    public static final String SOURCE_SAKURANOVEL = "SakuraNovel";
    public static final String SOURCE_WUXIAWORLD  = "WuxiaWorld";

    private static final String BASE_MEIO   = "https://meionovel.id";
    private static final String BASE_SAKURA = "https://sakuranovel.id";
    private static final String BASE_WUXIA  = "https://wuxiaworld.site";
    private static final String BASE_RANOBEDB = "https://ranobedb.org/api/v0/";

    private static final String IMAGE_BASE_URL = "https://images.ranobedb.org/";

    private static final String UA =
            "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final RanobeApiService apiService;

    public NovelScraper() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_RANOBEDB)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(RanobeApiService.class);
    }

    public interface ScrapeListener<T> {
        void onResult(T result);
        void onError(Exception e);
    }

    public static void initSakuraCookies(Context context, ScrapeListener<Boolean> listener) {
        new Handler(Looper.getMainLooper()).post(() -> {
            WebView webView = new WebView(context);
            webView.getSettings().setUserAgentString(UA);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    if (listener != null) listener.onResult(true);
                    webView.destroy();
                }
            });
            webView.loadUrl(BASE_SAKURA);
        });
    }

    public void browseNovels(int page, ScrapeListener<List<Novel>> listener) {
        executor.execute(() -> {
            try {
                Response<RanobeModels.SeriesResponse> response = apiService.searchSeries("", page, 24).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<Novel> list = new ArrayList<>();
                    for (RanobeModels.SeriesItem item : response.body().series) {
                        list.add(mapSeriesToNovel(item));
                    }
                    listener.onResult(list);
                } else {
                    listener.onError(new Exception("Gagal mengambil data dari RanobeDB"));
                }
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    public void browseNovels(String source, int page, ScrapeListener<List<Novel>> listener) {
        if (SOURCE_RANOBEDB.equals(source)) {
            browseNovels(page, listener);
            return;
        }
        executor.execute(() -> {
            try {
                List<Novel> result = new ArrayList<>();
                switch (source) {
                    case SOURCE_MEIONOVEL: result = browseMeio(page); break;
                    case SOURCE_SAKURANOVEL: result = browseSakura(page); break;
                    case SOURCE_WUXIAWORLD: result = browseWuxia(page); break;
                }
                listener.onResult(result);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    public void searchNovels(String query, ScrapeListener<List<Novel>> listener) {
        executor.execute(() -> {
            try {
                Response<RanobeModels.SeriesResponse> response = apiService.searchSeries(query, 1, 50).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<Novel> list = new ArrayList<>();
                    for (RanobeModels.SeriesItem item : response.body().series) {
                        list.add(mapSeriesToNovel(item));
                    }
                    listener.onResult(list);
                } else {
                    listener.onError(new Exception("Pencarian RanobeDB gagal"));
                }
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    private Novel mapSeriesToNovel(RanobeModels.SeriesItem item) {
        String coverUrl = "";
        if (item.book != null && item.book.image != null) {
            coverUrl = IMAGE_BASE_URL + item.book.image.filename;
        }

        Novel novel = new Novel(item.title, "", "", coverUrl, "");
        novel.setId(item.id);
        novel.setSource(SOURCE_RANOBEDB);
        novel.setLanguage(item.olang);
        return novel;
    }

    public void getNovelDetail(Novel novel, ScrapeListener<Novel> listener) {
        executor.execute(() -> {
            try {
                if (SOURCE_RANOBEDB.equals(novel.getSource()) && novel.getId() != 0) {
                    Response<RanobeModels.SeriesDetail> response = apiService.getSeriesDetail(novel.getId()).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        RanobeModels.SeriesDetail detail = response.body();
                        
                        // Coba ambil sinopsis dari berbagai field RanobeDB
                        String desc = detail.descriptionGeneric;
                        if (desc == null || desc.isEmpty()) desc = detail.descriptionEn;
                        if (desc == null || desc.isEmpty()) desc = detail.descriptionJa;
                        
                        if (desc != null && !desc.isEmpty()) {
                            novel.setDescription(desc);
                        }

                        // Ambil Penulis dari staff list
                        if (detail.staff != null) {
                            for (RanobeModels.StaffItem s : detail.staff) {
                                if ("author".equalsIgnoreCase(s.roleType) || "original_creator".equalsIgnoreCase(s.roleType)) {
                                    novel.setAuthor(s.name);
                                    break;
                                }
                            }
                        }

                        if (detail.books != null && !detail.books.isEmpty()) {
                            for (RanobeModels.BookItem b : detail.books) {
                                if (("main".equals(b.bType) || detail.books.size() == 1) && b.image != null) {
                                    novel.setCoverUrl(IMAGE_BASE_URL + b.image.filename);
                                    break;
                                }
                            }
                        }

                        if (detail.tags != null) {
                            List<String> tags = new ArrayList<>();
                            for (RanobeModels.TagItem t : detail.tags) tags.add(t.name);
                            novel.setTags(tags);
                        }
                    }

                    // Fallback ke MeioNovel jika data penting masih kosong
                    if (novel.getNovelUrl() == null || novel.getNovelUrl().isEmpty() || 
                        novel.getDescription() == null || novel.getDescription().isEmpty() ||
                        novel.getAuthor() == null || novel.getAuthor().isEmpty()) {
                        
                        String query = novel.getTitle();
                        String searchUrl = BASE_MEIO + "/?s=" + URLEncoder.encode(query, StandardCharsets.UTF_8.name()) + "&post_type=wp-manga";
                        Document searchDoc = Jsoup.connect(searchUrl).userAgent(UA).timeout(10000).get();
                        
                        // Selector pencarian yang lebih agresif
                        Element firstResult = searchDoc.select(".c-tabs-item__content .post-title a, .post-title a, .manga-title a").first();
                        
                        if (firstResult != null) {
                            String mUrl = firstResult.attr("abs:href");
                            novel.setNovelUrl(mUrl);
                            Document detailDoc = Jsoup.connect(mUrl).userAgent(UA).timeout(10000).get();
                            applyScrapedDetail(detailDoc, novel);
                        }
                    }
                } else if (novel.getNovelUrl() != null && !novel.getNovelUrl().isEmpty()) {
                    Document doc = Jsoup.connect(novel.getNovelUrl()).userAgent(UA).timeout(15000).get();
                    applyScrapedDetail(doc, novel);
                }

                listener.onResult(novel);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    private void applyScrapedDetail(Document doc, Novel novel) {
        // Selector Penulis yang lebih luas untuk WP-Manga/Madara Theme
        String author = doc.select(".author-content a, .mg_author .summary-content a, .post-content_item:contains(Author) .summary-content a").text().trim();
        if (!author.isEmpty() && (novel.getAuthor() == null || novel.getAuthor().isEmpty())) {
            novel.setAuthor(author);
        }
        
        // Selector Sinopsis yang lebih luas
        Elements descElements = doc.select(".summary__content p, .description-summary .summary__content p, .post-content_item .summary__content, .manga-excerpt, .summary__content");
        StringBuilder sb = new StringBuilder();
        for (Element p : descElements) {
            String text = p.text().trim();
            if (!text.isEmpty()) sb.append(text).append("\n\n");
        }
        
        String finalDesc = sb.toString().trim();
        if (!finalDesc.isEmpty() && (novel.getDescription() == null || novel.getDescription().isEmpty())) {
            novel.setDescription(finalDesc);
        }
    }

    public void getChapters(String novelUrl, String source, ScrapeListener<List<Novel.Chapter>> listener) {
        executor.execute(() -> {
            try {
                if (novelUrl == null || novelUrl.isEmpty()) {
                    listener.onResult(new ArrayList<>());
                    return;
                }
                Document doc = Jsoup.connect(novelUrl).userAgent(UA).timeout(15000).get();
                List<Novel.Chapter> chapters = parseChapters(doc);

                if (chapters.isEmpty()) {
                    String ajaxUrl = novelUrl.endsWith("/") ? novelUrl + "ajax/chapters/" : novelUrl + "/ajax/chapters/";
                    try {
                        Document ajaxDoc = Jsoup.connect(ajaxUrl).userAgent(UA).method(org.jsoup.Connection.Method.POST).ignoreContentType(true).post();
                        chapters = parseChapters(ajaxDoc);
                    } catch (IOException ignored) {}
                }
                Collections.reverse(chapters);
                listener.onResult(chapters);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    private List<Novel.Chapter> parseChapters(Document doc) {
        Elements els = doc.select("li.wp-manga-chapter a, .wp-manga-chapter a");
        List<Novel.Chapter> list = new ArrayList<>();
        for (Element el : els) {
            String title = el.text().trim();
            String url = el.attr("abs:href");
            if (!url.isEmpty()) list.add(new Novel.Chapter(title, url));
        }
        return list;
    }

    public void getChapterContent(String chapterUrl, String source, ScrapeListener<String> listener) {
        executor.execute(() -> {
            try {
                Document doc = Jsoup.connect(chapterUrl).userAgent(UA).timeout(20000).get();
                Element content = doc.select(".reading-content, .text-left, .chapter-content").first();
                if (content != null) {
                    content.select("script, style, .ads").remove();
                    listener.onResult(content.text().trim());
                } else {
                    listener.onResult("Konten tidak ditemukan.");
                }
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    private List<Novel> browseMeio(int page) throws IOException {
        Document doc = Jsoup.connect(BASE_MEIO + "/novel/?m_orderby=latest&page=" + page).userAgent(UA).get();
        return parseListing(doc, SOURCE_MEIONOVEL);
    }
    private List<Novel> browseSakura(int page) throws IOException {
        Document doc = Jsoup.connect(BASE_SAKURA + "/novel/?m_orderby=latest&page=" + page).userAgent(UA).get();
        return parseListing(doc, SOURCE_SAKURANOVEL);
    }
    private List<Novel> browseWuxia(int page) throws IOException {
        Document doc = Jsoup.connect(BASE_WUXIA + "/novel/?m_orderby=latest&page=" + page).userAgent(UA).get();
        return parseListing(doc, SOURCE_WUXIAWORLD);
    }

    private List<Novel> parseListing(Document doc, String source) {
        Elements els = doc.select(".page-item-detail, .manga-item");
        List<Novel> list = new ArrayList<>();
        for (Element el : els) {
            String title = el.select(".post-title a, .manga-title a").text().trim();
            String url = el.select(".post-title a, .manga-title a").attr("abs:href");
            String cover = el.select("img").attr("abs:src");
            Novel n = new Novel(title, "", "", cover, url);
            n.setSource(source);
            list.add(n);
        }
        return list;
    }
}
