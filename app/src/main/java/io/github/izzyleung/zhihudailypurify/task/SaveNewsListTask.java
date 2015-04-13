package io.github.izzyleung.zhihudailypurify.task;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;

public class SaveNewsListTask extends MyAsyncTask<Void, Void, Void> {
    private String date;
    private List<DailyNews> newsList;

    public SaveNewsListTask(String date, List<DailyNews> newsList) {
        this.date = date;
        this.newsList = newsList;
    }

    @Override
    protected Void doInBackground(Void... params) {
        saveNewsList(newsList);
        return null;
    }

    private void saveNewsList(List<DailyNews> newsList) {
        Type listType = new TypeToken<List<DailyNews>>() {

        }.getType();

        ZhihuDailyPurifyApplication.getDataSource()
                .insertOrUpdateNewsList(date, new GsonBuilder().create().toJson(newsList, listType));
    }
}