package io.github.izzyleung.zhihudailypurify.bean;

import org.jsoup.nodes.Document;

public class Story {
    private int storyId;
    private String date;
    private String dailyTitle;
    private String thumbnailUrl;
    private Document document;

    public int getStoryId() {
        return storyId;
    }

    public void setStoryId(int storyId) {
        this.storyId = storyId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDailyTitle() {
        return dailyTitle;
    }

    public void setDailyTitle(String dailyTitle) {
        this.dailyTitle = dailyTitle;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Story updateDocument(Document document) {
        Story result = new Story();

        result.setStoryId(this.getStoryId());
        result.setDate(this.getDate());
        result.setDailyTitle(this.getDailyTitle());
        result.setThumbnailUrl(this.getThumbnailUrl());
        result.setDocument(document);

        return result;
    }
}

