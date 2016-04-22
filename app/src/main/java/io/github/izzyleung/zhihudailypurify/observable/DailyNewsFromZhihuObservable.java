package io.github.izzyleung.zhihudailypurify.observable;

import android.text.TextUtils;

import com.annimon.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.bean.OriginalNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.support.lib.Http;
import io.github.izzyleung.zhihudailypurify.support.lib.optional.Optional;
import rx.Observable;
import rx.Subscriber;

import static io.github.izzyleung.zhihudailypurify.observable.Helper.getHtml;

public class DailyNewsFromZhihuObservable {
    private static final String QUESTION_TITLES_SELECTOR = "question-title";
    private static final String QUESTION_LINKS_SELECTOR = "div.view-more a";

    public static Observable<DailyNews> ofDate(String date) {
        return getHtml(Constants.Urls.ZHIHU_DAILY_BEFORE + date)
                .flatMap(DailyNewsFromZhihuObservable::getOriginalNewsObservable)
                .flatMap(DailyNewsFromZhihuObservable::updateNewsDocument)
                .map(DailyNewsFromZhihuObservable::originalNewsToDailyNews)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(dailyNews -> dailyNews.setDate(date));
    }

    private static Observable<OriginalNews> getOriginalNewsObservable(String html) {
        return Observable.create(subscriber -> {
            try {
                JSONArray newsArray = new JSONObject(html).getJSONArray("stories");

                for (int i = 0; i < newsArray.length(); i++) {
                    JSONObject newsJson = newsArray.getJSONObject(i);

                    OriginalNews news = new OriginalNews();
                    prepareOriginalNewsInfo(newsJson, news);

                    subscriber.onNext(news);
                }

                subscriber.onCompleted();
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        });
    }

    private static void prepareOriginalNewsInfo(JSONObject singleNews, OriginalNews news) throws JSONException {
        String thumbnailUrl = "";
        if (singleNews.has("images")) {
            thumbnailUrl = (String) singleNews.getJSONArray("images").get(0);
        }

        news.setThumbnailUrl(thumbnailUrl);
        news.setDailyTitle(singleNews.getString("title"));
        news.setNewsId(singleNews.getInt("id"));
    }


    private static Observable<OriginalNews> updateNewsDocument(OriginalNews news) {
        return getHtml(Constants.Urls.ZHIHU_DAILY_OFFLINE_NEWS + news.getNewsId())
                .flatMap(DailyNewsFromZhihuObservable::getNewsDocument)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(news::updateDocument);
    }

    private static Observable<Optional<Document>> getNewsDocument(String json) {
        return Observable.create(subscriber -> {
            try {
                JSONObject newsJson = new JSONObject(json);

                if (newsJson.has("body")) {
                    subscriber.onNext(Optional.of(Jsoup.parse(newsJson.getString("body"))));
                } else {
                    Optional<Document> empty = Optional.empty();
                    subscriber.onNext(empty);
                }

                subscriber.onCompleted();
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        });
    }

    private static Optional<DailyNews> originalNewsToDailyNews(OriginalNews news) {
        Elements questionLinkElements = getQuestionLinkElements(news);

        if (questionLinkElements.size() > 1) {
            return processMulti(news);
        } else if (questionLinkElements.size() == 1) {
            return processSingle(news);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<DailyNews> processMulti(OriginalNews news) {
        DailyNews dailyNews = new DailyNews();
        dailyNews.setMulti(true);
        dailyNews.setDailyTitle(news.getDailyTitle());
        dailyNews.setThumbnailUrl(news.getThumbnailUrl());

        for (String questionTitle : getQuestionTitleList(news)) {
            dailyNews.addQuestionTitle(questionTitle);
        }

        Optional<List<String>> questionUrlListOptional = getQuestionUrlList(news);
        if (questionUrlListOptional.isPresent()) {
            for (String questionUrl : questionUrlListOptional.get()) {
                dailyNews.addQuestionTitle(questionUrl);
            }
        } else {
            return Optional.empty();
        }

        return Optional.of(dailyNews);
    }

    private static boolean isLinkToZhihuDiscussion(Element element) {
        return element.text().equals(Constants.Strings.VIEW_ZHIHU_DISCUSSION);
    }

    private static Optional<List<String>> getQuestionUrlList(OriginalNews news) {
        List<String> result = new ArrayList<>();

        Elements questionLinkElements = getQuestionLinkElements(news);
        for (Element questionLinkElement : questionLinkElements) {
            if (isLinkToZhihuDiscussion(questionLinkElement)) {
                result.add(extractQuestionUrl(questionLinkElement));
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(result);
    }

    private static Elements getQuestionLinkElements(OriginalNews news) {
        return news.getDocument().select(QUESTION_LINKS_SELECTOR);
    }

    private static String extractQuestionUrl(Element questionLinkElement) {
        return questionLinkElement.attr("href");
    }

    private static List<String> getQuestionTitleList(OriginalNews news) {
        List<String> result = new ArrayList<>();

        Elements questionTitleElements = getQuestionTitleElements(news);
        for (int i = 0; i < questionTitleElements.size(); i++) {
            Optional<String> questionTitle = extractQuestionTitle(questionTitleElements.get(i));

            if (questionTitle.isPresent()) {
                result.add(questionTitle.get());
            } else if (i == 0) {
                result.add(news.getDailyTitle());
            }
        }

        return result;
    }

    private static Elements getQuestionTitleElements(OriginalNews news) {
        return news.getDocument().getElementsByClass(QUESTION_TITLES_SELECTOR);
    }

    private static Optional<String> extractQuestionTitle(Element questionTitleElement) {
        String title = questionTitleElement.text();

        if (TextUtils.isEmpty(title) || title.startsWith(Constants.Strings.ORIGINAL_DESCRIPTION)) {
            return Optional.empty();
        } else {
            return Optional.of(title);
        }
    }

    private static Optional<DailyNews> processSingle(OriginalNews news) {
        DailyNews dailyNews = new DailyNews();
        dailyNews.setMulti(false);
        dailyNews.setDailyTitle(news.getDailyTitle());
        dailyNews.setThumbnailUrl(news.getThumbnailUrl());

        dailyNews.setQuestionTitle(getQuestionTitle(news));

        Optional<String> questionUrl = getQuestionUrl(news);
        if (questionUrl.isPresent()) {
            dailyNews.setQuestionUrl(questionUrl.get());
        } else {
            return Optional.empty();
        }

        return Optional.of(dailyNews);
    }

    private static Optional<String> getQuestionUrl(OriginalNews news) {
        Element questionLinkElement = getQuestionLinkElements(news).first();

        if (isLinkToZhihuDiscussion(questionLinkElement)) {
            return Optional.of(extractQuestionUrl(questionLinkElement));
        } else {
            return Optional.empty();
        }
    }

    private static String getQuestionTitle(OriginalNews news) {
        return Stream.of(getQuestionTitleElements(news))
                .map(DailyNewsFromZhihuObservable::extractQuestionTitle)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(news.getDailyTitle());
    }
}
