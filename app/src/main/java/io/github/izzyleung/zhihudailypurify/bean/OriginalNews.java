package io.github.izzyleung.zhihudailypurify.bean;

import org.jsoup.nodes.Document;

public class OriginalNews {
    private String thumbnailUrl;
    private String dailyTitle;
    private int newsId;
    private Document document;

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getDailyTitle() {
        return dailyTitle;
    }

    public int getNewsId() {
        return newsId;
    }

    public Document getDocument() {
        return document;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setDailyTitle(String dailyTitle) {
        this.dailyTitle = dailyTitle;
    }

    public void setNewsId(int newsId) {
        this.newsId = newsId;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
