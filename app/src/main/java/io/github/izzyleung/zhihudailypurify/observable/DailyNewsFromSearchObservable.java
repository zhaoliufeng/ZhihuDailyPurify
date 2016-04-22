package io.github.izzyleung.zhihudailypurify.observable;

import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import rx.Observable;

import static io.github.izzyleung.zhihudailypurify.observable.Helper.getHtml;

public class DailyNewsFromSearchObservable {
    public static Observable<DailyNews> withKeyword(String keyword) {
        return getHtml(Constants.Urls.SEARCH + keyword.trim())
                .flatMap(DailyNewsFromSearchObservable::toNewsJSONArray)
                .flatMap(DailyNewsFromSearchObservable::toDailyNews);
    }

    private static Observable<JSONArray> toNewsJSONArray(String data) {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(new JSONArray(data));
                subscriber.onCompleted();
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        });
    }

    private static Observable<DailyNews> toDailyNews(JSONArray newsJSONArray) {
        return Observable.create(subscriber -> {
            try {
                for (int i = 0; i < newsJSONArray.length(); i++) {
                    JSONObject newsObject = newsJSONArray.getJSONObject(i);

                    DailyNews news = new GsonBuilder().create()
                            .fromJson(newsObject.getString("content"), Constants.Types.newsType);
                    news.setDate(newsObject.getString("date"));

                    subscriber.onNext(news);
                }

                subscriber.onCompleted();
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        });
    }
}
