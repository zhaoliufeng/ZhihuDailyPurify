package io.github.izzyleung.zhihudailypurify.task;

import android.os.AsyncTask;

import com.google.gson.GsonBuilder;

import java.util.List;

import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;

public class SaveNewsListTask extends AsyncTask<Void, Void, Void> {
    private String date;
    private List<DailyNews> newsList;

    public SaveNewsListTask(List<DailyNews> newsList) {
        this.newsList = newsList;
        this.date = newsList.get(0).getDate();
    }

    @Override
    protected Void doInBackground(Void... params) {
        saveNewsList(newsList);
        return null;
    }

    private void saveNewsList(List<DailyNews> newsList) {
        ZhihuDailyPurifyApplication.getDataSource().insertOrUpdateNewsList(date,
                new GsonBuilder().create().toJson(newsList, Constants.Types.newsListType));
    }
}