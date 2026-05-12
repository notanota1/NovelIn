package com.suryakusuma.novelin;

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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface ScrapeListener<T> {
        void onResult(T result);
        void onError(Exception e);
    }

    public void searchNovels(String query, ScrapeListener<List<Novel>> listener) {
        executor.execute(() -> {
            try {
                // WuxiaWorld.site search URL: spaces replaced with '+'
                String targetUrl = "https://wuxiaworld.site/?s=" + query.replace(" ", "+") + "&post_type=wp-manga";
                Document doc = Jsoup.connect(targetUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();

                // Selectors for WP-Manga theme used by wuxiaworld.site
                Elements elements = doc.select(".c-tabs-item__content");
                List<Novel> results = new ArrayList<>();

                for (Element el : elements) {
                    Element titleEl = el.select(".post-title a").first();
                    if (titleEl != null) {
                        String title = titleEl.text();
                        String url = titleEl.attr("abs:href");
                        
                        // Extract Author - More robust selector for Madara theme
                        String author = el.select(".mg_author .summary-content a").text().trim();
                        if (author.isEmpty()) {
                            author = el.select(".mg_author .summary-content").text().trim();
                        }
                        if (author.isEmpty()) {
                            author = el.select(".post-content_item:contains(Authors) .summary-content").text().trim();
                        }
                        if (author.isEmpty()) author = "Unknown Author";
                        
                        // Handle lazy loading images
                        Element imgEl = el.select("img").first();
                        String cover = "";
                        if (imgEl != null) {
                            cover = imgEl.attr("abs:data-src");
                            if (cover == null || cover.isEmpty()) cover = imgEl.attr("abs:src");
                        }
                        
                        String desc = el.select(".post-content_item .summary-content").last() != null ? 
                                el.select(".post-content_item .summary-content").last().text() : "Source: WuxiaWorld";

                        results.add(new Novel(title, author, desc, cover, url));
                    }
                }
                listener.onResult(results);
            } catch (IOException e) {
                listener.onError(e);
            }
        });
    }

    public void getChapters(String novelUrl, ScrapeListener<List<Novel.Chapter>> listener) {
        executor.execute(() -> {
            try {
                // Try fetching the novel page directly first
                Document doc = Jsoup.connect(novelUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();
                
                List<Novel.Chapter> chapters = parseChapters(doc);

                // If no chapters found, it might be using AJAX (very common for Madara/WP-Manga themes)
                if (chapters.isEmpty()) {
                    String ajaxUrl = novelUrl;
                    if (!ajaxUrl.endsWith("/")) ajaxUrl += "/";
                    ajaxUrl += "ajax/chapters/";
                    
                    doc = Jsoup.connect(ajaxUrl)
                            .userAgent("Mozilla/5.0")
                            .method(org.jsoup.Connection.Method.POST)
                            .timeout(10000)
                            .ignoreContentType(true)
                            .post();
                    
                    chapters = parseChapters(doc);
                }
                
                // Reverse chapters to show them in ascending order (usually they are descending on the site)
                Collections.reverse(chapters);
                
                listener.onResult(chapters);
            } catch (IOException e) {
                listener.onError(e);
            }
        });
    }

    private List<Novel.Chapter> parseChapters(Document doc) {
        Elements elements = doc.select(".wp-manga-chapter a");
        List<Novel.Chapter> chapters = new ArrayList<>();
        for (Element el : elements) {
            String title = el.text().trim();
            String url = el.attr("abs:href");
            if (!url.isEmpty()) {
                chapters.add(new Novel.Chapter(title, url));
            }
        }
        return chapters;
    }

    public void getChapterContent(String chapterUrl, ScrapeListener<String> listener) {
        executor.execute(() -> {
            try {
                Document doc = Jsoup.connect(chapterUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(15000)
                        .get();
                
                // WP-Manga reading content selector
                Element contentEl = doc.select(".reading-content").first();
                if (contentEl != null) {
                    // Remove unwanted elements like scripts, styles, and ads
                    contentEl.select("script, style, .ads, .social-sharing, .code-block").remove();
                    
                    // Madara often uses <p> tags, so we join them with newlines for readability
                    Elements paragraphs = contentEl.select("p");
                    if (!paragraphs.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Element p : paragraphs) {
                            String pText = p.text().trim();
                            if (!pText.isEmpty()) {
                                sb.append(pText).append("\n\n");
                            }
                        }
                        listener.onResult(sb.toString().trim());
                    } else {
                        listener.onResult(contentEl.text().trim());
                    }
                } else {
                    listener.onResult("Content not found.");
                }
            } catch (IOException e) {
                listener.onError(e);
            }
        });
    }
}
