package io.github.izzyleung.zhihudailypurify.bean;

import java.util.ArrayList;
import java.util.List;

public final class DailyNews {
    private boolean isMulti;
    private String questionTitle;
    private String questionUrl;
    private String dailyTitle;
    private List<String> questionTitleList = new ArrayList<>();
    private List<String> questionUrlList = new ArrayList<>();
    private String thumbnailUrl;
    private String date;

    public DailyNews() {

    }

    public String getQuestionUrl() {
        return questionUrl;
    }

    public void setQuestionUrl(String questionUrl) {
        this.questionUrl = questionUrl;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public void setMulti(boolean isMulti) {
        this.isMulti = isMulti;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getDailyTitle() {
        return dailyTitle;
    }

    public void setDailyTitle(String dailyTitle) {
        this.dailyTitle = dailyTitle;
    }

    public List<String> getQuestionTitleList() {
        return questionTitleList;
    }

    public List<String> getQuestionUrlList() {
        return questionUrlList;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void addQuestionTitle(String questionTitle) {
        questionTitleList.add(questionTitle);
    }

    public void addQuestionUrl(String questionUrl) {
        questionUrlList.add(questionUrl);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
