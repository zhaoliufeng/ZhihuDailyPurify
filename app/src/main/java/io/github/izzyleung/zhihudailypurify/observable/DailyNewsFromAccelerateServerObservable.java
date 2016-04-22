package io.github.izzyleung.zhihudailypurify.observable;

import com.annimon.stream.Stream;
import com.google.gson.Gson;
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

    private static Observable<DailyNews> convert(String data) {
        return Observable.create(subscriber -> {
            Gson gson = new GsonBuilder().create();
            List<DailyNews> newsList = gson.fromJson(data, Constants.Types.newsListType);

            Stream.of(newsList).forEach(subscriber::onNext);

            subscriber.onCompleted();
        });
    }
}
