package com.suryakusuma.novelin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NovelScraper {

    public static final String SOURCE_MEIONOVEL  = "MeioNovel";
    public static final String SOURCE_SAKURANOVEL = "SakuraNovel";
    public static final String SOURCE_WUXIAWORLD  = "WuxiaWorld";

    private static final String BASE_MEIO   = "https://meionovel.id";
    private static final String BASE_SAKURA = "https://sakuranovel.id";
    private static final String BASE_WUXIA  = "https://wuxiaworld.site";

    private static final String UA =
        "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    // Cached Cloudflare cookies for SakuraNovel
    private static volatile String sakuraCfCookies = null;
    private static volatile long sakuraCookieTimestamp = 0;
    private static final long COOKIE_TTL_MS = 25 * 60 * 1000L; // 25 menit

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public interface ScrapeListener<T> {
        void onResult(T result);
        void onError(Exception e);
    }

    // ================================================================
    // BROWSE — tampilkan daftar novel dari sumber tertentu
    // ================================================================

    public void browseNovels(String source, int page, ScrapeListener<List<Novel>> listener) {
        executor.execute(() -> {
            try {
                List<Novel> result = new ArrayList<>();
                switch (source) {
                    case SOURCE_MEIONOVEL:
                        result = browseMeio(page);
                        break;
                    case SOURCE_SAKURANOVEL:
                        result = browseSakura(page);
                        break;
                    case SOURCE_WUXIAWORLD:
                        result = browseWuxia(page);
                        break;
                }
                listener.onResult(result);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    private List<Novel> browseMeio(int page) throws IOException {
        String url = BASE_MEIO + "/novel/?m_orderby=latest&page=" + page;
        Document doc = Jsoup.connect(url).userAgent(UA).timeout(15000).get();
        return parseListing(doc, SOURCE_MEIONOVEL);
    }

    private List<Novel> browseSakura(int page) throws IOException {
        String url = BASE_SAKURA + "/novel/?m_orderby=latest&page=" + page;
        Document doc = connectSakura(url);
        return parseListing(doc, SOURCE_SAKURANOVEL);
    }

    private List<Novel> browseWuxia(int page) throws IOException {
        String url = BASE_WUXIA + "/novel/?m_orderby=latest&page=" + page;
        Document doc = Jsoup.connect(url).userAgent(UA).timeout(15000).get();
        return parseListing(doc, SOURCE_WUXIAWORLD);
    }

    // ================================================================
    // SEARCH — cari novel di semua sumber
    // ================================================================

    public void searchNovels(String query, ScrapeListener<List<Novel>> listener) {
        executor.execute(() -> {
            List<Novel> all = new ArrayList<>();
            Exception lastError = null;

            try { all.addAll(searchMeio(query)); } catch (Exception e) { lastError = e; }
            try { all.addAll(searchWuxia(query)); } catch (Exception e) { lastError = e; }
            try { all.addAll(searchSakura(query)); } catch (Exception e) { lastError = e; }

            if (all.isEmpty() && lastError != null) {
                listener.onError(lastError);
            } else {
                listener.onResult(all);
            }
        });
    }

    private List<Novel> searchMeio(String q) throws IOException {
        String url = BASE_MEIO + "/?s=" + q.replace(" ", "+") + "&post_type=wp-manga";
        Document doc = Jsoup.connect(url).userAgent(UA).timeout(15000).get();
        return parseListing(doc, SOURCE_MEIONOVEL);
    }

    private List<Novel> searchSakura(String q) throws IOException {
        String url = BASE_SAKURA + "/?s=" + q.replace(" ", "+") + "&post_type=wp-manga";
        Document doc = connectSakura(url);
        return parseListing(doc, SOURCE_SAKURANOVEL);
    }

    private List<Novel> searchWuxia(String q) throws IOException {
        String url = BASE_WUXIA + "/?s=" + q.replace(" ", "+") + "&post_type=wp-manga";
        Document doc = Jsoup.connect(url).userAgent(UA).timeout(15000).get();
        return parseListing(doc, SOURCE_WUXIAWORLD);
    }

    // ================================================================
    // DETAIL — ambil sinopsis & author dari halaman novel
    // ================================================================

    public void getNovelDetail(Novel novel, ScrapeListener<Novel> listener) {
        executor.execute(() -> {
            try {
                String novelUrl = novel.getNovelUrl();
                if (novelUrl == null || novelUrl.isEmpty()) {
                    listener.onResult(novel);
                    return;
                }
                Document doc;
                if (SOURCE_SAKURANOVEL.equals(novel.getSource())) {
                    doc = connectSakura(novelUrl);
                } else {
                    doc = Jsoup.connect(novelUrl).userAgent(UA).timeout(15000).get();
                }
                applyDetailToNovel(doc, novel);
                listener.onResult(novel);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    private void applyDetailToNovel(Document doc, Novel novel) {
        // Author — Madara theme selectors
        String author = doc.select(".author-content a").text().trim();
        if (author.isEmpty()) author = doc.select(".mg_author .summary-content a").text().trim();
        if (author.isEmpty()) author = doc.select(".summary-heading:contains(Author) + .summary-content a").text().trim();
        if (!author.isEmpty()) novel.setAuthor(author);

        // Synopsis — beberapa variasi selector Madara
        String desc = doc.select(".summary__content .g_txt_over p").text().trim();
        if (desc.isEmpty()) desc = doc.select(".summary__content p").text().trim();
        if (desc.isEmpty()) desc = doc.select(".summary__content").text().trim();
        if (desc.isEmpty()) desc = doc.select(".description-summary .summary__content").text().trim();
        if (!desc.isEmpty()) novel.setDescription(desc);

        // Cover berkualitas tinggi dari halaman detail
        if (novel.getCoverUrl().isEmpty()) {
            Element coverEl = doc.select(".summary-image img, .summary_image img").first();
            if (coverEl != null) {
                String src = coverEl.attr("abs:data-src");
                if (src.isEmpty()) src = coverEl.attr("abs:src");
                if (!src.isEmpty()) novel.setCoverUrl(src);
            }
        }
    }

    // ================================================================
    // CHAPTERS — ambil daftar chapter
    // ================================================================

    public void getChapters(String novelUrl, ScrapeListener<List<Novel.Chapter>> listener) {
        getChapters(novelUrl, "", listener);
    }

    public void getChapters(String novelUrl, String source, ScrapeListener<List<Novel.Chapter>> listener) {
        executor.execute(() -> {
            try {
                Document doc;
                if (SOURCE_SAKURANOVEL.equals(source)) {
                    doc = connectSakura(novelUrl);
                } else {
                    doc = Jsoup.connect(novelUrl).userAgent(UA).timeout(15000).get();
                }

                List<Novel.Chapter> chapters = parseChapters(doc);

                // Madara sering memakai AJAX untuk daftar chapter
                if (chapters.isEmpty()) {
                    String ajaxUrl = novelUrl.endsWith("/")
                            ? novelUrl + "ajax/chapters/"
                            : novelUrl + "/ajax/chapters/";
                    try {
                        Document ajaxDoc = Jsoup.connect(ajaxUrl)
                                .userAgent(UA)
                                .method(org.jsoup.Connection.Method.POST)
                                .timeout(15000)
                                .ignoreContentType(true)
                                .post();
                        chapters = parseChapters(ajaxDoc);
                    } catch (IOException ignored) {}
                }

                Collections.reverse(chapters); // Urutan ascending (chapter 1 paling atas)
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
            if (!url.isEmpty() && !title.isEmpty()) {
                list.add(new Novel.Chapter(title, url));
            }
        }
        return list;
    }

    // ================================================================
    // CHAPTER CONTENT — baca isi chapter
    // ================================================================

    public void getChapterContent(String chapterUrl, ScrapeListener<String> listener) {
        getChapterContent(chapterUrl, "", listener);
    }

    public void getChapterContent(String chapterUrl, String source, ScrapeListener<String> listener) {
        executor.execute(() -> {
            try {
                Document doc;
                if (SOURCE_SAKURANOVEL.equals(source)) {
                    doc = connectSakura(chapterUrl);
                } else {
                    doc = Jsoup.connect(chapterUrl).userAgent(UA).timeout(20000).get();
                }

                Element content = doc.select(
                    ".reading-content, .text-left, .chapter-content, #chapter-content"
                ).first();

                if (content != null) {
                    content.select("script, style, .ads, .code-block, ins, .social-sharing, iframe").remove();
                    Elements paras = content.select("p");
                    if (!paras.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Element p : paras) {
                            String text = p.text().trim();
                            if (!text.isEmpty()) sb.append(text).append("\n\n");
                        }
                        listener.onResult(sb.toString().trim());
                    } else {
                        listener.onResult(content.text().trim());
                    }
                } else {
                    listener.onResult("Konten tidak ditemukan.");
                }
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    // ================================================================
    // CLOUDFLARE BYPASS — SakuraNovel
    // ================================================================

    /**
     * Koneksi ke SakuraNovel dengan mencoba bypass Cloudflare.
     */
    private Document connectSakura(String url) throws IOException {
        org.jsoup.Connection conn = Jsoup.connect(url)
                .userAgent(UA)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(20000)
                .followRedirects(true);

        if (sakuraCfCookies != null
                && (System.currentTimeMillis() - sakuraCookieTimestamp) < COOKIE_TTL_MS) {
            conn.header("Cookie", sakuraCfCookies);
        }

        return conn.get();
    }

    /**
     * Inisialisasi cookies Cloudflare via WebView untuk SakuraNovel.
     */
    public static void initSakuraCookies(Context context, Runnable onDone) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        WebView webView = new WebView(context.getApplicationContext());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString(UA);
        settings.setLoadWithOverviewMode(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        final boolean[] done = {false};

        Runnable finish = () -> {
            if (done[0]) return;
            done[0] = true;
            String cookies = cookieManager.getCookie(BASE_SAKURA);
            if (cookies != null && !cookies.isEmpty()) {
                sakuraCfCookies = cookies;
                sakuraCookieTimestamp = System.currentTimeMillis();
            }
            webView.destroy();
            if (onDone != null) onDone.run();
        };

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mainHandler.postDelayed(finish, 4000);
            }
        });

        mainHandler.postDelayed(finish, 8000);
        webView.loadUrl(BASE_SAKURA);
    }

    // ================================================================
    // SHARED PARSER
    // ================================================================

    private List<Novel> parseListing(Document doc, String source) {
        Elements items = doc.select(".page-item-detail, .c-tabs-item__content");
        if (items.isEmpty()) {
            items = doc.select(".manga-item, .post-title");
        }

        List<Novel> list = new ArrayList<>();
        for (Element el : items) {
            Element titleEl = el.select(".post-title a, h3 a, h4 a").first();
            if (titleEl == null) continue;

            String title = titleEl.text().trim();
            String url   = titleEl.attr("abs:href");
            if (title.isEmpty() || url.isEmpty()) continue;

            Element imgEl = el.select("img").first();
            String cover = "";
            if (imgEl != null) {
                cover = imgEl.attr("abs:data-src");
                if (cover.isEmpty()) cover = imgEl.attr("abs:src");
            }

            String author = el.select(".mg_author .summary-content a").text().trim();
            if (author.isEmpty()) author = el.select(".author-content a").text().trim();

            Novel novel = new Novel(title, author, "", cover, url);
            novel.setSource(source);
            list.add(novel);
        }
        return list;
    }
}
