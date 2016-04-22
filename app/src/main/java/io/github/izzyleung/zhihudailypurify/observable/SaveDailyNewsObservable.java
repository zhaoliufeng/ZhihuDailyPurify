package io.github.izzyleung.zhihudailypurify.observable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import rx.Observable;

public class SaveDailyNewsObservable {
    public static Observable saveToDatabase(String date, List<DailyNews> newsList) {
        return Observable.create(subscriber -> {
            Gson gson = new GsonBuilder().create();
            ZhihuDailyPurifyApplication.getDataSource()
                    .insertOrUpdateNewsList(date, gson.toJson(newsList, Constants.Types.newsListType));

            subscriber.onCompleted();
        });
    }
}
