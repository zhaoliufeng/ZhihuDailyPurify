package io.github.izzyleung.zhihudailypurify.observable;

import com.google.gson.GsonBuilder;

import java.util.List;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import rx.Observable;

import static io.github.izzyleung.zhihudailypurify.observable.Helper.getHtml;

public class DailyNewsFromAccelerateServerObservable {
    public static Observable<DailyNews> ofDate(String date) {
        return getHtml(Constants.Urls.ZHIHU_DAILY_PURIFY_BEFORE + date)
                .map(Helper::decodeHtml)
                .flatMap(DailyNewsFromAccelerateServerObservable::convert)
                .doOnNext(dailyNews -> dailyNews.setDate(date));
    }

    public static Observable<DailyNews> convert(String data) {
        return Observable.create(subscriber -> {
            List<DailyNews> newsList
                    = new GsonBuilder().create().fromJson(data, Constants.Types.newsListType);

            for (DailyNews news : newsList) {
                subscriber.onNext(news);
            }

            subscriber.onCompleted();
        });
    }
}
