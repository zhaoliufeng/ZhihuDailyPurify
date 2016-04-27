package io.github.izzyleung.zhihudailypurify.observable;

import java.util.List;

import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import rx.Observable;

public class DailyNewsFromDatabaseObservable {
    public static Observable<List<DailyNews>> ofDate(String date) {
        return Observable.create(subscriber -> {
            List<DailyNews> newsList
                    = ZhihuDailyPurifyApplication.getDataSource().newsOfTheDay(date);

            if (newsList != null) {
                subscriber.onNext(newsList);
            }

            subscriber.onCompleted();
        });
    }
}
