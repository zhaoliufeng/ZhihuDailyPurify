package io.github.izzyleung.zhihudailypurify.observable;

import android.text.Html;

import java.io.IOException;

import io.github.izzyleung.zhihudailypurify.support.lib.Http;
import rx.Observable;
import rx.Subscriber;

public class Helper {
    static Observable<String> getHtml(String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext(Http.get(url));
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    static String decodeHtml(String in) {
        return Html.fromHtml(Html.fromHtml(in).toString()).toString();
    }
}
