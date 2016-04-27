package io.github.izzyleung.zhihudailypurify.observable;

import java.util.List;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import rx.Observable;

import static io.github.izzyleung.zhihudailypurify.observable.Helper.getHtml;

public class DailyNewsFromSearchObservable {
    public static Observable<List<DailyNews>> withKeyword(String keyword) {
        return getHtml(Constants.Urls.SEARCH, "q", keyword)
                .map(Helper::decodeHtml)
                .flatMap(Helper::toJSONObject)
                .flatMap(Helper::getDailyNewsJSONArray)
                .map(Helper::reflectNewsListFromJSON);
    }
}
