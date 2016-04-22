package io.github.izzyleung.zhihudailypurify.observable;

import java.util.List;

import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import rx.Observable;

public class DailyNewsFromDatabaseObservable {
    public static Observable<DailyNews> ofDate(String date) {
        return Observable.create(subscriber -> {
            List<DailyNews> newsList
                    = ZhihuDailyPurifyApplication.getDataSource().newsOfTheDay(date);

            if (newsList != null) {
                for (DailyNews news : newsList) {
                    news.setDate(date);
                    subscriber.onNext(news);
                }
            }

            subscriber.onCompleted();
        });
    }
}
