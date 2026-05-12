package com.suryakusuma.novelin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Plugin dasar untuk website yang menggunakan tema WordPress Madara.
 */
public abstract class MadaraPlugin implements SourcePlugin {

    protected final String baseUrl;
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";
    private static final int TIMEOUT = 14000;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    protected MadaraPlugin(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public void searchNovels(String query, int page, SourcePlugin.Callback<List<Novel>> cb) {
        executor.execute(() -> {
            try {
                String url = baseUrl + "/?s=" + query.replace(" ", "+") + "&post_type=wp-manga&paged=" + page;
                Document doc = Jsoup.connect(url).userAgent(UA).timeout(TIMEOUT).get();
                cb.onSuccess(parseNovelList(doc));
            } catch (Exception e) {
                cb.onError(e);
            }
        });
    }

    @Override
    public void getPopularNovels(int page, SourcePlugin.Callback<List<Novel>> cb) {
        executor.execute(() -> {
            try {
                String url = baseUrl + "/page/" + page + "/?m_orderby=views";
                Document doc = Jsoup.connect(url).userAgent(UA).timeout(TIMEOUT).get();
                List<Novel> list = parseNovelList(doc);
                if (list.isEmpty()) {
                    doc = Jsoup.connect(baseUrl).userAgent(UA).timeout(TIMEOUT).get();
                    list = parseNovelList(doc);
                }
                cb.onSuccess(list);
            } catch (Exception e) {
                cb.onError(e);
            }
        });
    }

    @Override
    public void getChapters(String novelUrl, SourcePlugin.Callback<List<Novel.Chapter>> cb) {
        executor.execute(() -> {
            try {
                Document doc = Jsoup.connect(novelUrl).userAgent(UA).timeout(TIMEOUT).get();
                List<Novel.Chapter> chapters = parseChapters(doc);

                if (chapters.isEmpty()) {
                    String ajaxUrl = (novelUrl.endsWith("/") ? novelUrl : novelUrl + "/") + "ajax/chapters/";
                    doc = Jsoup.connect(ajaxUrl)
                            .userAgent(UA)
                            .method(org.jsoup.Connection.Method.POST)
                            .timeout(TIMEOUT)
                            .ignoreContentType(true)
                            .post();
                    chapters = parseChapters(doc);
                }

                Collections.reverse(chapters);
                cb.onSuccess(chapters);
            } catch (Exception e) {
                cb.onError(e);
            }
        });
    }

    @Override
    public void getChapterContent(String chapterUrl, SourcePlugin.Callback<String> cb) {
        executor.execute(() -> {
            try {
                Document doc = Jsoup.connect(chapterUrl).userAgent(UA).timeout(TIMEOUT).get();

                Element content = doc.select(".reading-content").first();
                if (content == null) content = doc.select(".text-left").first();
                if (content == null) content = doc.select(".entry-content").first();

                if (content == null) {
                    cb.onSuccess("Konten tidak ditemukan.");
                    return;
                }

                content.select("script,style,.ads,.code-block,.social-sharing,img").remove();

                Elements paragraphs = content.select("p");
                StringBuilder sb = new StringBuilder();
                if (!paragraphs.isEmpty()) {
                    for (Element p : paragraphs) {
                        String text = p.text().trim();
                        if (!text.isEmpty()) sb.append(text).append("\n\n");
                    }
                } else {
                    sb.append(content.text().trim());
                }
                cb.onSuccess(sb.toString().trim());
            } catch (Exception e) {
                cb.onError(e);
            }
        });
    }

    private List<Novel> parseNovelList(Document doc) {
        List<Novel> list = new ArrayList<>();
        Elements items = doc.select(".c-tabs-item__content, .page-item-detail");
        for (Element item : items) {
            Element titleEl = item.select(".post-title a, h3 a, h4 a").first();
            if (titleEl == null) continue;

            String title = titleEl.text().trim();
            String url = titleEl.attr("abs:href");
            if (url.isEmpty()) continue;

            Element imgEl = item.select("img").first();
            String cover = imgEl != null ? imgEl.attr("abs:data-src") : "";
            if (cover.isEmpty() && imgEl != null) cover = imgEl.attr("abs:src");

            String author = item.select(".mg_author .summary-content a").text().trim();
            if (author.isEmpty()) author = item.select(".mg_author .summary-content").text().trim();
            if (author.isEmpty()) author = "Unknown";

            list.add(new Novel(title, author, "", cover, url));
        }
        return list;
    }

    private List<Novel.Chapter> parseChapters(Document doc) {
        List<Novel.Chapter> chapters = new ArrayList<>();
        Elements elements = doc.select(".wp-manga-chapter a, li.wp-manga-chapter a");
        for (Element el : elements) {
            String title = el.text().trim();
            String url = el.attr("abs:href");
            if (!url.isEmpty()) chapters.add(new Novel.Chapter(title, url));
        }
        return chapters;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getSite();

    public abstract String getLang();
}
