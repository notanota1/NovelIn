package com.suryakusuma.novelin;

import java.util.ArrayList;
import java.util.List;

public class SourceManager {

    private static final List<SourcePlugin> PLUGINS = new ArrayList<>();

    static {
        PLUGINS.add(new LightNovelWPPlugin("https://meionovel.id") {
            @Override public String getId()   { return "meionovel"; }
            @Override public String getName() { return "Meionovel"; }
            @Override public String getSite() { return "https://meionovel.id"; }
            @Override public String getLang() { return "Indonesian"; }
        });

        PLUGINS.add(new LightNovelWPPlugin("https://wbnovel.com") {
            @Override
            public String getId()   { return "wbnovel"; }
            @Override
            public String getName() { return "WBNovel"; }
            @Override
            public String getSite() { return "https://wbnovel.com"; }
            @Override
            public String getLang() { return "Indonesian"; }
        });

        PLUGINS.add(new MadaraPlugin("https://novelku.id") {
            @Override
            public String getId()   { return "novelkuid"; }
            @Override
            public String getName() { return "Novelku ID"; }
            @Override
            public String getSite() { return "https://novelku.id"; }
            @Override
            public String getLang() { return "Indonesian"; }
        });

        PLUGINS.add(new MadaraPlugin("https://novelringan.com") {
            @Override public String getId()   { return "novelringan"; }
            @Override public String getName() { return "NovelRingan"; }
            @Override public String getSite() { return "https://novelringan.com"; }
            @Override public String getLang() { return "Indonesian"; }
        });

        PLUGINS.add(new MadaraPlugin("https://novelbook.id") {
            @Override public String getId()   { return "novelbookid"; }
            @Override public String getName() { return "NovelbookID"; }
            @Override public String getSite() { return "https://novelbook.id"; }
            @Override public String getLang() { return "Indonesian"; }
        });

        PLUGINS.add(new MadaraPlugin("https://ngabaca.com") {
            @Override public String getId()   { return "ngabaca"; }
            @Override public String getName() { return "Ngabaca"; }
            @Override public String getSite() { return "https://ngabaca.com"; }
            @Override public String getLang() { return "Indonesian"; }
        });

        PLUGINS.add(new MadaraPlugin("https://wuxiaworld.site") {
            @Override public String getId()   { return "wuxiaworld"; }
            @Override public String getName() { return "WuxiaWorld"; }
            @Override public String getSite() { return "https://wuxiaworld.site"; }
            @Override public String getLang() { return "English"; }
        });

        PLUGINS.add(new ReadNovelFullPlugin("https://novelfull.com") {
            @Override public String getId()   { return "novelfull"; }
            @Override public String getName() { return "NovelFull"; }
            @Override public String getSite() { return "https://novelfull.com"; }
            @Override public String getLang() { return "English"; }
        });

        PLUGINS.add(new LightNovelWPPlugin("https://knoxt.space") {
            @Override public String getId()   { return "knoxt"; }
            @Override public String getName() { return "KnoxT"; }
            @Override public String getSite() { return "https://knoxt.space"; }
            @Override public String getLang() { return "English"; }
        });
    }

    public static List<SourcePlugin> getAll() {
        return PLUGINS;
    }

    public static SourcePlugin getDefault() {
        return PLUGINS.isEmpty() ? null : PLUGINS.get(0);
    }

    public static SourcePlugin findById(String id) {
        for (SourcePlugin p : PLUGINS) {
            if (p.getId().equals(id)) return p;
        }
        return getDefault();
    }

    public static String[] getNames() {
        String[] names = new String[PLUGINS.size()];
        for (int i = 0; i < PLUGINS.size(); i++) {
            SourcePlugin p = PLUGINS.get(i);
            String flag = p.getLang().equals("Indonesian") ? "🇮🇩 " : "🌐 ";
            names[i] = flag + p.getName();
        }
        return names;
    }

    public static abstract class ReadNovelFullPlugin implements SourcePlugin {
        protected final String baseUrl;
        private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        private static final int TIMEOUT = 14000;
        private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();

        protected ReadNovelFullPlugin(String baseUrl) {
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }

        @Override
        public void searchNovels(String query, int page, SourcePlugin.Callback<List<Novel>> cb) {
            executor.execute(() -> {
                try {
                    String url = baseUrl + "/search?keyword=" + query.replace(" ", "+") + "&page=" + page;
                    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url).userAgent(UA).timeout(TIMEOUT).get();
                    cb.onSuccess(parseList(doc));
                } catch (Exception e) { cb.onError(e); }
            });
        }

        @Override
        public void getPopularNovels(int page, SourcePlugin.Callback<List<Novel>> cb) {
            executor.execute(() -> {
                try {
                    String url = baseUrl + "/?list=hot&page=" + page;
                    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url).userAgent(UA).timeout(TIMEOUT).get();
                    cb.onSuccess(parseList(doc));
                } catch (Exception e) { cb.onError(e); }
            });
        }

        @Override
        public void getChapters(String novelUrl, SourcePlugin.Callback<List<Novel.Chapter>> cb) {
            executor.execute(() -> {
                try {
                    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(novelUrl).userAgent(UA).timeout(TIMEOUT).get();
                    List<Novel.Chapter> chapters = new ArrayList<>();
                    org.jsoup.select.Elements els = doc.select(".list-chapter li a, .chapter-list li a");
                    for (org.jsoup.nodes.Element el : els) {
                        String title = el.text().trim();
                        String url = el.attr("abs:href");
                        if (!url.isEmpty()) chapters.add(new Novel.Chapter(title, url));
                    }
                    cb.onSuccess(chapters);
                } catch (Exception e) { cb.onError(e); }
            });
        }

        @Override
        public void getChapterContent(String chapterUrl, SourcePlugin.Callback<String> cb) {
            executor.execute(() -> {
                try {
                    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(chapterUrl).userAgent(UA).timeout(TIMEOUT).get();
                    org.jsoup.nodes.Element content = doc.select("#chapter-content, .chapter-content, .content-area").first();
                    if (content == null) { cb.onSuccess("Konten tidak ditemukan."); return; }
                    content.select("script,style,.ads").remove();
                    org.jsoup.select.Elements paragraphs = content.select("p");
                    StringBuilder sb = new StringBuilder();
                    for (org.jsoup.nodes.Element p : paragraphs) {
                        String text = p.text().trim();
                        if (!text.isEmpty()) sb.append(text).append("\n\n");
                    }
                    cb.onSuccess(sb.toString().trim());
                } catch (Exception e) { cb.onError(e); }
            });
        }

        private List<Novel> parseList(org.jsoup.nodes.Document doc) {
            List<Novel> list = new ArrayList<>();
            org.jsoup.select.Elements items = doc.select(".list-truyen .row, .truyen-list .row");
            for (org.jsoup.nodes.Element item : items) {
                org.jsoup.nodes.Element titleEl = item.select("h3 a, .truyen-title a").first();
                if (titleEl == null) continue;
                String title = titleEl.text().trim();
                String url = titleEl.attr("abs:href");
                if (url.isEmpty()) continue;
                org.jsoup.nodes.Element imgEl = item.select("img").first();
                String cover = imgEl != null ? imgEl.attr("abs:src") : "";
                String author = item.select(".story-item-right span:first-child a").text().trim();
                if (author.isEmpty()) author = "Unknown";
                list.add(new Novel(title, author, "", cover, url));
            }
            return list;
        }
    }
}
